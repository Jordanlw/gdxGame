import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 1/4/13
 * Time: 4:55 PM
 * To change this template use File | Settings | File Templates.
 */
class Potions extends Character {
    static final float secsTillDisappear = 15;
    static final float healthGiven = 30;
    static final Texture[] textures = new Texture[PotionsTypes.amount()];
    float time;

    public static void initializeTextures() {
        textures[PotionsTypes.BLUE.potion] = new Texture(Gdx.files.internal("resources/blue.png"));
        textures[PotionsTypes.EMPTY.potion] = new Texture(Gdx.files.internal("resources/empty.png"));
        textures[PotionsTypes.GREEN.potion] = new Texture(Gdx.files.internal("resources/green.png"));
        textures[PotionsTypes.PURPLE.potion] = new Texture(Gdx.files.internal("resources/purple.png"));
        textures[PotionsTypes.RED.potion] = new Texture(Gdx.files.internal("resources/red.png"));
        textures[PotionsTypes.YELLOW.potion] = new Texture(Gdx.files.internal("resources/yellow.png"));
    }

}
