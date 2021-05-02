package com.robmcelhinney.popper;

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

import com.fbp.popper.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.robmcelhinney.popper.MainActivity.MY_PREFS_NAME;

public class InstalledAppsActivity extends AppCompatActivity {
    private static final String PACKAGE_NAME = "com.robmcelhinney.popper";
    private List<ApplicationInfo> installedApps;
    private ArrayList<String> installedAppsNames;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private Set<String> selectedAppsPackageNameSocial;
    private Set<String> selectedAppsPackageNameDesk;

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
        selectedAppsPackageNameSocial = new HashSet<>();
        selectedAppsPackageNameDesk = new HashSet<>();

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

        // define whether the list item was already checked.
        boolean checkStateListItemSocial[];
        boolean checkStateListItemDesk[];

        MyListAdapter(Context context, List<String> objects) {
            super(context, R.layout.list_item, objects);
            layout = R.layout.list_item;
            checkStateListItemSocial = new boolean[objects.size()];
            checkStateListItemDesk = new boolean[objects.size()];
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            //make a new viewholder
            ViewHolder viewHolder = new ViewHolder();
            //get the arraylist of apps tat are selected (for social package?)
            selectedAppsPackageNameSocial = new HashSet<>(settings.getStringSet("selectedAppsPackageSocial", new HashSet<String>()));
            selectedAppsPackageNameDesk = new HashSet<>(settings.getStringSet("selectedAppsPackageDesk", new HashSet<String>()));

            //if ther is no convertView, then make one, with viewholder. so convertView stores the data of View basically.
            if(convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(layout, parent, false);
                viewHolder.thumbnail = convertView.findViewById(R.id.listItemThumbnail);
                viewHolder.title = convertView.findViewById(R.id.listItemText);
                //checks state of social list checkboxes
                viewHolder.checkBoxSocial = convertView.findViewById(R.id.listCheckBoxSocial);
                viewHolder.checkBoxDesk = convertView.findViewById(R.id.listCheckBoxDesk);

                viewHolder.title.setText(installedApps.get(position).loadLabel(getApplicationContext().getPackageManager()));
                viewHolder.thumbnail.setImageDrawable(installedApps.get(position).loadIcon(getApplicationContext().getPackageManager()));

                //for the strings that are also in the list of installed apps set the checkbox to true
                // else set the checkbox to false.
                //Social
                if(selectedAppsPackageNameSocial.contains(installedApps.get(position).packageName)) {
                    viewHolder.checkBoxSocial.setChecked(true);
                    checkStateListItemSocial[position] = true;
                }
                else{
                    viewHolder.checkBoxSocial.setChecked(false);
                    checkStateListItemSocial[position] = false;
                }

                //Desk
                if(selectedAppsPackageNameDesk.contains(installedApps.get(position).packageName)) {
                    viewHolder.checkBoxDesk.setChecked(true);
                    checkStateListItemDesk[position] = true;
                }
                else{
                    viewHolder.checkBoxDesk.setChecked(false);
                    checkStateListItemDesk[position] = false;
                }
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
                if(settings.getStringSet("selectedAppsPackageSocial", new HashSet<String>()).contains(installedApps.get(position).packageName)) {
                    checkStateListItemSocial[position] = true;
                    viewHolder.checkBoxSocial.setChecked(true);
                }
                else{
                    checkStateListItemSocial[position] = false;
                    viewHolder.checkBoxSocial.setChecked(false);
                }

                // Desk
                if(settings.getStringSet("selectedAppsPackageDesk", new HashSet<String>()).contains(installedApps.get(position).packageName)) {
                    checkStateListItemDesk[position] = true;
                    viewHolder.checkBoxDesk.setChecked(true);
                }
                else{
                    checkStateListItemDesk[position] = false;
                    viewHolder.checkBoxDesk.setChecked(false);
                }
            }

            //set onclick listener to viewholder checkboxes (SOCIAL) and store this data in convertView
            viewHolder.checkBoxSocial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // if checkbox was clicked, unclick checkbox and remove the app from selectedAppsPackageName
                    // aka remove from settings (social
                if(checkStateListItemSocial[position]) {
                    selectedAppsPackageNameSocial.remove(installedApps.get(position).packageName);
                    editor.putStringSet("selectedAppsPackageSocial", selectedAppsPackageNameSocial).apply();
                }
                // else if checkbox was unclicked, click checkbox and add the app to selectedAppsPackageName
                // aka add to settings (social
                else {
                    selectedAppsPackageNameSocial.add(installedApps.get(position).packageName);
                    editor.putStringSet("selectedAppsPackageSocial", selectedAppsPackageNameSocial).apply();
                }
                checkStateListItemSocial[position] = !checkStateListItemSocial[position];
                notifyDataSetChanged();
                }
            });

            //set onclick listener to viewholder checkboxes (DESK) and store this data in convertView
            viewHolder.checkBoxDesk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // if checkbox was clicked, unclick checkbox and remove the app from selectedAppsPackageName
                    // aka remove from settings (social
                    if(checkStateListItemDesk[position]) {
                        selectedAppsPackageNameDesk.remove(installedApps.get(position).packageName);
                        editor.putStringSet("selectedAppsPackageDesk", selectedAppsPackageNameDesk).apply();
                    }
                    // else if checkbox was unclicked, click checkbox and add the app to selectedAppsPackageName
                    // aka add to settings (desk
                    else {
                        selectedAppsPackageNameDesk.add(installedApps.get(position).packageName);
                        editor.putStringSet("selectedAppsPackageDesk", selectedAppsPackageNameDesk).apply();
                    }
                    checkStateListItemDesk[position] = !checkStateListItemDesk[position];
                    notifyDataSetChanged();
                }
                });
            return convertView;
        }
    }

    //items of ViewHolder
    public class ViewHolder {
        ImageView thumbnail;
        TextView title;
        CheckBox checkBoxSocial;
        CheckBox checkBoxDesk;
    }

    // if the screen is paused go back and safe the sellected apps of
    // selectedAppsPackageName in "SelectedAppsPackageSocial" file for later use.
    @Override
    protected void onPause() {
        editor.clear()
                .putStringSet("selectedAppsPackageSocial", selectedAppsPackageNameSocial)
                .putStringSet("selectedAppsPackageDesk", selectedAppsPackageNameDesk)
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