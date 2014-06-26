/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 2/25/13
 * Time: 7:48 PM
 * To change this template use File | Settings | File Templates.
 */
public enum GoldTypes {
    COIN(0),
    SKULL(1),
    BAR(2),
    PILE(3);

    int type;
    private GoldTypes(int input) {
        type = input;
    }
}
