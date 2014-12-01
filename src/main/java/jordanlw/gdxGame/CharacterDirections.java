package jordanlw.gdxGame;

/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 1/3/13
 * Time: 11:07 AM
 * To change this template use File | Settings | File Templates.
 */
public enum CharacterDirections {
    RIGHT(0),
    DOWN(1),
    UP(2),
    LEFT(3);

    final Integer direction;

    private CharacterDirections(Integer input) {
        direction = input;
    }
}
