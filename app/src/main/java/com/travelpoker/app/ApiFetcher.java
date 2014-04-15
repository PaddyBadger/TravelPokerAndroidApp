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

//    private static final String FIND_DECK_TITLE = "title";
    private static final String FIND_DECK = "deck";
//    private static final String FIND_DECK_ID = "title";



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
            Log.i("url", " " +url);
            String xmlString = getUrl(url);
           // Log.i(TAG, "Received xml: " + xmlString);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
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
        parser.require(XmlPullParser.START_TAG, "", "deck");

        String title = null;
        String id = null;
        String mobile = null;
        int eventType = parser.nextTag();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                title = readTitle(parser);
                Log.i("title ", "" +title);
            } else if (name.equals("id")) {
                id = readId(parser);
            } else if (name.equals("mobile")) {
                mobile = readMobile(parser);
            }  else {
                skip(parser);
            }
            DeckGalleryItem item = new DeckGalleryItem();
            item.setTitle(title);
            item.setId(id);
            item.setUrl(mobile);
            items.add(item);
        }
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

    private String readMobile(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "url");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "url");
        return title;
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
