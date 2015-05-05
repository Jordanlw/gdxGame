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

import com.esotericsoftware.kryonet.Client;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by jordan on 2/21/15.
 */
public class GuiButtonMultiPlayer extends GuiButton {

    public GuiButtonMultiPlayer() {
        super("MultiPlayer");
        rect.setCenter((Game.windowSize.x / 2) - (glyph.width / 2), 100);
        rect.setSize(glyph.width, glyph.height);
        visible = true;
    }

    @Override
    public void clicked() {
        Game.clientNet = new Client();
        InetAddress server = Game.clientNet.discoverHost(12345, 1500);
        if (server != null) {
            NetworkSetup.joinServer(server);
        }
        else {
            NetworkSetup.startServer();
            Game.clientNet.stop();
            try {
                Game.clientNet.dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Game.gui.hideAll();
        Game.unPauseGame();
        Game.players.add(new Player(true));
    }
}
