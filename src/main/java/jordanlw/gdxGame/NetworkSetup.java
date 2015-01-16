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

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jordan on 1/12/15.
 */
public class NetworkSetup {

    static public void setupNetwork() {
        Game.serverNet = new Server();
        Game.clientNet = new Client();
        if (Game.serverAddress != "") {
            Kryo kryo = Game.clientNet.getKryo();
            registerClassesForNetwork(kryo);
            Game.isServer = false;
            Game.player.isServer = false;
            Game.clientNet.start();
            Game.otherPlayer.health = 100;
            try {
                Game.clientNet.connect(1500, Game.serverAddress, 12345);
            } catch (IOException e) {
                displayErrorText("Can't connect to player.","Multiplayer Network Error");
                e.printStackTrace();
                //System.exit(1);
            }
            Game.clientNet.addListener(new Listener() {
                public void received(Connection connection, Object object) {
                    if (object instanceof List) {
                        cloneArrayList(Game.enemies, (List<Zombie>) object);
                    } else if (object instanceof Player) {
                        if (((Player) object).isServer) {
                            Game.otherPlayer = (Player) object;
                        } else if (!((Player) object).isServer) {
                            Game.player.health = ((Character) object).health;
                            Game.player.secondsDamaged = ((Character) object).secondsDamaged;
                        }
                    }
                }
            });
        } else {
            Kryo kryo = Game.serverNet.getKryo();
            registerClassesForNetwork(kryo);
            Game.isServer = true;
            Game.player.isServer = true;
            Game.serverNet.start();
            try {
                Game.serverNet.bind(12345);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                //System.exit(1);
            }
            Game.serverNet.addListener(new Listener() {
                public void received(Connection connection, Object object) {
                    Game.otherPlayer.connected = true;
                    if (object instanceof List) {
                        for (int i = 0; i < Game.enemies.size(); i++) {
                            //noinspection unchecked
                            Game.enemies.get(i).health = ((List<Zombie>) object).get(i).health;
                        }
                    } else {
                        Game.otherPlayer = (Player)object;
                    }
                }
            });
        }
    }

    static public void getTextInput (String title) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setAlwaysOnTop(true);
            NetworkInputListener.input(JOptionPane.showInputDialog(frame, title));
            frame.dispose();
        });
    }

    static void displayErrorText(String message, String title) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setAlwaysOnTop(true);
            JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
            frame.dispose();
        });
    }
    static <T> void cloneArrayList(List<T> a, List<T> b) {
        for (int i = 0; i < a.size(); i++) {
            a.set(i, b.get(i));
        }
    }

    private static void registerClassesForNetwork(Kryo kryo) {
        kryo.register(Zombie.class);
        kryo.register(ArrayList.class);
        kryo.register(CharacterDirections.class);
        kryo.register(Character.class);
        kryo.register(Integer.class);
        kryo.register(Vector2.class);
    }
}
