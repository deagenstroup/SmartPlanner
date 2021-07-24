package deagen.smartplanner.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;


import java.time.Duration;
import java.time.LocalDate;

import deagen.smartplanner.MainActivity;
import deagen.smartplanner.logic.tasks.ToDoTask;
import deagen.smartplanner.logic.taskscheduling.TaskManager;
import deagen.smartplanner.ui.CompletedListAdapter;
import deagen.smartplanner.R;
import deagen.smartplanner.ui.ScheduledListAdapter;
import deagen.smartplanner.service.TaskManagerService;
import deagen.smartplanner.logic.Planner;
import deagen.smartplanner.logic.tasks.ScheduledToDoTask;


/**
 * Fragment containing the DailyPlanner functionality of the app. This fragment is responsible
 * for allowing the user to display the activities of a selected day, manipulating the activities
 * planned within a specific day, and tracking activities as they are done.
 *
 * This is the reworked branch.
 */
public class DailyPlannerFragment extends Fragment {

    // logic members

    /**
     * Object which contains the main logic of the program
     */
    private Planner planner;

    /**
     * If true, then the user is currently looking at the scheduled tasks. If false, the user is
     * looking at the completed tabs
     */
    private boolean scheduledMode = true;

    private boolean completeButtonVisible = false;



    // GUI members

    /**
     * The top level view which contains all the UI components.
     */
    private View view;

    /**
     * The main layout which contains all other UI objects.
     */
    private ConstraintLayout constraintLayout;

    /**
     * Contain the lists of the scheduled activities as well as the completed activities for the
     * current day
     */
    private RecyclerView scheduledListView, completedListView;

    /**
     * Plus button used to add a task to the list of scheduled tasks
     */
    private FloatingActionButton addButton;

    /**
     * Minus button used to delete the currently selected task
     */
    private FloatingActionButton deleteButton;

    private FloatingActionButton revertTaskButton;

    /**
     * Receiver to receive update UI messages from the TaskManagerService background service.
     */
    private BroadcastReceiver receiver;



    public DailyPlannerFragment() {
        // Required empty public constructor
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
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initializing the receiver to update the fragment's UI upon receiving a message from
        // the TaskManagerService
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra(TaskManagerService.UPDATE_UI);
                if(message.equals(TaskManagerService.UPDATE)) {
                    DailyPlannerFragment.this.updateCurrentTask();
                } else if(message.equals(TaskManagerService.POST_UPDATE)) {
                    DailyPlannerFragment.this.postEndTaskUpdate();
                }
            }
        };

        planner.getTaskManager().setDailyPlannerFragment(this);
    }

    /**
     * Method where all of the GUI objects are initialized, including definitions of handler methods
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(@org.jetbrains.annotations.NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_dailyplanner, container, false);

        constraintLayout = view.findViewById(R.id.dailyplanner_layout);

        // building the container for the list of scheduled tasks
        scheduledListView = view.findViewById(R.id.scheduled_view);
        scheduledListView.setHasFixedSize(true);
        scheduledListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        scheduledListView.setAdapter(new ScheduledListAdapter(planner, this));

        // building the container for the list of completed tasks
        completedListView = new RecyclerView(view.findViewById(R.id.list_container).getContext());
        completedListView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        completedListView.setHasFixedSize(true);
        completedListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        completedListView.setAdapter(new CompletedListAdapter(planner, this));

        // initializing tab buttons for completed and scheduled list containers
        ((TabLayout)view.findViewById(R.id.tabLayout)).addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switchTabs();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // initializing the date displaying text to today's date
        ((MainActivity)getActivity()).getToolbar().setTitle(planner.getDateText());

        // adding the handler for "add a new task" button on the bottom right
        addButton = view.findViewById(R.id.dailyplanner_add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTask();
            }
        });

        // adding a handler for the "delete a task" button in the bottom right, and initializing
        // it to be invisible
        deleteButton = view.findViewById(R.id.dailyplanner_delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCurrentTask(true);
                setDefaultViewMode();
            }
        });

        revertTaskButton = view.findViewById(R.id.dailyplanner_revert_button);
        revertTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCurrentTask(false);
            }
        });
        setRemoveButtonsVisible(false);

        // registering the receiver
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(receiver,
                new IntentFilter(TaskManagerService.UPDATE_UI));

        this.setBackKeyHandler();

        // check for unsafe exit and restore daily pla nner to previous state if so
        if(planner.getTaskManager().checkForAbruptExit()) {
            planner.getTaskManager().restartTasks(this);
            setUIActiveMode(true);
            Log.d("TaskManager", "TaskManager was abruptly stopped.");
        }

        Log.d("DailyPlannerFragment", "DailyPlanner was created successfully.");
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden) {
            this.updateAppBar();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("DailyPlannerFragment", "DailyPlannerFragment is resumed.");
        this.updateAppBar();
    }



    /**
     * The handler which is attached to each of the check buttons inside each viewholder of the
     * scheduled list RecyclerView.
     */
    private ImageButton.OnClickListener completeButtonListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            setCompleteButtonVisible(false);
            planner.getTaskManager().finishTask();
            updateCurrentTask();
        }
    };

    public ImageButton.OnClickListener getCompleteButtonListener() {
        return completeButtonListener;
    }

    public ScheduledListAdapter getScheduledListAdapter() {
        return (ScheduledListAdapter) scheduledListView.getAdapter();
    }

    /**
     * Removes the currently selected task from the scheduled list and either deletes it or
     * returns in to the activity planner.
     * @param delete If true, the task is deleted entirely, otherwise returned to the activity planner.
     */
    public void removeCurrentTask(boolean delete) {
        ScheduledToDoTask task = ((ScheduledListAdapter)scheduledListView.getAdapter()).deleteCurrentItem();
        if(!delete) {
            planner.getActivityPlanner().addTask(new ToDoTask(task));
        }
        ((MainActivity)getActivity()).getActivityPlannerFragment().updateUIList();
        ((MainActivity)getActivity()).saveToFile();
    }

    public void setPlanner(Planner inPlanner) {
        planner = inPlanner;
    }

    public Planner getPlanner() {return planner;}



    // UI updater methods

    /**
     * Called directly after the logical current task has been updated in order to update
     * the UI to reflect the change.
     */
    public void postEndTaskUpdate() {
        // highlighting the new current task
        setCurrentTaskHighlight(true);
    }

    public void updateCurrentTask() {
        if(planner.getTaskManager().isActive())
            setCurrentTaskHighlight(true);

        scheduledListView.getAdapter().notifyDataSetChanged();
        if(completeButtonVisible)
            setCompleteButtonVisible(true);
    }


    // UI manipulation methods

    /**
     * Displays a date picker dialog to the user and changes the date of the app accordingly
     */
    public void switchDates() {

        // if the app is in active mode, do nothing
        if(planner.getTaskManager().isActive())
            return;

        // creating the listener which will change the date in the planner and update the UI accordingly
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++;
                planner.selectDate(LocalDate.of(year, month, dayOfMonth));
                ((MainActivity)getActivity()).getToolbar().setTitle(planner.getDateText());
                scheduledListView.getAdapter().notifyDataSetChanged();
                completedListView.getAdapter().notifyDataSetChanged();
            }
        };

        // initializing and displaying the dialog for selecting the date
        LocalDate currentDate = planner.getDate();
        DatePickerDialog picker = new DatePickerDialog(this.getContext(), listener,
                currentDate.getYear(), currentDate.getMonthValue()-1, currentDate.getDayOfMonth());
        picker.show();
    }

    /**
     * Switches between the scheduled tasks and the completed tasks view
     */
    public void switchTabs() {
        // getting the container for each list
        FrameLayout layout = view.findViewById(R.id.list_container);

        // swapping out the respective lists within the container and adjusting ui accordingly
        if(scheduledMode) {
            layout.removeView(scheduledListView);
            setAddButtonVisible(false);
            setRemoveButtonsVisible(false);
            layout.addView(completedListView);
        } else {
            layout.removeView(completedListView);
            layout.addView(scheduledListView);
            setAddButtonVisible(true);
            setRemoveButtonsVisible(true);
        }

        scheduledMode = !scheduledMode;
    }

    /**
     * Stops the DailyPlanner if it is running and resets the list to its default state, with
     * no highlighted tasks, completion, or extend buttons
     */
    public void setDefaultViewMode() {
        if(planner.getTaskManager().isActive())
            toggleActiveMode();
        getScheduledListAdapter().unselectHolder();
        setCurrentTaskHighlight(false);

        // Get GUI container which displays the first task
        ScheduledListAdapter.ScheduledHolder holder =
                (ScheduledListAdapter.ScheduledHolder) scheduledListView.findViewHolderForAdapterPosition(0);
        if(holder != null)
            getScheduledListAdapter().setCompleteButtonVisible(holder, false);
    }

    /**
     * Switches the application in and out of active tracking mode.
     */
    public void toggleActiveMode() {
        if(planner.getTaskManager().isActive()) {
            planner.getTaskManager().stopTasks();
            setUIActiveMode(false);
        } else {
            if(!planner.getTaskManager().startTasks(this))
                return;
            setUIActiveMode(true);
        }
        ((MainActivity)getActivity()).saveToFile();
    }

    /**
     * Sets up all of the gui objects to either reflect active mode or non-active mode based on the
     * parameter.
     * @param activeMode If true the GUI objects in the DailyPlanner reflect active status, otherwise
     *                   they represent non-active (paused) status.
     */
    public void setUIActiveMode(boolean activeMode) {
        if(((MainActivity)getActivity()).getAppBarMenu() != null)
            ((MainActivity)getActivity()).updateToggleButtonImage();

        if(activeMode) {
            addButton.hide();
            ((ScheduledListAdapter)scheduledListView
                    .getAdapter())
                    .setAllowOperations(false);
            setCurrentTaskHighlight(true);
            setCompleteButtonVisible(false);
        } else {
            addButton.show();
            ((ScheduledListAdapter)scheduledListView
                    .getAdapter())
                    .setAllowOperations(true);
            setCurrentTaskHighlight(false);
            setCompleteButtonVisible(true);
            showCurrentTaskTime(true);
        }
    }

    /**
     * Sets whether the delete button is visible on screen or not.
     * @param visible If true, delete button is visible, otherwise it is hidden.
     */
    public void setRemoveButtonsVisible(boolean visible) {
        // if the user is trying to make the delete button visible, it doesn't already exist in
        // the layout, and there is a task selected for the user to delete, then add the button
        if(visible && ((ScheduledListAdapter)scheduledListView.getAdapter()).hasSelected()) {
            if(constraintLayout.findViewById(R.id.dailyplanner_delete_button) == null)
                constraintLayout.addView(deleteButton);
            if(constraintLayout.findViewById(R.id.dailyplanner_revert_button) == null)
                constraintLayout.addView(revertTaskButton);
        }
        // otherwise if trying to make the delete button invisible, check to make sure it is actually
        // in the layout before attempting to remove it
        else if(!visible) {
            if(constraintLayout.findViewById(R.id.dailyplanner_delete_button) != null)
                constraintLayout.removeView(deleteButton);
            if(constraintLayout.findViewById(R.id.dailyplanner_revert_button) != null)
                constraintLayout.removeView(revertTaskButton);
        }
    }

    /**
     * Sets whether the add button is visible to the user or not.
     * @param visible If true, button is visible, otherwise false.
     */
    public void setAddButtonVisible(boolean visible) {
        // if trying to make the add button visible and it doesn't already exist in layout, then add the button
        if(visible && constraintLayout.findViewById(R.id.dailyplanner_add_button) == null) {
            constraintLayout.addView(addButton);
        }
        // if trying to make the add button invisible and it does already exist in
        // the layout, then add the button
        else if(!visible && constraintLayout.findViewById(R.id.dailyplanner_add_button) != null) {
            constraintLayout.removeView(addButton);
        }
    }

    /**
     * Changes the handler of the back key, so that when pressed, the application is properly saved
     * before exitting.
     */
    public void setBackKeyHandler() {
        final MainActivity mainActivity = (MainActivity)this.getActivity();
        mainActivity.setOnBackKeyListener(new MainActivity.BackKeyListener() {
            @Override
            public void onBackKeyPressed() {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                dialogBuilder.setTitle("Are you sure you would like to exit?");
                dialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        planner.getTaskManager().stopService();
                        updateCurrentTask();
                        mainActivity.saveToFile();
                        mainActivity.finish();
                    }
                });
                dialogBuilder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialogBuilder.show();
            }
        });
    }

    public void setCompleteButtonVisible(boolean visible) {
        ScheduledListAdapter adapter = this.getScheduledListAdapter();
        if(adapter != null) {
            ScheduledListAdapter.ScheduledHolder holder = (ScheduledListAdapter.ScheduledHolder) this.scheduledListView.findViewHolderForAdapterPosition(0);
            adapter.setCompleteButtonVisible(holder, visible);
            completeButtonVisible = visible;
//            adapter.notifyItemChanged(0);
        }

//        if(completeButton != null) {
//            if(visible) {
//                completeButton.show();
//            } else {
//                completeButton.hide();
//            }
//        }

//        if(visible)
//            showCurrentTaskTime(false);
//        else
//            showCurrentTaskTime(true);
    }

    /**
     * Sets the task at the top of the current list to be highlighted or not
     * @param highlight If true, task is hightlighted green
     */
    public void setCurrentTaskHighlight(boolean highlight) {
        ScheduledListAdapter.ScheduledHolder holder = (ScheduledListAdapter.ScheduledHolder) scheduledListView.findViewHolderForAdapterPosition(0);
        if(holder == null)
            return;
        if(!highlight) {
            holder.layoutView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            holder.layoutView.setBackgroundColor(getResources().getColor(R.color.blue_selected));
        }
    }

    /**
     * Shows/Hides the time text for the current task.
     * @param visible If true, time text is shown, hidden if false.
     */
    public void showCurrentTaskTime(boolean visible) {
        ScheduledListAdapter.ScheduledHolder holder = (ScheduledListAdapter.ScheduledHolder) scheduledListView.findViewHolderForAdapterPosition(0);
        if(holder == null)
            return;
        if(visible)
            holder.time.setVisibility(View.VISIBLE);
        else
            holder.time.setVisibility(View.INVISIBLE);
    }

    public void updateAppBar() {
        ((MainActivity)getActivity()).getToolbar().setTitle(planner.getDateText());
    }


    // dialog methods for task manipulation

    /**
     * Prompt the user for a dialog to change the name of the task provided.
     * @param inTask
     */
    public void changeTaskName(final ScheduledToDoTask inTask) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Input task name: ");
        final EditText input = new EditText(builder.getContext());
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inTask.setName(input.getText().toString());
                scheduledListView.getAdapter().notifyDataSetChanged();
                ((ScheduledListAdapter)scheduledListView.getAdapter()).unselectHolder();
                ((MainActivity)getActivity()).saveToFile();
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

    /**
     * prompt the user for a dialog to change the category of the task provided
     * @param inTask
     */
    public void changeTaskCategory(final ScheduledToDoTask inTask) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Choose category for task: ");
        final int selectedPosition = 0;

        final String[] categories = planner.getActivityPlanner().getCategories();
        final Spinner spinner = new Spinner(builder.getContext());
        ArrayAdapter arrayAdapter = new ArrayAdapter(builder.getContext(), android.R.layout.simple_spinner_item, categories);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //inTask.setCategory(categories[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        builder.setView(spinner);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inTask.setCategory(spinner.getSelectedItem().toString());
                scheduledListView.getAdapter().notifyDataSetChanged();
                ((ScheduledListAdapter)scheduledListView.getAdapter()).unselectHolder();
                ((MainActivity)getActivity()).saveToFile();
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

    /**
     * Prompt the user for a dialog to change the amount of allocated time for the task provided
     * @param inTask
     */
    public void changeTaskTime(final ScheduledToDoTask inTask, final boolean toggleTime) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener(){
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Duration setTime = Duration.ofMinutes(minute + hourOfDay*60);
                inTask.allocateMoreTime(setTime);
                scheduledListView.getAdapter().notifyDataSetChanged();
                ((ScheduledListAdapter)scheduledListView.getAdapter()).unselectHolder();
                ((MainActivity)getActivity()).saveToFile();
                if(toggleTime)
                    toggleActiveMode();
            }
        };
        TimePickerDialog timePickerDialog =
                new TimePickerDialog(this.getContext(),
                                    TimePickerDialog.THEME_HOLO_LIGHT,
                                    listener,
                                    0,
                                    0,
                         true);
        timePickerDialog.setTitle("Choose allocated time:");
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();
    }

    /**
     * Asks the user if they would like to specify a time limit for the provided task and provides
     * them with a prompt to set said time limit if they answer yes.
     * @param inTask The task in question.
     */
    public void askForTaskTime(final ScheduledToDoTask inTask) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Do you want to specify a time limit for this task?");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeTaskTime(inTask, false);
                scheduledListView.getAdapter().notifyDataSetChanged();
                ((MainActivity)getActivity()).saveToFile();
            }
        });
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inTask.setAllocatedTime(null);
                scheduledListView.getAdapter().notifyDataSetChanged();
                ((MainActivity)getActivity()).saveToFile();
            }
        });
        builder.show();
    }

    /**
     * Pauses the current task if active and adds a break task with the user's specified time.
     */
    public void startBreakTask() {
        if(planner.getTaskManager().isActive()) {
            this.toggleActiveMode();
        }
        this.setDefaultViewMode();
        ScheduledToDoTask task = new ScheduledToDoTask();
        task.setName("break");
        task.setCategory("break");
        changeTaskTime(task, true);
        planner.addTask(task);
        scheduledListView.getAdapter().notifyDataSetChanged();
    }

    /**
     * Prompts the user for information to create a new task and adds the new task to the planner
     */
    public void addNewTask() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Would you like to schedule an existing task?");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity activity = (MainActivity)getActivity();
                activity.tapNavigationButton(1);
            }
        });
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ScheduledToDoTask task = new ScheduledToDoTask();
                askForTaskTime(task);
                changeTaskCategory(task);
                changeTaskName(task);
                planner.addTask(task);
                scheduledListView.getAdapter().notifyDataSetChanged();
            }
        });
        builder.show();

//        ScheduledToDoTask task = new ScheduledToDoTask();
//        this.changeTaskTime(task);
//        this.changeTaskCategory(task);
//        this.changeTaskName(task);
//        planner.addTask(task);
//        scheduledListView.getAdapter().notifyDataSetChanged();
    }
}
