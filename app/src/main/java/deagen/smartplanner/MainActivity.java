package deagen.smartplanner;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


import android.content.Context;
import android.net.Uri;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction ;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.List;

import deagen.smartplanner.fragments.ActivityPlannerFragment;
import deagen.smartplanner.fragments.DailyPlannerFragment;
import deagen.smartplanner.fragments.StatisticsFragment;
import deagen.smartplanner.logic.Planner;
import deagen.smartplanner.logic.taskscheduling.TaskManager;

/**
 * The only activity which composes the TaskManager app
 */
public class MainActivity extends AppCompatActivity
        implements  DailyPlannerFragment.OnFragmentInteractionListener,
                    ActivityPlannerFragment.OnFragmentInteractionListener,
                    StatisticsFragment.OnFragmentInteractionListener,
                    BottomNavigationView.OnNavigationItemSelectedListener {

    /**
     * The filename used to save the data of the planner
     */
    private static String mainFileName = "plannerfile.dat";

    private Toolbar toolbar;
    private Menu mAppBarMenu;
    private DailyPlannerFragment dailyPlanner;
    private ActivityPlannerFragment activityPlanner;
    private Planner planner;
    private BackKeyListener backKeyListener;

//    private boolean updateDailyPlannerUIFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView view = (BottomNavigationView) findViewById(R.id.mainactivity_navigation_view);
        view.setOnNavigationItemSelectedListener(this);

        dailyPlanner = new DailyPlannerFragment();
        activityPlanner = new ActivityPlannerFragment();

        planner = new Planner();
        this.loadFromFile();
        planner.addTestValues();
        planner.selectDate(LocalDate.now());

        dailyPlanner.setPlanner(planner);
        activityPlanner.setPlanner(planner);
        activityPlanner.setActivity(this);

        // adding the ActivityPlanner fragment and hiding it
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, activityPlanner);
        transaction.hide(activityPlanner);

        // adding the DailyPlanner fragment and showing it
        transaction.add(R.id.fragment_container, dailyPlanner);
        transaction.show(dailyPlanner);
        transaction.commit();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                Log.d("AdMob", "MobileAds initialization completed.");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        mAppBarMenu = menu;
        this.updateToggleButtonImage();
        return true;
    }

//    public void setUpdateDailyPlannerUIFlag(boolean inFlag) {
//        updateDailyPlannerUIFlag = inFlag;
//    }

    public void updateToggleButtonImage() {
        if(planner.getTaskManager().isActive())
            mAppBarMenu.findItem(R.id.task_toggle_option)
                .setIcon(android.R.drawable.ic_media_pause);
        else {
            getAppBarMenu()
                    .findItem(R.id.task_toggle_option)
                    .setIcon(android.R.drawable.ic_media_play);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.task_toggle_option:
                this.getDailyPlannerFragment().toggleActiveMode();
                return true;
            case R.id.change_date_option:
                this.getDailyPlannerFragment().switchDates();
                return true;
            case R.id.break_option:
                this.getDailyPlannerFragment().startBreakTask();
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    //fragment manipulation methods

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public Fragment getVisibleFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if(fragments != null && !fragments.isEmpty()) {
            for(Fragment fragment : fragments) {
                if(fragment != null && fragment.isVisible()) {
                    return fragment;
                }
            }
        }

        return null;
    }

    public void switchFragments(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(this.getVisibleFragment() != null)
            transaction.hide(this.getVisibleFragment());
        transaction.show(fragment);
        transaction.commit();
        if(fragment instanceof DailyPlannerFragment) {
            ((DailyPlannerFragment) fragment).setBackKeyHandler();
        }
        else if(fragment instanceof ActivityPlannerFragment) {
            ((ActivityPlannerFragment) fragment).setCategoryViewBackKeyListener();
        }
    }

    public DailyPlannerFragment getDailyPlannerFragment() {
        return dailyPlanner;
    }

    public ActivityPlannerFragment getActivityPlannerFragment() { return activityPlanner; }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public Menu getAppBarMenu() { return mAppBarMenu; }

    //navigation button methods

    /**
     * Emulates the user taping the navigation buttons at the bottom of the screen to switch
     * between the DailyPlanner, ActivityPlanner, and StatisticsViewer fragments.
     * @param i Number to indicate which button is being pressed, starting with 0
     */
    public void tapNavigationButton(int i) {
        BottomNavigationView navView = findViewById(R.id.mainactivity_navigation_view);
        switch(i) {
            case 0:
                navView.setSelectedItemId(R.id.dailyplanner_button);
                break;

            case 1:
                navView.setSelectedItemId(R.id.activityplanner_button);
                break;
        }
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.dailyplanner_button:
                this.switchFragments(dailyPlanner);
                toolbar.setTitle(R.string.dailyplanner_title);
                return true;
            case R.id.activityplanner_button:
                this.switchFragments(activityPlanner);
                toolbar.setTitle(R.string.activityplanner_title);
                return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("DEBUG", "User has switched away from the application");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TaskManager taskManager = planner.getTaskManager();
        if(taskManager.isActive())
            taskManager.stopTasks();
        planner.save(mainFileName);
    }

    //back button methods

    @Override
    public void onBackPressed() {
        if(backKeyListener != null) {
            backKeyListener.onBackKeyPressed();
        } else {
            super.onBackPressed();
        }
        Log.d("BACK KEY", "the back key was pressed");
    }

    public void setOnBackKeyListener(BackKeyListener inListener) {
        backKeyListener = inListener;
    }

    public interface BackKeyListener {
        void onBackKeyPressed();
    }

    //file I/O methods

    public void saveToFile() {
        try {
            ObjectOutputStream stream =
                    new ObjectOutputStream(getApplicationContext().openFileOutput(mainFileName, Context.MODE_PRIVATE));
            planner.save(stream);
            Log.d("MainActivity", "saveToFile successful.");
        } catch(FileNotFoundException exp) {
            Log.d("I/O Exception", "FileNotFound while saving");
        } catch(IOException exp) {
            Log.d("I/O Exception", "IOException while saving");
        }
    }

    public void loadFromFile() {
        try {
            ObjectInputStream stream =
                    new ObjectInputStream(getApplicationContext().openFileInput(mainFileName));
            planner.load(stream);
            Log.d("debug", "file loaded successfully");
        } catch(FileNotFoundException exp) {
            Log.d("I/O Exception", "FileNotFound while loading");
        } catch(IOException exp) {
            Log.d("I/O Exception", "IOException while loading");
        }
    }
}
