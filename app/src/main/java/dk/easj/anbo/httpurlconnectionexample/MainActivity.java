package dk.easj.anbo.httpurlconnectionexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "SHIT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        DownloadImageTask imageTask = new DownloadImageTask();
        imageTask.execute("http://anbo-easj.dk/cv/andersBorjesson.jpg");

        DownloadTextTask textTask = new DownloadTextTask();
        textTask.execute("http://anbo-easj.dk/");
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                return downloadImage(urls[0]);
            } catch (IOException ex) {
                Log.e(LOG_TAG, "DownloadImageTask: " + ex.toString());
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView img = findViewById(R.id.mainImageView);
            img.setImageBitmap(bitmap);
        }

        @Override
        protected void onCancelled(Bitmap bitmap) { // parameter is null
            super.onCancelled(bitmap);
            TextView view = findViewById(R.id.main_textview_imageerror);
            view.setText("Problem downloading image");
        }
    }

    private Bitmap downloadImage(String urlString) throws IOException {
        InputStream in = openHttpConnection(urlString);
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        return bitmap;
    }

    private class DownloadTextTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadText(urls[0]);
            } catch (IOException ex) {
                Log.e(LOG_TAG, "DownloadImageTask: " + ex.toString());
                cancel(true);
                return ex.toString();
            }
        }

        @Override
        protected void onPostExecute(String text) {
            TextView view = findViewById(R.id.mainHtmlTextView);
            view.setText(text);
        }

        @Override
        protected void onCancelled(String text) {
            super.onCancelled();
            Log.e(LOG_TAG, "DownloadImageTask onCancelled");
            TextView view = findViewById(R.id.text);
            view.setText(text);
        }
    }

    private String downloadText(String url) throws IOException {
        InputStream in = openHttpConnection(url);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    private InputStream openHttpConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        if (!(conn instanceof HttpURLConnection)) {
            throw new IOException("Not an HTTP connection " + conn.getURL());
        }
        HttpURLConnection httpConn = (HttpURLConnection) conn;
        httpConn.setAllowUserInteraction(false); // No user interaction like dialogs, etc.
        httpConn.setInstanceFollowRedirects(true);    // follow redirects, response code 3xxx
        httpConn.setRequestMethod("GET");
        httpConn.connect();
        int responseCode = httpConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return httpConn.getInputStream();
        } else {
            throw new IOException("HTTP response " + responseCode + " " + httpConn.getResponseMessage());
        }
    }
}
