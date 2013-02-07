import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 1/4/13
 * Time: 4:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class Potions extends Character {
    float time;
    static float timeToReach = 15;
    static float healthGiven = 15;
    static Texture[] textures = new Texture[PotionsTypes.amount()];

    public static void initializeTextures() {
        textures[PotionsTypes.BLUE.ordinal()] = new Texture(Gdx.files.internal("blue.png"));
        textures[PotionsTypes.EMPTY.ordinal()] = new Texture(Gdx.files.internal("empty.png"));
        textures[PotionsTypes.GREEN.ordinal()] = new Texture(Gdx.files.internal("green.png"));
        textures[PotionsTypes.PURPLE.ordinal()] = new Texture(Gdx.files.internal("purple.png"));
        textures[PotionsTypes.RED.ordinal()] = new Texture(Gdx.files.internal("red.png"));
        textures[PotionsTypes.YELLOW.ordinal()] = new Texture(Gdx.files.internal("yellow.png"));
    }

}
