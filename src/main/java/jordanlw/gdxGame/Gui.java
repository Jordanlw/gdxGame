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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.ArrayList;

/**
 * Created by jordan on 2/15/15.
 */
class Gui {
    static BitmapFont bitmapFont;
    public ArrayList<GuiButton> buttons = new ArrayList<>();

    public Gui() {
        FileHandle font = Gdx.files.internal("font/Roboto-Regular.ttf");
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(font);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 35;
        bitmapFont = generator.generateFont(parameter);
        generator.dispose();

        buttons.add(new GuiButtonSinglePlayer());
        buttons.add(new GuiButtonMultiPlayer());
    }

    public void update() {
        for (GuiButton button : buttons) {
            if (button.visible && button.rect.contains(Gdx.input.getX(),Game.camera.viewportHeight - Gdx.input.getY())) {
                if(Game.LeftMouseThisFrame) {
                    button.clicked();
                }
            }
        }
    }

    public void draw(Batch batch) {
        for (GuiButton button : buttons) {
            if (!button.visible) {
                continue;
            }
            if (button.rect.contains(Gdx.input.getX(), Game.camera.viewportHeight - Gdx.input.getY())) {
                bitmapFont.setColor(0.75f, 0.75f, 0.75f, 1);
            } else {
                bitmapFont.setColor(1, 1, 1, 1);
            }
            bitmapFont.draw(batch, button.text, button.rect.x, button.rect.y + button.glyph.height);
        }
    }

    public void hideAll() {
        for (GuiButton button : buttons) {
            button.visible = false;
        }
    }
}
