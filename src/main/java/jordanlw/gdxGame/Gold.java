package jordanlw.gdxGame;

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
class Gold {
    private static final List<GoldOnFloor> gold = new ArrayList<>();

    public static void saveEnemy(int wave, int enemy) {
        GoldTypes type;
        double randomResult = (Math.random() - 0.5) * 2;
        if (wave > randomResult + 3) {
            type = GoldTypes.BAR;
        } else if (wave > randomResult + 5) {
            type = GoldTypes.PILE;
        } else if (wave > randomResult + 8) {
            type = GoldTypes.SKULL;
        } else {
            type = GoldTypes.COIN;
        }
        Vector2 tmp = Character.getEnemyMid();
        gold.add(new GoldOnFloor(new Vector2(Game.enemies.get(enemy).position.x + tmp.x, Game.enemies.get(enemy).position.y + tmp.y), type));
    }

    public static void spawnLootFromEnemies(SpriteBatch batch) {
        for (GoldOnFloor floor : gold) {
            batch.draw(Game.goldSheet[floor.type.type], floor.position.x, floor.position.y);
        }
    }
}
