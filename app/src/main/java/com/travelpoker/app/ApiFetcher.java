package com.travelpoker.app;

import android.net.Uri;
import android.util.Log;

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
 * Created by patriciaestridge on 4/14/14.
 */
public class ApiFetcher {
    public static final String TAG = "ApiFetcher";
    private static final String ns = null;
    private static final String ENDPOINT = "http://www.travelpoker.co/decks.xml";

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

    public ArrayList<DeckGalleryItem> getItems() {
        ArrayList<DeckGalleryItem> items = new ArrayList<DeckGalleryItem>();
        try{
            String url = Uri.parse(ENDPOINT).toString();
            String xmlString = getUrl(url);
            Log.i(TAG, "Received xml: " + xmlString.length());

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            Log.i("parser", " "+parser);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(xmlString));
            parseItems(items, parser);
        } catch (IOException ioe) {
            Log.e(TAG, "FAIL", ioe);
        } catch (XmlPullParserException xppe) {
            Log.e(TAG, "FAIL in parse Items", xppe);
        }
        return items;
    }

    void parseItems(ArrayList<DeckGalleryItem> items, XmlPullParser parser)
        throws XmlPullParserException, IOException {

        int eventType = parser.nextTag();

        parser.require(XmlPullParser.START_TAG, ns,"decks");
        while (parser.nextTag() != XmlPullParser.END_TAG) {
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("deck")) {
                items.add(readDeck(parser));
            } else {
                skip(parser);
                Log.i("parser skipped", "");
            }
        }
    }

    private DeckGalleryItem readDeck(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "deck");

        String title = null;
        String id = null;
        String mobile = null;

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
            }  else {
                skip(parser);
            }
        }
        DeckGalleryItem item = new DeckGalleryItem();
        item.setTitle(title);
        item.setId(id);
        item.setUrl(mobile);
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
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "id");
        return title;
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
        Log.i("ReadText", "gets called");
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
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
