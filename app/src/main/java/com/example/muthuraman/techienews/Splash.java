package com.example.muthuraman.techienews;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
              //  Intent mainIntent = new Intent(Splash.this, MainActivity.class);
              //  Splash.this.startActivity(mainIntent);
                Splash.this.finish();
            }
        },getIntent().getIntExtra("time",1000));
    }
}
