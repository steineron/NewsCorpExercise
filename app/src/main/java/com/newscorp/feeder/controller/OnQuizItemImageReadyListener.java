package com.newscorp.feeder.controller;

import android.content.Context;
import android.graphics.Bitmap;

import com.newscorp.feeder.model.QuizFeedItem;

/**
 * Created by rosteiner on 5/7/15.
 */
public interface OnQuizItemImageReadyListener {
    void onImageReady(Context context, QuizFeedItem quizFeedItem);
}
