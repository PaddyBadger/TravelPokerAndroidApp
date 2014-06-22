package com.travelpoker.app.fetchers;

import android.util.Log;

import com.travelpoker.app.objects.CardGalleryItem;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by patriciaestridge on 5/22/14.
 */
public class DeckPageApiFetcher {
    public static final String TAG = "DeckPageApiFetcher";
    private static final String ns = null;

    byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public ArrayList<CardGalleryItem> getItems(String url) {
        ArrayList<CardGalleryItem> items = new ArrayList<CardGalleryItem>();
        try{
            String xmlString = getUrl(url);

            String[] s = xmlString.split("cards");
            String start = "<cards";
            String end = "cards>";
            String cardsArray = start + s[1] + end;

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(new StringReader(cardsArray));
            parseItems(items, parser);
        } catch (IOException ioe) {
            Log.e(TAG, "FAIL", ioe);
        } catch (XmlPullParserException xppe) {
            Log.e(TAG, "FAIL in parse Items", xppe);
        }
        return items;
    }

    void parseItems(ArrayList<CardGalleryItem> items, XmlPullParser parser)
            throws XmlPullParserException, IOException {

        int eventType = parser.nextTag();

        parser.require(XmlPullParser.START_TAG, ns,"cards");
        while (parser.nextTag() != XmlPullParser.END_TAG) {
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("card")) {
                items.add(readCard(parser));
            } else {
                skip(parser);
                Log.i("parser skipped at card", "");
            }
        }
    }

    private CardGalleryItem readCard(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "card");

        String title = null;
        String id = null;
        String mobile = null;
        String description = null;
        String longitude = null;
        String latitude = null;

        while (parser.nextTag() != XmlPullParser.END_TAG) {
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("id")) {
                id = readId(parser);
            } else if (name.equals("title")) {
                title = readTitle(parser);
            } else if (name.equals("image")) {
                mobile = readImage(parser);
            } else if (name.equals("description")) {
                description = readDescription(parser);
            } else if (name.equals("longitude")) {
                longitude = readLongitude(parser);
            } else if (name.equals("latitude"))  {
                latitude = readLatitude(parser);
            }
              else
            {
                skip(parser);
            }
        }
        CardGalleryItem item = new CardGalleryItem();
        item.setTitle(title);
        item.setId(id);
        item.setUrl(mobile);
        item.setDescription(description);
        item.setLatitude(latitude);
        item.setLongitude(longitude);
        return item;
    }

    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }

    private String readId(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "id");
        String id = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "id");
        return id;
    }

    private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return description;
    }

    private String readImage(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "image");
        String mobile = null;
        while (parser.nextTag() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("url")) {
                mobile = readImageUrl(parser);
            } else {
                skip(parser);
            }
        }

        return mobile;
    }

    private String readImageUrl(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "url");
        String imageUrl = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "url");
        return imageUrl;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";

        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private String readLongitude(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "longitude");
        String longitude = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "longitude");
        return longitude;
    }

    private String readLatitude(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "latitude");
        String latitude = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "latitude");
        return latitude;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
