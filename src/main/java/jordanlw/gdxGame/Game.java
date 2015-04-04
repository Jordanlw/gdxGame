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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

final class Game implements ApplicationListener {
    static final Vector2 windowSize = new Vector2(1280, 720);
    static final Vector2 mouseClick = new Vector2(-1, -1);
    public static OrthographicCamera camera;
    static public boolean movementThisFrame = false;
    static public float shootingTime = 0;
    static TextureRegion[] goldSheet;
    static ConcurrentLinkedQueue<Zombie> enemies = new ConcurrentLinkedQueue<>();
    static boolean LeftMouseThisFrame = false;
    static Animation legsAnim;
    static Animation torsoAnim;
    static ConcurrentLinkedQueue<Player> players = new ConcurrentLinkedQueue<>();
    static Server serverNet;
    static Client clientNet;
    static boolean isServer = true;
    static Gui gui;
    private static float volume = 0.3f;
    static private MusicLibrary aMusicLibrary;
    static private boolean gamePaused = true;
    private final Gold gold = new Gold();
    private float timeGunSound;
    private Texture backgroundTexture;
    private Texture gameOverTexture;
    private TextureRegion singlePixel;
    private SpriteBatch batch;
    private Medkit medkit;
    private float waveTime = 0;
    private int currentWave = 1;
    private float totalTime = 0;
    private long lastPacketSent = 0;

    static public void unPauseGame() {
        gamePaused = false;
        aMusicLibrary.backgroundMusic.play();
    }

    static public Player getLocalPlayer() {
        for (Player player : players) {
            if (player.isSelf) {
                return player;
            }
        }
        return null;
    }

    public void create() {
        aMusicLibrary = new MusicLibrary();
        aMusicLibrary.backgroundMusic.setLooping(false);
        aMusicLibrary.backgroundMusic.setVolume(0.15f * volume);

        Gdx.input.setInputProcessor(new InputProcessor());

        //Load images of text
        gameOverTexture = new Texture(Gdx.files.internal("images/gameover.png"));

        //tiled background images
        backgroundTexture = new Texture(Gdx.files.internal("images/grey-background-seamless.jpg"));
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        TextureRegion playerLegsCropped = new TextureRegion(new Texture(Gdx.files.internal("images/feet-sheet.png")));
        legsAnim = new Animation(0.105f, playerLegsCropped.split(23, 38)[0]);
        legsAnim.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion playerTorso = new TextureRegion(new Texture(Gdx.files.internal("images/human-shooting-sheet.png")));
        float torsoAnimLength = 0.20f;
        torsoAnim = new Animation(torsoAnimLength / 6, playerTorso.split(33, 63)[0]);
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

        Zombie.init();

        gui = new Gui();
    }

    private void handleInput(Vector2 clickRelativePlayer, Vector2 mousePressedPosition, Vector2 distanceToMouse) {
        Player player = getLocalPlayer();
        Integer movementSpeed = 250;
        Vector2 deltaPosition = new Vector2(0, 0);
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
        deltaPosition.nor().scl(movementSpeed * Gdx.graphics.getDeltaTime());
        player.position.setPosition(player.position.getPosition(new Vector2()).add(deltaPosition));

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            mousePressedPosition.set(Gdx.input.getX(), windowSize.y - Gdx.input.getY());
        }
        clickRelativePlayer.set(
                mousePressedPosition.x - player.position.x,
                mousePressedPosition.y - player.position.y);
        distanceToMouse.x = (float) Math.sqrt(clickRelativePlayer.x * clickRelativePlayer.x + clickRelativePlayer.y * clickRelativePlayer.y);
    }

    private void spawnEnemies() {
        waveTime += Gdx.graphics.getDeltaTime();
        //Handle Enemy Waves
        if (waveTime > 3) {
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
        Vector2 relativeMousePosition = new Vector2();
        Vector2 distanceToMouse = new Vector2();
        Boolean gunFiredThisFrame = false;
        float delta = Gdx.graphics.getDeltaTime();
        movementThisFrame = false;

        gui.update();

        Vector2 d1 = new Vector2();
        Vector2 d2 = new Vector2();

        if (!gamePaused) {
            totalTime += delta;
            for (Player player : players) {
                player.secondsDamaged -= delta;
            }
            //Update player rotation wrt mouse position
            getLocalPlayer().rotation = (float) Mouse.angleBetween(getLocalPlayer().position.getCenter(new Vector2()));

            Zombie.zombeGroanSoundTimer += delta;
            if (Zombie.zombeGroanSoundTimer > 6f) {
                int index = (int) (Math.random() * (aMusicLibrary.zombieSounds.length - 1));
                aMusicLibrary.zombieSounds[index].setVolume(aMusicLibrary.zombieSounds[index].play(), 0.5f * volume);
                Zombie.zombeGroanSoundTimer = 0;
            }

            handleInput(relativeMousePosition, mouseClick, distanceToMouse);

            //Anything serverside eg. enemy movement, medkit respawning.
            if (isServer) {
                spawnEnemies();
                if (!aMusicLibrary.backgroundMusic.isPlaying()) {
                    for (Player player : players) {
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
                    for (Player loopPlayer : players) {
                        float tmp = Character.distance(loopPlayer, enemy);
                        if (!(distance > tmp)) {
                            player = loopPlayer;
                            distance = tmp;
                        }
                    }

                    Vector2 vecPlayer = new Vector2();
                    Vector2 vecEnemy = new Vector2();
                    enemy.position.getCenter(vecEnemy);
                    player.position.getCenter(vecPlayer);

                    Vector2 tmpEnemy = new Vector2(vecPlayer.sub(vecEnemy).nor().scl(delta * enemy.walkingSpeed));

                    float ratio = 200 / (Character.distance(enemy, player) + 1);
                    ratio = Math.min(ratio, 1);
                    tmpEnemy.rotate(enemy.swarmAngle * ratio);

                    enemy.rotation = tmpEnemy.angle();
                    tmpEnemy.add(enemy.position.x, enemy.position.y);
                    enemy.position.setPosition(tmpEnemy);
                }

                for (Player player : players) {
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
                if (serverNet != null && System.nanoTime() - lastPacketSent > 50000000) {
                    lastPacketSent = System.nanoTime();
                    Packet packet = new Packet();
                    for (Zombie enemy : enemies) {
                        packet.id = enemy.id.toString();
                        packet.rotation = enemy.rotation;
                        packet.x = enemy.position.x;
                        packet.y = enemy.position.y;
                        serverNet.sendToAllUDP(packet);
                    }

                }
            } else {
                if (clientNet != null && System.nanoTime() - lastPacketSent > 50000000) {
                    lastPacketSent = System.nanoTime();
                    Packet packet = new Packet();
                    Player local = getLocalPlayer();
                    packet.id = local.id.toString();
                    packet.x = local.position.x;
                    packet.y = local.position.y;
                    packet.rotation = local.rotation;
                    clientNet.sendUDP(packet);
                }
            }
            if (mouseClick.x != -1 && mouseClick.y != -1 && LeftMouseThisFrame) {
                for (Zombie enemy : enemies) {
                    Rectangle eRect = new Rectangle((int)enemy.position.x, (int)enemy.position.y, (int)enemy.position.width, (int)enemy.position.height);
                    Vector2 pVec = new Vector2();
                    getLocalPlayer().position.getCenter(pVec);
                    Vector2 mVec = new Vector2(relativeMousePosition);
                    mVec.nor().scl(windowSize.x * windowSize.y).add(pVec);
                    if (eRect.intersectsLine(pVec.x, pVec.y, mVec.x, mVec.y)) {
                        enemy.secondsDamaged = 2;
                    }
                    d1.set(pVec);
                    d2.set(mVec);
                }
            }
        }
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.disableBlending();
        for (int width = 0; width < windowSize.x; width += backgroundTexture.getWidth()) {
            for (int height = 0; height < windowSize.y; height += backgroundTexture.getHeight()) {
                batch.draw(backgroundTexture, width, height);
            }
        }
        batch.enableBlending();
        medkit.draw(batch);
        gold.draw(batch);

        //Draw enemies
        for (Zombie enemy : enemies) {
            enemy.draw(batch, totalTime);
        }
        batch.setColor(Color.WHITE);

        for (Player player : players) {
            player.draw(batch, totalTime, delta);
        }

        if (players.size() > 0 && getLocalPlayer().health <= 0) {
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
                    180 + (float) Math.toDegrees(Math.atan2((double) relativeMousePosition.x, (double) relativeMousePosition.y)));

        }
        gui.draw(batch);
        batch.end();

        ShapeRenderer shape = new ShapeRenderer();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.line(d1.x, d1.y, d2.x, d2.y);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.rect(mouseClick.x, mouseClick.y, 10, 10);
        shape.end();

        LeftMouseThisFrame = false;
        mouseClick.set(-1, -1);
    }

    private void handlePlayersBeingAttacked(Character victim, Character attacker) {
        Vector2 relativeEnemyPosition = new Vector2(victim.position.x - attacker.position.x, victim.position.y - attacker.position.y);
        if (relativeEnemyPosition.len() <= 10) {
            if (attacker.lastAttack + attacker.attackDelay > totalTime) {
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
        /*if (clientNet != null) {
            try {
                clientNet.stop();
                clientNet.dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }
}