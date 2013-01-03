import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Game implements ApplicationListener {
    Music backgroundMusic;
    Texture backgroundTexture;
    Texture spriteSheetCharactersTexture;
    Texture spriteSheetEnemiesTexture;
    TextureRegion singlePixel;
    TextureRegion[][] spriteSheetCharacters;
    TextureRegion[][] spriteSheetEnemies;
    OrthographicCamera camera;
    SpriteBatch batch;
    Character player;
    Character[] enemies;
    final Integer spriteSheetRows = 1;
    final Integer spriteSheetCols = 4;
    final Integer spriteEnemyRows = 1;
    final Integer spriteEnemyCols = 4;
    Integer movementSpeed = 150;

    public void create () {
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("Heroic Demise (New)_0.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        backgroundTexture = new Texture(Gdx.files.internal("imgp5493_seamless_1.jpg"));
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat,Texture.TextureWrap.Repeat);

        spriteSheetCharactersTexture = new Texture(Gdx.files.internal("unfinishedchars1.PNG"));
        spriteSheetCharacters = TextureRegion.split(spriteSheetCharactersTexture,
                spriteSheetCharactersTexture.getWidth() / spriteSheetCols,
                spriteSheetCharactersTexture.getHeight() / spriteSheetRows);

        spriteSheetEnemiesTexture = new Texture(Gdx.files.internal("orcs.png"));
        spriteSheetEnemies = TextureRegion.split(spriteSheetEnemiesTexture,
                spriteSheetEnemiesTexture.getWidth() / spriteEnemyCols,
                spriteSheetEnemiesTexture.getHeight() / spriteEnemyRows);

        singlePixel = new TextureRegion(new Texture(Gdx.files.internal("singlePixel.png")));

        camera = new OrthographicCamera();
        camera.setToOrtho(false,800,600);

        batch = new SpriteBatch();

        player = new Character();
        player.health = 100;
        player.direction = CharacterDirections.DOWN;
        player.position.set((camera.viewportWidth / 2) - (spriteSheetCharacters[0][0].getRegionWidth() / 2),
                (camera.viewportHeight / 2) - (spriteSheetCharacters[0][0].getRegionHeight() / 2));

        enemies = new Character[5];
        for(int i = 0;i < enemies.length;i++) {
            enemies[i] = new Character();
        }
        for(Character enemy : enemies) {
            enemy.secondsDamaged = 0;
            enemy.health = 100;
            enemy.walkingSpeed = orcWalkingSpeedSet();
            enemy.direction = CharacterDirections.DOWN;
            enemy.position.set(0,0);
        }
    }

    public void render () {
        Vector2 mousePressedPosition = new Vector2();
        mousePressedPosition.set(-1,-1);
        Vector2 relativeMousePosition = new Vector2();
        Integer distanceToMouse;
        Gdx.gl.glClearColor(0,0,0,0);
        Gdx.gl.glClear(0);

        for(Character enemy : enemies) {
            enemy.secondsDamaged -= Gdx.graphics.getDeltaTime();
            if(enemy.health <= 0) {
                enemy.health = 100;
                enemy.direction = CharacterDirections.DOWN;
                enemy.position.set(0,0);
                enemy.walkingSpeed = orcWalkingSpeedSet();
            }
        }

        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            player.position.y += movementSpeed * Gdx.graphics.getDeltaTime();
            player.direction = CharacterDirections.UP;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            player.position.y -= movementSpeed * Gdx.graphics.getDeltaTime();
            player.direction = CharacterDirections.DOWN;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            player.position.x -= movementSpeed * Gdx.graphics.getDeltaTime();
            player.direction = CharacterDirections.LEFT;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.position.x += movementSpeed * Gdx.graphics.getDeltaTime();
            player.direction = CharacterDirections.RIGHT;
        }
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) mousePressedPosition.set(Gdx.input.getX(),camera.viewportHeight - Gdx.input.getY());

        relativeMousePosition.set(
                mousePressedPosition.x - player.position.x - (spriteSheetCharacters[0][0].getRegionWidth() / 2),
                -(mousePressedPosition.y - player.position.y - (spriteSheetCharacters[0][0].getRegionHeight() / 2)));
        distanceToMouse = (int)Math.sqrt(relativeMousePosition.x * relativeMousePosition.x + relativeMousePosition.y * relativeMousePosition.y);

        for(Character enemy : enemies) {
            Vector2 relativeEnemyPosition = new Vector2(player.position.x - enemy.position.x,
                    player.position.y - enemy.position.y);
            relativeEnemyPosition.set(relativeEnemyPosition.x / relativeEnemyPosition.len(),
                    relativeEnemyPosition.y / relativeEnemyPosition.len());
            enemy.position.add(Gdx.graphics.getDeltaTime() * relativeEnemyPosition.x * enemy.walkingSpeed,
                    Gdx.graphics.getDeltaTime() * relativeEnemyPosition.y * enemy.walkingSpeed);
        }

        Vector2 relativeFiringPosition = new Vector2(0,0);
        if(mousePressedPosition.x != -1 && mousePressedPosition.y != -1) {
            for(Character enemy : enemies) {
                relativeFiringPosition.set(enemy.position.x - player.position.x,
                        enemy.position.y - player.position.y);
                double firingAngle = Math.toDegrees(Math.atan2(-relativeFiringPosition.y,relativeFiringPosition.x));
                double mouseAngle = Math.toDegrees(Math.atan2(relativeMousePosition.y,relativeMousePosition.x));
                if(firingAngle <= mouseAngle + 10 && firingAngle >= mouseAngle - 10) {
                    enemy.secondsDamaged = 1f;
                    enemy.health -= Gdx.graphics.getDeltaTime() * 30;
                }
            }
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(backgroundTexture,0,0);
        for(Character enemy : enemies) {
            if(enemy.secondsDamaged > 0f) {
                batch.setColor(Color.RED);
            }
            batch.draw(spriteSheetEnemies[0][enemy.direction.getValue()],enemy.position.x,enemy.position.y);
            batch.setColor(Color.WHITE);
        }
        batch.draw(spriteSheetCharacters[0][player.direction.getValue()], player.position.x, player.position.y);
        batch.draw(singlePixel,player.position.x + (spriteSheetCharacters[0][0].getRegionWidth() / 2),
                player.position.y + (spriteSheetCharacters[0][0].getRegionHeight() / 2),0,0,1,1,1,distanceToMouse,180 +
                (float)Math.toDegrees(Math.atan2((double)relativeMousePosition.x,(double)relativeMousePosition.y)));

        batch.end();
    }

    public void resize (int width, int height) {
    }

    public void pause () {
    }

    public void resume () {
    }

    public void dispose () {
    }

    public Integer orcWalkingSpeedSet() {
        return (int)(50 * Math.random() + 50);
    }
}