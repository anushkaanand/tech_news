package com.example.muthuraman.techienews;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Muthuraman on 7/25/2016.
 */
public class Download extends AsyncTask<String,Void,Bitmap>
{
    @Override
    protected Bitmap doInBackground(String... strings) {
        try {
            Bitmap result1 = null;
            String query = strings[0];
            //String bingURL = "https://pixabay.com/api/?key=2977535-019a722eff64256f5e7bacc5c&q="+ URLEncoder.encode(query, "UTF-8")+"&image_type=photo";
            String result = "";
            String key="AIzaSyB-yb4aKe0-qxZnq3RFjckSFDyz41Nm-2o";
            URL url = new URL(
                    "https://www.googleapis.com/customsearch/v1?key="+key+ "&cx=000657771731621450584:bgt17ps-dtu&q="+ URLEncoder.encode(query, "UTF-8") + "&searchType=image");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //conn.setRequestProperty("Accept", "application/json");
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader b = new BufferedReader(isr);
            String line="";
            Log.e("my error", result);
            //while((line=b.readLine())!=null)
            //result+=line;
            final StringBuilder response = new StringBuilder();
            while ((line = b.readLine()) != null)
                response.append(line);
            result = response.toString();
            Log.i("val",result);
            Pattern p = Pattern.compile("\"link\": \"(.*?)\",");
            Matcher m = p.matcher(result);
            m.find();
            URL url1 = new URL(m.group(1));
            HttpURLConnection urlConnection1 = (HttpURLConnection) url1.openConnection();
            InputStream in = urlConnection1.getInputStream();
            result1 = BitmapFactory.decodeStream(in);
            return result1;

        } catch (MalformedURLException e) {
            Log.e("my error","Mal");
            e.printStackTrace();

        } catch (Exception e) {
            Log.e("my error","IO");
            e.printStackTrace();

        }

        return null;
    }
}
