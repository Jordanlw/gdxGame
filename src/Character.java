import com.badlogic.gdx.math.Vector2;

/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 1/1/13
 * Time: 1:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class Character {
    float secondsDamaged;
    float health;
    Integer walkingSpeed;
    Vector2 position;
    CharacterDirections direction;

    public Character() {
        this.position = new Vector2();
    }
}