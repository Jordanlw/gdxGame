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
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.ArrayList;

final class Game implements ApplicationListener {
    static final Vector2 windowSize = new Vector2(1280, 720);
    static final Vector2 mousePressedPosition = new Vector2(-1,-1);
    static TextureRegion[] goldSheet;
    static ArrayList<Zombie> enemies = new ArrayList<>();
    static boolean isLeftMousePressedThisFrame = false;
    static Animation legsAnim;
    static Animation torsoAnim;
    static ArrayList<Player> players = new ArrayList<>(0);
    static String serverAddress;
    static Server serverNet;
    static Client clientNet;
    static boolean isServer = true;
    private static float volume = 0.3f;
    static Gui gui;
    private boolean isGameCreated = false;
    private final Gold gold = new Gold();
    private float timeGunSound;
    private Texture backgroundTexture;
    private Texture gameOverTexture;
    private TextureRegion singlePixel;
    public static OrthographicCamera camera;
    private SpriteBatch batch;
    private Medkit medkit;
    static private MusicLibrary aMusicLibrary;
    static public boolean movementThisFrame = false;
    static private boolean gamePaused = true;
    private float waveTime = 0;
    private int currentWave = 1;
    private float totalTime = 0;
    static public float shootingTime = 0;

    public void create() {
        aMusicLibrary = new MusicLibrary();
        aMusicLibrary.backgroundMusic.setLooping(false);
        aMusicLibrary.backgroundMusic.setVolume(0.15f*volume);

        Gdx.input.setInputProcessor(new InputProcessor());

        //Load images of text
        gameOverTexture = new Texture(Gdx.files.internal("images/gameover.png"));

        //tiled background images
        backgroundTexture = new Texture(Gdx.files.internal("images/grey-background-seamless.jpg"));
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        TextureRegion playerLegsCropped = new TextureRegion(new Texture(Gdx.files.internal("images/feet-sheet.png")));
        legsAnim = new Animation(0.105f,playerLegsCropped.split(23,38)[0]);
        legsAnim.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion playerTorso = new TextureRegion(new Texture(Gdx.files.internal("images/human-shooting-sheet.png")));
        float torsoAnimLength = 0.20f;
        torsoAnim = new Animation(torsoAnimLength /6,playerTorso.split(33,63)[0]);
        torsoAnim.setPlayMode(Animation.PlayMode.LOOP);

        //gold coin spritesheet
        Texture goldTexture = new Texture(Gdx.files.internal("images/goldcoin-sheet.png"));
        TextureRegion[][] goldTmp = TextureRegion.split(goldTexture, goldTexture.getWidth() / 4, goldTexture.getHeight());
        goldSheet = goldTmp[0];

        singlePixel = new TextureRegion(new Texture(Gdx.files.internal("images/singlePixel.png")));

        camera = new OrthographicCamera();
        camera.setToOrtho(false, windowSize.x, windowSize.y);

        batch = new SpriteBatch();

        medkit = new Medkit();
        medkit.health = 0;

        players.add(0, new Player());
        players.get(0).isSelf = true;
        players.get(0).connected = true;

        Zombie.init();

        gui = new Gui();
    }

    static public void unPauseGame() {
        gamePaused = false;
        aMusicLibrary.backgroundMusic.play();
    }

    private void setupGame() {
        isGameCreated = true;

        for (int i = 0; i < 5; i++) {
            enemies.add(new Zombie());
        }

        for (Zombie enemy : enemies) {
            enemy.respawn(1);
        }
        getLocalPlayer().isServer = true;
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
        gui.update();

        if (!gamePaused) {
            totalTime += delta;
            for(Player player : players) {
                player.secondsDamaged -= delta;
            }
            //Update player rotation wrt mouse position
            getLocalPlayer().rotation = (float)Mouse.angleBetween(getLocalPlayer().position.getPosition(new Vector2()));

            Zombie.zombeGroanSoundTimer += delta;
            if (Zombie.zombeGroanSoundTimer > 6f) {
                int index = (int) (Math.random() * (aMusicLibrary.zombieSounds.length - 1));
                aMusicLibrary.zombieSounds[index].setVolume(aMusicLibrary.zombieSounds[index].play(), 0.5f*volume);
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
                for (Zombie enemy : enemies) {
                    if (enemy.health <= 0) {
                        continue;
                    }
                    enemy.secondsDamaged -= delta;

                    float distance = 0;
                    Player player = getLocalPlayer();
                    for(Player loopPlayer : players) {
                        float tmp = Character.distance(loopPlayer, enemy);
                        if(tmp < distance) {
                            player = loopPlayer;
                            distance = tmp;
                        }
                    }

                    Vector2 vecPlayer = new Vector2();
                    Vector2 vecEnemy = new Vector2();
                    enemy.position.getPosition(vecEnemy);
                    player.position.getPosition(vecPlayer);

                    Vector2 tmpEnemy = new Vector2(vecPlayer.sub(vecEnemy).nor().scl(delta * enemy.walkingSpeed));

                    float ratio = 200/(Character.distance(enemy,player) + 1);
                    ratio = Math.min(ratio,1);
                    tmpEnemy.rotate(enemy.swarmAngle * ratio);

                    enemy.rotation = tmpEnemy.angle();
                    tmpEnemy.add(enemy.position.x, enemy.position.y);
                    enemy.position.setPosition(tmpEnemy);
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
                        aMusicLibrary.medkitSound.play(0.3f * volume);
                        if (player.health > 100) {
                            player.health = 100;
                        }
                    }
                }
                if (serverNet != null) {
                    Packet packet = new Packet();
                    for (int i = 0; i < enemies.size(); i++) {
                        packet.id = enemies.get(i).id;
                        packet.rotation = enemies.get(i).rotation;
                        packet.x = enemies.get(i).position.x;
                        packet.y = enemies.get(i).position.y;
                        serverNet.sendToAllUDP(packet);
                    }

                }
            }
            /*else {
                if (clientNet != null) {
                    Packet packet = new Packet();
                    Player local = getLocalPlayer();
                    packet.id = local.id;
                    packet.x = local.position.x;
                    packet.y = local.position.y;
                    packet.rotation = local.rotation;
                }
            }*/
            /*
            //Respond to player pressing mouse button
            if (mousePressedPosition.x != -1 && mousePressedPosition.y != -1 && getLocalPlayer().health > 0) {
                //Gun sound for player
                if(totalTime > timeGunSound) {
                    timeGunSound = totalTime + 0.5f;
                    aMusicLibrary.gunSound.play(0.25f * volume);
                    //aMusicLibrary.gunSound.setPitch(soundId, 1 + (long) (0.3f * Math.random()));
                    gunFiredThisFrame = true;
                    shootingTime = torsoAnimLength;
                    Collections.sort(enemies,new ZombieDistance());
                    for (Zombie enemy : enemies) {
                        if (enemy.health <= 0) {
                            continue;
                        }

                        Vector2 vecPlayer = new Vector2();
                        getLocalPlayer().position.getCenter(vecPlayer);

                        float angle = (float)Mouse.angleBetween(vecPlayer);
                        Vector2 vecAngle = new Vector2(vecPlayer);
                        Vector2 tmp = new Vector2(1,1);
                        tmp.setAngle(angle).nor().scl(98765);
                        vecAngle.add(tmp);

                        if (Intersector.intersectSegmentCircle(vecPlayer,vecAngle,enemy.position.getCenter(new Vector2()),(enemy.position.width/2)*(enemy.position.width/2))) {
                            enemy.secondsDamaged = 0.5f;
                            enemy.health -= 35;
                            if (enemy.health <= 0) {
                                gold.saveEnemy(currentWave,enemies.indexOf(enemy));
                            }
                            break;
                        }
                    }
                }
            }
            */
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

        if (getLocalPlayer().health <= 0) {
            batch.draw(gameOverTexture, camera.viewportWidth / 2 - gameOverTexture.getWidth() / 2,
                    camera.viewportHeight / 2 - gameOverTexture.getHeight() / 2);
        }

        batch.setColor(Color.YELLOW);
        if (gunFiredThisFrame) {
            batch.draw(
                    singlePixel,
                    getLocalPlayer().position.x,
                    getLocalPlayer().position.y,
                    0, 0, 1, 1, 1,
                    distanceToMouse.x,
                    180+(float) Math.toDegrees(Math.atan2((double) relativeMousePosition.x, (double) relativeMousePosition.y)));

        }
        gui.draw(batch);
        batch.end();

        /*ShapeRenderer shape = new ShapeRenderer();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(1,1,1,1);
        Rectangle rect = gui.buttons.get(0).rect;
        shape.rect(rect.x,rect.y,rect.width,rect.height);
        shape.end();*/

        isLeftMousePressedThisFrame = false;
        mousePressedPosition.set(-1,-1);
    }

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
        if (serverNet != null) {
            try {
                serverNet.stop();
                serverNet.dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (clientNet != null) {
            try {
                clientNet.stop();
                clientNet.dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}