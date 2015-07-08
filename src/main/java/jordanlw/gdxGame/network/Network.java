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

package jordanlw.gdxGame.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;
import jordanlw.gdxGame.Game;
import jordanlw.gdxGame.character.Character;
import jordanlw.gdxGame.character.Player;
import jordanlw.gdxGame.character.Zombie;

import java.io.IOException;

/**
 * Created by jordan on 5/18/15.
 */
public class Network {
    static Client client = new Client();
    static Server server = new Server();

    public static void clientToServer() {
        Packet packet = new Packet();
        for (Zombie enemy : Game.enemies) {
            packet.health = enemy.health;
            packet.id = enemy.id.toString();
            packet.type = Character.Types.enemy;
            client.sendUDP(packet);
        }
        packet.health = Game.localPlayer.health;
        packet.id = Game.localPlayer.id.toString();
        packet.rotation = Game.localPlayer.rotation;
        packet.movedThisFrame = Game.localPlayer.movedThisFrame;
        packet.x = Game.localPlayer.position.x;
        packet.y = Game.localPlayer.position.y;
        packet.type = Character.Types.player;
        client.sendUDP(packet);
    }

    public static void serverToClients(){
        Packet packet = new Packet();
        for (Zombie enemy : Game.enemies) {
            packet.health = enemy.health;
            packet.rotation = enemy.rotation;
            packet.x = enemy.position.x;
            packet.y = enemy.position.y;
            packet.id = enemy.id.toString();
            packet.type = Character.Types.enemy;
            server.sendToAllUDP(packet);
        }
        for (Player player : Game.players) {
            packet.rotation = player.rotation;
            packet.movedThisFrame = player.movedThisFrame;
            packet.x = player.position.x;
            packet.y = player.position.y;
            packet.id = player.id.toString();
            packet.type = Character.Types.player;
            server.sendToAllUDP(packet);
        }
    }

    public static void shutdownServer() {
        if(server != null) {
            server.stop();
            try {
                server.dispose();
            } catch (IOException e) {
                e.printStackTrace();
                Gdx.app.exit();
            }
        }
    }

}
