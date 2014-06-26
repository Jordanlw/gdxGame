import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
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
import java.util.ArrayList;
import java.util.List;

class Game implements ApplicationListener {
    static final Vector2 windowSize = new Vector2(800, 600);
    static TextureRegion[] goldSheet;
    static TextureRegion[][] spriteSheetEnemies;
    static List<Character> enemies;
    static String[] cmdArgs;
    private long timeGunSound;
    private float networkTimeDelta;
    private Texture backgroundTexture;
    private Texture gameOverTexture;
    private Texture bombTexture;
    private TextureRegion[] explosionSheet;
    private TextureRegion singlePixel;
    private TextureRegion[][] spriteSheetCharacters;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Character player;
    private Character otherPlayer;
    private Potions potion;
    private MusicLibrary aMusicLibrary;
    private Server serverNet;
    private Client clientNet;
    private boolean isServer;
    private float sinceLastZombieIdleSound;
    private float sinceHurtSound = 1000;
    private boolean gamePaused = false;
    private boolean pauseButtonPressedPrior = false;
    private boolean hurtSoundPlayedThisFrame = false;
    private float waveTime = 0;
    private int currentWave = 1;
    private Animation explosionAnimation;
    private int explosionTarget = -1;
    private float animationTimer;

    public void create () {
        aMusicLibrary = new MusicLibrary();
        aMusicLibrary.backgroundMusic.setLooping(true);
        aMusicLibrary.backgroundMusic.play();

        gameOverTexture = new Texture(Gdx.files.internal("resources/gameover.png"));

        backgroundTexture = new Texture(Gdx.files.internal("resources/imgp5493_seamless_1.jpg"));
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat,Texture.TextureWrap.Repeat);

        bombTexture = new Texture(Gdx.files.internal("resources/bomb.gif"));

        Potions.initializeTextures();

        Texture spriteSheetCharactersTexture = new Texture(Gdx.files.internal("resources/unfinishedchars1.PNG"));
        Integer spriteSheetRows = 1;
        Integer spriteSheetCols = 4;
        spriteSheetCharacters = TextureRegion.split(spriteSheetCharactersTexture,
                spriteSheetCharactersTexture.getWidth() / spriteSheetCols,
                spriteSheetCharactersTexture.getHeight() / spriteSheetRows);

        Texture spriteSheetEnemiesTexture = new Texture(Gdx.files.internal("resources/orcs.png"));
        Integer spriteEnemyRows = 1;
        Integer spriteEnemyCols = 4;
        spriteSheetEnemies = TextureRegion.split(spriteSheetEnemiesTexture,
                spriteSheetEnemiesTexture.getWidth() / spriteEnemyCols,
                spriteSheetEnemiesTexture.getHeight() / spriteEnemyRows);

        Texture explosionTexture = new Texture(Gdx.files.internal("resources/Explosion_JasonGosen.png"));
        TextureRegion[][] explosionTmp = TextureRegion.split(explosionTexture, explosionTexture.getWidth() / 4, explosionTexture.getHeight());
        explosionSheet = explosionTmp[0];
        explosionAnimation = new Animation(0.16f,explosionSheet);

        Texture goldTexture = new Texture(Gdx.files.internal("resources/Gold_Moosader.png"));
        TextureRegion[][] goldTmp = TextureRegion.split(goldTexture, goldTexture.getWidth() / 4, goldTexture.getHeight());
        goldSheet = goldTmp[0];


        singlePixel = new TextureRegion(new Texture(Gdx.files.internal("resources/singlePixel.png")));

        camera = new OrthographicCamera();
        camera.setToOrtho(false,windowSize.x,windowSize.y);

        batch = new SpriteBatch();

        player = new Character();
        player.connected = true;
        player.position.set((camera.viewportWidth / 2) - (spriteSheetCharacters[0][0].getRegionWidth() / 2),
                (camera.viewportHeight / 2) - (spriteSheetCharacters[0][0].getRegionHeight() / 2));

        otherPlayer = new Character();
        otherPlayer.connected = false;

        enemies = new ArrayList<>();
        for(int i  = 0;i < 5;i++) {
            enemies.add(new Character());
        }

        for(Character enemy : enemies) {
            respawnEnemy(enemy,1);
        }
        potion = new Potions();
        potion.health = 0;

        timeGunSound = 0;

        serverNet = new Server();
        clientNet = new Client();
        if(cmdArgs.length > 0) {
            Kryo kryo = clientNet.getKryo();
            registerClassesForNetwork(kryo);
            isServer = false;
            player.isServer = false;
            clientNet.start();
            otherPlayer.health = 100;
            try {
                clientNet.connect(5000,cmdArgs[0],12345);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                System.exit(1);
            }
            clientNet.addListener(new Listener() {
                public void received(Connection connection,Object object) {
                    if(object instanceof List) {
                        cloneArrayList(enemies,(ArrayList<Character>)object);
                    }
                    else if(object instanceof Character){
                        if(((Character)object).isServer) {
                            otherPlayer = (Character)object;
                        }
                        else if(!((Character)object).isServer) {
                            player.health = ((Character)object).health;
                            player.secondsDamaged = ((Character)object).secondsDamaged;
                        }
                    }
                }
            });
        }
        else {
            Kryo kryo = serverNet.getKryo();
            registerClassesForNetwork(kryo);
            isServer = true;
            player.isServer = true;
            serverNet.start();
            try {
                serverNet.bind(12345);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                System.exit(1);
            }
            serverNet.addListener(new Listener() {
                public void received(Connection connection,Object object) {
                    otherPlayer.connected = true;
                    if(object instanceof List) {
                        for(int i = 0;i < enemies.size();i++) {
                            enemies.get(i).health = ((List<Character>) object).get(i).health;
                        }
                    }
                    else {
                        otherPlayer = (Character)object;
                    }
                }
            });
        }
    }

    <T> void cloneArrayList(List<T> a, List<T> b) {
        for(int i = 0;i < a.size();i++) {
            a.set(i,b.get(i));
        }
    }

    void registerClassesForNetwork(Kryo kryo) {
        kryo.register(Character.class);
        kryo.register(ArrayList.class);
        kryo.register(CharacterDirections.class);
        kryo.register(Integer.class);
        kryo.register(Vector2.class);
    }

    void handleInput(Vector2 relativeMousePosition, Vector2 mousePressedPosition, Vector2 distanceToMouse,
                     Vector2 bulletVector) {
        Integer movementSpeed = 150;
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
        distanceToMouse.x = (int)Math.sqrt(relativeMousePosition.x * relativeMousePosition.x + relativeMousePosition.y * relativeMousePosition.y);
        bulletVector.x = ((windowSize.x  + windowSize.y) * relativeMousePosition.x) + player.position.x;
        bulletVector.y = ((windowSize.x + windowSize.y) * -relativeMousePosition.y) + player.position.y;
    }

// --Commented out by Inspection START (6/26/14 7:33 PM):
//    public void checkAndHandleEnemyDeath(Character inputCharacters) {
//        if(inputCharacters.health <= 0) {
//            respawnEnemy(inputCharacters,1);
//        }
//    }
// --Commented out by Inspection STOP (6/26/14 7:33 PM)

    void respawnEnemy(Character inputCharacter, int currentWave) {
        inputCharacter.health = 100 + (25 * currentWave);
        inputCharacter.direction = CharacterDirections.DOWN;
        inputCharacter.position.set(Math.random() < 0.5f ? windowSize.x + 50 : -50,Math.random() < 0.5f ? windowSize.y + 50 : -50);
        inputCharacter.walkingSpeed = inputCharacter.getNewWalkingSpeed();
        inputCharacter.secondsDamaged = 0;
        //noinspection RedundantConditionalExpression
        inputCharacter.circleDirection = Math.random() < 0.5f ? true : false;
        inputCharacter.circleChangeTimer = 7.5f + (Math.random() * 2.5f);
    }

    void decrementSecondsDamaged(Character inputCharacter) {
        if(inputCharacter.secondsDamaged > 0) {
            inputCharacter.secondsDamaged -= Gdx.graphics.getDeltaTime();
        }
    }
    public void render () {
        Vector2 mousePressedPosition = new Vector2();
        Vector2 bulletVector = new Vector2();
        mousePressedPosition.set(-1,-1);
        Vector2 relativeMousePosition = new Vector2();
        Vector2 distanceToMouse = new Vector2();
        Boolean gunFiredThisFrame = false;
        hurtSoundPlayedThisFrame = false;
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(0);

        waveTime += Gdx.graphics.getDeltaTime();
        if(waveTime / 30 > currentWave) {
            currentWave++;
            int newEnemies = 0;
            for (Character enemy : enemies) {
                if (enemy.health > 0) {
                    continue;
                }
                newEnemies++;
            }
            for(int i = 0;i < 5 - newEnemies;i++) {
                enemies.add(new Character());
            }
            for (Character enemy : enemies) {
                respawnEnemy(enemy, currentWave);
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.P)) {
            if(!pauseButtonPressedPrior) {
                pauseButtonPressedPrior = true;
                gamePaused = !gamePaused;
            }
        }
        else {
            pauseButtonPressedPrior = false;
        }
        if(!gamePaused) {
            if(player.secondsDamaged > 0) {
               player.secondsDamaged -= Gdx.graphics.getDeltaTime();
            }
            if(otherPlayer.secondsDamaged > 0) {
                otherPlayer.secondsDamaged -= Gdx.graphics.getDeltaTime();
            }
            sinceLastZombieIdleSound += Gdx.graphics.getDeltaTime();
            if(sinceLastZombieIdleSound > 6f) {
                int index = (int) (Math.random() * (aMusicLibrary.zombieSounds.length - 1));
                aMusicLibrary.zombieSounds[index].setVolume(aMusicLibrary.zombieSounds[index].play(),1f);
                sinceLastZombieIdleSound = 0;
            }
            for(Character enemy : enemies) {
                decrementSecondsDamaged(enemy);
            }
            handleInput(relativeMousePosition,mousePressedPosition,distanceToMouse,bulletVector);

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
            if(isServer) {
                for(Character enemy : enemies) {
                    if(enemy.health <= 0) {
                        continue;
                    }
                    for(Character selectedEnemy : enemies) {
                        if(enemy.health <= 0) {
                            continue;
                        }
                        if(isCharacterCollided(selectedEnemy,enemy)) {
                            double angle = angleBetweenCharacters(enemy, selectedEnemy) - Math.PI;
                            selectedEnemy.position.add((float)(Math.cos(angle) * 10 * Gdx.graphics.getDeltaTime()),(float)(Math.sin(angle) * 10* Gdx.graphics.getDeltaTime()));
                        }
                    }
                    Vector2 relativeEnemyPosition = new Vector2(player.position.x - enemy.position.x,
                            player.position.y - enemy.position.y);
                    Vector2 remoteRelativeEnemyPosition = new Vector2(otherPlayer.position.x - enemy.position.x,
                            otherPlayer.position.y - enemy.position.y);
                    if(relativeEnemyPosition.len() > remoteRelativeEnemyPosition.len() && otherPlayer.health > 0 && otherPlayer.connected) {
                        relativeEnemyPosition = remoteRelativeEnemyPosition;
                    }
                    if(player.health < 0) {
                        relativeEnemyPosition = remoteRelativeEnemyPosition;
                    }
                    boolean availablePlayer = true;
                    if(player.health <= 0 && otherPlayer.health <= 0) {
                        availablePlayer = false;
                    }
                    enemy.circleChangeTimer -= Gdx.graphics.getDeltaTime();
                    if(enemy.circleChangeTimer < 0) {
                        enemy.circleDirection = !enemy.circleDirection;
                        enemy.circleChangeTimer = (Math.random() * 7) + 7;
                    }
                    float relativeAngle = (float)Math.atan2(relativeEnemyPosition.y,relativeEnemyPosition.x);
                    double angleAdd;
                    angleAdd = enemy.circleDirection ? 25 : -25;
                    relativeAngle += Math.toRadians(angleAdd);
                    relativeEnemyPosition.set((float)Math.cos(relativeAngle),(float)Math.sin(relativeAngle));

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
                    if(availablePlayer) {
                        enemy.position.add(Gdx.graphics.getDeltaTime() * relativeEnemyPosition.x * enemy.walkingSpeed,
                                Gdx.graphics.getDeltaTime() * relativeEnemyPosition.y * enemy.walkingSpeed);
                    }

                    handlePlayersBeingAttacked(player,enemy);
                    if(otherPlayer.connected) {
                        handlePlayersBeingAttacked(otherPlayer,enemy);
                    }
                }
            }

            if(mousePressedPosition.x != -1 && mousePressedPosition.y != -1) {
                if(TimeUtils.millis() > timeGunSound + 500 + (long)(50 * Math.random() + 50)) {
                    timeGunSound = TimeUtils.millis();
                    long soundId = aMusicLibrary.gunSound.play();
                    aMusicLibrary.gunSound.setPitch(soundId,1 + (long)(0.3f * Math.random()));
                    gunFiredThisFrame = true;
                }
                boolean bulletUsed = false;
                for(int i = 0;i < enemies.size();i++) {
                    Rectangle enemyRect = new Rectangle((int)enemies.get(i).position.x,(int)enemies.get(i).position.y,
                            spriteSheetEnemies[0][1].getRegionWidth(),spriteSheetEnemies[0][1].getRegionHeight());
                    if(!bulletUsed && enemyRect.intersectsLine((int) player.position.x, (int) player.position.y, (int) bulletVector.x,
                            (int) bulletVector.y)) {
                        enemies.get(i).secondsDamaged = 1f;
                        enemies.get(i).health -= Gdx.graphics.getDeltaTime() * 30;
                        if(enemies.get(i).health <= 0) {
                            Gold.saveEnemy(i);
                        }
                        bulletUsed = true;
                        explosionTarget = i;
                    }
                }
            }
        }
        networkTimeDelta += Gdx.graphics.getDeltaTime();
        if(networkTimeDelta >= 20f/1000f) {
            networkTimeDelta = 0;
            if(!isServer) {
                clientNet.sendTCP(player);
                clientNet.sendTCP(enemies);
            }
            else if(isServer) {
                serverNet.sendToAllTCP(player);
                serverNet.sendToAllTCP(enemies);
                serverNet.sendToAllTCP(otherPlayer);
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
        batch.draw(bombTexture, 50, 50);

        Gold.spawnLootFromEnemies(currentWave,batch);

        for(Character enemy : enemies) {
            if(enemy.secondsDamaged > 0f) {
                batch.setColor(Color.RED);
            }
            if(enemy.health <= 0) {
                continue;
            }
            batch.draw(spriteSheetEnemies[0][enemy.direction.getValue()],enemy.position.x,enemy.position.y);
            batch.setColor(Color.WHITE);
        }
        if(player.health > 0) {
            if(player.secondsDamaged > 0) {
                batch.setColor(Color.RED);
            }
            batch.draw(spriteSheetCharacters[0][player.direction.getValue()],player.position.x,player.position.y);

        }
        batch.setColor(Color.WHITE);
        if(otherPlayer.health > 0 && otherPlayer.connected) {
            if(otherPlayer.secondsDamaged > 0) {
                batch.setColor(Color.RED);
            }
            batch.draw(spriteSheetCharacters[0][otherPlayer.direction.getValue()], otherPlayer.position.x, otherPlayer.position.y);
        }
        batch.setColor(Color.WHITE);
        if(explosionTarget >= 0 && enemies.get(explosionTarget).health > 0) {
            TextureRegion explosionKeyframe = explosionAnimation.getKeyFrame(animationTimer,false);
            Vector2 explosionPosition = new Vector2(enemies.get(explosionTarget).position.x,enemies.get(explosionTarget).position.y);
            explosionPosition.add(spriteSheetCharacters[0][0].getRegionWidth() / 2,spriteSheetCharacters[0][0].getRegionHeight() / 2);
            explosionPosition.sub(explosionSheet[0].getRegionWidth() / 2, explosionSheet[0].getRegionHeight() / 2);
            explosionPosition.add((float)(Math.random() - 0.5f) * 3.45f,(float)(Math.random() - 0.5f) * 3.45f);
            batch.draw(explosionKeyframe,explosionPosition.x,explosionPosition.y);
            animationTimer += Gdx.graphics.getDeltaTime();
            if(explosionAnimation.isAnimationFinished(animationTimer)) {
                explosionTarget = -1;
                animationTimer = 0;
            }
        }

        if(player.health <= 0) {
            batch.draw(gameOverTexture,camera.viewportWidth / 2 - gameOverTexture.getWidth() / 2,
                    camera.viewportHeight / 2 - gameOverTexture.getHeight() / 2);
        }

        batch.setColor(Color.YELLOW);
        if(gunFiredThisFrame) {
            batch.draw(singlePixel,player.position.x + (spriteSheetCharacters[0][0].getRegionWidth() / 2),
                    player.position.y + (spriteSheetCharacters[0][0].getRegionHeight() / 2),0,0,1,1,1,distanceToMouse.x,180 +
                    (float)Math.toDegrees(Math.atan2((double)relativeMousePosition.x,(double)relativeMousePosition.y)));

        }
        batch.end();
    }

    double angleBetweenCharacters(Character a, Character b) {
        return Math.atan2(a.position.y - b.position.y,a.position.x - b.position.x);
    }

    boolean isCharacterCollided(Character a, Character b) {
        int characterWidth = spriteSheetCharacters[0][0].getRegionWidth();
        int characterHeight = spriteSheetCharacters[0][0].getRegionHeight();
        Rectangle rectA = new Rectangle((int)a.position.x,(int)a.position.y, characterWidth, characterHeight);
        return rectA.intersects(b.position.x,b.position.y, characterWidth, characterHeight);
    }

    void handlePlayersBeingAttacked(Character victim, Character attacker) {
        Vector2 relativeEnemyPosition = new Vector2(victim.position.x - attacker.position.x,victim.position.y - attacker.position.y);
        if(relativeEnemyPosition.len() <= ((spriteSheetCharacters[0][0].getRegionHeight() >
                spriteSheetCharacters[0][0].getRegionWidth()) ? spriteSheetCharacters[0][0].getRegionHeight() :
                spriteSheetCharacters[0][0].getRegionWidth()))
        {
            victim.health -= 10 * Gdx.graphics.getDeltaTime();
            victim.secondsDamaged = 1;
            sinceHurtSound += Gdx.graphics.getDeltaTime();
            if(sinceHurtSound > 1.0f + (Math.random() * 1.35) && !hurtSoundPlayedThisFrame) {
                aMusicLibrary.hurtSound.play();
                sinceHurtSound = 0;
                hurtSoundPlayedThisFrame = true;
            }
        }
    }

    boolean isCollide(Vector2 a, Vector2 b, float widthA, float heightA, float widthB, float heightB) {
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
}