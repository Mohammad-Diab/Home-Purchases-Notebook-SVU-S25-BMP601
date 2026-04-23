package com.example.homepurchases.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.example.homepurchases.R;

public class SoundManager {

    private SoundPool soundPool;
    private int soundFab;
    private int soundSave;
    private int soundClick;
    private boolean loaded = false;

    public SoundManager(Context context) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(attrs)
                .build();

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) loaded = true;
        });

        soundFab   = soundPool.load(context, R.raw.sound_fab,   1);
        soundSave  = soundPool.load(context, R.raw.sound_save,  1);
        soundClick = soundPool.load(context, R.raw.sound_click, 1);
    }

    public void playFab() {
        play(soundFab);
    }

    public void playSave() {
        play(soundSave);
    }

    public void playClick() {
        play(soundClick);
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        loaded = false;
    }

    private void play(int soundId) {
        if (loaded && soundPool != null) {
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f);
        }
    }
}
