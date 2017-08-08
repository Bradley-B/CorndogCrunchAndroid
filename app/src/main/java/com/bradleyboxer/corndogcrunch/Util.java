package com.bradleyboxer.corndogcrunch;

import android.util.Log;

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

/**
 * Created by Bradley on 7/28/2017.
 */

public class Util {

    /**
     * Sorts scores
     * @param scores Scores to be sorted
     * @return The sorted scores
     */
    public static ArrayList<Score> sort(ArrayList<Score> scores) {
        ScoreComparator comparator = new ScoreComparator();
        Collections.sort(scores, comparator);
        return scores;
    }

    /**
     * Pushes specified scores to the score file
     * @param file The location of the file
     * @param scores scoreboard scores to be pushed to the score file
     */
    public static void updateScoreFile(File file, ArrayList<Score> scores) {
        ObjectOutputStream objectOutputStream = null;

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

    /**
     * Returns the scores contained in the score file
     * @param file The location of the file
     * @return Current scoreboard scores
     */
    public static ArrayList<Score> loadScoreFile(File file) {
        ObjectInputStream objectInputStream = null;

        try {
            objectInputStream = new ObjectInputStream(new FileInputStream(file));
            ArrayList<Score> scores = (ArrayList<Score>) objectInputStream.readObject();
            return scores;
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
            } catch (IOException e) {
                Log.e("HIGHSCORES", e.getMessage());
            }
        }

        return null;
    }

    public static int extractNumber(final String str) {

        if(str == null || str.isEmpty()) return 0;

        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for(char c : str.toCharArray()){
            if(Character.isDigit(c)){
                sb.append(c);
                found = true;
            } else if(found){
                // If we already found a digit before and this char is not a digit, stop looping
                break;
            }
        }

        try {
            return Integer.valueOf(sb.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String getCommand(String data) {
        if(data.startsWith("/")) {
            String basecommand = null;

            for(int i=0;i<data.length();i++) {
                if(Character.isWhitespace(data.charAt(i))) {
                    basecommand = data.substring(1, i);
                }
            }
            if(basecommand==null) {
                basecommand = data.substring(1, data.length());
            }

            return basecommand;
        } else return data;
    }

    public static String getSubcommand(String data) {

        if(data.startsWith("/")) {
            String subcommand = null;

            for(int i=0;i<data.length();i++) {
                if(Character.isWhitespace(data.charAt(i))) {
                    subcommand = data.substring(i+1, data.length());
                    return subcommand;
                }
            }
        }
        return "";
    }
}
