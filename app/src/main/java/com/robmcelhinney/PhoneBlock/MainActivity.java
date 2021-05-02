package com.robmcelhinney.PhoneBlock;

import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {


    private Switch switchOtherAppsSocial;
    private Switch switchOtherAppsDesk;
    private static Context appContext;

    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private final static int REQUEST_CODE_OVERLAY = 123;
    private final static int REQUEST_CODE_USAGE = 124;

    public static final String CHANNEL_ID = "com.robmcelhinney.PhoneBlock.ANDROID";
    public static final String Button_list_Activity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = settings.edit();
        appContext = getApplicationContext();

        // Splash Screen first time launch
        if (!PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("pref_previously_started", false)) {
            startActivity(new Intent(MainActivity.this, PermissionsSplashActivity.class));
        }

        // Social button to go to list
        Button appsButtonList = findViewById(R.id.appsButton);
        final Intent installedAppsActivityIntent = new Intent(this, InstalledAppsActivity.class);
        appsButtonList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installedAppsActivityIntent.putExtra(Button_list_Activity,"Button");
                startActivity(installedAppsActivityIntent);
            }
        });


        // switch other social apps 
        switchOtherAppsSocial = findViewById(R.id.switchOtherAppsSocial);
        switchOtherAppsSocial.setChecked(settings.getBoolean("switchOtherAppsSocial", false));
        switchOtherAppsSocial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                editor.putBoolean("switchOtherAppsSocial", true);
                //checks if is view app running in Foreground.
                //checks whether premission is granted to block other applications while driving
                try {
                    ApplicationInfo applicationInfo = MainActivity.this.getPackageManager().getApplicationInfo(MainActivity.this.getPackageName(), 0);
                    assert ( MainActivity.this.getSystemService(Context.APP_OPS_SERVICE)) != null;
                    if(((AppOpsManager) MainActivity.this.getSystemService(Context.APP_OPS_SERVICE))
                            .checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName)
                            != AppOpsManager.MODE_ALLOWED) {
                        // checks if is allowed to overlay on top of other apps, if not then send user to settings.
                        Toast.makeText(MainActivity.this, "Please grant permission in order to block other applications while driving", Toast.LENGTH_LONG).show();

                        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), REQUEST_CODE_USAGE);

                        switchOtherAppsSocial.setChecked(false);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                // checks if is allowed to overlay on top of other apps, if not then send user to settings.
                if(!Settings.canDrawOverlays(MainActivity.this)) {
                    Toast.makeText(MainActivity.this, "Please grant permission in order to block other applications while driving", Toast.LENGTH_LONG).show();
                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), REQUEST_CODE_OVERLAY);

                    switchOtherAppsSocial.setChecked(false);
                }
                startOverlayService();
            } else {
                editor.putBoolean("switchOtherAppsSocial", false);
            }
            editor.commit();
            }
        });


        // switch other desk apps
        switchOtherAppsDesk = findViewById(R.id.switchOtherAppsDesk);
        switchOtherAppsDesk.setChecked(settings.getBoolean("switchOtherAppsDesk", false));
        switchOtherAppsDesk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean("switchOtherAppsDesk", true);
                    //checks if is view app running in Foreground.
                    try {
                        ApplicationInfo applicationInfo = MainActivity.this.getPackageManager().getApplicationInfo(MainActivity.this.getPackageName(), 0);
                        assert ( MainActivity.this.getSystemService(Context.APP_OPS_SERVICE)) != null;
                        if(((AppOpsManager) MainActivity.this.getSystemService(Context.APP_OPS_SERVICE))
                                .checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName)
                                != AppOpsManager.MODE_ALLOWED) {
                            Toast.makeText(MainActivity.this, "Please grant permission in order to block other applications while driving", Toast.LENGTH_LONG).show();

                            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), REQUEST_CODE_USAGE);

                            switchOtherAppsDesk.setChecked(false);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    // checks if is allowed to overlay on top of other apps, if not then send user to settings.
                    if(!Settings.canDrawOverlays(MainActivity.this)) {
                        Toast.makeText(MainActivity.this, "Please grant permission in order to block other applications while driving", Toast.LENGTH_LONG).show();
                        startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), REQUEST_CODE_OVERLAY);

                        switchOtherAppsDesk.setChecked(false);
                    }
                    startOverlayService();
                } else {
                    editor.putBoolean("switchOtherAppsDesk", false);

                }
                editor.commit();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiverToggleButton, new IntentFilter("intentToggleButton"));
    }

    private final BroadcastReceiver mMessageReceiverToggleButton = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        // Get extra data included in the Intent
        boolean value = intent.getBooleanExtra("valueBool", false);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    public static void checkPermission(Context context) {
        // checks if user gave permission to change notification policy. If not, then launch
        // settings to get them to give permission.
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            context.startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
        }
    }

    //start or stop the overlay for the app that is open(ed)
    private static void startOverlayService() {
        Intent intent = new Intent(appContext, Overlay.class);
        appContext.startService(intent);
    }

    private static void stopOverlayService() {
        Intent intent = new Intent(appContext, Overlay.class);
        appContext.stopService(intent);
    }
}

