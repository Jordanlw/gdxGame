package jordanlw.gdxGame;

import com.badlogic.gdx.math.Vector2;

/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 1/1/13
 * Time: 1:06 AM
 * To change this template use File | Settings | File Templates.
 */
class Character {
    final Vector2 position;
    float rotation;
    float secondsDamaged;
    float health = 100;
    Integer walkingSpeed;
    double circleChangeTimer;
    boolean circleDirection;
    boolean isServer;
    boolean connected = false;

    public Character() {
        this.position = new Vector2();
        this.health = 100;
        this.position.set(Math.random() < 0.5f ? Game.windowSize.x + 50 : -50, Math.random() < 0.5f ? Game.windowSize.y + 50 : -50);
        this.walkingSpeed = getNewWalkingSpeed();
        this.secondsDamaged = 0;
        this.circleDirection = Math.random() < 0.5f;
        this.circleChangeTimer = 7.5f + (Math.random() * 2.5f);
    }

    public static Vector2 getEnemyMid() {
        return new Vector2(Game.enemyAnim.getKeyFrames()[0].getRegionWidth() / 2, Game.enemyAnim.getKeyFrames()[0].getRegionHeight() / 2);
    }

    public Integer getNewWalkingSpeed() {
        return (int) (50 * Math.random() + 50);
    }
}