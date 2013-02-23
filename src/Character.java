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
    float health = 100;
    Integer walkingSpeed;
    Vector2 position;
    CharacterDirections direction;
    boolean circleDirection;
    boolean isServer;
    boolean connected = false;

    public Character() {
        this.position = new Vector2();
        direction = CharacterDirections.DOWN;
    }

    public Integer getNewWalkingSpeed() {
        return (int)(50 * Math.random() + 50);
    }
}