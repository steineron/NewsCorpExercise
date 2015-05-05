package com.newscorp.feeder.model;

import android.os.Parcelable;

/**
 * Created by rosteiner on 5/5/15.
 */
public interface FeedItem extends Parcelable {

    int getCorrectAnswerIndex();

    String getImageUrl();

    String getStandFirst();

    String getStoryUrl();

    String getSection();

    String[] getHeadlines();
}

