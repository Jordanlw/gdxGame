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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by jordan on 5/11/15.
 */
public class Turret extends Character {
    static private Texture turretBase = new Texture(Gdx.files.internal("images/turretBase.png"));
    static private Texture turretCannon = new Texture(Gdx.files.internal("images/turretCannon.png"));

    public Turret() {
        position.setPosition((Game.windowSize.x / 2) - (turretBase.getWidth() / 2), Game.windowSize.y * 0.3f);
    }

    public void draw(SpriteBatch batch) {
        float dist = 999999;
        Character target = this;
        for (Zombie enemy : Game.enemies) {
            if (Character.distance(enemy, this) < dist) {
                dist = Character.distance(enemy, this);
                target = enemy;
            }
        }
        Vector2 tmp = target.position.getPosition(new Vector2());
        tmp.sub(position.getPosition(new Vector2()));
        tmp.sub(turretCannon.getWidth() / 2, turretCannon.getHeight() / 2);
        float angle = tmp.angle();
        angle -= 90;

        batch.draw(turretBase, position.x, position.y);
        batch.draw(turretCannon, position.x, position.y, turretCannon.getWidth() / 2, turretCannon.getHeight() / 2, turretCannon.getWidth(), turretCannon.getHeight(), 1, 1, angle, 0, 0, turretCannon.getWidth(), turretCannon.getHeight(), false, false);
    }
}
