import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 2/25/13
 * Time: 7:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class Gold {
    static int currentDeadEnemy = -1;
    static List<GoldOnFloor>gold = new ArrayList<GoldOnFloor>();

    public static void saveEnemy(int enemy) {
        currentDeadEnemy = enemy;
    }
    public static void spawnLootFromEnemies(int wave,SpriteBatch batch) {
        GoldTypes type;
        if(currentDeadEnemy == -1) {
            return;
        }
        double randomResult = (Math.random() - 0.5) * 2;
        if(wave > randomResult + 3) {
            type = GoldTypes.BAR;
        }
        else if(wave > randomResult + 5) {
            type = GoldTypes.PILE;
        }
        else if(wave > randomResult + 8) {
            type = GoldTypes.SKULL;
        }
        else {
            type = GoldTypes.COIN;
        }
        Vector2 tmp = Character.getEnemyMid();
        gold.add(new GoldOnFloor(new Vector2(Game.enemies.get(currentDeadEnemy).position.x + tmp.x, Game.enemies.get(currentDeadEnemy).position.y + tmp.y),type));
        for(GoldOnFloor floor : gold) {
            batch.draw(Game.goldSheet[floor.type.ordinal()],floor.position.x,floor.position.y);
        }
        currentDeadEnemy = -1;
    }
}
