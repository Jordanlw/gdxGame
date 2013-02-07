import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.awt.*;
import java.io.IOException;

public class Game implements ApplicationListener {
    long timeGunSound;
    float networkTimeDelta;
    Texture backgroundTexture;
    Texture spriteSheetCharactersTexture;
    Texture spriteSheetEnemiesTexture;
    Texture gameOverTexture;
    Texture bombTexture;
    TextureRegion singlePixel;
    TextureRegion[][] spriteSheetCharacters;
    TextureRegion[][] spriteSheetEnemies;
    OrthographicCamera camera;
    SpriteBatch batch;
    Character player;
    Character otherPlayer;
    Character[] enemies;
    Potions potion;
    final Integer spriteSheetRows = 1;
    final Integer spriteSheetCols = 4;
    final Integer spriteEnemyRows = 1;
    final Integer spriteEnemyCols = 4;
    final float shouldCircleAt = 150;
    Integer movementSpeed = 150;
    Vector2 windowSize = new Vector2(800,600);
    MusicLibrary aMusicLibrary;
    Server serverNet = new Server();
    Client clientNet = new Client();
    static String[] cmdArgs;
    boolean isServer;

    public void create () {
        aMusicLibrary = new MusicLibrary();
        aMusicLibrary.backgroundMusic.setLooping(true);
        aMusicLibrary.backgroundMusic.play();

        gameOverTexture = new Texture(Gdx.files.internal("gameover.png"));

        backgroundTexture = new Texture(Gdx.files.internal("imgp5493_seamless_1.jpg"));
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat,Texture.TextureWrap.Repeat);

        bombTexture = new Texture(Gdx.files.internal("bomb.gif"));

        Potions.initializeTextures();

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
        player.position.set((camera.viewportWidth / 2) - (spriteSheetCharacters[0][0].getRegionWidth() / 2),
                (camera.viewportHeight / 2) - (spriteSheetCharacters[0][0].getRegionHeight() / 2));

        enemies = new Character[5];
        for(int i = 0;i < enemies.length;i++) {
            enemies[i] = new Character();
        }
        for(Character enemy : enemies) {
            respawnEnemy(enemy);
        }
        potion = new Potions();

        timeGunSound = 0;

        if(cmdArgs.length > 0) {
            Kryo kryo = clientNet.getKryo();
            registerClassesForNetwork(kryo);
            isServer = false;
            clientNet.start();
            try {
                clientNet.connect(5000,cmdArgs[0],12345);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                System.exit(1);
            }
            clientNet.addListener(new Listener() {
                public void received(Connection connection,Object object) {
                    otherPlayer = (Character)object;
                }
            });
        }
        else {
            Kryo kryo = serverNet.getKryo();
            registerClassesForNetwork(kryo);
            isServer = true;
            serverNet.start();
            try {
                serverNet.bind(12345);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                System.exit(1);
            }
            serverNet.addListener(new Listener() {
                public void received(Connection connection,Object object) {
                    otherPlayer = (Character)object;
                }
            });
        }
    }

    public void registerClassesForNetwork(Kryo kryo) {
        kryo.register(Character.class);
        kryo.register(CharacterDirections.class);
        kryo.register(Integer.class);
        kryo.register(Vector2.class);
    }

    public void handleInput(Vector2 relativeMousePosition, Vector2 mousePressedPosition, Vector2 distanceToMouse,
                            Vector2 bulletVector) {
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

        potion.time += Gdx.graphics.getDeltaTime();
        if(potion.time > Potions.timeToReach && potion.health == 0) {
            potion.health = Potions.healthGiven;
            potion.position.set((float)(camera.viewportWidth * Math.random()),(float)(camera.viewportHeight * Math.random()));
        }
        else if(potion.time >= Potions.timeToReach && isCollide(player.position,potion.position,spriteSheetCharacters[0][0].getRegionWidth(),
                spriteSheetCharacters[0][0].getRegionHeight(),Potions.textures[PotionsTypes.RED.ordinal()].getWidth() * 0.05f,
                Potions.textures[PotionsTypes.RED.ordinal()].getHeight() * 0.05f)) {
            player.health += potion.health;
            potion.health = 0;
            potion.time = 0;
            aMusicLibrary.potionSound.play();
            if(player.health > 100) {
                player.health = 100;
            }
        }
        for(Character enemy : enemies) {
            Vector2 relativeEnemyPosition = new Vector2(player.position.x - enemy.position.x,
                    player.position.y - enemy.position.y);
            if(Math.sqrt(relativeEnemyPosition.x * relativeEnemyPosition.x + relativeEnemyPosition.y * relativeEnemyPosition.y) < shouldCircleAt) {
                float relativeAngle = (float)Math.atan2(relativeEnemyPosition.y,relativeEnemyPosition.x);
                double angleAdd = 0;
                angleAdd = enemy.circleDirection ? 45 : -45;
                relativeAngle += Math.toRadians(angleAdd);
                relativeEnemyPosition.set((float)Math.cos(relativeAngle),(float)Math.sin(relativeAngle));
            }
            relativeEnemyPosition.set(relativeEnemyPosition.x / relativeEnemyPosition.len(),
                    relativeEnemyPosition.y / relativeEnemyPosition.len());
            if(Math.abs(relativeEnemyPosition.x) > Math.abs(relativeEnemyPosition.y)) {
                enemy.direction = CharacterDirections.LEFT;
                if(relativeEnemyPosition.x > relativeEnemyPosition.y) {
                    enemy.direction = CharacterDirections.RIGHT;
                }
            }
            else {
                enemy.direction = CharacterDirections.DOWN;
                if(relativeEnemyPosition.y > relativeEnemyPosition.x) {
                    enemy.direction = CharacterDirections.UP;
                }

            }
            enemy.position.add(Gdx.graphics.getDeltaTime() * relativeEnemyPosition.x * enemy.walkingSpeed,
                    Gdx.graphics.getDeltaTime() * relativeEnemyPosition.y * enemy.walkingSpeed);

            relativeEnemyPosition.set(player.position.x - enemy.position.x,player.position.y - enemy.position.y);
            if(relativeEnemyPosition.len() <= ((spriteSheetCharacters[0][0].getRegionHeight() >
            spriteSheetCharacters[0][0].getRegionWidth()) ? spriteSheetCharacters[0][0].getRegionHeight() :
            spriteSheetCharacters[0][0].getRegionWidth()))
            {
                player.health -= 10 * Gdx.graphics.getDeltaTime();
                player.secondsDamaged = 1;
                aMusicLibrary.hurtSound.play();
            }
        }

        if(mousePressedPosition.x != -1 && mousePressedPosition.y != -1) {
            if(TimeUtils.millis() > timeGunSound + 500 + (long)(50 * Math.random() + 50)) {
                timeGunSound = TimeUtils.millis();
                long soundId = aMusicLibrary.gunSound.play();
                aMusicLibrary.gunSound.setPitch(soundId,1 + (long)(0.3f * Math.random()));
                gunFiredThisFrame = true;
            }
            for(Character enemy : enemies) {
                Rectangle enemyRect = new Rectangle((int)enemy.position.x,(int)enemy.position.y,
                        spriteSheetEnemies[0][1].getRegionWidth(),spriteSheetEnemies[0][1].getRegionHeight());
                if(enemyRect.intersectsLine((int) player.position.x, (int) player.position.y, (int) bulletVector.x,
                        (int) bulletVector.y)) {
                    enemy.secondsDamaged = 1f;
                    enemy.health -= Gdx.graphics.getDeltaTime() * 30;
                }
            }
        }
        networkTimeDelta += Gdx.graphics.getDeltaTime();
        if(networkTimeDelta >= 20f/1000f) {
            networkTimeDelta = 0;
            if(isServer == false) {
                clientNet.sendTCP(player);
            }
            else if(isServer == true) {
                serverNet.sendToAllTCP(player);
            }
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(Color.WHITE);

        batch.draw(backgroundTexture,0,0);
        if(potion.time > Potions.timeToReach) {
            batch.draw(new TextureRegion(Potions.textures[PotionsTypes.RED.ordinal()]),potion.position.x,potion.position.y,0f,0f,
                    Potions.textures[PotionsTypes.RED.ordinal()].getWidth(),Potions.textures[PotionsTypes.RED.ordinal()].getHeight(),0.05f,0.05f,0f);
        }
        batch.draw(bombTexture,50,50);

        for(Character enemy : enemies) {
            if(enemy.secondsDamaged > 0f) {
                batch.setColor(Color.RED);
            }
            batch.draw(spriteSheetEnemies[0][enemy.direction.getValue()],enemy.position.x,enemy.position.y);
            batch.setColor(Color.WHITE);
        }
        if(player.secondsDamaged >= 1) {
            batch.setColor(Color.RED);
        }
        batch.draw(spriteSheetCharacters[0][player.direction.getValue()], player.position.x, player.position.y);
        batch.setColor(Color.WHITE);
        if(player.health <= 0) {
            batch.draw(gameOverTexture,camera.viewportWidth / 2 - gameOverTexture.getWidth() / 2,
                    camera.viewportHeight / 2 - gameOverTexture.getHeight() / 2);
        }
        batch.setColor(Color.YELLOW);
        if(gunFiredThisFrame) {
            batch.draw(singlePixel,player.position.x + (spriteSheetCharacters[0][0].getRegionWidth() / 2),
                    player.position.y + (spriteSheetCharacters[0][0].getRegionHeight() / 2),0,0,1,1,1,distanceToMouse,180 +
                    (float)Math.toDegrees(Math.atan2((double)relativeMousePosition.x,(double)relativeMousePosition.y)));

        }
        batch.end();
    }

    public boolean isCollide(Vector2 a,Vector2 b,float widthA,float heightA,float widthB,float heightB) {
        if(a.x + widthA >= b.x && a.x <= (b.x + widthB)) {
            if(a.y + heightA >= b.y && a.y <= (b.y + heightB)) {
                return true;
            }
        }
        return false;
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