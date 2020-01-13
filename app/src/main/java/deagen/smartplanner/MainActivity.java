package deagen.smartplanner;

import android.net.Uri;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.time.LocalDate;
import java.util.List;

import deagen.smartplanner.fragments.ActivityPlannerFragment;
import deagen.smartplanner.fragments.DailyPlannerFragment;
import deagen.smartplanner.fragments.StatisticsFragment;
import deagen.smartplanner.logic.Planner;

/**
 * The only activity which composes the TaskManager app
 */
public class MainActivity extends AppCompatActivity
        implements  DailyPlannerFragment.OnFragmentInteractionListener,
                    ActivityPlannerFragment.OnFragmentInteractionListener,
                    StatisticsFragment.OnFragmentInteractionListener,
                    BottomNavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DailyPlannerFragment dailyPlanner;
    private ActivityPlannerFragment activityPlanner;
    private Fragment statsViewer;
    private Planner planner;
    private BackKeyListener backKeyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView view = (BottomNavigationView) findViewById(R.id.mainactivity_navigation_view);
        view.setOnNavigationItemSelectedListener(this);

        dailyPlanner = new DailyPlannerFragment();
        activityPlanner = new ActivityPlannerFragment();
        statsViewer = StatisticsFragment.newInstance(null, null);

        planner = new Planner();
        planner.addTestValues();
        planner.selectDate(LocalDate.now());

        dailyPlanner.setPlanner(planner);
        activityPlanner.setPlanner(planner);
        activityPlanner.setActivity(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        // adding the ActivityPlanner fragment and hiding it
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, activityPlanner);
        transaction.hide(activityPlanner);

        // adding the statistics viewer fragment and hiding it
        transaction.add(R.id.fragment_container, statsViewer);
        transaction.hide(statsViewer);

        // adding the DailyPlanner fragment and showing it
        transaction.add(R.id.fragment_container, dailyPlanner);
        transaction.show(dailyPlanner);
        transaction.commit();
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void switchFragments(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(this.getVisibleFragment() != null)
            transaction.hide(this.getVisibleFragment());
        transaction.show(fragment);
        transaction.commit();
    }

    public DailyPlannerFragment getDailyPlannerFragment() {
        return dailyPlanner;
    }

    public void tapNavigationButton(int i) {
        BottomNavigationView navView = findViewById(R.id.mainactivity_navigation_view);
        switch(i) {
            case 0:
                navView.setSelectedItemId(R.id.dailyplanner_button);
                break;

            case 1:
                navView.setSelectedItemId(R.id.activityplanner_button);
                break;

            case 2:
                navView.setSelectedItemId(R.id.stats_button);
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
            case R.id.stats_button:
                this.switchFragments(statsViewer);
                toolbar.setTitle(R.string.statisticsviewer_title);
                return true;
        }
        return false;
    }

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
}
