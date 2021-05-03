package com.fbp.Popper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.rvalerio.fgchecker.AppChecker;

import java.util.HashMap;

import static com.fbp.Popper.MainActivity.MY_PREFS_NAME;

public class TokenImage extends AppCompatActivity {
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        Log.d("MyActivity","TokenImage activty started");
    }
}
