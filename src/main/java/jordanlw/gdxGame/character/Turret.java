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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import jordanlw.gdxGame.Game;

/**
 * Created by jordan on 5/11/15.
 */
public class Turret extends Character {
    static private Texture turretBase = new Texture(Gdx.files.internal("images/turretBase.png"));
    static private Animation turretCannon = new Animation(.1f,new TextureRegion(new Texture(Gdx.files.internal("images/turretCannon.png"))).split(93,148)[0]);
    private float stateTime = 0;

    public Turret() {
        position.setPosition((Game.windowSize.x / 2) - (turretBase.getWidth() / 2), Game.windowSize.y * 0.3f);
        position.setSize(turretBase.getWidth(), turretBase.getHeight());

        turretCannon.setPlayMode(Animation.PlayMode.LOOP);
    }

    public void draw(SpriteBatch batch, float delta) {
        float dist = 999999;
        Character target = this;
        for (Zombie enemy : Game.enemies) {
            if (enemy.health <= 0) {
                continue;
            }
            if (Character.distance(enemy, this) < dist) {
                dist = Character.distance(enemy, this);
                target = enemy;
            }
        }
        Vector2 tmp = target.position.getPosition(new Vector2());
        tmp.sub(position.getPosition(new Vector2()));
        tmp.sub(position.width / 2, position.height / 2);
        float angle = tmp.angle();
        angle -= 90;

        batch.draw(turretBase, position.x, position.y);
        batch.draw(turretCannon.getKeyFrame(stateTime), position.x, position.y, position.width / 2, position.height / 2, position.width, position.height, 1, 1, angle);
        stateTime += delta;

        target.health -= 30 * delta;
    }
}
