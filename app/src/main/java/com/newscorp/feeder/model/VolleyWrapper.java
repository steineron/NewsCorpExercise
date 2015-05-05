package com.newscorp.feeder.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by ron on 4/10/15.
 */
public class VolleyWrapper {

    private static final String TAG = VolleyWrapper.class.getSimpleName();

    public static final int MAX_SIZE = 8 * 1024 * 1024; // 8MiB;

    private static VolleyWrapper mInstance;

    private RequestQueue mRequestQueue;

    private ImageLoader mImageLoader;

    private static Context mCtx;

    private VolleyWrapper(Context context) {

        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {

                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(MAX_SIZE);

                    @Override
                    public Bitmap getBitmap(String url) {

                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {

                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized VolleyWrapper getInstance(Context context) {

        synchronized (TAG) {
            if (mInstance == null) {
                mInstance = new VolleyWrapper(context);
            }
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {

        synchronized (TAG) {
            if (mRequestQueue == null) {
                // getApplicationContext() is key, it keeps you from leaking the
                // Activity or BroadcastReceiver if someone passes one in.
                mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext(), 2 * MAX_SIZE);
            }
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {

        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {

        return mImageLoader;
    }

}
