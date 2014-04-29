package com.travelpoker.app;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by patriciaestridge on 4/14/14.
 */
public class DeckGalleryFragment extends Fragment {
    GridView mGridView;
    ArrayList<DeckGalleryItem> mItems;
    private static final String TAG = "DeckGalleryFragment";
    DeckThumbnailDownloader<ImageView> mThumbnailThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        new FetchItemsTask().execute();

        mThumbnailThread = new DeckThumbnailDownloader<ImageView>(new Handler());
        mThumbnailThread.setListener(new DeckThumbnailDownloader.Listener<ImageView>() {
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if (isVisible()) {
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View v = inflater.inflate(R.layout.fragment_main, container, false);

        mGridView = (GridView)v.findViewById(R.id.gridView);
        setupAdapter();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailThread.clearQueue();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailThread.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    void setupAdapter() {
        if (getActivity() == null || mGridView == null) return;

        if (mItems != null) {
            mGridView.setAdapter(new DeckItemAdapter(mItems));
        } else {
            mGridView.setAdapter(null);
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<DeckGalleryItem>> {
        @Override
        protected ArrayList<DeckGalleryItem> doInBackground(Void... params) {
            return new ApiFetcher().getItems();
        }

        @Override
        protected void onPostExecute(ArrayList<DeckGalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }

    private class DeckItemAdapter extends  ArrayAdapter<DeckGalleryItem> {
        public DeckItemAdapter(ArrayList<DeckGalleryItem> items) {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.deck_item, parent, false);
            }

            ImageView imageView = (ImageView)convertView.findViewById(R.id.deck_item_imageView);
            DeckGalleryItem item = getItem(position);
            mThumbnailThread.queueThumbnail(imageView, item.getUrl());

            return convertView;
        }
    }
}
