package deagen.smartplanner.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.time.Duration;

import deagen.smartplanner.logic.taskplanning.ActivityCategory;
import deagen.smartplanner.logic.tasks.ScheduledToDoTask;
import deagen.smartplanner.ui.CategoryListAdapter;
import deagen.smartplanner.MainActivity;
import deagen.smartplanner.R;
import deagen.smartplanner.ui.TaskListAdapter;
import deagen.smartplanner.logic.Planner;
import deagen.smartplanner.logic.tasks.ToDoTask;


/**
 * Fragment containing the ActivityPlanner functionality of the app. This fragment is responsible
 * for allowing the user to store, view, and manipulate tasks as well as the categories they fall
 * into. Tasks are stored here until they are scheduled into the DailyPlanner.
 */
public class ActivityPlannerFragment extends Fragment {

    /**
     * Object which contains the main logic of the program
     */
    private Planner planner;

    private MainActivity mainActivity;

    /**
     * The main layout which contains all other UI objects.
     */
    private ConstraintLayout constraintLayout;

    /**
     * Container for the different lists which can be displayed to the user, the list of
     * categories and the list of tasks
     */
    private FrameLayout recycleViewContainer;

    /**
     * Graphical list of all of the categories of tasks.
     */
    private RecyclerView categoryView;

    /**
     * Graphical list of all of the tasks of the selected category
     */
    private RecyclerView taskView;

    /**
     * Buttons in for adding and deleting tasks and task categories
     */
    private FloatingActionButton addButton, deleteButton, scheduleButton;

    private int selectedCategoryPosition = -1;

    private OnFragmentInteractionListener mListener;

    public ActivityPlannerFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        constraintLayout = (ConstraintLayout) inflater.inflate(R.layout.fragment_activityplanner, container, false);

        recycleViewContainer = constraintLayout.findViewById(R.id.activityplanner_recycle_container);

        // initializing the graphical category list
        categoryView = new RecyclerView(recycleViewContainer.getContext());
        categoryView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        categoryView.setHasFixedSize(true);
        categoryView.setLayoutManager(new LinearLayoutManager(getActivity()));
        categoryView.setAdapter(new CategoryListAdapter(planner, this));

        // initializing the add and delete buttons and making the delete button invisible initially
        addButton = constraintLayout.findViewById(R.id.activityplanner_add_button);
        deleteButton = constraintLayout.findViewById(R.id.activityplanner_delete_button);
        scheduleButton = constraintLayout.findViewById(R.id.activityplanner_schedule_button);
        setEditButtonsVisible(false);

        // setting the list of categories to be displayed
        setCategoryView();

        return constraintLayout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void setPlanner(Planner inPlanner) {
        planner = inPlanner;
    }

    public void setActivity(MainActivity inActivity) {
        mainActivity = inActivity;
    }

    /**
     * Sets whether the edit buttons for the task (schedule & delete) are visible on screen or not.
     * @param visible If true, buttons are visible, otherwise they are hidden.
     */
    public void setEditButtonsVisible(boolean visible) {
//        if(visible && constraintLayout.findViewById(R.id.activityplanner_delete_button) == null) {
//            constraintLayout.addView(deleteButton);
//        } else {
//            constraintLayout.removeView(deleteButton);
//        }
        if(visible) {
            ((View) deleteButton).setVisibility(View.VISIBLE);
            ((View) scheduleButton).setVisibility(View.VISIBLE);
        } else {
            ((View) deleteButton).setVisibility(View.GONE);
            ((View) scheduleButton).setVisibility(View.GONE);
        }
    }

    public void setDeleteButtonVisible(boolean visible) {
        if(visible) {
            ((View) deleteButton).setVisibility(View.VISIBLE);
        } else {
            ((View) deleteButton).setVisibility(View.GONE);
        }
    }

    /**
     * Sets the activity to list categories for the user to select.
     */
    public void setCategoryView() {
        mainActivity.setOnBackKeyListener(null);
        recycleViewContainer.addView(categoryView);
        ((TextView)constraintLayout.findViewById(R.id.activityplanner_title_text)).setText(R.string.task_categories_title);

        // setting the listeners for the add and delete buttons
        addButton.setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.d("DEV", "add button pressed - add a task category");
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CategoryListAdapter adapter = ((CategoryListAdapter)categoryView.getAdapter());
                int removePosition = selectedCategoryPosition;
                adapter.unselectHolder();
                planner.getActivityPlanner().removeActivityCategory(removePosition);
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Open the category located at the position specified and display the tasks from that category.
     * @param position The location of the category within the list of categories contained in
     *                 the planner object.
     */
    public void openCategory(int position) {
//        Log.d("CATEGORY SELECTION", "category position: " + position);
        selectedCategoryPosition = position;

        // adding the task list to the screen
        recycleViewContainer.removeView(categoryView);
        taskView = new RecyclerView(recycleViewContainer.getContext());
        taskView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        taskView.setHasFixedSize(true);
        taskView.setLayoutManager(new LinearLayoutManager(getActivity()));
        taskView.setAdapter(new TaskListAdapter(planner, this));
        recycleViewContainer.addView(taskView);

        // changing the title at the top
        String titleString = planner.getActivityPlanner().getCategories()[selectedCategoryPosition];
        ((TextView)constraintLayout.findViewById(R.id.activityplanner_title_text)).setText(titleString);

        // setting the listener for the back key so that the user can go back to the main menu
        mainActivity.setOnBackKeyListener(new MainActivity.BackKeyListener() {
            @Override
            public void onBackKeyPressed() {
                recycleViewContainer.removeView(taskView);
                setCategoryView();
                setEditButtonsVisible(false);
            }
        });

        // setting the listeners for the add and delete buttons
        addButton.setOnClickListener(new FloatingActionButton.OnClickListener() {
           @Override
           public void onClick(View view) {
               // creating a dialog to get task info from the user
               AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
               final ToDoTask task = new ToDoTask();
               builder.setTitle("Input task name: ");
               final EditText input = new EditText(builder.getContext());
               builder.setView(input);

               // setting listeners for the buttons of the dialog
               builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       task.setName(input.getText().toString());
                       // adding the task to the selected category
                       planner.getActivityPlanner().getActivityCategory(selectedCategoryPosition).addToDoTask(task);
                       taskView.getAdapter().notifyDataSetChanged();
                   }
               });
               builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.cancel();
                   }
               });
               builder.show();
           }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // unselecting the selected task and removing from the planner
                TaskListAdapter adapter = ((TaskListAdapter)taskView.getAdapter());
                getSelectedActivityCategory().removeTask(adapter.getSelectedHolderPosition());
                adapter.unselectHolder();
                adapter.notifyDataSetChanged();
            }
        });
        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskListAdapter adapter = ((TaskListAdapter) taskView.getAdapter());
                ToDoTask task = getSelectedActivityCategory().getTask(adapter.getSelectedHolderPosition());
                ScheduledToDoTask scheduledTask = new ScheduledToDoTask(task, Duration.ofMinutes(10L));
                mainActivity.switchFragments(mainActivity.getDailyPlannerFragment());
                mainActivity.tapNavigationButton(0);
                mainActivity.getDailyPlannerFragment().changeTaskTime(scheduledTask);
                planner.addTask(scheduledTask);
                getSelectedActivityCategory().removeTask(adapter.getSelectedHolderPosition());
                adapter.unselectHolder();
                adapter.notifyDataSetChanged();
                mainActivity.getDailyPlannerFragment().updateCurrentTask();
            }
        });
    }

    /**
     * Adds the currently selected task into the current DailyPlanner schedule and removes it from
     * the ActivityPlanner.
     */
    public void scheduleSelectedTask() {

    }

    public void addTaskCategory() {

    }

    public void addTask() {

    }

    public ActivityCategory getSelectedActivityCategory() {
        //if the user is in the category selection screen then no category is currently selected
        if(selectedCategoryPosition < 0)
            return null;

        return planner.getActivityPlanner().getActivityCategory(selectedCategoryPosition);
    }

    public int getSelectedCategoryPosition() {
        return selectedCategoryPosition;
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
}
