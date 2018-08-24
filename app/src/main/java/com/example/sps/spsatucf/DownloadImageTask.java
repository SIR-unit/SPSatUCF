package com.example.sps.spsatucf;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImageTask  extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;

        Log.d("LOADING","Url: " + urldisplay);
        try {
            URL url = new URL(urldisplay);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setConnectTimeout(30000);
            con.setReadTimeout(30000);
            InputStream in = con.getInputStream();

            //InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
            if (in == null)
                Log.e("ERROR", "null input stream");
            in.close();
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        if (result == null)
            Log.e("ERROR", "Null Bitmap Result");
        bmImage.setImageBitmap(result);
        Log.d("SETTINGIMAGE", "We made it");
    }
}