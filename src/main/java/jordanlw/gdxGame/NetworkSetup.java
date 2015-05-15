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
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import jordanlw.gdxGame.character.Character;
import jordanlw.gdxGame.character.Player;
import jordanlw.gdxGame.character.Zombie;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Created by jordan on 1/12/15.
 */
class NetworkSetup {

    static public void joinServer(InetAddress address) {
        Gdx.graphics.setTitle("Client");
        Game.clientNet = new Client();
        Kryo kryo = Game.clientNet.getKryo();
        //Log.set(Log.LEVEL_DEBUG);
        registerClassesForNetwork(kryo);
        Game.isServer = false;
        Game.clientNet.start();
        try {
            Game.clientNet.connect(1500, address, 12345, 12345);
        } catch (IOException e) {
            e.printStackTrace();
            Gdx.app.exit();
        }
        Game.clientNet.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof Packet) {
                    Packet packet = (Packet) object;
                    boolean isFound = false;
                    if (packet.type == Character.Types.player) {
                        for (Player player : Game.players) {
                            if (UUID.fromString(packet.id).compareTo(player.id) == 0) {
                                player.rotation = packet.rotation;
                                player.position.x = packet.x;
                                player.position.y = packet.y;
                                player.movedThisFrame = packet.movedThisFrame;
                                isFound = true;
                            }
                        }
                        if (!isFound) {
                            System.out.println("Player ID not found: " + packet.id);
                            Player player = new Player(false);
                            player.movedThisFrame = packet.movedThisFrame;
                            player.position.x = packet.x;
                            player.position.y = packet.y;
                            player.rotation = packet.rotation;
                            player.id = UUID.fromString(packet.id);
                            Game.players.add(player);
                        }
                    }
                    isFound = false;
                    if (packet.type == Character.Types.enemy) {
                        for (Zombie enemy : Game.enemies) {
                            if (UUID.fromString(packet.id).compareTo(enemy.id) == 0) {
                                enemy.rotation = packet.rotation;
                                enemy.position.x = packet.x;
                                enemy.position.y = packet.y;
                                if (packet.health < enemy.health) {
                                    enemy.secondsDamaged = 1;
                                    enemy.health = packet.health;
                                }
                                isFound = true;
                            }
                        }
                        if (!isFound) {
                            System.out.println("Character ID not found: " + packet.id);
                            Zombie enemy = new Zombie();
                            enemy.rotation = packet.rotation;
                            enemy.position.x = packet.x;
                            enemy.position.y = packet.y;
                            enemy.id = UUID.fromString(packet.id);
                            Game.enemies.add(enemy);
                        }
                    }
                }
            }
        });
    }

    public static void startServer() {
        Gdx.graphics.setTitle("Server");
        Game.serverNet = new Server();
        Kryo kryo = Game.serverNet.getKryo();
        //Log.set(Log.LEVEL_DEBUG);
        registerClassesForNetwork(kryo);
        Game.isServer = true;
        Game.serverNet.start();
        try {
            Game.serverNet.bind(12345, 12345);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            Gdx.app.exit();
        }
        Game.serverNet.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof Packet) {
                    Packet packet = (Packet) object;
                    boolean isFound = false;
                    if (packet.type == Character.Types.player) {
                        for (Player player : Game.players) {
                            if (UUID.fromString(packet.id).compareTo(player.id) == 0) {
                                player.health = packet.health;
                                player.rotation = packet.rotation;
                                player.position.x = packet.x;
                                player.position.y = packet.y;
                                player.movedThisFrame = packet.movedThisFrame;
                                isFound = true;
                            }
                        }
                        if (!isFound) {
                            System.out.println("Player ID not found: " + packet.id);
                            Player player = new Player(false);
                            player.health = packet.health;
                            player.rotation = packet.rotation;
                            player.position.x = packet.x;
                            player.position.y = packet.y;
                            player.movedThisFrame = packet.movedThisFrame;
                            player.id = UUID.fromString(packet.id);
                            Game.players.add(player);
                        }
                    }
                    else if (packet.type == Character.Types.enemy) {
                        for (Zombie enemy : Game.enemies) {
                            if (UUID.fromString(packet.id).compareTo(enemy.id) == 0) {
                                enemy.secondsDamaged = 2;
                                enemy.health = packet.health;
                            }
                        }
                    }
                }
            }
        });
    }

    private static void registerClassesForNetwork(Kryo kryo) {
        kryo.register(Packet.class);
        kryo.register(Character.Types.class);
    }
}
