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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by jordan on 12/5/14.
 */
public class Player extends Character {
    boolean isSelf = false;
    boolean movedThisFrame = false;
    float shootingTime;

    public Player(boolean isSelf) {
        this.isSelf = isSelf;
        position.setSize(Game.legsAnim.getKeyFrame(0).getRegionWidth(), Game.legsAnim.getKeyFrame(0).getRegionHeight());
        respawn();
    }

    public void respawn() {
        position.setPosition(Game.windowSize.x / 2, Game.windowSize.y / 2);
        health = 100;
        secondsDamaged = 0;
    }


    public void draw(SpriteBatch batch,float totalTime, float delta) {
        if(isSelf) {
            batch.setColor(Color.WHITE);
        }
        else {
            batch.setColor(Color.GRAY);
        }
        float keyFrame = 0;
        if(movedThisFrame) {
            keyFrame = totalTime;
        }
        batch.draw(
                Game.legsAnim.getKeyFrame(keyFrame),
                position.x - 16,
                position.y - 53,
                16,53,
                Game.legsAnim.getKeyFrame(keyFrame).getRegionWidth(),
                Game.legsAnim.getKeyFrame(keyFrame).getRegionHeight(),
                1, 1,
                this.rotation + 90);
        keyFrame = 0;
        if(shootingTime > 0) {
            shootingTime -= delta;
            keyFrame = totalTime;
        }
        batch.draw(
                Game.torsoAnim.getKeyFrame(keyFrame),
                position.x - 16,
                position.y - 53,
                16,53,
                Game.torsoAnim.getKeyFrame(keyFrame).getRegionWidth(),
                Game.torsoAnim.getKeyFrame(keyFrame).getRegionHeight(),
                1,1,
                this.rotation + 90);
    }
}
