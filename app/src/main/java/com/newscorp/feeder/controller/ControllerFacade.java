package com.newscorp.feeder.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;

/**
 * Created by rosteiner on 5/7/15.
 */
public final class ControllerFacade {

    private ControllerFacade() {

    }

    private static final String TAG = ControllerFacade.class.getSimpleName();

    private static final String ACTION_IMAGE_READY = TAG + ".ACTION_IMAGE_READY";

    private static final String EXTRA_IMAGE_READY_IMAGE = TAG + ".EXTRA_IMAGE_READY_IMAGE";

    private static final String ACTION_QUIZ_ENDED = TAG + ".ACTION_QUIZ_ENDED";

    private static final String EXTRA_QUIZ_END_RESULT = TAG + ".EXTRA_QUIZ_END_RESULT";


    /**
     * HANDLING QUIZ IMAGE LOADED
     */

    public static Intent createOnQuizItemImageReadyIntent() {

        return new Intent(ACTION_IMAGE_READY);
    }

    public static BroadcastReceiver registerOnQuizItemImageReadyListener(final Context context,
                                                                         final OnQuizItemImageReadyListener listener) {

        OnQuizItemImageReadyReceiver receiver = null;
        if (listener != null) {
            receiver = new OnQuizItemImageReadyReceiver() {

                @Override
                public void onImageReady(final Context context) {

                    listener.onImageReady(context);
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

            if (ACTION_IMAGE_READY.equals(intent.getAction())) {
                onImageReady(context);
            }
        }
    }




    /**
     * HANDLING QUIZ RESULTS
     */

    public static Intent createOnQuizEndedIntent(QuizResult quizResult) {

        return new Intent(ACTION_QUIZ_ENDED)
                .putExtra(EXTRA_QUIZ_END_RESULT, quizResult);
    }

    public static BroadcastReceiver registerOnQuizEndedListener(final Context context,
                                                                final OnQuizEndedListener listener) {

        OnQuizEndedReceiver receiver = null;
        if (listener != null) {
            receiver = new OnQuizEndedReceiver() {


                @Override
                public void onQuizEnded(final Context context, final QuizResult quizResult) {

                    listener.onQuizEnded(context, quizResult);
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

            if (ACTION_IMAGE_READY.equals(intent.getAction())) {
                onQuizEnded(context, (QuizResult) intent.getParcelableExtra(EXTRA_QUIZ_END_RESULT));
            }

        }
    }


}
