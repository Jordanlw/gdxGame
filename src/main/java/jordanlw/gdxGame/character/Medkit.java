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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 1/4/13
 * Time: 4:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class Medkit extends Character {
    public static final float SECS_TILL_DISAPPEAR = 5;
    public static final float healthGiven = 30;
    static final TextureRegion texture = new TextureRegion(new Texture(Gdx.files.internal("images/medkit.png")));
    public float time;

    public Medkit() {
        position.setSize(texture.getRegionWidth(),texture.getRegionHeight());
    }

    public void draw(SpriteBatch batch) {
        if (this.time > Medkit.SECS_TILL_DISAPPEAR) {
            batch.draw(
                    texture,
                    position.x - (texture.getRegionWidth() / 2),
                    position.y - (texture.getRegionHeight() / 2),
                    texture.getRegionWidth(),
                    texture.getRegionHeight());
        }
    }

}
