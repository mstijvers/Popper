package com.robmcelhinney.popper;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import com.fbp.popper.R;
import com.rvalerio.fgchecker.AppChecker;

import java.util.HashMap;
import java.util.HashSet;

import static com.robmcelhinney.popper.MainActivity.MY_PREFS_NAME;
//close apps and display overlay message 'don't open app.' or another message.
public class Overlay extends Service {

    private final int delayMillis = 2000;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private AppChecker appChecker;
    private SharedPreferences settings;
    private HashMap closedApps;

    @Override
    public void onCreate() {
        super.onCreate();
        //get shared preferences (data list of checkmarks) with the string MY_PREFS_NAME
        settings = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        appChecker = new AppChecker();
        closedApps = new HashMap<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handlerLoop();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    // when this is called then CloseApps is run.
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handlerLoop() {
        runnable= new Runnable() {
            @Override
            public void run() {
            closeApps();
            handler.postDelayed(this, delayMillis);
            }
        };

        handler.postDelayed(runnable, 3000);
    }

    // Everything below is for looking whether the app is in the list of selected apps (not to disturb)
    // and displaying a popup, and sending you back to home.
    // Close apps when app is in (Hash)set 'selectedAppsPackage' Saved in *settings*.
    private void closeApps() {
        boolean stateSocialActive = settings.getBoolean("switchOtherAppsSocial", false);
        boolean stateDeskActive = settings.getBoolean("switchOtherAppsDesk", false);
        String fgApp = getForegroundApp();
        if (fgApp != null && settings.getStringSet("selectedAppsPackageSocial", new HashSet<String>()).contains(fgApp) && stateSocialActive) {
            if(closedApps.containsKey(fgApp)){
                //if app has already been opend once change the message (but still close app)
                if((int)closedApps.get(fgApp) >= 0) {
                    goHome();
                    displayToast(getString(R.string.close_app_driving));
                    closedApps.put(fgApp, (int)closedApps.get(fgApp)+1);
                }
                return;
            }
            //close app when opened for the first time with message.
            goHome();
            displayToast(getString(R.string.blocked_app_warning));
            closedApps.put(fgApp, 0);
        }
        if (fgApp != null && settings.getStringSet("selectedAppsPackageDesk", new HashSet<String>()).contains(fgApp) && stateDeskActive) {
            if(closedApps.containsKey(fgApp)){
                //if app has already been opend once change the message (but still close app)
                if((int)closedApps.get(fgApp) >= 0) {
                    goHome();
                    displayToast(getString(R.string.close_app_driving));
                    closedApps.put(fgApp, (int)closedApps.get(fgApp)+1);
                }
                return;
            }
            //close app when opened for the first time with message.
            goHome();
            displayToast(getString(R.string.blocked_app_warning));
            closedApps.put(fgApp, 0);
        }
    }
    //Go to home screen after app is closed
    private void goHome() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

    private String getForegroundApp(){
        return appChecker.getForegroundApp(getApplicationContext());
    }

    private void displayToast(final String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
