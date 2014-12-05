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

/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 1/4/13
 * Time: 4:55 PM
 * To change this template use File | Settings | File Templates.
 */
class Potions extends Character {
    static final float secsTillDisappear = 15;
    static final float healthGiven = 30;
    static final Texture[] textures = new Texture[PotionsTypes.amount()];
    float time;

    public static void initializeTextures() {
        //textures[PotionsTypes.BLUE.potion] = new Texture(Gdx.files.internal("blue.png"));
        //textures[PotionsTypes.EMPTY.potion] = new Texture(Gdx.files.internal("empty.png"));
        //textures[PotionsTypes.GREEN.potion] = new Texture(Gdx.files.internal("green.png"));
        //textures[PotionsTypes.PURPLE.potion] = new Texture(Gdx.files.internal("purple.png"));
        textures[PotionsTypes.RED.potion] = new Texture(Gdx.files.internal("images/potion-red.png"));
        //textures[PotionsTypes.YELLOW.potion] = new Texture(Gdx.files.internal("yellow.png"));
    }

}
