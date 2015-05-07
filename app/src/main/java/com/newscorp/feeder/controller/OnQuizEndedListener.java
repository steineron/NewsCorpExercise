package com.newscorp.feeder.controller;

import android.content.Context;

import com.newscorp.feeder.model.QuizFeedItem;

/**
 * Created by rosteiner on 5/7/15.
 */
public interface OnQuizEndedListener {

    void onQuizEnded(Context context, QuizFeedItem item, QuizResult quizResult);
}
