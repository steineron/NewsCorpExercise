package com.newscorp.feeder.controller;

import android.content.Context;

/**
 * Created by rosteiner on 5/7/15.
 */
public interface OnQuizEndedListener {

    void onQuizEnded(Context context, QuizResult quizResult);
}
