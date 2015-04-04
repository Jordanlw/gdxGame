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

/**
 * Created by jordan on 12/15/14.
 */
public class Zombie extends Character {
    static final Rectangle animRect = new Rectangle();
    static float zombeGroanSoundTimer = 0;
    static Animation anim = null;
    float swarmAngle;
    int walkingSpeed;

    public Zombie() {
        respawn(0);
        position.setSize(anim.getKeyFrame(0).getRegionWidth(),anim.getKeyFrame(0).getRegionHeight());
    }

    static public void init() {
        if(anim == null) {
            //Load image of enemy & creates animation object for them
            TextureRegion enemyCropped = new TextureRegion(new Texture(Gdx.files.internal("images/zombies.png")));
            anim = new Animation(0.20f,enemyCropped.split(41,41)[0]);
            anim.setPlayMode(Animation.PlayMode.LOOP);

            animRect.width = anim.getKeyFrame(0).getRegionWidth();
            animRect.height = anim.getKeyFrame(0).getRegionHeight();
        }
    }

    public void draw(SpriteBatch batch, float stateTime) {
        stateTime += Math.abs(this.id.getLeastSignificantBits()) % 0.9;
        if (this.secondsDamaged > 0f) {
            batch.setColor(Color.RED);
        } else {
            batch.setColor(Color.WHITE);
        }
        if (this.health <= 0) {
            return;
        }
        batch.draw(
                anim.getKeyFrame(stateTime),
                this.position.x,
                this.position.y,
                anim.getKeyFrame(stateTime).getRegionWidth() / 2,
                anim.getKeyFrame(stateTime).getRegionHeight() / 2,
                anim.getKeyFrame(stateTime).getRegionWidth(),
                anim.getKeyFrame(stateTime).getRegionHeight(),
                1,1,this.rotation + 90);
        }

    public void respawn(int wave) {
        this.health = 100 + (20 * wave);
        this.position.setPosition(Math.random() < 0.5f ? Game.windowSize.x + 20 : -20, Math.random() < 0.5f ? Game.windowSize.y + 20 : -20);
        this.walkingSpeed = getNewWalkingSpeed();
        this.secondsDamaged = 0;
        this.swarmAngle = (float)(-100 * Math.random() + 50);
    }

    public Integer getNewWalkingSpeed() {
        return (int) (50 * Math.random() + 50);
    }
}
