package com.example.muthuraman.techienews;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Muthuraman on 8/3/2016.
 */
public class getcontent extends AsyncTask<String , Void , String>
{
    @Override
    protected String doInBackground(String... strings) {
        URL url;
        String result = "" , line="";
        HttpURLConnection urlConnection;

        try {
            url = new URL(strings[0]);
            urlConnection= (HttpURLConnection) url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((InputStream) urlConnection.getInputStream()));
            while((line = bufferedReader.readLine())!=null)
            {
                result+=line;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
