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
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import jordanlw.gdxGame.character.Character;
import jordanlw.gdxGame.character.*;
import jordanlw.gdxGame.network.Network;

import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Game implements ApplicationListener {
    static public final Vector2 windowSize = new Vector2(1280, 720);
    static public final ConcurrentLinkedQueue<Zombie> enemies = new ConcurrentLinkedQueue<>();
    public static final ConcurrentLinkedQueue<Player> players = new ConcurrentLinkedQueue<>();
    static final Vector2 mouseClick = new Vector2(-1, -1);
    private static final FPSLogger log = new FPSLogger();
    public static Player localPlayer;
    public static OrthographicCamera camera;
    public static boolean isServer = true;
    public static boolean isMultiplayer = false;
    static boolean LeftMouseThisFrame = false;
    static Gui gui;
    private static boolean movementThisFrame = false;
    static private Jeep jeep;
    static private Turret turret;
    private static float volume = 0.3f;
    static private MusicLibrary aMusicLibrary;
    static private boolean gamePaused = true;
    static private boolean gameOver = false;
    static private boolean gameStarted = false;
    private Texture backgroundTexture;
    private SpriteBatch batch;
    private Medkit medkit;
    private float waveTime = 9999;
    private float totalTime = 0;
    private long lastPacketSent = 0;

    static public void unPauseGame() {
        gamePaused = false;
        gameStarted = true;
    }

    private static Player getLocalPlayer() {
        for (Player player : players) {
            if (player.isSelf) {
                return player;
            }
        }
        Gdx.app.exit();
        return new Player(true);
    }

    public void create() {
        Game.players.add(new Player(true));
        localPlayer = getLocalPlayer();

        aMusicLibrary = new MusicLibrary();

        Gdx.input.setInputProcessor(new InputProcessor());

        //tiled background images
        backgroundTexture = new Texture(Gdx.files.internal("images/grey-background-seamless.jpg"));
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, windowSize.x, windowSize.y);

        batch = new SpriteBatch();

        medkit = new Medkit();
        medkit.health = 0;

        jeep = new Jeep();
        turret = new Turret();

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

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            gamePaused = !gamePaused;
        }

        if (!gamePaused) {
            totalTime += delta;
            for (Player player : players) {
                player.tickDownSecondsDamaged(delta);
            }
            {
                //Update player rotation wrt mouse position
                Vector2 pVec = new Vector2();
                localPlayer.position.getPosition(pVec);
                //pVec.add(16, 53);
                localPlayer.rotation = (float) Mouse.angleBetween(pVec);
            }

            Zombie.groanSoundTimer += delta;
            if (Zombie.groanSoundTimer > 6f) {
                int index = (int) (Math.random() * (aMusicLibrary.zombieSounds.length - 1));
                aMusicLibrary.zombieSounds[index].setVolume(aMusicLibrary.zombieSounds[index].play(), 0.5f * volume);
                Zombie.groanSoundTimer = 0;
            }

            medkit.time += delta;
            if (medkit.time > Medkit.SECS_TILL_DISAPPEAR && medkit.health <= 0) {
                medkit.health = Medkit.healthGiven;
                medkit.position.setPosition((float) (camera.viewportWidth * Math.random()), (float) (camera.viewportHeight * Math.random()));
            } else if (medkit.time >= Medkit.SECS_TILL_DISAPPEAR && localPlayer.position.getPosition(new Vector2()).dst(medkit.position.getPosition(new Vector2())) < 20) {
                localPlayer.health += medkit.health;
                medkit.health = 0;
                medkit.time = 0;
                aMusicLibrary.medkitSound.play(0.3f * volume);
                if (localPlayer.health > 100) {
                    localPlayer.health = 100;
                }
            }

            handleInput(relativeMousePosition, mouseClick, distanceToMouse);
            localPlayer.movedThisFrame = movementThisFrame;

            //Anything serverside eg. enemy movement
            if (isServer) {
                spawnEnemies();
                //Enemy movement
                for (Zombie enemy : enemies) {
                    if (enemy.health <= 0) {
                        continue;
                    }
                    enemy.tickDownSecondsDamaged(delta);

                    Character target = localPlayer;
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
                               (enemy.position.x > target.position.x - (target.position.width / 2))
                            && (enemy.position.x < target.position.x + (target.position.width / 2))
                            && (enemy.position.y > target.position.y - (target.position.height / 2))
                            && (enemy.position.y < target.position.y + (target.position.height / 2))) {
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
            } else {
                for (Zombie enemy : enemies) {
                    enemy.tickDownSecondsDamaged(delta);
                }
            }
            //local player fires weapon at enemies
            if (mouseClick.x != -1 && mouseClick.y != -1 && LeftMouseThisFrame) {
                aMusicLibrary.gunSound.play(volume);
                localPlayer.shootingTime = Player.torso.getAnimationDuration();
                for (Zombie enemy : enemies) {
                    if (enemy.health <= 0) {
                        continue;
                    }
                    Rectangle eRect = new Rectangle((int) (enemy.position.x - (enemy.position.width/2)), (int) (enemy.position.y - (enemy.position.height/2)), (int) enemy.position.width, (int) enemy.position.height);
                    Vector2 pVec = new Vector2();
                    localPlayer.position.getPosition(pVec);
                    Vector2 mVec = new Vector2(relativeMousePosition);
                    mVec.nor().scl(windowSize.x * windowSize.y).add(pVec);
                    if (eRect.intersectsLine(pVec.x, pVec.y, mVec.x, mVec.y)) {
                       Character.attack(enemy,60);
                    }
                    d1.set(pVec.x, pVec.y);
                    d2.set(mVec.x, mVec.y);
                }
            }
            //Enemies attacking
            for (Zombie enemy : enemies) {
                if (enemy.health <= 0) {
                    continue;
                }
                //attack localPlayer
                if (enemy.position.getPosition(new Vector2()).dst(localPlayer.position.getPosition(new Vector2())) < 40) {
                    localPlayer.health -= 10 * delta;
                    continue;
                }
                //attack jeep
                if ((enemy.position.x > jeep.position.x - (jeep.position.width / 2))
                    && (enemy.position.x < jeep.position.x + (jeep.position.width / 2))
                    && (enemy.position.y > jeep.position.y - (jeep.position.height / 2))
                    && (enemy.position.y < jeep.position.y + (jeep.position.height / 2))) {
                    jeep.health -= 1 * delta;
                }
            }
            if (localPlayer.health <= 0) {
                localPlayer.respawn();
            }
            if (jeep.health <= 0) {
                gamePaused = true;
                gameOver = true;
            }
        }
        if (isMultiplayer) {
            if (isServer) {
                Network.serverToClients();
            } else {
                Network.clientToServer();
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

        if (gameStarted) {
            //Draw enemies
            for (Zombie enemy : enemies) {
                if (enemy.health > 0) {
                    continue;
                }
                enemy.draw(batch, delta);
            }
            jeep.draw(batch);
            turret.draw(batch, delta);
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
        }

        gui.draw(batch);

        if (gameOver) {
            int kills = 0;
            for (Zombie enemy : enemies) {
                if (enemy.health <= 0) {
                    kills++;
                }
            }
            GlyphLayout glyph = new GlyphLayout(Gui.bitmapFont, "Game Over!\nThe Jeep Was Destroyed\nYou have killed " + kills + " zombies!");
            Gui.bitmapFont.setColor(1,1,1,1);
            Gui.bitmapFont.draw(batch, glyph, (windowSize.x/2) - (glyph.width / 2), (windowSize.y/2) - (glyph.height / 2));
        }

        if (gamePaused && gameStarted) {
            GlyphLayout glyph = new GlyphLayout(Gui.bitmapFont, "Game Paused! Press 'P' to unpause");
            Gui.bitmapFont.setColor(1, 1, 1, 1);
            Gui.bitmapFont.draw(batch,glyph,(windowSize.x/2) - (glyph.width / 2), (windowSize.y/2) - (glyph.height / 2));
        }

        batch.end();

        if(mouseClick.x != -1 && mouseClick.y != -1) {
            ShapeRenderer shape = new ShapeRenderer();
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.line(d1.x,d1.y,d2.x,d2.y);
            shape.end();
        }

        LeftMouseThisFrame = false;
        mouseClick.set(-1, -1);

        log.log();
    }

    public void resize(int width, int height) {
    }

    public void pause() {
    }

    public void resume() {
    }

    public void dispose() {
        Network.shutdownServer();
    }
}