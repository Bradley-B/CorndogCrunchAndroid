package com.bradleyboxer.corndogcrunch;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bradleyboxer.corndogcrunch.highscores.Score;
import com.bradleyboxer.corndogcrunch.highscores.ScoreComparator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class ScoreboardActivity extends AppCompatActivity {

    int[] nameViewIds = new int[10];
    int[] scoreViewIds = new int[10];
    private File file;
    private ArrayList<Score> scores = new ArrayList<Score>();
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    public static int lowestBestScore = 0;
    public boolean scoreAdded = false;
    public int newScore = 0;
    public String newName = "";
    public Intent savedIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        int baseViewId = R.id.nameView0;
        int j = 0;
        for(int i=0;i<(nameViewIds.length);i++) {
            if(i<10) {
                nameViewIds[i] = baseViewId + j;
                scoreViewIds[i] = baseViewId + j + 1;
            }
            j+=2;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        file = new File(getFilesDir(), "scores.dat");
        populate2();
        lowestBestScore = getScores().get(scores.size()-1).getScore();

        Intent intent = getIntent();
        if(intent.hasExtra("playerScore")) {
            savedIntent = intent;
            findViewById(R.id.highscoreName).setVisibility(View.VISIBLE);
            findViewById(R.id.highscoreSubmitButton).setVisibility(View.VISIBLE);
        }
    }

    public void onNameSubmit(View v) {
        if(!scoreAdded) {
            newName = ((EditText)findViewById(R.id.highscoreName)).getText().toString();
            newScore = savedIntent.getIntExtra("playerScore", 0);
            addScore(newName, newScore);
            scoreAdded = true;
            findViewById(R.id.highscoreName).setVisibility(View.GONE);
            findViewById(R.id.highscoreSubmitButton).setVisibility(View.GONE);
            populate();
        }
    }

    public void loadScoreFile() {
        try {
            objectInputStream = new ObjectInputStream(new FileInputStream(file));
            scores = (ArrayList<Score>) objectInputStream.readObject();
        } catch (FileNotFoundException e) {
            Log.e("HIGHSCORES", e.getMessage());
        } catch (IOException e) {
            Log.e("HIGHSCORES", e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("HIGHSCORES", e.getMessage());
        } finally {
            try {
                if(objectInputStream!=null) {
                    objectInputStream.close();
                }
            } catch(Exception e) {
                Log.e("HIGHSCORES", e.getMessage());
            }
        }
    }

    public void updateScoreFile() {
        try {
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
            objectOutputStream.writeObject(scores);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(objectOutputStream!=null) {
                    objectOutputStream.flush();
                    objectOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void populate2() {
        try {
            populate();
        }
        catch (IndexOutOfBoundsException e) {
            for(int i=0;i<nameViewIds.length;i++) {
                addScore("N/A", 0);
            }
            populate();
        }
    }

    public void populate() throws IndexOutOfBoundsException {
        for(int i=0;i<nameViewIds.length;i++) {
            scores = getScores(); //filling the scores-arraylist
            if(i<9) {
                findTextViewById(nameViewIds[i]).setText(String.valueOf(i+1) + ".        " + scores.get(i).getName());
            } else {
                findTextViewById(nameViewIds[i]).setText(String.valueOf(i+1) + ".       " + scores.get(i).getName());
            }

            findTextViewById(scoreViewIds[i]).setText(String.valueOf(scores.get(i).getScore()));
        }
    }

    public void addScore(String name, int score) {
        loadScoreFile();
        scores.add(new Score(name, score));
        updateScoreFile();
    }

    public ArrayList<Score> getScores() {
        loadScoreFile();
        sort();
        return scores;
    }

    private void sort() {
        ScoreComparator comparator = new ScoreComparator();
        Collections.sort(scores, comparator);
    }

    public TextView findTextViewById(int id) {
        return ((TextView) findViewById(id));
    }
}