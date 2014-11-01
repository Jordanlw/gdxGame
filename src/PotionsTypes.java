/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 1/4/13
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */
public enum PotionsTypes {
    BLUE(0),
    EMPTY(1),
    GREEN(2),
    PURPLE(3),
    RED(4),
    YELLOW(5);

    final Integer potion;
    private PotionsTypes(Integer potion) {
        this.potion = potion;
    }

    public static Integer amount() {
        return PotionsTypes.values().length;
    }
}
