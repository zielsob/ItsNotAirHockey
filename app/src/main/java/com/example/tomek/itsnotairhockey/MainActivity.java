package com.example.tomek.itsnotairhockey;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v7.app.ActionBarActivity;
//---------------------
import android.os.Handler;
//---------------------

public class MainActivity extends AppCompatActivity {
    int volume;
    private SoundMeter mSensor;
    MenuItem item;
    //--------------------
    private Handler handler;
    //--------------------

    private static final int PROGRESS = 0x1;
    private ProgressBar  mProgress;
    private int mProgressStatus = 0;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgress = (ProgressBar) findViewById(R.id.progressBar);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){


                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgress.setProgress(volume);
                        }
                    });


                }
            }
        }).start();



    }

    @Override
    protected void onStart() {
        super.onStart();

        mSensor = new SoundMeter();
        try {
            mSensor.start();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //-------------------------------------------------------
        handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                //mSensor.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                         volume = (int)(20 * Math.log10(mSensor.getTheAmplitude() / 51805 / 0.00002));
                        if (volume >= 0) {
                            updateTextView(R.id.volumeLevel, "Volume: " + String.valueOf(volume) + "[dB]");
                        }
                        handler.postDelayed(this, 100); // amount of delay between every cycle of volume level detection
                    }
                });
            }
        };
        handler.postDelayed(r, 250);    // NECESSARY -  w/o the loop never runs this tells Java to run "r"
        //-------------------------------------------------------
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensor.stop();//--------------------------
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.item = item;
        if (item.getItemId() == R.id.graph_activity) {
            Intent intent = new Intent(this, GraphActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.quit) {
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateTextView(int text_id, String toThis) {
        TextView val = (TextView) findViewById(text_id);
        val.setText(toThis);
        return;
    }
}
