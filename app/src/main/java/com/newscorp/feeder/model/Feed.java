package com.newscorp.feeder.model;

import android.os.Parcelable;

/**
 * Created by rosteiner on 5/5/15.
 */
interface Feed extends Parcelable {

    String getProduct();

    int getResultSize();

    int getVersion();

    QuizFeedItem[] getItems();
}
