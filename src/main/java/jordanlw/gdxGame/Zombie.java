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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.UUID;

/**
 * Created by jordan on 12/15/14.
 */
public class Zombie extends Character {
    static final Rectangle animRect = new Rectangle();
    static final Rectangle deadRect = new Rectangle();
    static float zombeGroanSoundTimer;
    static Animation anim = null;
    static Animation dead;
    static int difficulty;
    TargetTypes target = TargetTypes.player;
    float deadTimer;
    float walkTimer = (float)Math.random();
    float swarmAngle;
    int walkingSpeed;

    public Zombie() {
        respawn();
        position.setSize(anim.getKeyFrame(0).getRegionWidth(), anim.getKeyFrame(0).getRegionHeight());
    }

    static public void init() {
        if(anim == null) {
            //Load image of enemy & creates animation object for them
            TextureRegion enemyCropped = new TextureRegion(new Texture(Gdx.files.internal("images/zombies.png")));
            anim = new Animation(0.20f,enemyCropped.split(41,41)[0]);
            anim.setPlayMode(Animation.PlayMode.LOOP);

            animRect.width = anim.getKeyFrame(0).getRegionWidth();
            animRect.height = anim.getKeyFrame(0).getRegionHeight();

            TextureRegion deadCropped = new TextureRegion(new Texture(Gdx.files.internal("images/zombies-dead.png")));
            dead = new Animation(1.5f,deadCropped.split(36,87)[0]);
            dead.setPlayMode(Animation.PlayMode.NORMAL);

            deadRect.width = dead.getKeyFrame(0).getRegionWidth();
            deadRect.height = dead.getKeyFrame(0).getRegionHeight();
        }
    }

    public void draw(SpriteBatch batch, float delta) {
        walkTimer += delta;
        if (secondsDamaged > 0f && health > 0) {
            batch.setColor(Color.RED);
        } else {
            batch.setColor(Color.WHITE);
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

    public void respawn() {
        health = 100 + (5 * difficulty);
        position.setPosition(-50, (int)(Math.random() * Game.windowSize.y));
        walkingSpeed = getNewWalkingSpeed();
        secondsDamaged = 0;
        swarmAngle = (float)(-100 * Math.random() + 50);
        id = UUID.randomUUID();
        if (Math.random() * 5 < 1) {
            target = TargetTypes.jeep;
        }
    }

    public Integer getNewWalkingSpeed() {
        return 35 + (int)((Math.random() * 20) - 10);
    }

    public enum TargetTypes {
        player,jeep
    }
}
