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


public class GraphActivity extends AppCompatActivity implements OnClickListener {

    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private RealDoubleFFT realDoubleFFT;
    int dataSize = 256;

    Button buttonStartStop;
    boolean started = false;

    RecordAudio recordTask;

    ImageView graphView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        buttonStartStop = (Button) this.findViewById(R.id.buttonStartStop);
        buttonStartStop.setOnClickListener(this);

        realDoubleFFT = new RealDoubleFFT(dataSize);

        graphView = (ImageView) this.findViewById(R.id.GraphView);
        bitmap = Bitmap.createBitmap((int) 256, (int) 300, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        graphView.setImageBitmap(bitmap);
    }

    public class RecordAudio extends AsyncTask<Void, double[], Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
                short[] buffer = new short[dataSize];
                double[] transformFFT = new double[dataSize];
                audioRecord.startRecording();
                while (started) {
                    int checkReadResult = audioRecord.read(buffer, 0, dataSize);
                    for (int i = 0; i < dataSize && i < checkReadResult; i++) {
                        transformFFT[i] = (double) buffer[i] / 32768.0;
                    }
                    realDoubleFFT.ft(transformFFT);
                    publishProgress(transformFFT);
                }
                audioRecord.stop();
            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(double[]... transformFFT) {
            canvas.drawColor(Color.BLACK);
            for (int i = 0; i < transformFFT[0].length; i++) {
                int axis_X = i;
                int axis_downY = (int) (150 - (transformFFT[0][i] * 10));
                int axis_upY = 150;
                canvas.drawLine(axis_X, axis_downY, axis_X, axis_upY, paint);
            }
            graphView.invalidate();
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
            buttonStartStop.setText("Start");
            recordTask.cancel(true);
        } else {
            started = true;
            buttonStartStop.setText("Stop");
            recordTask = new RecordAudio();
            recordTask.execute();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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