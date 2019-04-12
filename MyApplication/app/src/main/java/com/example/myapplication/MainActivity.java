package com.example.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;
import android.widget.Button;

import java.util.Random;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Handler handler = new Handler();
        final Button button1 = findViewById(R.id.button);

//
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Choreographer choreographer = Choreographer.getInstance();
            choreographer.postFrameCallback(new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long l) {
                    Log.d(TAG, "doFrame: " + l);
                }
            });
        }



    }


}
