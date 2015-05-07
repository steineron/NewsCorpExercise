package com.newscorp.feeder.controller;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.newscorp.feeder.R;
import com.newscorp.feeder.model.QuizFeedItem;

import auto.parcel.AutoParcel;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuizFragment extends QuizBaseFragment implements OnQuizItemImageReadyListener {

    private TextView mProgressTextView;

    private ProgressBar mQuizProgressBar;

    private Runnable mCountDownRunnable;

    private int mQuizTimeSeconds;

    private int mPointsLostOnErrors;

    int[] mWrongAnswersPenalties = new int[ 3 ]; // how many point can be lost for each wrong answer

    private Button[] mArrayOfAnswerViews;

    private BroadcastReceiver mQuizImageReadyReceiver;


    public QuizFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_quiz, container, false);
        mProgressTextView = (TextView) rootView.findViewById(R.id.quiz_progress_text);
        mQuizProgressBar = (ProgressBar) rootView.findViewById(R.id.quiz_remaining_time_progress);

        mArrayOfAnswerViews = new Button[ 3 ];
        mArrayOfAnswerViews[ 0 ] = (Button) rootView.findViewById(R.id.quiz_answer_1);;
        mArrayOfAnswerViews[ 1 ] = (Button) rootView.findViewById(R.id.quiz_answer_2);;
        mArrayOfAnswerViews[ 2 ] = (Button) rootView.findViewById(R.id.quiz_answer_3);;

        for (int i = 0; i < mArrayOfAnswerViews.length; i++) {

            final int answer = i;
            mArrayOfAnswerViews[ i ].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {

                    onAnswer(answer, getQuizFeedItem());
                }
            });
        }

        return rootView;
    }


    @Override
    public void onResume() {

        super.onResume();
        mQuizImageReadyReceiver = ControllerFacade.registerOnQuizItemImageReadyListener(getActivity(),this);
    }

    @Override
    public void onPause() {

        super.onPause();
        if(mQuizImageReadyReceiver!=null){
            try {
                getActivity().unregisterReceiver(mQuizImageReadyReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onAnswer(final int answerIndex, final QuizFeedItem quizFeedItem) {

        if (quizFeedItem != null) {
            if (answerIndex != quizFeedItem.getCorrectAnswerIndex()) {
                computeWrongAnswerPenalty(answerIndex);
                if (calculatePointsRemaining() <= 0) {
                    onQuizEnded(QuizResult.QuizTermination.ZERO_POINTS);
                }
            }
            else {
                onQuizEnded(QuizResult.QuizTermination.USER_ANSWERED);
            }
        }

    }


    private void computeWrongAnswerPenalty(final int answerIndex) {

        mPointsLostOnErrors += mWrongAnswersPenalties[ answerIndex ];
        mWrongAnswersPenalties[ answerIndex ] = 0;
    }


    private void updateQuizProgress() {

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

    private void onQuizEnded(final QuizResult.QuizTermination termination) {


        synchronized (this) {
            mCountDownRunnable = null;
        }
//        resetQuiz();

        for (int i = 0; i < mArrayOfAnswerViews.length; i++) {
            mArrayOfAnswerViews[ i ].setVisibility(View.INVISIBLE);
        }
        mArrayOfAnswerViews[ getQuizFeedItem().getCorrectAnswerIndex() ].setVisibility(View.VISIBLE);

        boolean isSuccess = termination == QuizResult.QuizTermination.USER_ANSWERED;
        getActivity().sendBroadcast(
                ControllerFacade
                        .createOnQuizEndedIntent(
                                QuizResultImpl.create(isSuccess, termination)));
    }


    @Override
    protected void cancelQuiz() {

        synchronized (this) {
            mCountDownRunnable = null;
        }
    }

    @Override
    protected void resetQuiz() {

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
        updateQuizProgress();

        synchronized (this) {
            mCountDownRunnable = new Runnable() {

                @Override
                public void run() {

                    if (this != mCountDownRunnable) {
                        return; // 'this' shouldn't be running
                    }
                    updateQuizProgress();
                    if (calculatePointsRemaining() <= 0) {
                        onQuizEnded(QuizResult.QuizTermination.ZERO_POINTS);
                    }
                    else {
                        mQuizTimeSeconds--;
                        getView().postDelayed(this, 1000);
                    }
                }
            };
        }
    }


    @Override
    public void onImageReady(final Context context) {

        QuizFeedItem quizFeedItem = getQuizFeedItem();
        int length = mArrayOfAnswerViews != null && quizFeedItem != null ?
                     mArrayOfAnswerViews.length :
                     0;
        for (int i = 0; i < length; i++) {

            mArrayOfAnswerViews[ i ].setText(quizFeedItem.getHeadlines()[ i ]);
        }

        getView().postDelayed(mCountDownRunnable, 1000);

    }

    @AutoParcel
    static abstract class QuizResultImpl implements QuizResult {

        static QuizResultImpl create(
                boolean isSuccess,
                @Nullable QuizTermination quizTermination) {

            return new AutoParcel_QuizFragment_QuizResultImpl(isSuccess, quizTermination);
        }
    }
}
