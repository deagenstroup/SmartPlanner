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
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    /**
     * Button used to start and stop active mode
     */
    private ImageButton startStopButton;

    private OnFragmentInteractionListener mListener;

    /**
     * Receiver to receive update UI messages from the TaskManagerService background service.
     */
    private BroadcastReceiver receiver;



    public DailyPlannerFragment() {
        // Required empty public constructor
    }

    public void setPlanner(Planner inPlanner) {
        planner = inPlanner;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("DEV", "onCreate called in the DailyPlannerFragment");

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_dailyplanner, container, false);

        constraintLayout = view.findViewById(R.id.dailyplanner_layout);

        scheduledListView = (RecyclerView) view.findViewById(R.id.scheduled_view);
        scheduledListView.setHasFixedSize(true);
        scheduledListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        scheduledListView.setAdapter(new ScheduledListAdapter(planner, this));

        completedListView = new RecyclerView(view.findViewById(R.id.list_container).getContext());
        completedListView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        completedListView.setHasFixedSize(true);
        completedListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        completedListView.setAdapter(new CompletedListAdapter(planner, this));

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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

        final ImageButton dateButton = (ImageButton) view.findViewById(R.id.date_button);
        LocalDate date = planner.getDate();
        ((TextView)view.findViewById(R.id.date_text)).setText("" + date.getMonthValue() + "/" + date.getDayOfMonth() + "/" + date.getYear());
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchDates();
            }
        });

        addButton = view.findViewById(R.id.dailyplanner_add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTask();
            }
        });

        deleteButton = view.findViewById(R.id.dailyplanner_delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ScheduledListAdapter)scheduledListView.getAdapter()).deleteCurrentItem();
            }
        });
        setDeleteButtonVisible(false);

        final DailyPlannerFragment fragment = this;
        startStopButton = view.findViewById(R.id.start_stop_button);
        startStopButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(planner.getTaskManager().isActive()) {
                    planner.getTaskManager().stopTasks();
                    startStopButton.setImageResource(android.R.drawable.ic_media_play);
                    addButton.show();
                    ((ScheduledListAdapter)scheduledListView.getAdapter()).setAllowOperations(true);
                    setCurrentTaskHighlight(false);
                } else {
                    planner.getTaskManager().startTasks(fragment);
                    startStopButton.setImageResource(android.R.drawable.ic_media_pause);
                    addButton.hide();
                    ((ScheduledListAdapter)scheduledListView.getAdapter()).setAllowOperations(false);
                    setCurrentTaskHighlight(true);
                }
            }
        });

        // registering the receiver
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(receiver,
                new IntentFilter(TaskManagerService.UPDATE_UI));

        return view;
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
        scheduledListView.getAdapter().notifyDataSetChanged();
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
                ((TextView)constraintLayout.findViewById(R.id.date_text)).setText("" + month + "/" + dayOfMonth + "/" + year);
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
            setDeleteButtonVisible(false);
            layout.addView(completedListView);
        } else {
            layout.removeView(completedListView);
            layout.addView(scheduledListView);
            setAddButtonVisible(true);
            setDeleteButtonVisible(true);
        }

        scheduledMode = !scheduledMode;
    }

    /**
     * Sets whether the delete button is visible on screen or not.
     * @param visible If true, delete button is visible, otherwise it is hidden.
     */
    public void setDeleteButtonVisible(boolean visible) {
        // if the user is trying to make the delete button visible, it doesn't already exist in
        // the layout, and there is a task selected for the user to delete, then add the button
        if(visible && constraintLayout.findViewById(R.id.dailyplanner_delete_button) == null
        && ((ScheduledListAdapter)scheduledListView.getAdapter()).hasSelected()) {
            constraintLayout.addView(deleteButton);
        }
        // otherwise if trying to make the delete button invisible, check to make sure it is actually
        // in the layout before attempting to remove it
        else if(!visible && constraintLayout.findViewById(R.id.dailyplanner_delete_button) != null) {
            constraintLayout.removeView(deleteButton);
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

    public void setCurrentTaskHighlight(boolean highlight) {
        ScheduledListAdapter.ScheduledHolder holder = (ScheduledListAdapter.ScheduledHolder) scheduledListView.findViewHolderForAdapterPosition(0);
        if(holder == null)
            return;
        if(!highlight) {
            holder.layoutView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            holder.layoutView.setBackgroundColor(getResources().getColor(R.color.green_selected));
        }
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
    public void changeTaskTime(final ScheduledToDoTask inTask) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener(){
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                inTask.setAllocatedTime(Duration.ofMinutes(minute + hourOfDay*60));
                scheduledListView.getAdapter().notifyDataSetChanged();
                ((ScheduledListAdapter)scheduledListView.getAdapter()).unselectHolder();
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this.getContext(), listener,
                hour, minute, true);
        timePickerDialog.setTitle("Choose allocated time:");
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();
    }

    /**
     * Prompts the user for information to create a new task and adds the new task to the planner
     */
    public void addNewTask() {
        ScheduledToDoTask task = new ScheduledToDoTask();
        this.changeTaskTime(task);
        this.changeTaskCategory(task);
        this.changeTaskName(task);
        planner.addTask(task);
        scheduledListView.getAdapter().notifyDataSetChanged();
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
}
