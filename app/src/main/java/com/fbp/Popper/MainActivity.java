package com.fbp.Popper;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private Switch switchOtherAppsSocial;
    private Switch switchOtherAppsDesk;
    private static Context appContext;

    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private final static int REQUEST_CODE_OVERLAY = 123;
    private final static int REQUEST_CODE_USAGE = 124;
    private static final int ENABLE_BT_REQUEST_CODE = 1;
    public static final int REQUEST_DISCOVERABLE_CODE = 2;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final String Button_list_Activity = null;
    public BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public BluetoothSocket btSocket = null;

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

        //Bluetooth connection
        ImageView BleImage = findViewById(R.id.BTEnabled);
        if (mBluetoothAdapter == null) {
            //Display a toast notifying the user that their device doesn’t support Bluetooth//
            Toast.makeText(getApplicationContext(),"This device doesn’t support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            //Create an intent with the ACTION_REQUEST_ENABLE action, which we’ll use to display our system Activity//
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            //Pass this intent to startActivityForResult(). ENABLE_BT_REQUEST_CODE is a locally defined integer that must be greater than 0,
            startActivityForResult(enableIntent, ENABLE_BT_REQUEST_CODE);
            Toast.makeText(getApplicationContext(), "Enabling Bluetooth!", Toast.LENGTH_LONG).show();
            BleImage.setBackgroundResource(R.drawable.ic_blue_off);
        }
        if(mBluetoothAdapter.isEnabled()) {

            BleImage.setBackgroundResource(R.drawable.ic_blue_on);
        }

        //get list of paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // If there’s 1 or more paired devices...//
        if (pairedDevices.size() >= 0) {

            //...then loop through these devices and show in log for debugging purposes
            for (BluetoothDevice device : pairedDevices) {
                //Retrieve each device’s public identifier and MAC address. Add each device’s name and address to an ArrayAdapter, ready to incorporate into a
                //ListView
                String list_of_devices = device.getName() + "\n" + device.getAddress();
                Log.d("MyActivity", list_of_devices);

                //try to connect to desk token
                if(device.getName().equals("DESK TOKEN")){
                    Log.d("MyActivity", "Trying to Connect Desk");
                    try {
                        btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                        //Now you will start the connection
                        btSocket.connect();
                        Log.d("MyActivity", "Connected Desk!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //try to connect to Social token
                else if(device.getName().equals("SOCIAL TOKEN")) {
                    Log.d("MyActivity", "Trying to Connect Social");
                    try {
                        btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                        //Now you will start the connection
                        btSocket.connect();
                        Log.d("MyActivity", "Connected Social!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }





        //make device discoverable for 400 milli sec.
        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //Specify how long the device will be discoverable for, in seconds.//
        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 400);
        startActivity(discoveryIntent);

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
                startTokenImageChanger();
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
                startTokenImageChanger();
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

    private void startTokenImageChanger() {
        ImageView TokenImage = findViewById(R.id.ImageViewToken);
        if(settings.getBoolean("switchOtherAppsDesk", true) && !settings.getBoolean("switchOtherAppsSocial", true)){
            TokenImage.setBackground(getResources().getDrawable(R.drawable.popper_blue));
        }
        if(!settings.getBoolean("switchOtherAppsDesk", true) && settings.getBoolean("switchOtherAppsSocial", true)){
            TokenImage.setBackground(getResources().getDrawable(R.drawable.popper_orange));
        }
        if(settings.getBoolean("switchOtherAppsDesk",true) && settings.getBoolean("switchOtherAppsSocial",true)){
            TokenImage.setBackground(getResources().getDrawable(R.drawable.popper_both));
        }
        if(!settings.getBoolean("switchOtherAppsDesk",true) && !settings.getBoolean("switchOtherAppsSocial",true)){
            TokenImage.setBackground(getResources().getDrawable(R.drawable.popper_gray));
        }
    }

    //start or stop the overlay for the app that is open(ed)
    private static void startOverlayService() {
        Intent intent = new Intent(appContext, Overlay.class);
        appContext.startService(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Check what request we’re responding to//
        if (requestCode == ENABLE_BT_REQUEST_CODE) {

            //If the request was successful…//
            if (resultCode == Activity.RESULT_OK) {

                //...then display the following toast.//
                Toast.makeText(getApplicationContext(), "Bluetooth has been enabled", Toast.LENGTH_SHORT).show();
            }

            //If the request was unsuccessful...//
            if(resultCode == RESULT_CANCELED){

                //...then display this alternative toast.//
                Toast.makeText(getApplicationContext(), "An error occurred while attempting to enable Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

