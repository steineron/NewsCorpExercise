package com.newscorp.feeder.controller;

import android.os.Parcelable;

/**
 * Created by rosteiner on 5/7/15.
 */
public interface QuizResult extends Parcelable{

    enum QuizTermination{
        USER_ANSWERED,
        ZERO_POINTS,
        TIMEOUT
    }

    boolean isSuccess();

    QuizTermination getTermination();

    int getScore();

}
