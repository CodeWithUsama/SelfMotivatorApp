package at.ciit.usagestats;


import androidx.appcompat.app.AppCompatActivity;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.IpSecManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

public class MainActivity extends AppCompatActivity {

    Button enableBtn, showBtn;
    TextView permissionDescriptionTv, usageTv;
    ListView appsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enableBtn = findViewById(R.id.enable_btn);
        showBtn =  findViewById(R.id.show_btn);
        permissionDescriptionTv =findViewById(R.id.permission_description_tv);
        usageTv =  findViewById(R.id.usage_tv);
        appsList =  findViewById(R.id.apps_list);

        this.loadStatistics();
    }


    // each time the application gets in foreground -> getGrantStatus and render the corresponding buttons
    @Override
    protected void onStart() {
        super.onStart();
        if (getGrantStatus()) {
            showHideWithPermission();
            showBtn.setOnClickListener(view -> loadStatistics());
        } else {
            showHideNoPermission();
            enableBtn.setOnClickListener(view -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        }
    }


    /**
     * load the usage stats for last 24h
     */
    public void loadStatistics() {

        //subtracting a day
Calendar c = Calendar.getInstance();
Date date = null;

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String dateString = "10-04-2021 00:00:00";
        try {
             date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.setTime(date);

        long start = System.currentTimeMillis() - 1000*3600*24;

        UsageStatsManager usm = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);
        }
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  start,  System.currentTimeMillis());
        for(int i=0;i<appList.size();i++) {
            Log.d("TAG", appList.toString());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appList = appList.stream().filter(app -> app.getTotalTimeInForeground() >= 60000).collect(Collectors.toList());
        }



        // Group the usageStats by application and sort them by total time in foreground
        if (appList.size() > 0) {
            Map<String, UsageStats> mySortedMap = new TreeMap<>();
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getPackageName(), usageStats);
            }
            showAppsUsage(mySortedMap);
        }
    }


    public void showAppsUsage(Map<String, UsageStats> mySortedMap) {
    //public void showAppsUsage(List<UsageStats> usageStatsList) {
        ArrayList<App> appsList = new ArrayList<>();
        List<UsageStats> usageStatsList = new ArrayList<>(mySortedMap.values());

        // sort the applications by time spent in foreground
        Collections.sort(usageStatsList, (z1, z2) -> Long.compare(z1.getTotalTimeInForeground(), z2.getTotalTimeInForeground()));

        // get total time of apps usage to calculate the usagePercentage for each app

        long totalTime = 0;
        String b = "";
        String n = "";
for(int i =0;i<usageStatsList.size();i++) {
        try {

            ApplicationInfo img = getApplicationContext().getPackageManager().getApplicationInfo(usageStatsList.get(i).getPackageName(), 0);
//Log.d("TAG",usageStatsList.get(i).getPackageName());






            n = getApplicationContext().getPackageManager().getApplicationLabel(img).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }




    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ) {

        if(n.equals("Quickstep") == false && n.equals("Package installer") == false &&  n.equals("Android System") == false && n.equals("Phone Services") == false  &&  n.equals("System UI") == false &&  n.equals("Settings") == false && n.equals(getApplicationName(this)) == false && isAppInfoAvailable(usageStatsList.get(i))) {

            totalTime += usageStatsList.get(i).getTotalTimeInForeground();

//            Log.d("TAG",getDurationBreakdown(totalTime));
//            totalTime = usageStatsList.stream().map(UsageStats::getTotalTimeInForeground).mapToLong(Long::longValue).sum();
        }

    }
}

        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Integer> time = new ArrayList<Integer>();



        //fill the appsList
        for (UsageStats usageStats : usageStatsList) {

            try {

//                Drawable icon = getDrawable(R.drawable.no_image);
//                String[] packageNames = packageName.split("\\.");
//                String appName = packageNames[packageNames.length-1].trim();
Drawable icon = getDrawable(R.drawable.no_image);
String appName = "";



                if(isAppInfoAvailable(usageStats)){


                    ApplicationInfo ai = getApplicationContext().getPackageManager().getApplicationInfo(usageStats.getPackageName(), 0);


                   icon = getApplicationContext().getPackageManager().getApplicationIcon(ai);
                    appName = getApplicationContext().getPackageManager().getApplicationLabel(ai).toString();


                }


if(appName.equals("Quickstep") == false && appName.equals("Package installer") == false &&  appName.equals("Android System") == false && appName.equals("Phone Services") == false  &&  appName.equals("System UI") == false &&  appName.equals("Settings") == false && appName.equals(getApplicationName(this)) == false && isAppInfoAvailable(usageStats)  ){

    names.add(appName);

        time.add(returnTime(usageStats.getTotalTimeInForeground()));


    String usageDuration = getDurationBreakdown(usageStats.getTotalTimeInForeground());
    int usagePercentage = (int) (usageStats.getTotalTimeInForeground() * 100 / totalTime);
    App usageStatDTO = new App(icon, appName, usagePercentage, usageDuration);
    appsList.add(usageStatDTO);
}


            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

Collections.reverse(time);
        Collections.reverse(names);
        // reverse the list to get most usage first
        Collections.reverse(appsList);

        // build the adapter
        AppsAdapter adapter = new AppsAdapter(this, appsList);

        // attach the adapter to a ListView
        ListView listView = findViewById(R.id.apps_list);
        listView.setAdapter(adapter);
listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(MainActivity.this,chart.class);

intent.putIntegerArrayListExtra("minutes",time);
intent.putStringArrayListExtra("apps",names);

        startActivity(intent);
//       String tim = String.valueOf(usageStatsList.get(position).getTotalTimeInForeground());
//        String txt = appsList.get(position).appName + " " + tim ;
//        Toast.makeText(MainActivity.this,txt,Toast.LENGTH_SHORT).show();
    }
});
        showHideItemsWhenShowApps();
    }

    /**
     * check if PACKAGE_USAGE_STATS permission is aloowed for this application
     * @return true if permission granted
     */
    private boolean getGrantStatus() {


        AppOpsManager appOps = (AppOpsManager) getApplicationContext()
                .getSystemService(Context.APP_OPS_SERVICE);

        int mode = appOps.unsafeCheckOpNoThrow(OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getApplicationContext().getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            return (getApplicationContext().checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            return (mode == MODE_ALLOWED);
        }
    }

    /**
     * check if the application info is still existing in the device / otherwise it's not possible to show app detail
     * @return true if application info is available
     */
    private boolean isAppInfoAvailable(UsageStats usageStats) {
        try {
            getApplicationContext().getPackageManager().getApplicationInfo(usageStats.getPackageName(), 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    /**
     * helper method to get string in format hh:mm:ss from miliseconds
     *
     * @param millis (application time in foreground)
     * @return string in format hh:mm:ss from miliseconds
     */
    private String getDurationBreakdown(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long hours = TimeUnit.MILLISECONDS.toHours(millis);

        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        return (hours + " h " +  minutes + " m " + seconds + " s");
    }


    /**
     * helper method used to show/hide items in the view when  PACKAGE_USAGE_STATS permission is not allowed
     */
    public void showHideNoPermission() {
        enableBtn.setVisibility(View.VISIBLE);
        permissionDescriptionTv.setVisibility(View.VISIBLE);
        showBtn.setVisibility(View.GONE);
        usageTv.setVisibility(View.GONE);
        appsList.setVisibility(View.GONE);

    }

    /**
     * helper method used to show/hide items in the view when  PACKAGE_USAGE_STATS permission allowed
     */
    public void showHideWithPermission() {
        enableBtn.setVisibility(View.GONE);
        permissionDescriptionTv.setVisibility(View.GONE);
        showBtn.setVisibility(View.VISIBLE);
        usageTv.setVisibility(View.GONE);
        appsList.setVisibility(View.GONE);
    }

    /**
     * helper method used to show/hide items in the view when showing the apps list
     */
    public void showHideItemsWhenShowApps() {
        enableBtn.setVisibility(View.GONE);
        permissionDescriptionTv.setVisibility(View.GONE);
        showBtn.setVisibility(View.GONE);
        usageTv.setVisibility(View.VISIBLE);
        appsList.setVisibility(View.VISIBLE);

    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public  int returnTime(Long millis){
int minutes1 = 0;
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
else {
            long hours = TimeUnit.MILLISECONDS.toHours(millis);

            millis -= TimeUnit.HOURS.toMillis(hours);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
            millis -= TimeUnit.MINUTES.toMillis(minutes);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

             minutes1 = (int) minutes;
            int hour1 = (int) hours;
            int seconds1 = (int) seconds;
            if (hour1 > 0) {
                minutes1 = minutes1 + (hour1 * 60);
            }
            if(seconds1 >= 50){
                minutes1 = minutes1 + 1;
            }

        }




return  minutes1;
    }









}