package com.newscorp.feeder.controller;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
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
            getActivity().startService(GetFeedService.createNextQuizItemIntent(getActivity()));
        }
    }

    @Override
    public void onPause() {

        try {
            getActivity().unregisterReceiver(mFeedReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        mAnswerButton1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {

                onAnswer(0);
            }
        });
        mAnswerButton2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {

                onAnswer(1);
            }
        });
        mAnswerButton3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {

                onAnswer(2);
            }
        });
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void revealView(View view) {
        // this groovy effect requires Android 5.0 and above

        if (view != null) {
            float radius =
                    Math.max(view.getMeasuredHeight(), view.getMeasuredWidth());

            Animator reveal = ViewAnimationUtils.createCircularReveal(
                    view,           // The  View to reveal
                    Math.round(
                            radius *
                                    0.5f),      // x to start the mask from - start from the middle
                    Math.round(
                            radius),      // y to start the mask from - start from the bottom
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
                mPointsLostOnErrors += mWrongAnswersPenalties[ answerIndex ];
                mWrongAnswersPenalties[ answerIndex ] = 0;

            }
            else {
                onQuizEnded();
            }
        }

    }


    private void setProgress(final int quizTimeSeconds) {

        if (mProgressTextView != null) {
            mProgressTextView.setText(getResources().getString(R.string.quiz_progress_text_remaining, calculatePointsRemaining()));
        }

        if (mQuizProgressBar != null) {
            mQuizProgressBar.setProgress(mQuizTimeSeconds);
        }
    }

    private int calculatePointsRemaining() {

        return mQuizTimeSeconds - mPointsLostOnErrors;
    }

    private void onQuizEnded() {


    }

    private void resetQuiz() {

        mPointsLostOnErrors = 0;
        mQuizTimeSeconds = getResources().getInteger(R.integer.quiz_time);
        mWrongAnswersPenalties[ 0 ] = 2;
        mWrongAnswersPenalties[ 1 ] = 2;
        mWrongAnswersPenalties[ 2 ] = 2;
        setProgress(mQuizTimeSeconds);

        mCountDownRunnable = new Runnable() {

            @Override
            public void run() {

                if (this != mCountDownRunnable) {
                    return; // 'this' shouldn't be running
                }
                if (calculatePointsRemaining()<=0) {
                    onQuizEnded();
                }
                else {
                    setProgress(mQuizTimeSeconds);
                    mQuizTimeSeconds--;
                    getView().postDelayed(this, 1000);
                }
            }
        };
    }


    private void getPreviewImageForFeedItem(final FeedItem feedItem) {

        ImageRequest imageRequest = new ImageRequest(feedItem.getImageUrl(),
                new Response.Listener<Bitmap>() {

                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onResponse(Bitmap bitmap) {

                        mPreviewImageView.setVisibility(View.VISIBLE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                                mPreviewImageView.isAttachedToWindow()) {
                            revealView(mPreviewImageView);
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
                .addToRequestQueue(imageRequest);


    }

    // preview image was displayed properly - expose the answers and start the countdown
    private void onImageDisplayed(final FeedItem feedItem) {

        mAnswerButton1.setText(feedItem.getHeadlines()[ 0 ]);
        mAnswerButton2.setText(feedItem.getHeadlines()[ 1 ]);
        mAnswerButton3.setText(feedItem.getHeadlines()[ 2 ]);

        getView().postDelayed(mCountDownRunnable, 1000);

    }
}
