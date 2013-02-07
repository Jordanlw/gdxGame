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
    boolean circleDirection;

    public Character() {
        this.position = new Vector2();
        health = 100;
        direction = CharacterDirections.DOWN;
    }

    public Integer getNewWalkingSpeed() {
        return (int)(50 * Math.random() + 50);
    }
}