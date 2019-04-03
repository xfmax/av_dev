package com.example.audiorecord;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button record, play, record_stop, play_stop;
    private int SAMPLING_RATE = 44100;
    private int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private volatile boolean isRecroding = false;
    private volatile boolean isPlay = false;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        record = findViewById(R.id.record);
        play = findViewById(R.id.play);
        play_stop = findViewById(R.id.play_stop);
        record_stop = findViewById(R.id.record_stop);

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                record();
            }
        });

        record_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                record_stop();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
            }
        });

        play_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play_stop();
            }
        });
    }

    private void record() {

        if (isRecroding) {
            Toast.makeText(MainActivity.this, "正在录制中...", Toast.LENGTH_SHORT).show();
        } else {
            record.setText("开始");
            isRecroding = true;

            final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_CONFIG, AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLING_RATE,
                    CHANNEL_CONFIG, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
            final File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");
            if (file.mkdirs()) {
                Log.d(TAG, "dir 存在！");
            }

            if (file.exists()) {
                file.delete();
            }
            final byte[] data = new byte[minBufferSize];

            audioRecord.startRecording();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = new FileOutputStream(file);

                        if (fileOutputStream != null) {

                            while (isRecroding) {
                                if (audioRecord.read(data, 0, minBufferSize) != AudioRecord.ERROR_INVALID_OPERATION) {
                                    fileOutputStream.write(data);
                                }
                            }

                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (null != audioRecord) {
                            audioRecord.stop();
                            audioRecord.release();
                        }
                    }
                }
            }).start();
        }
    }

    private void record_stop() {
        if (isRecroding) {
            record.setText("停止");
            isRecroding = false;
            Toast.makeText(MainActivity.this, "已经停止录制...", Toast.LENGTH_SHORT).show();
        }
    }


    private void play() {

        if (isPlay) {
            Toast.makeText(MainActivity.this, "正在播放中...", Toast.LENGTH_SHORT).show();
        } else {
            isPlay = true;
            int minBufferSize = AudioTrack.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioTrack = new AudioTrack(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                    new AudioFormat.Builder().setSampleRate(SAMPLING_RATE)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build(),
                    minBufferSize, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
            final File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");


            if (file.exists()) {
                Log.d(TAG, "file exist ");
            }


            final byte[] data = new byte[minBufferSize];
            audioTrack.play();


            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
                        try {
                            while (fis.available() > 0) {
                                int code = fis.read(data);
                                if (code == AudioTrack.ERROR_BAD_VALUE || code == AudioTrack.ERROR_INVALID_OPERATION) {
                                    continue;
                                }
                                if (code != 0 && code != -1) {
                                    audioTrack.write(data, 0, code);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (fis != null) {
                                fis.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }).start();


        }


    }

    private void play_stop() {
        if (isPlay) {
            audioTrack.stop();
            isPlay = false;
            Toast.makeText(MainActivity.this, "已经停止播放...", Toast.LENGTH_SHORT).show();
        }
    }

}
