package com.newscorp.feeder.controller;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;

import com.newscorp.feeder.model.QuizFeedItem;
import com.newscorp.feeder.model.GetFeedService;
import com.newscorp.feeder.model.OnFeedItemResultListener;

/**
 * Created by rosteiner on 5/7/15.
 */
public abstract class QuizBaseFragment extends Fragment implements OnFeedItemResultListener {

    protected final QuizFeedItem getQuizFeedItem() {

        return mQuizFeedItem;
    }

    private QuizFeedItem mQuizFeedItem;

    private BroadcastReceiver mFeedReceiver;



    @Override
    public void onResume() {

        super.onResume();
        mFeedReceiver = GetFeedService.registerOnFeedItemResultListener(getActivity(), this);
        resetQuiz();
        onFeedItemResult(getActivity(), getQuizFeedItem());
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void revealView(View view) {
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

    @Override
    public void onFeedItemResult(final Context context, final QuizFeedItem item) {

        mQuizFeedItem = item;
        resetQuiz();
    }

    @Override
    public void onFeedItemFault(final Context context) {

    }

    protected abstract void cancelQuiz();

    protected abstract void resetQuiz();
}
