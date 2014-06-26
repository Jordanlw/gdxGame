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
class MusicLibrary {
    final Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("resources/Heroic Demise (New)_0.mp3"));
    final Sound gunSound = Gdx.audio.newSound(Gdx.files.internal("resources/laser1.wav"));
    final Sound potionSound = Gdx.audio.newSound(Gdx.files.internal("resources/healspell1.wav"));
    final Sound hurtSound = Gdx.audio.newSound(Gdx.files.internal("resources/slightscream-01.wav"));
    final Sound[] zombieSounds = new Sound[20];
    public MusicLibrary() {
        for(int i = 1;i <= zombieSounds.length;i++) {
            try {
                zombieSounds[i - 1] = Gdx.audio.newSound(Gdx.files.internal("resources/scaled-zombie" + String.valueOf(i) + ".wav"));
            } catch (Exception e) {
                System.out.println("Can't read zombie idle sound number:" + String.valueOf(i));
                System.exit(1);
            }
        }
    }
}
