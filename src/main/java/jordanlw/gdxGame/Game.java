/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Jordan Windsor
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jordanlw.gdxGame;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;

class Game implements ApplicationListener {
    static final Vector2 windowSize = new Vector2(1280, 720);
    static final Vector2 mousePressedPosition = new Vector2(-1,-1);
    static TextureRegion[] goldSheet;
    static ArrayList<Zombie> enemies;
    static String[] cmdArgs;
    static boolean isLeftMousePressedThisFrame = false;
    static Animation legsAnim;
    static Animation torsoAnim;
    static ArrayList<Player> players = new ArrayList<>();
    static String serverAddress;
    static Server serverNet;
    static Client clientNet;
    static boolean isServer = true;
    private boolean isGameCreated = false;
    private final Gold gold = new Gold();
    private final float torsoAnimLength = 0.20f;
    private float timeGunSound;
    private Texture backgroundTexture;
    private Texture gameOverTexture;
    private Texture gameStartTexture;
    private TextureRegion[] explosionSheet;
    private TextureRegion singlePixel;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Medkit medkit;
    private MusicLibrary aMusicLibrary;
    static public boolean movementThisFrame = false;
    private boolean gamePaused = true;
    private float waveTime = 0;
    private int currentWave = 1;
    private Animation explosionAnimation;
    private int explosionTarget = -1;
    private float animationTimer;
    private float totalTime = 0;
    static public float shootingTime = 0;

    public void create() {
        aMusicLibrary = new MusicLibrary();
        aMusicLibrary.backgroundMusic.setLooping(false);
        aMusicLibrary.backgroundMusic.setVolume(0.10f);

        Gdx.input.setInputProcessor(new InputProcessor());

        //Load images of text
        gameOverTexture = new Texture(Gdx.files.internal("images/gameover.png"));
        gameStartTexture = new Texture(Gdx.files.internal("images/press-p-to-play.png"));

        //tiled background images
        backgroundTexture = new Texture(Gdx.files.internal("images/grey-background-seamless.jpg"));
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        TextureRegion playerLegsCropped = new TextureRegion(new Texture(Gdx.files.internal("images/feet-sheet.png")));
        legsAnim = new Animation(0.105f,playerLegsCropped.split(23,38)[0]);
        legsAnim.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion playerTorso = new TextureRegion(new Texture(Gdx.files.internal("images/human-shooting-sheet.png")));
        torsoAnim = new Animation(torsoAnimLength/6,playerTorso.split(33,63)[0]);
        torsoAnim.setPlayMode(Animation.PlayMode.LOOP);

        //Explosion/damaged overlay spirtesheet
        Texture explosionTexture = new Texture(Gdx.files.internal("images/explosion-sheet.png"));
        TextureRegion[][] explosionTmp = TextureRegion.split(explosionTexture, explosionTexture.getWidth() / 4, explosionTexture.getHeight());
        explosionSheet = explosionTmp[0];
        explosionAnimation = new Animation(0.16f, explosionSheet);

        //gold coin spritesheet
        Texture goldTexture = new Texture(Gdx.files.internal("images/goldcoin-sheet.png"));
        TextureRegion[][] goldTmp = TextureRegion.split(goldTexture, goldTexture.getWidth() / 4, goldTexture.getHeight());
        goldSheet = goldTmp[0];

        singlePixel = new TextureRegion(new Texture(Gdx.files.internal("images/singlePixel.png")));

        camera = new OrthographicCamera();
        camera.setToOrtho(false, windowSize.x, windowSize.y);

        batch = new SpriteBatch();

    }

    private void setupGame() {
        isGameCreated = true;

        enemies = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            enemies.add(new Zombie());
        }

        for (Zombie enemy : enemies) {
            enemy.respawn(1);
        }

        players.add(0, new Player());
        players.get(0).isSelf = true;

        medkit = new Medkit();
        medkit.health = 0;
    }

    static public Player getLocalPlayer() {
        for (Player player : players) {
            if(player.isSelf) {
                return player;
            }
        }
        return null;
    }

    private void handleInput(Vector2 clickRelativePlayer, Vector2 mousePressedPosition, Vector2 distanceToMouse,
                             Vector2 bulletVector) {
        Player player = getLocalPlayer();
        Integer movementSpeed = 250;
        Vector2 deltaPosition = new Vector2(0,0);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            deltaPosition.y += movementSpeed;
            movementThisFrame = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            deltaPosition.y -= movementSpeed;
            movementThisFrame = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            deltaPosition.x -= movementSpeed;
            movementThisFrame = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            deltaPosition.x += movementSpeed;
            movementThisFrame = true;
        }
        deltaPosition = deltaPosition.nor().scl(movementSpeed * Gdx.graphics.getDeltaTime());
        player.position.setPosition(player.position.getPosition(new Vector2()).add(deltaPosition));

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
            mousePressedPosition.set(Gdx.input.getX(), camera.viewportHeight - Gdx.input.getY());
        clickRelativePlayer.set(
                mousePressedPosition.x - player.position.x,
                -(mousePressedPosition.y - player.position.y));
        distanceToMouse.x = (float) Math.sqrt(clickRelativePlayer.x * clickRelativePlayer.x + clickRelativePlayer.y * clickRelativePlayer.y);
        bulletVector.x = ((windowSize.x + windowSize.y) * clickRelativePlayer.x) + player.position.x;
        bulletVector.y = ((windowSize.x + windowSize.y) * -clickRelativePlayer.y) + player.position.y;
    }

    private void handleEnemyWaves() {
        waveTime += Gdx.graphics.getDeltaTime();
        //Handle Enemy Waves
        if ((waveTime > (currentWave * 5) && currentWave != 1) || (waveTime > 10 && currentWave == 1)) {
            waveTime = 0;
            currentWave++;
            for (int i = 0; i < 5; i++) {
                enemies.add(new Zombie());
            }
            for (Zombie enemy : enemies) {
                if (enemy.health <= 0) {
                    enemy.respawn(currentWave);
                }
            }
        }
    }

    public void render() {
        Vector2 bulletVector = new Vector2();
        Vector2 relativeMousePosition = new Vector2();
        Vector2 distanceToMouse = new Vector2();
        Boolean gunFiredThisFrame = false;
        float delta = Gdx.graphics.getDeltaTime();
        movementThisFrame = false;

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(0);
        handleEnemyWaves();

        //Handle player wanting to pause
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (gamePaused) {
                gamePaused = false;
                aMusicLibrary.backgroundMusic.play();
            } else {
                gamePaused = true;
                aMusicLibrary.backgroundMusic.pause();
            }
        }

        //Does the user want multiplayer?
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            NetworkSetup.getTextInput("Multiplayer Network Address");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            NetworkSetup.getAnswer("Host Multiplayer?");
        }

        if (!gamePaused) {
            if(!isGameCreated && isServer) {
                setupGame();
            }
            totalTime += delta;
            for(Player player : players) {
                player.secondsDamaged -= delta;
            }
            //Update player rotation wrt mouse position
            getLocalPlayer().rotation = (float)Mouse.angleBetween(getLocalPlayer().position.getPosition(new Vector2()));

            Zombie.zombeGroanSoundTimer += delta;
            if (Zombie.zombeGroanSoundTimer > 6f) {
                int index = (int) (Math.random() * (aMusicLibrary.zombieSounds.length - 1));
                aMusicLibrary.zombieSounds[index].setVolume(aMusicLibrary.zombieSounds[index].play(), 1f);
                Zombie.zombeGroanSoundTimer = 0;
            }

            handleInput(relativeMousePosition, mousePressedPosition, distanceToMouse, bulletVector);

            //Anything serverside eg. enemy movement, medkit respawning.
            if (isServer) {
                if (!aMusicLibrary.backgroundMusic.isPlaying()) {
                    for(Player player : players) {
                        player.health = 0;
                    }
                }
                for(Player player : players) {
                    medkit.time += delta;
                    if (medkit.time > Medkit.SECS_TILL_DISAPPEAR && medkit.health <= 0) {
                        medkit.health = Medkit.healthGiven;
                        medkit.position.setPosition((float) (camera.viewportWidth * Math.random()), (float) (camera.viewportHeight * Math.random()));
                    } else if (medkit.time >= Medkit.SECS_TILL_DISAPPEAR && player.position.overlaps(medkit.position)) {
                        player.health += medkit.health;
                        medkit.health = 0;
                        medkit.time = 0;
                        aMusicLibrary.medkitSound.play();
                        if (player.health > 100) {
                            player.health = 100;
                        }
                    }

                    for (Zombie enemy : enemies) {
                        if (enemy.health <= 0) {
                            continue;
                        }
                        for (Zombie enemy2 : enemies) {
                            if (enemy.health <= 0) {
                                continue;
                            }
                            if (enemy2.position.overlaps(enemy.position)) {
                                double angle = angleBetweenCharacters(enemy, enemy2) - Math.PI;
                                enemy2.position.setX(enemy2.position.getX() + (float)Math.cos(angle) * 10 * delta);
                                enemy2.position.setY(enemy2.position.getY() + (float)Math.sin(angle) * 10 * delta);
                            }
                        }
                        enemy.secondsDamaged -= delta;
                        enemy.circleChangeTimer -= delta;
                        if (enemy.circleChangeTimer < 0) {
                            enemy.circleDirection = !enemy.circleDirection;
                            enemy.circleChangeTimer = (Math.random() * 7) + 7;
                        }

                        float relAngle = enemy.position.getPosition(new Vector2()).angle(player.position.getPosition(new Vector2()));
                        relAngle += enemy.circleDirection ? 25 : -25;
                        Vector2 pos = new Vector2();
                        enemy.position.getPosition(pos);
                        pos.rotate(relAngle).scl(enemy.walkingSpeed);
                        //handlePlayersBeingAttacked(players, enemy);

                    }
                }
            }
            //Respond to player pressing mouse button
            if (mousePressedPosition.x != -1 && mousePressedPosition.y != -1 && getLocalPlayer().health > 0) {
                //Gun sound for player
                if(totalTime > timeGunSound) {
                    timeGunSound = totalTime + 0.5f;
                    aMusicLibrary.gunSound.play(0.25f);
                    //aMusicLibrary.gunSound.setPitch(soundId, 1 + (long) (0.3f * Math.random()));
                    gunFiredThisFrame = true;
                    shootingTime = torsoAnimLength;
                    Collections.sort(enemies,new ZombieDistance());
                    for (Zombie enemy : enemies) {
                        if (enemy.health <= 0) {
                            continue;
                        }
                        if (getLocalPlayer().position.overlaps(enemy.position)) {
                            enemy.secondsDamaged = 0.5f;
                            enemy.health -= 35;
                            if (enemy.health <= 0) {
                                gold.saveEnemy(currentWave,enemies.indexOf(enemy));
                            }
                            explosionTarget = enemies.indexOf(enemy);
                            break;
                        }
                    }
                }
            }
        }
        if(!isGameCreated) {
            return;
        }
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        for (int width = 0; width < windowSize.x; width += backgroundTexture.getWidth()) {
            for (int height = 0; height < windowSize.y; height += backgroundTexture.getHeight()) {
                batch.draw(backgroundTexture, width, height);
            }
        }
        medkit.draw(batch);
        gold.draw(batch);

        //Draw enemies
        for (Zombie enemy : enemies) {
            enemy.draw(batch,totalTime);
        }
        batch.setColor(Color.WHITE);

        for (Player player : players) {
            player.draw(batch,totalTime,delta);
        }

        if (explosionTarget >= 0 && enemies.get(explosionTarget).health > 0) {
            TextureRegion explosionKeyframe = explosionAnimation.getKeyFrame(animationTimer, false);
            Vector2 explosionPosition = new Vector2(enemies.get(explosionTarget).position.x, enemies.get(explosionTarget).position.y);
            explosionPosition.sub(Zombie.getCenter());
            explosionPosition.add(explosionSheet[0].getRegionWidth() / 2, explosionSheet[0].getRegionHeight() / 2);
            explosionPosition.add((float) (Math.random() - 0.5f) * 3.45f, (float) (Math.random() - 0.5f) * 3.45f);
            batch.draw(explosionKeyframe, explosionPosition.x, explosionPosition.y);
            animationTimer += delta;
            if (explosionAnimation.isAnimationFinished(animationTimer)) {
                explosionTarget = -1;
                animationTimer = 0;
            }
        }

        if (getLocalPlayer().health <= 0) {
            batch.draw(gameOverTexture, camera.viewportWidth / 2 - gameOverTexture.getWidth() / 2,
                    camera.viewportHeight / 2 - gameOverTexture.getHeight() / 2);
        }
        else if (gamePaused) {
            batch.draw(
                    gameStartTexture,
                    camera.viewportWidth/2 - gameStartTexture.getWidth()/2,
                    camera.viewportHeight/2 - gameStartTexture.getHeight()/2);
        }

        batch.setColor(Color.YELLOW);
        if (gunFiredThisFrame) {
            //noinspection SuspiciousNameCombination
            batch.draw(
                    singlePixel,
                    getLocalPlayer().position.x,
                    getLocalPlayer().position.y,
                    0, 0, 1, 1, 1,
                    distanceToMouse.x,
                    180+(float) Math.toDegrees(Math.atan2((double) relativeMousePosition.x, (double) relativeMousePosition.y)));

        }
        batch.end();

        isLeftMousePressedThisFrame = false;
        mousePressedPosition.set(-1,-1);
    }

    private double angleBetweenCharacters(Character a, Character b) {
        return Math.atan2(a.position.y - b.position.y, a.position.x - b.position.x);
    }
    /*
    private boolean isCharacterCollided(Character a, Character b) {
        int characterWidth = legsAnim.getKeyFrame(0).getRegionWidth();
        int characterHeight = legsAnim.getKeyFrame(0).getRegionHeight();
        Rectangle rectA = new Rectangle((int) a.position.x, (int) a.position.y, characterWidth, characterHeight);
        return rectA.overlaps(new Rectangle(b.position.x, b.position.y, characterWidth, characterHeight));
    }
    */
    private void handlePlayersBeingAttacked(Character victim, Character attacker) {
        Vector2 relativeEnemyPosition = new Vector2(victim.position.x - attacker.position.x, victim.position.y - attacker.position.y);
        if (relativeEnemyPosition.len() <= 10) {
            if(attacker.lastAttack + attacker.attackDelay > totalTime) {
                return;
            }
            attacker.lastAttack = totalTime;

            victim.health -= 10 * Gdx.graphics.getDeltaTime();
            victim.secondsDamaged = 1;
            aMusicLibrary.hurtSound.play(0.5f);
        }
    }

    public void resize(int width, int height) {
    }

    public void pause() {
    }

    public void resume() {
    }

    public void dispose() {
    }
}