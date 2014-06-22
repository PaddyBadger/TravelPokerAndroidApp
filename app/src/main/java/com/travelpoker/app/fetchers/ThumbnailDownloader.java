package com.travelpoker.app.fetchers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by patriciaestridge on 4/22/14.
 */
public class ThumbnailDownloader<Token> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private LruCache<String, Bitmap> mMemoryCache;
    Handler mHandler;
    Handler mResponseHandler;
    Listener<Token> mListener;

    Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());

    public interface Listener<Token> {
        void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener) {
        mListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    Token token = (Token)msg.obj;
                    handleRequest(token);
                }
            }
        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void queueThumbnail(Token token, String url) {

        requestMap.put(token, url);

        mHandler
                .obtainMessage(MESSAGE_DOWNLOAD, token)
                .sendToTarget();
    }

    private void handleRequest(final Token token) {
        try {
            final String url = requestMap.get(token);

            if (url == null)
                return;

            final Bitmap bitmapCache = getBitmapFromMemCache(url);
            final Bitmap bitmap;

            if (bitmapCache == null) {
                byte[] bitmapBytes = new HomePageApiFetcher().getUrlBytes(url);
                bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                addBitmapToMemoryCache(url, bitmap);
            } else {
                bitmap = bitmapCache;
            }

            mResponseHandler.post(new Runnable() {
                public void run() {
                    if (requestMap.get(token) != url)
                        return;

                    requestMap.remove(token);
                    mListener.onThumbnailDownloaded(token, bitmap);
                }
            });

        } catch (IOException ioe) {
            Log.e(TAG, "Error in image download");
        }
    }

    public void clearQueue() {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }
}
