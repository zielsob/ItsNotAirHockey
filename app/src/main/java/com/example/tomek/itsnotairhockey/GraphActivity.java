package com.example.tomek.itsnotairhockey;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import ca.uol.aig.fftpack.RealDoubleFFT;

//---------
//--------

public class GraphActivity extends AppCompatActivity implements OnClickListener {

    MenuItem item;

    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private RealDoubleFFT transformer;
    int blockSize = 256;

    Button startStopButton;
    boolean started = false;

    RecordAudio recordTask;

    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;

    private TextView frequency_text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        startStopButton = (Button) this.findViewById(R.id.StartStopButton);
        startStopButton.setOnClickListener(this);

        transformer = new RealDoubleFFT(blockSize);

        imageView = (ImageView) this.findViewById(R.id.ImageView01);
        bitmap = Bitmap.createBitmap((int) 256, (int) 300, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        imageView.setImageBitmap(bitmap);

        frequency_text = (TextView)findViewById(R.id.frequency_text);
    }

    public class RecordAudio extends AsyncTask<Void, double[], Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
                short[] buffer = new short[blockSize];
                double[] toTransform = new double[blockSize];
                audioRecord.startRecording();
                while (started) {
                    int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0;
                    }
                    transformer.ft(toTransform);
                    publishProgress(toTransform);
                }
                audioRecord.stop();
            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(double[]... toTransform) {
            canvas.drawColor(Color.BLACK);
            for (int i = 0; i < toTransform[0].length; i++) {
                int x = i;
                int downy = (int) (150 - (toTransform[0][i] * 10));
                int upy = 150;
                canvas.drawLine(x, downy, x, upy, paint);
            }
            imageView.invalidate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    public void onClick(View arg0) {
        if (started) {
            started = false;
            startStopButton.setText("Start");
            recordTask.cancel(true);
        } else {
            started = true;
            startStopButton.setText("Stop");
            recordTask = new RecordAudio();
            recordTask.execute();
        }
    }
/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.item = item;
        if (item.getItemId() == R.id.main_activity) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.quit) System.exit(0);
        return super.onOptionsItemSelected(item);
    }*/
    //---------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Log.i(TAG, item.toString());
        switch (item.getItemId()) {
            case R.id.main_activity:
                Intent intent = new Intent(this, MainActivity.class);
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
    //---------------------

    @Override
    protected void onPause() {
        super.onPause();
        started = false;
        recordTask.cancel(true);
        finish();
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
}