package com.newscorp.feeder.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.newscorp.feeder.model.QuizFeedItem;

import junit.framework.Assert;

/**
 * {@linkplain ControllerFacade} is a facade of static methods for the controller's package.
 * this facade exposes two events:
 * 1. the quiz's image is loaded
 * 2. the quiz ended
 * <p/>
 * components interested in these events can register a listener via the register methods of this facade.
 * (the register methods return a {@linkplain BroadcastReceiver} that can be unregistered by any {@linkplain Context} when no longer needed)
 * <p/>
 * component wishing to broadcast such events can use the create methods of this facade.
 */
public final class ControllerFacade {

    private ControllerFacade() {

    }

    private static final String TAG = ControllerFacade.class.getSimpleName();

    private static final String ACTION_IMAGE_READY = TAG + ".ACTION_IMAGE_READY";

    private static final String EXTRA_IMAGE_READY_IMAGE = TAG + ".EXTRA_IMAGE_READY_IMAGE";

    private static final String ACTION_QUIZ_ENDED = TAG + ".ACTION_QUIZ_ENDED";

    private static final String EXTRA_QUIZ_FEED_ITEM = TAG + ".EXTRA_QUIZ_FEED_ITEM";

    private static final String EXTRA_QUIZ_END_RESULT = TAG + ".EXTRA_QUIZ_END_RESULT";


    // simple utility class for handling hte task of building an intent for quiz events
    private static class IntentBuilder {

        QuizFeedItem mQuizFeedItem;

        String mAction;

        private QuizResult mQuizResult;

        IntentBuilder() {

        }

        IntentBuilder withAction(String action) {

            mAction = action;
            return this;
        }

        IntentBuilder withQuizFeedItem(QuizFeedItem quizFeedItem) {

            mQuizFeedItem = quizFeedItem;
            return this;
        }

        IntentBuilder withQuizResult(QuizResult quizResult) {

            mQuizResult = quizResult;
            return this;
        }

        Intent build() {

            return new Intent(mAction)
                    .putExtra(EXTRA_QUIZ_FEED_ITEM, mQuizFeedItem)
                    .putExtra(EXTRA_QUIZ_END_RESULT, mQuizResult);
        }

    }

    // simple utility class for handling the task of parsing an intent of quiz events
    private static class IntentParser {

        final Intent mIntent;

        final Bundle mExtras;

        private IntentParser(final Intent intent) {

            Assert.assertNotNull(intent);

            mIntent = intent;
            mExtras = mIntent.getExtras();
        }

        String getAction() {

            return mIntent.getAction();
        }

        QuizFeedItem getFeedItem() {

            return mExtras == null ?
                   null :
                   (QuizFeedItem) mExtras.getParcelable(EXTRA_QUIZ_FEED_ITEM);
        }

        QuizResult getResult(){
            return mExtras == null ?
                   null :
                   (QuizResult) mExtras.getParcelable(EXTRA_QUIZ_END_RESULT);

        }
    }

    /**
     * HANDLING QUIZ IMAGE LOADED
     */

    public static Intent createOnQuizItemImageReadyIntent(QuizFeedItem quizItem) {

        return new IntentBuilder()
                .withAction(ACTION_IMAGE_READY)
                .withQuizFeedItem(quizItem)
                .build();
    }

    public static BroadcastReceiver registerOnQuizItemImageReadyListener(final Context context,
                                                                         final OnQuizItemImageReadyListener listener) {

        OnQuizItemImageReadyReceiver receiver = null;
        if (listener != null) {
            receiver = new OnQuizItemImageReadyReceiver() {

                @Override
                public void onImageReady(final Context context, final QuizFeedItem quizFeedItem) {

                    listener.onImageReady(context, quizFeedItem);
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_IMAGE_READY);

            context.registerReceiver(receiver, filter);
        }
        return receiver;
    }


    private abstract static class OnQuizItemImageReadyReceiver extends BroadcastReceiver implements OnQuizItemImageReadyListener {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            IntentParser parser = new IntentParser(intent);
            if (ACTION_IMAGE_READY.equals(parser.getAction())) {
                onImageReady(context, parser.getFeedItem());
            }
        }
    }


    /**
     * HANDLING QUIZ RESULTS
     */

    public static Intent createOnQuizEndedIntent(QuizFeedItem quizItem, QuizResult quizResult) {

        return new IntentBuilder()
                .withAction(ACTION_QUIZ_ENDED)
                .withQuizFeedItem(quizItem)
                .withQuizResult(quizResult)
                .build();
    }

    public static BroadcastReceiver registerOnQuizEndedListener(final Context context,
                                                                final OnQuizEndedListener listener) {

        OnQuizEndedReceiver receiver = null;
        if (listener != null) {
            receiver = new OnQuizEndedReceiver() {


                @Override
                public void onQuizEnded(final Context context, QuizFeedItem item,
                                        final QuizResult quizResult) {

                    listener.onQuizEnded(context, item, quizResult);
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_QUIZ_ENDED);

            context.registerReceiver(receiver, filter);
        }
        return receiver;
    }

    private abstract static class OnQuizEndedReceiver extends BroadcastReceiver implements OnQuizEndedListener {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            IntentParser parser = new IntentParser(intent);

            if (ACTION_QUIZ_ENDED.equals(parser.getAction())) {
                onQuizEnded(context,
                        parser.getFeedItem(),
                        parser.getResult());
            }

        }
    }


}
