package com.bradleyboxer.corndogcrunch.highscores;

/**
 * Created by Bradley on 7/27/2017.
 */

import java.io.Serializable;

public class Score implements Serializable {

    private static final long serialVersionUID = 1L;
    private int score;
    private String name;

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public Score(String name, int score) {
        this.score = score;
        this.name = name;
    }
}