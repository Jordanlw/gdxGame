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

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 1/1/13
 * Time: 1:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class Character {
    public final Rectangle position = new Rectangle();
    public float secondsDamaged;
    public float rotation;
    public float health = 100;
    public UUID id;

    Character() {
        this.id = UUID.randomUUID();
    }

    public static float distance(Character a, Character b) {
        return a.position.getPosition(new Vector2()).dst(b.position.getPosition(new Vector2()));
    }

    public static void attack(Character target, float damage) {
        target.health -= damage;
        target.secondsDamaged = 1;
    }

    public void tickDownSecondsDamaged(float delta) {
        secondsDamaged -= delta;
    }

    boolean isAlive() {
        return health > 0;
    }


    public enum Types {
        player,enemy
    }
}