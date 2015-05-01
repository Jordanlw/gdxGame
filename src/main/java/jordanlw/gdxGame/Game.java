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
    static ConcurrentLinkedQueue<Zombie> enemies = new ConcurrentLinkedQueue<>();
    static boolean LeftMouseThisFrame = false;
    static Animation legsAnim;
    static Animation torsoAnim;
    static ConcurrentLinkedQueue<Player> players = new ConcurrentLinkedQueue<>();
    static Server serverNet;
    static Client clientNet;
    static boolean isServer = true;
    static Gui gui;
    static private Jeep jeep;
    private static float volume = 0.3f;
    static private MusicLibrary aMusicLibrary;
    static private boolean gamePaused = true;
    private Texture backgroundTexture;
    private SpriteBatch batch;
    private Medkit medkit;
    private float waveTime = 9999;
    private float totalTime = 0;
    private long lastPacketSent = 0;

    static public void unPauseGame() {
        gamePaused = false;
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

        Gdx.input.setInputProcessor(new InputProcessor());

        //tiled background images
        backgroundTexture = new Texture(Gdx.files.internal("images/grey-background-seamless.jpg"));
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        TextureRegion playerLegsCropped = new TextureRegion(new Texture(Gdx.files.internal("images/feet-sheet.png")));
        legsAnim = new Animation(0.105f, playerLegsCropped.split(33, 69)[0]);
        legsAnim.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion playerTorso = new TextureRegion(new Texture(Gdx.files.internal("images/human-shooting-sheet.png")));
        float torsoAnimLength = 0.20f;
        torsoAnim = new Animation(torsoAnimLength / 6, playerTorso.split(33, 69)[0]);
        torsoAnim.setPlayMode(Animation.PlayMode.LOOP);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, windowSize.x, windowSize.y);

        batch = new SpriteBatch();

        medkit = new Medkit();
        medkit.health = 0;

        jeep = new Jeep();

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
        Vector2 cVec = new Vector2();
        player.position.getPosition(cVec);
        clickRelativePlayer.set(
                mousePressedPosition.x - cVec.x,
                mousePressedPosition.y - cVec.y);
        distanceToMouse.x = (float) Math.sqrt(clickRelativePlayer.x * clickRelativePlayer.x + clickRelativePlayer.y * clickRelativePlayer.y);
    }

    private void spawnEnemies() {
        waveTime += Gdx.graphics.getDeltaTime();
        //Handle Enemy Waves
        if (waveTime > 2) {
            waveTime = 0;
            for (int i = 0; i < 3; i++) {
                enemies.add(new Zombie());
            }
        }
    }

    public void render() {
        Vector2 relativeMousePosition = new Vector2();
        Vector2 distanceToMouse = new Vector2();
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
            {
                //Update player rotation wrt mouse position
                Vector2 pVec = new Vector2();
                getLocalPlayer().position.getPosition(pVec);
                //pVec.add(16, 53);
                getLocalPlayer().rotation = (float) Mouse.angleBetween(pVec);
            }

            Zombie.zombeGroanSoundTimer += delta;
            if (Zombie.zombeGroanSoundTimer > 6f) {
                int index = (int) (Math.random() * (aMusicLibrary.zombieSounds.length - 1));
                aMusicLibrary.zombieSounds[index].setVolume(aMusicLibrary.zombieSounds[index].play(), 0.5f * volume);
                Zombie.zombeGroanSoundTimer = 0;
            }

            handleInput(relativeMousePosition, mouseClick, distanceToMouse);
            getLocalPlayer().movedThisFrame = movementThisFrame;

            //Anything serverside eg. enemy movement, medkit respawning.
            if (isServer) {
                spawnEnemies();
                for (Zombie enemy : enemies) {
                    if (enemy.health <= 0) {
                        enemy.secondsDamaged = 0;
                        continue;
                    }

                    enemy.secondsDamaged -= delta;
                    Character target = getLocalPlayer();
                    float distance = Character.distance(target,enemy);
                    for (Player loopPlayer : players) {
                        float tmp = Character.distance(loopPlayer, enemy);
                        if (tmp < distance) {
                            target = loopPlayer;
                            distance = tmp;
                        }
                    }
                    if (enemy.target == Zombie.TargetTypes.jeep) {
                        target = jeep;
                    }

                    Vector2 vecTarget = new Vector2();
                    Vector2 vecEnemy = new Vector2();
                    enemy.position.getPosition(vecEnemy);
                    target.position.getPosition(vecTarget);

                    if (
                               (enemy.position.x + (enemy.position.width/2) > target.position.x - (target.position.width / 2))
                            && (enemy.position.x - (enemy.position.width/2) < target.position.x + (target.position.width / 2))
                            && (enemy.position.y + (enemy.position.height/2) > target.position.y - (target.position.height / 2))
                            && (enemy.position.y - (enemy.position.height/2) < target.position.y + (target.position.height / 2))) {
                        continue;
                    }
                    Vector2 tmpEnemy = new Vector2(vecTarget.sub(vecEnemy).nor().scl(delta * enemy.walkingSpeed));

                    float ratio = 200 / (Character.distance(enemy, target) + 1);
                    ratio = Math.min(ratio, 1);
                    tmpEnemy.rotate(enemy.swarmAngle * ratio);

                    enemy.rotation = tmpEnemy.angle();
                    tmpEnemy.add(enemy.position.x, enemy.position.y);
                    enemy.position.setPosition(tmpEnemy);
                }

                for (Player player : players) {
                    if (player.health <= 0) {
                        Zombie.difficulty++;
                        player.respawn();
                        System.out.println("Player has died! setting zombie difficulty to " + Zombie.difficulty);
                    }
                }

                for (Player player : players) {
                    medkit.time += delta;
                    if (medkit.time > Medkit.SECS_TILL_DISAPPEAR && medkit.health <= 0) {
                        medkit.health = Medkit.healthGiven;
                        medkit.position.setPosition((float) (camera.viewportWidth * Math.random()), (float) (camera.viewportHeight * Math.random()));
                    } else if (medkit.time >= Medkit.SECS_TILL_DISAPPEAR && player.position.getPosition(new Vector2()).dst(medkit.position.getPosition(new Vector2())) < 20) {
                        player.health += medkit.health;
                        medkit.health = 0;
                        medkit.time = 0;
                        aMusicLibrary.medkitSound.play(0.3f * volume);
                        if (player.health > 100) {
                            player.health = 100;
                        }
                    }
                }
                if (serverNet != null && System.nanoTime() - lastPacketSent > 25000000) {
                    lastPacketSent = System.nanoTime();
                    Packet packet = new Packet();
                    for (Zombie enemy : enemies) {
                        packet.id = enemy.id.toString();
                        packet.health = enemy.health;
                        packet.rotation = enemy.rotation;
                        packet.x = enemy.position.x;
                        packet.y = enemy.position.y;
                        packet.type = Character.Types.enemy;
                        serverNet.sendToAllUDP(packet);
                    }
                    Player local = getLocalPlayer();
                    packet.id = local.id.toString();
                    packet.x = local.position.x;
                    packet.y = local.position.y;
                    packet.rotation = local.rotation;
                    packet.type = Character.Types.player;
                    packet.movedThisFrame = movementThisFrame;
                    serverNet.sendToAllUDP(packet);
                }
            } else {
                if (clientNet != null && System.nanoTime() - lastPacketSent > 25000000) {
                    lastPacketSent = System.nanoTime();
                    Packet packet = new Packet();
                    Player local = getLocalPlayer();
                    packet.id = local.id.toString();
                    packet.x = local.position.x;
                    packet.y = local.position.y;
                    packet.rotation = local.rotation;
                    packet.type = Character.Types.player;
                    packet.movedThisFrame = movementThisFrame;
                    clientNet.sendUDP(packet);
                }
                for (Zombie enemy : enemies) {
                    enemy.secondsDamaged -= delta;
                }
            }
            if (mouseClick.x != -1 && mouseClick.y != -1 && LeftMouseThisFrame) {
                aMusicLibrary.gunSound.play(volume);
                getLocalPlayer().shootingTime = torsoAnim.getAnimationDuration();
                for (Zombie enemy : enemies) {
                    if (enemy.health <= 0) {
                        continue;
                    }
                    Rectangle eRect = new Rectangle((int) (enemy.position.x - (enemy.position.width/2)), (int) (enemy.position.y - (enemy.position.height/2)), (int) enemy.position.width, (int) enemy.position.height);
                    Vector2 pVec = new Vector2();
                    getLocalPlayer().position.getPosition(pVec);
                    Vector2 mVec = new Vector2(relativeMousePosition);
                    mVec.nor().scl(windowSize.x * windowSize.y).add(pVec);
                    if (eRect.intersectsLine(pVec.x, pVec.y, mVec.x, mVec.y)) {
                        enemy.secondsDamaged = 2;
                        enemy.health -= 60;
                        if (clientNet != null) {
                            Packet packet = new Packet();
                            packet.health = enemy.health;
                            packet.id = enemy.id.toString();
                            packet.type = Character.Types.enemy;
                            clientNet.sendUDP(packet);
                        }
                    }
                    d1.set(pVec.x, pVec.y);
                    d2.set(mVec.x, mVec.y);
                }
            }
            for (Zombie enemy : enemies) {
                if (enemy.health <= 0) {
                    continue;
                }
                Player player = getLocalPlayer();
                if (enemy.position.getPosition(new Vector2()).dst(player.position.getPosition(new Vector2())) < 40) {
                    player.health -= 10 * delta;
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
        jeep.draw(batch);
        medkit.draw(batch);

        //Draw enemies
        for (Zombie enemy : enemies) {
            if (enemy.health > 0) {
                continue;
            }
            enemy.draw(batch, delta);
        }
        for (Zombie enemy : enemies) {
            if (enemy.health <= 0) {
                continue;
            }
            enemy.draw(batch, delta);
        }

        batch.setColor(Color.WHITE);

        for (Player player : players) {
            player.draw(batch, totalTime, delta);
        }

        gui.draw(batch);
        batch.end();

        if(mouseClick.x != -1 && mouseClick.y != -1) {
            ShapeRenderer shape = new ShapeRenderer();
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.line(d1.x,d1.y,d2.x,d2.y);
            shape.end();
        }

        LeftMouseThisFrame = false;
        mouseClick.set(-1, -1);
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