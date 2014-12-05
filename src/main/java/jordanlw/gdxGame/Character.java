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

import com.badlogic.gdx.math.Vector2;

/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 1/1/13
 * Time: 1:06 AM
 * To change this template use File | Settings | File Templates.
 */
class Character {
    final Vector2 position;
    float rotation;
    float secondsDamaged;
    float health = 100;
    Integer walkingSpeed;
    double circleChangeTimer;
    boolean circleDirection;
    boolean isServer;
    boolean connected = false;

    public Character() {
        this.position = new Vector2();
        this.health = 100;
        this.position.set(Math.random() < 0.5f ? Game.windowSize.x + 50 : -50, Math.random() < 0.5f ? Game.windowSize.y + 50 : -50);
        this.walkingSpeed = getNewWalkingSpeed();
        this.secondsDamaged = 0;
        this.circleDirection = Math.random() < 0.5f;
        this.circleChangeTimer = 7.5f + (Math.random() * 2.5f);
    }

    public static Vector2 getEnemyMid() {
        return new Vector2(Game.enemyAnim.getKeyFrames()[0].getRegionWidth() / 2, Game.enemyAnim.getKeyFrames()[0].getRegionHeight() / 2);
    }

    public Integer getNewWalkingSpeed() {
        return (int) (50 * Math.random() + 50);
    }
}