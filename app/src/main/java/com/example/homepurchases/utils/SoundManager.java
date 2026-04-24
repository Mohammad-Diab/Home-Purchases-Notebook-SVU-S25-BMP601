package com.example.homepurchases.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.example.homepurchases.R;

public class SoundManager {

    private SoundPool soundPool;
    private int soundOpen;
    private int soundDelete;
    private int soundTab;
    private int soundConfirm;
    private int soundCancel;
    private int soundSave;
    private int soundClick;
    private boolean loaded = false;

    public SoundManager(Context context) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(7)
                .setAudioAttributes(attrs)
                .build();

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) loaded = true;
        });

        soundOpen    = soundPool.load(context, R.raw.sound_open,    1);
        soundDelete  = soundPool.load(context, R.raw.sound_delete,  1);
        soundTab     = soundPool.load(context, R.raw.sound_tab,     1);
        soundConfirm = soundPool.load(context, R.raw.sound_confirm, 1);
        soundCancel  = soundPool.load(context, R.raw.sound_cancel,  1);
        soundSave    = soundPool.load(context, R.raw.sound_save,    1);
        soundClick   = soundPool.load(context, R.raw.sound_click,   1);
    }

    public void playOpen()    { play(soundOpen);    }
    public void playFab()     { play(soundOpen);    }
    public void playDelete()  { play(soundDelete);  }
    public void playTab()     { play(soundTab);     }
    public void playConfirm() { play(soundConfirm); }
    public void playCancel()  { play(soundCancel);  }
    public void playSave()    { play(soundSave);    }
    public void playClick()   { play(soundClick);   }

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
