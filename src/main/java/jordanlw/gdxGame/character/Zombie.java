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

package jordanlw.gdxGame.character;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import jordanlw.gdxGame.Game;

import java.util.UUID;

/**
 * Created by jordan on 12/15/14.
 */
public class Zombie extends Character {
    private static final Rectangle animRect = new Rectangle();
    private static final Rectangle deadRect = new Rectangle();
    static public float groanSoundTimer = 0;
    private static Animation anim = new Animation(0.2f,new TextureRegion(new Texture(Gdx.files.internal("images/zombies.png"))).split(41,41)[0]);
    private static Animation dead = new Animation(1.5f,new TextureRegion(new Texture(Gdx.files.internal("images/zombies-dead.png"))).split(36,87)[0]);
    public TargetTypes target = TargetTypes.player;
    public float swarmAngle;
    public int walkingSpeed;
    private ZombieTypes type = ZombieTypes.normal;
    private float deadTimer;
    private float walkTimer = (float)Math.random();

    public Zombie() {
        respawn();
        position.setSize(anim.getKeyFrame(0).getRegionWidth(), anim.getKeyFrame(0).getRegionHeight());

        anim.setPlayMode(Animation.PlayMode.LOOP);
        animRect.width = anim.getKeyFrame(0).getRegionWidth();
        animRect.height = anim.getKeyFrame(0).getRegionHeight();

        dead.setPlayMode(Animation.PlayMode.NORMAL);
        deadRect.width = dead.getKeyFrame(0).getRegionWidth();
        deadRect.height = dead.getKeyFrame(0).getRegionHeight();
    }

    public void draw(SpriteBatch batch, float delta) {
        walkTimer += delta;

        switch (type) {
            case normal:
                batch.setColor(Color.WHITE);
                break;
            case infected:
                batch.setColor(Color.GREEN);
                break;
            case fast:
                batch.setColor(Color.YELLOW);
        }
        if (secondsDamaged > 0f && isAlive()) {
            batch.setColor(Color.RED);
        }
        if (this.health <= 0) {
            deadTimer += delta;
            batch.draw(
                    dead.getKeyFrame(deadTimer),
                    position.x - deadRect.width / 2,
                    position.y - deadRect.height / 2,
                    deadRect.width / 2,
                    deadRect.height / 2,
                    dead.getKeyFrame(deadTimer).getRegionWidth(),
                    dead.getKeyFrame(deadTimer).getRegionHeight(),
                    1, 1,rotation + 90);
            return;
        }
        batch.draw(
                anim.getKeyFrame(walkTimer),
                position.x - animRect.width / 2,
                position.y - animRect.height / 2,
                animRect.width / 2,
                animRect.height / 2,
                anim.getKeyFrame(walkTimer).getRegionWidth(),
                anim.getKeyFrame(walkTimer).getRegionHeight(),
                1,1,rotation + 90);
    }

    private void respawn() {
        if (Math.random() * 30 < 1) {
            type = ZombieTypes.infected;
        }
        else if (Math.random() * 15 < 1) {
            type = ZombieTypes.fast;
        }

        switch (type) {
            case normal:
                health = 100;
                walkingSpeed = 35 + (int) ((Math.random() * 20) - 10);
                break;
            case infected:
                health = 5000;
                walkingSpeed = 5 + (int) ((Math.random() * 5) - 2);
                break;
            case fast:
                health = 50;
                walkingSpeed = 200 + (int)((Math.random() * 100) - 50);
        }

        position.setPosition(-50, (int) (Math.random() * Game.windowSize.y));
        secondsDamaged = 0;
        swarmAngle = (float) (-100 * Math.random() + 50);
        id = UUID.randomUUID();
        if (Math.random() * 5 < 1) {
            target = TargetTypes.jeep;
        }
    }

    public enum TargetTypes {
        player,jeep
    }

    public enum ZombieTypes {
        normal,infected,fast
    }
}
