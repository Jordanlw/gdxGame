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
    double circleChangeTimer;
    Vector2 position;
    CharacterDirections direction;
    boolean circleDirection;
    boolean isServer;
    boolean connected = false;

    public Character() {
        this.position = new Vector2();
        direction = CharacterDirections.DOWN;
        position.set(Math.random() < 0.5f ? Game.windowSize.x + 50 : -50,Math.random() < 0.5f ? Game.windowSize.y + 50 : -50);
    }

    public Integer getNewWalkingSpeed() {
        return (int)(50 * Math.random() + 50);
    }

    public static Vector2 getEnemyMid() {
        return new Vector2(Game.spriteSheetEnemies[0][0].getRegionWidth() / 2,Game.spriteSheetEnemies[0][0].getRegionHeight() / 2);
    }
}