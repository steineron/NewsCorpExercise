package com.newscorp.feeder.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.newscorp.feeder.R;
import com.newscorp.feeder.model.GetFeedService;
import com.newscorp.feeder.model.QuizFeedItem;


public class QuizResultFragment extends Fragment {


    private final QuizFeedItem mQuizFeedItem;

    private final QuizResult mQuizResult;

    @SuppressLint("ValidFragment")
    public QuizResultFragment(final QuizFeedItem item, final QuizResult quizResult) {
        mQuizFeedItem = item;
        mQuizResult = quizResult;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_quiz_result, container, false);

        Button getNext = (Button) rootView.findViewById(R.id.get_next_button);
        getNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {

                Activity activity = getActivity();
                activity.startService(GetFeedService.createNextQuizItemIntent(activity));
            }
        });

        TextView title = (TextView) rootView.findViewById(R.id.result_title);
        TextView standFirst = (TextView) rootView.findViewById(R.id.result_stand_first);
        standFirst.setText(mQuizFeedItem.getStandFirst());

        title.setText(mQuizFeedItem.getHeadlines()[mQuizFeedItem.getCorrectAnswerIndex()]);
        TextView score = (TextView) rootView.findViewById(R.id.score_text);
            String scoreText;
        int color;
        if(mQuizResult.isSuccess()){
            scoreText= getResources().getString(R.string.quiz_score_success, mQuizResult.getScore());
            color = getResources().getColor(R.color.success);
        }
        else{

            scoreText= getResources().getString(R.string.quiz_score_fail);
            color = getResources().getColor(R.color.fail);
        }
        score.setBackgroundColor(color);
        score.setText(scoreText);
        return rootView;
    }



}
