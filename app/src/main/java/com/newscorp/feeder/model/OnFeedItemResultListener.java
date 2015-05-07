package com.newscorp.feeder.model;

import android.content.Context;

/**
 * Created by rosteiner on 5/5/15.
 */
public interface OnFeedItemResultListener {

    void onFeedItemResult(Context context, QuizFeedItem item);
    void onFeedItemFault(Context context /*and error codes*/);
}
