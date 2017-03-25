package com.example.muthuraman.techienews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity{
    String result1;
    ArrayList<String> news;
    ArrayList<String> link;
    ArrayList<Bitmap> img;
    SQLiteDatabase db;
    Intent i;
    int num;
    ListView listView;
    boolean flag = true;
    SwipeRefreshLayout swipeRefreshLayout;
    CustomAdapter cadapter;
    SharedPreferences sharedPreferences;
    StoreInDb x;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //this.deleteDatabase("TechieNews");

        db = this.openOrCreateDatabase("TechieNews" , MODE_PRIVATE , null);

        db.setVersion(1);
        db.setLocale(Locale.getDefault());
         Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        news = new ArrayList<String>();
        link = new ArrayList<String>();
        img = new ArrayList<Bitmap>();
        sharedPreferences = this.getSharedPreferences(getPackageName(),MODE_PRIVATE);
        //sharedPreferences.edit().putInt("num",0).commit();
        num = sharedPreferences.getInt("num", -1);
        Log.e("val" , Integer.toString(num));
        if(num==-1)
        {
            this.deleteDatabase("TechieNews");
            db = this.openOrCreateDatabase("TechieNews" , MODE_PRIVATE , null);
            db.setVersion(1);
            db.setLocale(Locale.getDefault());
            num=0;
            sharedPreferences.edit().putInt("num",num).commit();
        }
        listView = (ListView) findViewById(R.id.listView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setDistanceToTriggerSync(50);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        listView.setAdapter(cadapter);
        flag=true;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), webact.class);
                intent.putExtra("url", link.get(i));
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
               try{ db.execSQL("DELETE FROM Newss WHERE id IN (SELECT id FROM Newss WHERE news = '" +    news.get(position) +"')");
                num--;
                populate();}catch(Exception e){e.printStackTrace();return false;}
                return true;
            }
        });
        i = new Intent(this,Splash.class);
        if(doesDatabaseExist())
        {
            populate();
        }
        else
        {
          i.putExtra("time", 5000);
            refresh();

        }
        startActivity(i);
    }
    private  boolean doesDatabaseExist() {
        try {
            if(!db.isDatabaseIntegrityOk())
            {
                db.execSQL("DROP TABLE Newss");
            }
            db.execSQL("CREATE TABLE IF NOT EXISTS  Newss (id INTEGER UNIQUE,news VARCHAR,image BLOB,link VARCHAR)");
            Cursor cur = db.rawQuery("SELECT COUNT(*) FROM Newss", null);
            if (cur != null) {
                cur.moveToFirst();
                Log.e("g",Integer.toString(cur.getInt(0)));// Always one row returned.
                if (cur.getInt(0) == 0)
                    return false;
                else return true;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    void populate()
    {
        news.clear();
        link.clear();
        img.clear();
        swipeRefreshLayout.setRefreshing(false);
        Log.e("populate" , "once");
        Cursor c = db.rawQuery("SELECT * FROM Newss ORDER BY id DESC",null);
        try {
            c.moveToFirst();
            int newsIndex = c.getColumnIndex("news");
            int linkIndex = c.getColumnIndex("link");
            int imageIndex = c.getColumnIndex("image");
            while (true) {
                news.add(c.getString(newsIndex));
                link.add(c.getString(linkIndex));
                img.add(getImage(c.getBlob(imageIndex)));
                if (!c.isLast())
                    c.moveToNext();
                else break;
            }
            c.close();

        }catch(Exception w){w.printStackTrace();}
        cadapter = new CustomAdapter(this, news.toArray(new String[0]),link.toArray(new String[0]),img.toArray(new Bitmap[0]));
        listView.setAdapter(cadapter);
        Toast.makeText(getApplicationContext(), "More news loaded!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    String res(String url)
    {
        String result="";
        try {
            result = new getcontent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void refresh() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        while(!( connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()))
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("No Internet Connection!").setMessage("Please connect to internet and try again").setPositiveButton("retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
        }
        Toast.makeText(getApplicationContext(), "Refreshing", Toast.LENGTH_LONG).show();
        swipeRefreshLayout.setRefreshing(true);
        result1 = res("https://hacker-news.firebaseio.com/v0/topstories.json");
        String s ="";
        for(int i=1;i<result1.length()-1;i++)
            s=s+result1.charAt(i);
        s=s+",";
        result1 = s;
        flag=false;
        Log.e("id:", result1);
        if(x!=null && x.getStatus()!= AsyncTask.Status.FINISHED)
        {
            x.cancel(true);
            while(x.isCancelled());
        }
        x = new StoreInDb();
        x.execute();
    }


    class StoreInDb extends AsyncTask<Void,Void,Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... voids) {

            int id;
            int x=0,d=0;
            String news = "", link = "";
            byte[] b;
            db.execSQL("CREATE TABLE IF NOT EXISTS  Newss (id INTEGER UNIQUE,news VARCHAR,image BLOB,link VARCHAR)");
            List<String> numbers = Arrays.asList(result1.split(","));
            Log.e("id:", numbers.toString());
            while (x++<numbers.size()&&d<20) {
                id = Integer.parseInt(numbers.get(x-1));
                if(CheckIsDataAlreadyInDBorNot("Newss" , "id" , id))
                    continue;
                String url = "https://hacker-news.firebaseio.com/v0/item/" + id + ".json";
                String ans = res(url);
                try {
                    JSONObject jsonObject = new JSONObject(ans);
                    news = jsonObject.getString("title");
                    link = jsonObject.getString("url");
                    news = news.replace("'", "|");
                    link = link.replace("'","|");
                    b =  getBytes(call(news));
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                String sql ="INSERT INTO Newss (id , news , image , link ) VALUES (?,?,?,?)";
                SQLiteStatement insertStmt      =   db.compileStatement(sql);
                insertStmt.clearBindings();
                insertStmt.bindLong(1, id);
                insertStmt.bindString(2, news);
                insertStmt.bindBlob(3, b);
                insertStmt.bindString(4,link);
                insertStmt.executeInsert();
                num++;d++;
                sharedPreferences.edit().putInt("num", num).commit();
                if(num>20)
                {
                    db.execSQL("DELETE FROM Newss WHERE id NOT IN (SELECT id FROM Newss ORDER BY id DESC LIMIT "+16+" )");
                    num=16;
                    sharedPreferences.edit().putInt("num", num).commit();
                    d=16;
                }
                if(d%2==0)
                publishProgress();

            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            sharedPreferences.edit().putInt("num",num).commit();
            populate();
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            populate();
            super.onPostExecute(aBoolean);
        }
    }

Bitmap call(String news)
{
    Bitmap m =  BitmapFactory.decodeResource(this.getResources(), R.drawable.dialogwarning); ;
   try {
        if((m =(new Download()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, news).get())==null)
            m = BitmapFactory.decodeResource(this.getResources(), R.drawable.dialogwarning);
    } catch (InterruptedException e) {
        e.printStackTrace();
    } catch (ExecutionException e) {
        e.printStackTrace();
    }

    return  m;
}

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public  boolean CheckIsDataAlreadyInDBorNot(String TableName,String dbfield, int fieldValue) {
        String Query = "Select * from " + TableName + " where " + dbfield + " = " + fieldValue;
        Log.e("checkdataexists" , "once");
        Cursor cursor = db.rawQuery(Query, null);
        try {
            if (cursor.getCount() <= 0) {
                cursor.close();
                return false;
            }
        }
        catch (Exception w)
        {
            w.printStackTrace();
            cursor.close();
            return true;
        }
        cursor.close();
        return true;
    }

}
