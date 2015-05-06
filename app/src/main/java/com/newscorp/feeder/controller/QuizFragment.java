package com.newscorp.feeder.controller;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.newscorp.feeder.R;
import com.newscorp.feeder.model.FeedItem;
import com.newscorp.feeder.model.GetFeedService;
import com.newscorp.feeder.model.OnFeedItemResultListener;
import com.newscorp.feeder.model.VolleyWrapper;

/**
 * Created by rosteiner on 5/5/15.
 */
public class QuizFragment extends Fragment implements OnFeedItemResultListener {

    private ImageView mPreviewImageView;

    private TextView mProgressTextView;

    private ProgressBar mQuizProgressBar;

    private Button mAnswerButton1;

    private Button mAnswerButton2;

    private Button mAnswerButton3;

    private BroadcastReceiver mFeedReceiver;

    private FeedItem mFeedItem;

    private Runnable mCountDownRunnable;

    private int mQuizTimeSeconds;

    private int mPointsLostOnErrors;

    int[] mWrongAnswersPenalties = new int[ 3 ]; // how many point can be lost for each wrong answer

    private Button[] mArrayOfAnswerViews;

    private ImageRequest mImageRequest;

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        resetQuiz();
    }


    @Override
    public void onResume() {

        super.onResume();
        mFeedReceiver = GetFeedService.registerOnFeedItemResultListener(getActivity(), this);
        if (mFeedItem == null) {
            getNextQuizItem();
        }
        else{
            onFeedItemResult(getActivity(),mFeedItem);
        }
    }


    @Override
    public void onPause() {

        try {
            getActivity().unregisterReceiver(mFeedReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cancelQuiz();
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_quiz_main, container, false);

        mPreviewImageView = (ImageView) rootView.findViewById(R.id.quiz_preview_image);
        mProgressTextView = (TextView) rootView.findViewById(R.id.quiz_progress_text);
        mQuizProgressBar = (ProgressBar) rootView.findViewById(R.id.quiz_remaining_time_progress);
        mAnswerButton1 = (Button) rootView.findViewById(R.id.quiz_answer_1);
        mAnswerButton2 = (Button) rootView.findViewById(R.id.quiz_answer_2);
        mAnswerButton3 = (Button) rootView.findViewById(R.id.quiz_answer_3);

        mArrayOfAnswerViews = new Button[ 3 ];
        mArrayOfAnswerViews[ 0 ] = mAnswerButton1;
        mArrayOfAnswerViews[ 1 ] = mAnswerButton2;
        mArrayOfAnswerViews[ 2 ] = mAnswerButton3;

        for (int i = 0; i < mArrayOfAnswerViews.length; i++) {

            final int answer = i;
            mArrayOfAnswerViews[ i ].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {

                    onAnswer(answer);
                }
            });
        }


        return rootView;
    }

    @Override
    public void onFeedItemResult(final Context context, final FeedItem item) {

        resetQuiz();

        mFeedItem = item;
        getPreviewImageForFeedItem(mFeedItem);
    }


    @Override
    public void onFeedItemFault(final Context context) {

    }

    private void getNextQuizItem() {

        getActivity().startService(GetFeedService.createNextQuizItemIntent(getActivity()));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void revealView(View view) {
        // this groovy effect requires Android 5.0 and above

        if (view != null) {
            float radius =
                    Math.max(view.getMeasuredHeight(), view.getMeasuredWidth());

            Animator reveal = ViewAnimationUtils.createCircularReveal(
                    view,           // The  View to reveal
                    Math.round(
                            radius * 0.5f),      // x to start the mask from - start from the middle
                    Math.round(radius),      // y to start the mask from - start from the bottom
                    0f,                          // radius of the starting mask
                    radius);                     // radius of the final mask
            reveal.setDuration(150L)
                    .setInterpolator(AnimationUtils.loadInterpolator(getActivity(),
                            android.R.interpolator.linear_out_slow_in));
            reveal.start();
        }
    }

    private void onAnswer(final int answerIndex) {

        if (mFeedItem != null) {
            if (answerIndex != mFeedItem.getCorrectAnswerIndex()) {
                computeWrongAnswerPenalty(answerIndex);
                if (calculatePointsRemaining() <= 0) {
                    onQuizEnded();
                }
            }
            else {
                onQuizEnded();
            }
        }

    }

    private void computeWrongAnswerPenalty(final int answerIndex) {

        mPointsLostOnErrors += mWrongAnswersPenalties[ answerIndex ];
        mWrongAnswersPenalties[ answerIndex ] = 0;
    }


    private void updateQuizProgress(final int quizTimeSeconds) {

        try {
            if (mProgressTextView != null) {
                mProgressTextView.setText(getResources().getString(R.string.quiz_progress_text_remaining, calculatePointsRemaining()));
            }

            if (mQuizProgressBar != null) {
                mQuizProgressBar.setProgress(mQuizTimeSeconds);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    private int calculatePointsRemaining() {

        return mQuizTimeSeconds - mPointsLostOnErrors;
    }

    private void onQuizEnded() {


        synchronized (this) {
            mCountDownRunnable = null;
        }
//        resetQuiz();

        for (int i = 0; i < mArrayOfAnswerViews.length; i++) {
            mArrayOfAnswerViews[ i ].setVisibility(View.INVISIBLE);
        }
        mArrayOfAnswerViews[ mFeedItem.getCorrectAnswerIndex() ].setVisibility(View.VISIBLE);

        getView().postDelayed(new Runnable() {

            @Override
            public void run() {

                getNextQuizItem();
            }
        }, 2000);
    }

    private void cancelQuiz() {

        synchronized (this) {
            mCountDownRunnable = null;
        }
    }

    private void resetQuiz() {

        if (mArrayOfAnswerViews != null) {
            for (int i = 0; i < mArrayOfAnswerViews.length; i++) {
                mArrayOfAnswerViews[ i ].setVisibility(View.VISIBLE);
                mArrayOfAnswerViews[ i ].setText(null);
            }
        }
        mPointsLostOnErrors = 0;
        mQuizTimeSeconds = getResources().getInteger(R.integer.quiz_time);
        mWrongAnswersPenalties[ 0 ] = 2;
        mWrongAnswersPenalties[ 1 ] = 2;
        mWrongAnswersPenalties[ 2 ] = 2;
        updateQuizProgress(mQuizTimeSeconds);

        synchronized (this) {
            mCountDownRunnable = new Runnable() {

                @Override
                public void run() {

                    if (this != mCountDownRunnable) {
                        return; // 'this' shouldn't be running
                    }
                    updateQuizProgress(mQuizTimeSeconds);
                    if (calculatePointsRemaining() <= 0) {
                        onQuizEnded();
                    }
                    else {
                        mQuizTimeSeconds--;
                        getView().postDelayed(this, 1000);
                    }
                }
            };
        }
    }


    private void getPreviewImageForFeedItem(final FeedItem feedItem) {

        mImageRequest = new ImageRequest(feedItem.getImageUrl(),
                new Response.Listener<Bitmap>() {

                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onResponse(Bitmap bitmap) {

                        mPreviewImageView.setVisibility(View.VISIBLE);
                        View root = getView();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                                root.isAttachedToWindow()) {
                            revealView(root);
                        }
                        mPreviewImageView.setImageBitmap(bitmap);
                        onImageDisplayed(feedItem);
                    }

                }, 0, 0, ImageView.ScaleType.CENTER_CROP, null,
                new Response.ErrorListener() {

                    public void onErrorResponse(VolleyError error) {

                        //TODO: handle the error properly
                        mPreviewImageView.setImageBitmap(/*R.drawable.image_load_error*/
                                null);
                    }
                }
        );

        VolleyWrapper.getInstance(getActivity())
                .addToRequestQueue(mImageRequest);


    }

    // preview image was displayed properly - expose the answers and start the countdown
    private void onImageDisplayed(final FeedItem feedItem) {

        int length = mArrayOfAnswerViews!=null ? mArrayOfAnswerViews.length : 0;
        for (int i = 0; i < length; i++) {

            mArrayOfAnswerViews[i].setText(feedItem.getHeadlines()[ i ]);
        }

        getView().postDelayed(mCountDownRunnable, 1000);

    }
}
