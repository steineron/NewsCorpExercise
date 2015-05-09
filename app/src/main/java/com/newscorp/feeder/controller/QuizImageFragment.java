package com.newscorp.feeder.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.newscorp.feeder.R;
import com.newscorp.feeder.model.QuizFeedItem;
import com.newscorp.feeder.model.VolleyWrapper;

/**
 * a fragment that is responsible for diplaying hte image a the quiz.
 * the fragment responds to 'new quiz item' by loading the image, and broadcasting an event that the image is ready.
 *
 * Created by rosteiner on 5/5/15.
 */
public class QuizImageFragment extends QuizBaseFragment {

    private ImageView mPreviewImageView;

    private ImageRequest mImageRequest;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_quiz_image, container, false);

        mPreviewImageView = (ImageView) rootView.findViewById(R.id.quiz_preview_image);
        return rootView;
    }

    @Override
    public void onFeedItemResult(final Context context, final QuizFeedItem item) {

        super.onFeedItemResult(context, item);
        getPreviewImageForFeedItem(item);
    }


    @Override
    public void onFeedItemFault(final Context context) {
        //TODO: cause I'm not handling a fault in this exercise....
    }

    @Override
    protected void cancelQuiz() {

        synchronized (this) {
            if (mImageRequest != null) {
                try {
                    mImageRequest.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void resetQuiz() {

    }


    private void getPreviewImageForFeedItem(final QuizFeedItem quizFeedItem) {

        if (quizFeedItem != null) {
            mImageRequest = new ImageRequest(quizFeedItem.getImageUrl(),
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
                            synchronized (QuizImageFragment.this) {
                                mImageRequest = null;
                            }
                            getActivity().sendBroadcast(
                                    ControllerFacade
                                            .createOnQuizItemImageReadyIntent(
                                                    getQuizFeedItem())
                                                       );

                        }

                    }, 0, 0, ImageView.ScaleType.CENTER_CROP, null,
                    new Response.ErrorListener() {

                        public void onErrorResponse(VolleyError error) {

                            //TODO: handle the error properly
                            mPreviewImageView.setImageBitmap(/*R.drawable.image_load_error*/
                                    null);
                            synchronized (QuizImageFragment.this) {
                                mImageRequest = null;
                            }
                        }
                    }
            );
            VolleyWrapper.getInstance(getActivity())
                    .addToRequestQueue(mImageRequest);
        }

    }


}
