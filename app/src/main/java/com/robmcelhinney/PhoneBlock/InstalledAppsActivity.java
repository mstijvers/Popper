package com.robmcelhinney.PhoneBlock;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.robmcelhinney.PhoneBlock.MainActivity.MY_PREFS_NAME;

public class InstalledAppsActivity extends AppCompatActivity {
    private static final String PACKAGE_NAME = "com.robmcelhinney.PhoneBlock";
    private List<ApplicationInfo> installedApps;
    private ArrayList<String> installedAppsNames;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private Set<String> selectedAppsPackageName;
    //private Set<String> selectedAppsPackageNameDesk;

    private View progressOverlay;
    private ListView listView;
    private Context thisContext;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_installed_apps);

        progressOverlay = findViewById(R.id.progress_loading_overlay);

        //get shared preferences from home
        settings = getApplicationContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = settings.edit();
        thisContext = this;
        selectedAppsPackageName = new HashSet<>();
//        selectedAppsPackageNameDesk = new HashSet<>();

        listView = findViewById(R.id.listViewID);
        new LoadApplications().execute();
    }

    //apps installed by the user
    private void userInstalledApps() {
        List<ApplicationInfo> apps = getPackageManager().getInstalledApplications(0);

        installedApps = new ArrayList();
        installedAppsNames = new ArrayList();

        for(ApplicationInfo app : apps) {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !((String) app.loadLabel(getApplicationContext().getPackageManager())).equalsIgnoreCase(PACKAGE_NAME)) {
                installedApps.add(app);
                installedAppsNames.add((String) app.loadLabel(getApplicationContext().getPackageManager()));
            }
        }

        Collections.sort(installedApps, new Comparator<ApplicationInfo>() {
            public int compare(ApplicationInfo v1, ApplicationInfo v2) {
                return ((String) v1.loadLabel(getApplicationContext().getPackageManager())).compareToIgnoreCase((String)v2.loadLabel(getApplicationContext().getPackageManager()));
            }
        });
        Collections.sort(installedAppsNames);
    }


    private class MyListAdapter extends ArrayAdapter<String> {
        private int layout;
        boolean checkState[];
//        boolean checkStateD[];

        MyListAdapter(Context context, List<String> objects) {
            super(context, R.layout.list_item, objects);
            layout = R.layout.list_item;
            checkState = new boolean[objects.size()];
//            checkStateD = new boolean[objects.size()];
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            //make a new viewholder
            ViewHolder viewHolder = new ViewHolder();
            //get the arraylist of apps tat are selected (for social package?)
            selectedAppsPackageName = new HashSet<>(settings.getStringSet("selectedAppsPackage", new HashSet<String>()));
//            selectedAppsPackageNameDesk = new HashSet<>(settings.getStringSet("selectedAppsPackageD", new HashSet<String>()));

            //if ther is no convertView, then make one, with viewholder. so convertView stores the data of View basically.
            if(convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(layout, parent, false);
                viewHolder.thumbnail = convertView.findViewById(R.id.listItemThumbnail);
                viewHolder.title = convertView.findViewById(R.id.listItemText);
                //checks state of social list checkboxes
                viewHolder.checkBox = convertView.findViewById(R.id.listCheckBoxSocial);
//                viewHolder.checkBoxD = convertView.findViewById(R.id.listCheckBoxDesk);

                viewHolder.title.setText(installedApps.get(position).loadLabel(getApplicationContext().getPackageManager()));
                viewHolder.thumbnail.setImageDrawable(installedApps.get(position).loadIcon(getApplicationContext().getPackageManager()));

                //for the strings that are also in the list of installed apps set the checkbox to true
                // else set the checkbox to false.
                if(selectedAppsPackageName.contains(installedApps.get(position).packageName)) {
                    viewHolder.checkBox.setChecked(true);
                    checkState[position] = true;
                }
                else{
                    viewHolder.checkBox.setChecked(false);
                    checkState[position] = false;
                }

//                if(selectedAppsPackageNameDesk.contains(installedApps.get(position).packageName)) {
//                    viewHolder.checkBoxD.setChecked(true);
//                    checkStateD[position] = true;
//                }
//                else{
//                    viewHolder.checkBoxD.setChecked(false);
//                    checkStateD[position] = false;
//                }
                convertView.setTag(viewHolder);
            }
            //if convertView does exist, set the viewholder to the data stored in convertView
            else {
                viewHolder = (ViewHolder) convertView.getTag();
                viewHolder.title.setText(installedApps.get(position).loadLabel(getApplicationContext().getPackageManager()));
                viewHolder.thumbnail.setImageDrawable(installedApps.get(position).loadIcon(getApplicationContext().getPackageManager()));

                // for the strings that are also in the list of installed apps set the checkbox to true
                // else set the checkbox to false.
                // Social
                if(settings.getStringSet("selectedAppsPackage", new HashSet<String>()).contains(installedApps.get(position).packageName)) {
                    checkState[position] = true;
                    viewHolder.checkBox.setChecked(true);
                }
                else{
                    checkState[position] = false;
                    viewHolder.checkBox.setChecked(false);
                }
                // Desk
//                if(settings.getStringSet("selectedAppsPackageD", new HashSet<String>()).contains(installedApps.get(position).packageName)) {
//                    checkStateD[position] = true;
//                    viewHolder.checkBoxD.setChecked(true);
//                }
//                else{
//                    checkStateD[position] = false;
//                    viewHolder.checkBoxD.setChecked(false);
//                }
            }

            //set onclick listener to viewholder checkboxes (SOCIAL) and store this data in convertView
            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // if checkbox was clicked, unclick checkbox and remove the app from selectedAppsPackageName
                    // aka remove from settings (social
                if(checkState[position]) {
                    selectedAppsPackageName.remove(installedApps.get(position).packageName);
                    editor.putStringSet("selectedAppsPackage", selectedAppsPackageName).apply();
                }
                // else if checkbox was unclicked, click checkbox and add the app to selectedAppsPackageName
                // aka add to settings (social
                else {
                    selectedAppsPackageName.add(installedApps.get(position).packageName);
                    editor.putStringSet("selectedAppsPackage", selectedAppsPackageName).apply();
                }
                checkState[position] = !checkState[position];
                notifyDataSetChanged();
                }
            });

            //set onclick listener to viewholder checkboxes (DESK) and store this data in convertView
//            viewHolder.checkBoxD.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // if checkbox was clicked, unclick checkbox and remove the app from selectedAppsPackageName
//                    // aka remove from settings (social
//                    if(checkStateD[position]) {
//                        selectedAppsPackageNameDesk.remove(installedApps.get(position).packageName);
//                        editor.putStringSet("selectedAppsPackageD", selectedAppsPackageNameDesk).apply();
//                    }
//                    // else if checkbox was unclicked, click checkbox and add the app to selectedAppsPackageName
//                    // aka add to settings (desk
//                    else {
//                        selectedAppsPackageNameDesk.add(installedApps.get(position).packageName);
//                        editor.putStringSet("selectedAppsPackageD", selectedAppsPackageNameDesk).apply();
//                    }
//                    checkStateD[position] = !checkStateD[position];
//                    notifyDataSetChanged();
//                }
//                });
            return convertView;
        }
    }

    //items of ViewHolder
    public class ViewHolder {
        ImageView thumbnail;
        TextView title;
        CheckBox checkBox;
       // CheckBox checkBoxD;
    }

    // if the screen is paused go back and safe the sellected apps of
    // selectedAppsPackageName in "SelectedAppsPackage" file for later use.
    @Override
    protected void onPause() {
        editor.clear()
                .putStringSet("selectedAppsPackage", selectedAppsPackageName)
               // .putStringSet("selectedAppsPackageD", selectedAppsPackageNameDesk)
                .commit();
        super.onPause();
    }

    private void animateView(final View view, final int toVisibility, float toAlpha) {
        boolean show = (toVisibility == View.VISIBLE);
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate()
            .setDuration(200)
            .alpha(show ? toAlpha : 0)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(toVisibility);
                }
            });
    }


    @SuppressLint("StaticFieldLeak")
    private class LoadApplications extends AsyncTask<Void, Void, Void> {

        //loading screen
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            animateView(progressOverlay, View.VISIBLE, 1f);

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            animateView(progressOverlay, View.GONE, 0);
        }

        //In background
        @Override
        protected Void doInBackground(Void... params) {
            userInstalledApps();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.setAdapter(new MyListAdapter(thisContext, installedAppsNames));
                }
            });

            return null;
        }
    }
}