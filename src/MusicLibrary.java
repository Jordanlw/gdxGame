import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * Created with IntelliJ IDEA.
 * User: jordan
 * Date: 2/7/13
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class MusicLibrary {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("Heroic Demise (New)_0.mp3"));
    Sound gunSound = Gdx.audio.newSound(Gdx.files.internal("laser1.wav"));
    Sound potionSound = Gdx.audio.newSound(Gdx.files.internal("healspell1.wav"));
    Sound hurtSound = Gdx.audio.newSound(Gdx.files.internal("slightscream-01.wav"));
    Sound[] zombieSounds = new Sound[20];
    public MusicLibrary() {
        for(int i = 1;i <= zombieSounds.length;i++) {
            try {
                zombieSounds[i - 1] = Gdx.audio.newSound(Gdx.files.internal("idleZombieSounds/scaled-zombie" + String.valueOf(i) + ".wav"));
            } catch (Exception e) {
                System.out.println("Can't read zombie idle sound number:" + String.valueOf(i));
                System.exit(1);
            }
        }
    }
}
