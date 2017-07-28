package com.bradleyboxer.corndogcrunch;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.media.SoundPool;

import java.util.Random;

public class SingleplayerActivity extends AppCompatActivity {

    boolean isGameRunning = false;
    int score = 0;
    int activeCreatureId = -1;
    int[] creatureIds = new int[9];
    int[] imageIds = new int[9];
    Random random = new Random();
    int biteSound = R.raw.bite;
    int airhornSound = R.raw.airhorn;
    int[] soundIds = new int[2];
    AudioAttributes attrs;
    SoundPool sp;

    @Override
    protected void onCreate(Bundle state) {

        attrs = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        sp = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(attrs).build();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundIds[0] = sp.load(getApplicationContext(), biteSound, 1);
        soundIds[1] = sp.load(getApplicationContext(), airhornSound, 1);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        int baseCreatureId = R.id.imageButton0;
        int baseImageId = R.drawable.c0;
        int referenceOffset = 0;
        for(int i=0;i<9;i++) {
            imageIds[i] = baseImageId + i;
            if(i==3 || i==6) {
                referenceOffset++;
            }
            creatureIds[i] = baseCreatureId + i + referenceOffset;
        }

        super.onCreate(state);
        setContentView(R.layout.activity_singleplayer);

        //for(int i=0;i<0;i++) {
        //    getCreatureById(creatureIds[i]); //set creature IDs because they can't be set earlier
        //}
    }

    public void onStartButton(View v) throws InterruptedException {
        if(!isGameRunning) {
            isGameRunning = true;

            Thread t = new Thread() {
                @Override
                public void run() {
                    final long initTime = System.currentTimeMillis();
                    final int pregameTime = 4000;
                    final int gameTime = 8000;

                    while(System.currentTimeMillis()-initTime<=pregameTime) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setGameText("Game beginning in "+String.valueOf((initTime+pregameTime)-System.currentTimeMillis()) + " ms");
                            }
                        });
                        try{Thread.sleep(random.nextInt(50));} catch (InterruptedException e){};
                    }

                    runOnUiThread(new Runnable() { //tick game once to get it started
                        @Override
                        public void run() {
                            startupGame();
                        }
                    });

                    while(System.currentTimeMillis()-initTime<=pregameTime+gameTime) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setGameText("Game ending in "+String.valueOf((initTime+pregameTime+gameTime)-System.currentTimeMillis()) + " ms");
                            }
                        });
                        try{Thread.sleep(random.nextInt(50));} catch (InterruptedException e){};
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            shutdownGame();
                        }
                    });
                }
            };
            t.start();
        }
    }

    public void startupGame() {
        activeCreatureId = creatureIds[random.nextInt(creatureIds.length)];
        System.out.println(activeCreatureId);
        setCreatureImage(activeCreatureId, imageIds[random.nextInt(imageIds.length)]);
    }

    public void shutdownGame() {
        sp.play(soundIds[1], 1, 1, 1, 0, 1.0f);
        setGameText("Your score is "+score);
        resetCreatureImageById(activeCreatureId);
        activeCreatureId = -1;
        boolean goToScoreboard = score>ScoreboardActivity.lowestBestScore;
        Intent scoreboardIntent = null;

        if(goToScoreboard) {
            Intent intent = new Intent(this, ScoreboardActivity.class);
            intent.putExtra("playerScore", score);
            startActivity(intent);
        }

        score = 0;
        isGameRunning = false;

        if(goToScoreboard && scoreboardIntent!=null) {
            startActivity(scoreboardIntent);
        }
    }

    public void onCreatureButton(View v) {
        if(activeCreatureId==v.getId()) {
            sp.play(soundIds[0], 1, 1, 1, 0, 1.0f);
            score++;
            resetCreatureImageById(activeCreatureId);
            startupGame();
        }
    }

    public void setCreatureImage(int creatureId, int imageId) {
        getCreatureById(creatureId).setImageResource(imageId);
    }

    public ImageButton getCreatureById(int creatureId) {
        ImageButton viewFound = (ImageButton) findViewById(creatureId);
        if(viewFound!=null) {
            return viewFound;
        } else {
            System.err.println("creature ID machine broke");
            return null;
        }
    }

    public void resetCreatureImageById(int creatureId) {
        setCreatureImage(creatureId, R.drawable.default_image);
    }

    public void setGameText(String text) {
        ((TextView) findViewById(R.id.gameText)).setText(text);
    }

    @Override
    public void onDestroy() {
        sp.release();
        super.onDestroy();
    }
}