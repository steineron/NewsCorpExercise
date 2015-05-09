package com.newscorp.feeder.model;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * the service that retrieves the feed for the quiz items and provides the next item upon request.
 * components interested in the 'new-quiz-item' event can register a listener though this service's {@code registerOnFeedItemResultListener}
 * and can trigger retrieval of the next quiz item by starting this service with the intent created by {@code createNextQuizItemIntent}
 *
 * Created by rosteiner on 5/5/15.
 */
public class GetFeedService extends Service {

    private static final String FEED_URL = "https://dl.dropboxusercontent.com/u/30107414/game.json";

    private static final String TAG = GetFeedService.class.getSimpleName();

    // get a feed item
    private static final String ACTION_GET_FEED_ITEM = TAG + ".ACTION_GET_FEED_ITEM";

    // report the result
    private static final String ACTION_GET_FEED_ITEM_RESULT = TAG + ".ACTION_GET_FEED_ITEM_RESULT";

    // and extra for the index of the desired feed item
    private static final String EXTRA_FEED_ITEM_INDEX = TAG + ".EXTRA_FEED_ITEM_INDEX";

    // an extra for the feed item
    private static final String EXTRA_FEED_ITEM = TAG + ".EXTRA_FEED_ITEM";

    // a boolean indicating whether or not the operation was successful
    private static final String EXTRA_SUCCESS = TAG + ".EXTRA_SUCCESS";

    private static FeedImpl mFeed;

    private static int mFeedItemIndex=0;

    public static Intent createNextQuizItemIntent(Context context) {

        return new Intent(context, GetFeedService.class);
    }

    private static class IntentParser {

        private final Bundle mExtras;

        private IntentParser(Bundle extras) {

            mExtras = extras;
        }

        boolean isSuccess() {

            return mExtras != null && mExtras.getBoolean(EXTRA_SUCCESS, false);
        }

        QuizFeedItem getFeedItem() {

            return mExtras != null ?
                   (QuizFeedItem) mExtras.getParcelable(EXTRA_FEED_ITEM) :
                   null;
        }

        int getFeedItemIndex() {

            return mExtras != null ?
                   mExtras.getInt(EXTRA_FEED_ITEM_INDEX, -1) :
                   -1;
        }
    }

    public static BroadcastReceiver registerOnFeedItemResultListener(Context context,
                                                                     final OnFeedItemResultListener listener) {

        BroadcastReceiver receiver = null;
        if (listener != null) {
            receiver = new OnFeedItemResultReceiver() {

                @Override
                public void onFeedItemResult(final Context context, final QuizFeedItem item) {

                    listener.onFeedItemResult(context, item);
                }

                @Override
                public void onFeedItemFault(final Context context) {

                    listener.onFeedItemFault(context);
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_GET_FEED_ITEM_RESULT);
            context.registerReceiver(receiver, filter);
        }
        return receiver;
    }

    @Override
    public IBinder onBind(final Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        synchronized (TAG) {
            if (mFeed == null) {

                new FeedRequestTask().execute();
            }
            else{
                mFeedItemIndex++;
                broadcastNextItem();
                stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void broadcastNextItem() {
        boolean isSuccess = mFeed != null && mFeed.items != null && mFeed.items.length > 0;
        QuizFeedItemImpl feedItem = isSuccess ?
                                mFeed.items[ mFeedItemIndex ] :
                                null;

        sendBroadcast(new Intent(ACTION_GET_FEED_ITEM_RESULT)
                .putExtra(EXTRA_SUCCESS, isSuccess)
                .putExtra(EXTRA_FEED_ITEM, feedItem)
                .putExtra(EXTRA_FEED_ITEM_INDEX, mFeedItemIndex));

    }


    public abstract static class OnFeedItemResultReceiver extends BroadcastReceiver implements OnFeedItemResultListener {


        @Override
        final public void onReceive(final Context context, final Intent intent) {

            if (ACTION_GET_FEED_ITEM_RESULT.equals(intent.getAction())) {
                IntentParser parser = new IntentParser(intent.getExtras());

                if (parser.isSuccess()) {
                    onFeedItemResult(context, parser.getFeedItem());
                }
                else {
                    onFeedItemFault(context);
                }
            }
        }
    }


    static class FeedImpl implements Feed {

        String product;

        int resultSize;

        int version;

        QuizFeedItemImpl[] items;

        protected FeedImpl(Parcel in) {

            product = in.readString();
            resultSize = in.readInt();
            version = in.readInt();
            int nItems = in.readInt();
            if (nItems > 0) {
                items = new QuizFeedItemImpl[ nItems ];
                for (int i = 0; i < nItems; i++) {
                    items[ i ] = in.readParcelable(QuizFeedItemImpl.class.getClassLoader());
                }
            }
        }

        @Override
        public int describeContents() {

            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            dest.writeString(product);
            dest.writeInt(resultSize);
            dest.writeInt(version);
            int nItems = items != null ? items.length : 0;
            dest.writeInt( nItems );
            if (nItems>0) {
                for (int i = 0; i < nItems; i++) {
                    dest.writeParcelable(items[ i ], flags);
                }
            }

        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<FeedImpl> CREATOR =
                new Parcelable.Creator<FeedImpl>() {

                    @Override
                    public FeedImpl createFromParcel(Parcel in) {

                        return new FeedImpl(in);
                    }

                    @Override
                    public FeedImpl[] newArray(int size) {

                        return new FeedImpl[ size ];
                    }
                };

        @Override
        public String getProduct() {

            return product;
        }

        @Override
        public int getResultSize() {

            return resultSize;
        }

        @Override
        public int getVersion() {

            return version;
        }

        @Override
        public QuizFeedItemImpl[] getItems() {

            return items;
        }
    }

    static class QuizFeedItemImpl implements QuizFeedItem {

        int correctAnswerIndex;

        String imageUrl;

        String standFirst;

        String storyUrl;

        String section;

        String[] headlines;

        protected QuizFeedItemImpl(Parcel in) {

            correctAnswerIndex = in.readInt();
            imageUrl = in.readString();
            standFirst = in.readString();
            storyUrl = in.readString();
            section = in.readString();
            int nItems = in.readInt();
            if (nItems > 0) {
                headlines = new String[ nItems ];
                for (int i = 0; i < nItems; i++) {
                    headlines[ i ] = in.readString();
                }
            }
        }

        @Override
        public int describeContents() {

            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            dest.writeInt(correctAnswerIndex);
            dest.writeString(imageUrl);
            dest.writeString(standFirst);
            dest.writeString(storyUrl);
            dest.writeString(section);
            int nItems = headlines != null ? headlines.length : 0;
            dest.writeInt(nItems );
            if ( nItems > 0) {
                for (int i = 0; i < nItems; i++) {
                    dest.writeString(headlines[ i ]);
                }
            }
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<QuizFeedItemImpl> CREATOR =
                new Parcelable.Creator<QuizFeedItemImpl>() {

                    @Override
                    public QuizFeedItemImpl createFromParcel(Parcel in) {

                        return new QuizFeedItemImpl(in);
                    }

                    @Override
                    public QuizFeedItemImpl[] newArray(int size) {

                        return new QuizFeedItemImpl[ size ];
                    }
                };

        @Override
        public int getCorrectAnswerIndex() {

            return correctAnswerIndex;
        }

        @Override
        public String getImageUrl() {

            return imageUrl;
        }

        @Override
        public String getStandFirst() {

            return standFirst;
        }

        @Override
        public String getStoryUrl() {

            return storyUrl;
        }

        @Override
        public String getSection() {

            return section;
        }

        @Override
        public String[] getHeadlines() {

            return headlines;
        }
    }

    private class FeedRequestTask extends AsyncTask<Void, Void, FeedImpl> {

        @Override
        protected FeedImpl doInBackground(Void... params) {

            try {
                RestTemplate restTemplate = new RestTemplate();

                // hte GET is of type 'text/plain' and not 'application/json' so there's no build-in
                // spring message-converter that can produce a POJO.
                // ideally - i'd implement one myself using fasterxml, but given the time constrains
                // for this exercise - i chose the 'quick-and-dirty solution:
                // get the string, and use GSON to convert to POJO.

                restTemplate.getMessageConverters()
                        .add(new StringHttpMessageConverter());

                String feedStr = restTemplate.getForObject(FEED_URL, String.class);

                Gson gson = new Gson();
                FeedImpl feed = gson.fromJson(feedStr, FeedImpl.class);

                return feed;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(FeedImpl feed) {

            mFeed = feed;
            mFeedItemIndex = 0;
            broadcastNextItem();
            stopSelf();
        }

    }

}
