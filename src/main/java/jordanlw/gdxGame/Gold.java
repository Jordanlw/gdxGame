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

/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 2/25/13
 * Time: 7:57 PM
 * To change this template use File | Settings | File Templates.
 */
/*class Gold {
    private final List<GoldOnFloor> gold = new ArrayList<>();

    public void saveEnemy(int wave, int enemy) {
        GoldTypes type;
        double randomResult = (Math.random() - 0.5) * 2;
        if (wave > randomResult + 3) {
            type = GoldTypes.BAR;
        } else if (wave > randomResult + 5) {
            type = GoldTypes.PILE;
        } else if (wave > randomResult + 8) {
            type = GoldTypes.SKULL;
        } else {
            type = GoldTypes.COIN;
        }
        Vector2 tmp = new Vector2(Game.goldSheet[0].getRegionWidth()/2,Game.goldSheet[0].getRegionHeight()/2);
        //gold.add(new GoldOnFloor(new Vector2(Game.enemies.get(enemy).position.x - tmp.x,Game.enemies.get(enemy).position.y - tmp.y),type));
    }

    public void draw(SpriteBatch batch) {
        for (GoldOnFloor floor : gold) {
            batch.draw(Game.goldSheet[floor.type.type], floor.position.x, floor.position.y);
        }
    }
}*/
