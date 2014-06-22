package com.travelpoker.app.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.travelpoker.app.R;
import com.travelpoker.app.fetchers.DeckPageApiFetcher;
import com.travelpoker.app.fetchers.ThumbnailDownloader;
import com.travelpoker.app.objects.CardGalleryItem;

import java.util.ArrayList;

/**
 * Created by patriciaestridge on 5/21/14.
 */
public class DeckFragmentActivity extends FragmentActivity {
    GridView mGridView;
    LinearLayout mLinearLayout;
    TextView mDescription;
    private String deckUrl;
    private String deckDescription;
    ArrayList<CardGalleryItem> mItems;
    private static final String TAG = "DeckFragment";
    ThumbnailDownloader<ImageView> mThumbnailThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deck_fragment);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        String description = intent.getStringExtra("description");

        deckUrl = url;
        deckDescription = description;

        mLinearLayout = (LinearLayout) findViewById(R.id.deckFragment);
        mDescription = (TextView) findViewById(R.id.deckDescription);
        mDescription.setText(deckDescription);
        mGridView = (GridView) findViewById(R.id.deckCardGridView);

        setupAdapter();
        setListeners();

        new FetchItemsTask().execute();

        mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
               imageView.setImageBitmap(thumbnail);
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
    }

    public void setListeners() {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> gridView, View view, int pos, long id) {
                CardGalleryItem item = mItems.get(pos);

                String cardUri = Uri.parse(item.getCardPageUrl()).toString();
                String cardDescription = Uri.parse(item.getDescription()).toString();
                String cardLatitude = Uri.parse(item.getLatitude()).toString();
                String cardLongitude = Uri.parse(item.getLongitude()).toString();

                Intent i = new Intent(DeckFragmentActivity.this,CardFragmentActivity.class);
                i.putExtra("url", cardUri);
                i.putExtra("description", cardDescription);
                i.putExtra("longitude", cardLongitude);
                i.putExtra("latitude", cardLatitude);

                startActivity(i);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailThread.clearQueue();
        mThumbnailThread.quit();
    }

    void setupAdapter() {
        if (DeckFragmentActivity.this == null || mGridView == null) return;

        if (mItems != null) {
            mGridView.setAdapter(new CardItemAdapter(mItems));
            int px = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240, getResources().getDisplayMetrics()));
            int items = mItems.size();
            int h = mGridView.getNumColumns();

            ViewGroup.LayoutParams params = mGridView.getLayoutParams();
            params.height = items*px/h;

            createMapItems(mItems);
        } else {
            mGridView.setAdapter(null);
        }
    }

    private void createMapItems(ArrayList<CardGalleryItem> mItems) {
        GoogleMap mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.deckMap)).getMap();

        for (int i = 0; i < mItems.size(); i++) {
            CardGalleryItem item = mItems.get(i);
            String longString = item.getLongitude();
            double cardLongitude = Double.parseDouble(longString);

            String latString = item.getLatitude();
            double cardLatitude = Double.parseDouble(latString);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(cardLatitude, cardLongitude), 11));

            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.sightseeing))
                    .anchor(0.0f, 1.0f)
                    .position(new LatLng(cardLatitude, cardLongitude)));
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<CardGalleryItem>> {
        @Override
        protected ArrayList<CardGalleryItem> doInBackground(Void... params) {
            return new DeckPageApiFetcher().getItems(deckUrl);
        }

        @Override
        protected void onPostExecute(ArrayList<CardGalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }

    private class CardItemAdapter extends ArrayAdapter<CardGalleryItem> {
        public CardItemAdapter(ArrayList<CardGalleryItem> items) {
            super(DeckFragmentActivity.this, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = DeckFragmentActivity.this.getLayoutInflater().inflate(R.layout.thumbnail_item, parent, false);
            }

            CardGalleryItem item = getItem(position);

            TextView textView = (TextView) convertView.findViewById(R.id.deckTitle);
            textView.setText(item.getTitle());

            ImageView imageView = (ImageView) convertView.findViewById(R.id.deck_item_imageView);
            mThumbnailThread.queueThumbnail(imageView, item.getUrl());

            return convertView;
        }
    }

}
