package com.bradleyboxer.corndogcrunch;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
    }

    public void onSinglePlayerButton(View view) {
        Intent intent = new Intent(this, SingleplayerActivity.class);
        startActivity(intent);
    }

    public void onScoreboardButton(View view) {
        Intent intent = new Intent(this, ScoreboardActivity.class);
        startActivity(intent);
    }

    public void onMultiplayerButton(View v) {
        Intent intent = new Intent(this, MultiplayerSettingsActivity.class);
        startActivity(intent);
    }
}
