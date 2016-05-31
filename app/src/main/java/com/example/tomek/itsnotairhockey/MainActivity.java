package com.example.tomek.itsnotairhockey;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    int volume;
    private SoundMeter mSensor;
    private Handler handler;
    private ProgressBar  mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mSensor = new SoundMeter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        decibelMeter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensor.stop();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.graph_activity:
                Intent intent = new Intent(this, GraphActivity.class);
                startActivity(intent);
                return true;
            case R.id.quit:
                System.exit(0);
            case R.id.info:
                showInstructions();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //----------------
    public void updateTextView(int text_id, String toThis) {
        TextView val = (TextView) findViewById(text_id);
        val.setText(toThis);
        return;
    }

    public void showInstructions() {
        TextView tv = new TextView(this);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setText(Html.fromHtml(getString(R.string.instructions_text)));
        new AlertDialog.Builder(this)
                .setTitle(R.string.instructions_title)
                .setView(tv)
                .setNegativeButton(R.string.dismiss, null)
                .create().show();
    }

    public void decibelMeter(){
        try {
            mSensor.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        volume = (int)(20 * Math.log10(mSensor.getTheAmplitude() / 51805 / 0.00002));
                        if (volume >= 0) {
                            updateTextView(R.id.volumeLevel, "Volume: " + String.valueOf(volume) + "[dB]");
                            mProgress.setProgress(volume);
                        }
                        handler.postDelayed(this, 100);
                    }
                });
            }
        };
        handler.postDelayed(r, 250);
    }
}
