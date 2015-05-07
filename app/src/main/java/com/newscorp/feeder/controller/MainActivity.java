package com.newscorp.feeder.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.newscorp.feeder.R;
import com.newscorp.feeder.model.GetFeedService;
import com.newscorp.feeder.model.OnFeedItemResultListener;
import com.newscorp.feeder.model.QuizFeedItem;


public class MainActivity extends Activity implements OnFeedItemResultListener, OnQuizEndedListener {

    private QuizImageFragment mQuizImageFragment;

    private QuizFragment mQuizFragment;

    private QuizResultFragment mQuizResultFragment;

    private QuizFeedItem mQuizFeedItem;

    private BroadcastReceiver mFeedItemReceiver;

    private BroadcastReceiver mQuizEndReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            mQuizImageFragment = new QuizImageFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.quiz_image_container, mQuizImageFragment)
                    .commit();
            mQuizFragment = new QuizFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.quiz_container, mQuizFragment)
                    .commit();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        mFeedItemReceiver = GetFeedService.registerOnFeedItemResultListener(this,this);
        mQuizEndReceiver = ControllerFacade.registerOnQuizEndedListener(this,this);
        if (mQuizFeedItem == null) {
            getNextQuizItem();
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        try {
            unregisterReceiver(mFeedItemReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            unregisterReceiver(mQuizEndReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getNextQuizItem() {

        startService(GetFeedService.createNextQuizItemIntent(this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onFeedItemResult(final Context context, final QuizFeedItem item) {

        mQuizFeedItem = item;
        if(mQuizResultFragment!=null){
            getFragmentManager()
                    .beginTransaction()
                    .remove(mQuizResultFragment)
                    .commit();
            mQuizResultFragment=null;
        }
    }

    @Override
    public void onFeedItemFault(final Context context) {

    }

    @Override
    public void onQuizEnded(final Context context, final QuizFeedItem item,
                            final QuizResult quizResult) {

        mQuizResultFragment = new QuizResultFragment(item,quizResult);
        getFragmentManager().beginTransaction()
                .add(R.id.quiz_container, mQuizResultFragment)
                .commit();
    }
}
