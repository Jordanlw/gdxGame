/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Jordan Windsor
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jordanlw.gdxGame;

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
    final Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music/invincible.ogg"));
    final Sound gunSound = Gdx.audio.newSound(Gdx.files.internal("soundEffects/pistol.ogg"));
    final Sound potionSound = Gdx.audio.newSound(Gdx.files.internal("soundEffects/healspell1.ogg"));
    final Sound hurtSound = Gdx.audio.newSound(Gdx.files.internal("soundEffects/human-hurt.ogg"));
    final Sound[] zombieSounds = new Sound[10];

    public MusicLibrary() {
        for (int i = 1; i <= zombieSounds.length; i++) {
            try {
                zombieSounds[i - 1] = Gdx.audio.newSound(Gdx.files.internal("soundEffects/scaled-zombie" + i + ".ogg"));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
