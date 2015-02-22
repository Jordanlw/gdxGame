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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import javax.swing.*;
import java.io.IOException;

/**
 * Created by jordan on 1/12/15.
 */
public class NetworkSetup {

    static public void joinServer() {
        Game.clientNet = new Client();
        Kryo kryo = Game.clientNet.getKryo();
        //Log.set(Log.LEVEL_DEBUG);
        registerClassesForNetwork(kryo);
        Game.isServer = false;
        Game.clientNet.start();
        try {
            Game.clientNet.connect(1500, Game.serverAddress, 12345,12345);
        } catch (IOException e) {
            displayErrorText("Can't connect to player.","Multiplayer Network Error");
            e.printStackTrace();
            System.exit(1);
        }
        Game.clientNet.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof Packet) {
                    Packet packet = (Packet) object;
                    boolean isFound = false;
                    for(Zombie enemy: Game.enemies) {
                        if(enemy.id == packet.id) {
                            enemy.rotation = packet.rotation;
                            enemy.position.x = packet.x;
                            enemy.position.y = packet.y;
                            isFound = true;
                        }
                    }
                    if(!isFound) {
                        System.out.println("Character ID not found: " + packet.id);
                        Zombie enemy = new Zombie();
                        enemy.rotation = packet.rotation;
                        enemy.position.x = packet.x;
                        enemy.position.y = packet.y;
                        enemy.id = packet.id;
                        Game.enemies.add(enemy);
                    }
                }
            }
        });
    }

    public static void startServer() {
        Game.serverNet = new Server();
        Kryo kryo = Game.serverNet.getKryo();
        //Log.set(Log.LEVEL_DEBUG);
        registerClassesForNetwork(kryo);
        Game.isServer = true;
        Game.serverNet.start();
        try {
            Game.serverNet.bind(12345,12345);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.exit(1);
        }
        Game.serverNet.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof Packet) {
                    Packet packet = (Packet) object;
                    boolean isFound = false;
                    for (Player player : Game.players) {
                        if (player.id == packet.id) {
                            player.rotation = packet.rotation;
                            player.position.x = packet.x;
                            player.position.y = packet.y;
                            isFound = true;
                        }
                    }
                    if (!isFound) {
                        System.out.println("Player ID not found: " + packet.id);
                    }
                }
            }
        });
    }

    static public void getTextInput (String title) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setAlwaysOnTop(true);
            NetworkInputListener.textInput(JOptionPane.showInputDialog(frame, title));
            frame.dispose();
        });
    }

    static public void getAnswer(String title) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setAlwaysOnTop(true);
            NetworkInputListener.answerInput(JOptionPane.showConfirmDialog(frame, title));
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

    private static void registerClassesForNetwork(Kryo kryo) {
        kryo.register(float[].class);
        kryo.register(Packet.class);
    }
}
