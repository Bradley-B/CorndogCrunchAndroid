package com.bradleyboxer.corndogcrunch;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.bradleyboxer.corndogcrunch.R;

import java.io.File;
import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        file = new File(getFilesDir(), "scores.dat");
    }

    public void onResetScoreboard(View v) {
        if(file.exists()) {
            try {
                file.getCanonicalFile().delete();
            } catch (IOException e) {file.delete();}
        }
    }
}
