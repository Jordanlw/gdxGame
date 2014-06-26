import com.badlogic.gdx.math.Vector2;

/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 2/25/13
 * Time: 9:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoldOnFloor {
    Vector2 position;
    GoldTypes type;
    public GoldOnFloor(Vector2 vec, GoldTypes gold) {
        position = new Vector2(vec);
        type = gold;
    }
}
