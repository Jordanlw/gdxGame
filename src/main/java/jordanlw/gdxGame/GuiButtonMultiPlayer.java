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

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by jordan on 2/21/15.
 */
public class GuiButtonMultiPlayer extends GuiButton {

    public GuiButtonMultiPlayer() {
        super("MultiPlayer");
        super.rect.setPosition(Game.windowSize.x*0.70f, Game.windowSize.y*0.25f);
        BitmapFont.TextBounds tmp = super.bitmapFont.getBounds(super.text);
        super.rect.setSize(tmp.width,tmp.height);
        super.visible = true;
    }

    @Override
    public void clicked() {
        Game.clientNet = new Client();
        InetAddress server = Game.clientNet.discoverHost(1234, 1500);
        if (server != null) {
            try {
                Game.clientNet.start();
                Game.clientNet.connect(1500, server, 1234, 1234);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Game.serverNet = new Server();
            try {
                Game.serverNet.start();
                Game.serverNet.bind(1234, 1234);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Game.gui.hideAll();
    }
}
