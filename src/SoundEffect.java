/*
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.TimeUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.InputStream;

class SoundEffect {
    private final Sound sound;
    private final String path;
    private long lastPlayed;

    public SoundEffect(String soundFile) {
        sound = Gdx.audio.newSound(Gdx.files.internal(soundFile));
        path = soundFile;
    }

    public boolean play() {
        if(TimeUtils.millis() > lastPlayed + ((long)length() * 1000 + ((Math.random() * 500) + 500))) {
            sound.play();
            lastPlayed = TimeUtils.millis();
            return true;
        }
        return false;
    }

    float length() {
        InputStream inputStream = this.getClass().getResourceAsStream(path);
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(inputStream);
        } catch (Exception error) {
            System.out.println(error.toString());
            System.exit(1);

        }
        AudioFormat format = audioInputStream.getFormat();
        long frames = audioInputStream.getFrameLength();
        return frames / format.getFrameRate();
    }
}
*/