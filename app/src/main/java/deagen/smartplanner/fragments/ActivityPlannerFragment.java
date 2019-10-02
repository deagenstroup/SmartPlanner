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

import deagen.smartplanner.ui.CategoryListAdapter;
import deagen.smartplanner.MainActivity;
import deagen.smartplanner.R;
import deagen.smartplanner.ui.TaskListAdapter;
import deagen.smartplanner.logic.Planner;
import deagen.smartplanner.logic.ToDoTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ActivityPlannerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ActivityPlannerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActivityPlannerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private MainActivity mainActivity;

    private Planner planner;

    private ConstraintLayout topView;

    private TextView titleText;

    private FrameLayout recycleViewContainer;

    private RecyclerView categoryView;

    private RecyclerView taskView;

    private FloatingActionButton addButton, deleteButton;

    private int selectedCategoryPosition;

    public ActivityPlannerFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ActivityPlannerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ActivityPlannerFragment newInstance(String param1, String param2) {
        ActivityPlannerFragment fragment = new ActivityPlannerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        topView = (ConstraintLayout) inflater.inflate(R.layout.fragment_activityplanner, container, false);

        titleText = topView.findViewById(R.id.activityplanner_title_text);

        recycleViewContainer = topView.findViewById(R.id.activityplanner_recycle_container);

        categoryView = new RecyclerView(recycleViewContainer.getContext());
        categoryView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        categoryView.setHasFixedSize(true);
        categoryView.setLayoutManager(new LinearLayoutManager(getActivity()));
        categoryView.setAdapter(new CategoryListAdapter(planner, this));

        addButton = topView.findViewById(R.id.activityplanner_add_button);
        deleteButton = topView.findViewById(R.id.activityplanner_delete_button);
        setDeleteButtonVisible(false);

        setCategoryView();

        return topView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    /**
     * Sets whether the delete button is visible on screen or not.
     * @param visible If true, delete button is visible, otherwise it is hidden.
     */
    public void setDeleteButtonVisible(boolean visible) {
        if(visible && topView.findViewById(R.id.activityplanner_delete_button) == null) {
            topView.addView(deleteButton);
        } else {
            topView.removeView(deleteButton);
        }
    }

    /**
     * Sets the activity to list categories for the user to select.
     */
    public void setCategoryView() {
        mainActivity.setOnBackKeyListener(null);
        recycleViewContainer.addView(categoryView);
        titleText.setText(R.string.task_categories_title);

        // setting the listeners for the add and delete buttons
        addButton.setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DEV", "add button pressed - add a task category");
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEV", "delete button pressed - delete a task category");
                CategoryListAdapter adapter = ((CategoryListAdapter)categoryView.getAdapter());
                int removePosition = selectedCategoryPosition;
                adapter.unselectHolder();
                planner.removeActivityCategory(removePosition);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void openCategory(int position) {
        Log.d("CATEGORY SELECTION", "category position: " + position);
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
        titleText.setText(planner.getCategories()[selectedCategoryPosition]);

        // setting the listener for the back key so that the user can go back to the main menu
        mainActivity.setOnBackKeyListener(new MainActivity.BackKeyListener() {
            @Override
            public void onBackKeyPressed() {
                recycleViewContainer.removeView(taskView);
                setCategoryView();
                setDeleteButtonVisible(false);
            }
        });

        // setting the listeners for the add and delete buttons
        addButton.setOnClickListener(new FloatingActionButton.OnClickListener() {
           @Override
           public void onClick(View view) {
               Log.d("DEV", "add button pressed - add a task");
               AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
               final ToDoTask task = new ToDoTask();
               builder.setTitle("Input task name: ");
               final EditText input = new EditText(builder.getContext());
               builder.setView(input);
               builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       task.setName(input.getText().toString());
                       planner.addTaskToCategory(selectedCategoryPosition, task);
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
                Log.d("DEV", "delete button pressed - delete a task");
                TaskListAdapter adapter = ((TaskListAdapter)taskView.getAdapter());
                int selectedTaskPosition = adapter.getSelectedHolderPosition();
                adapter.unselectHolder();
                planner.removeTaskFromCategory(selectedCategoryPosition, selectedTaskPosition);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void setPlanner(Planner inPlanner) {
        planner = inPlanner;
    }

    public void setActivity(MainActivity inActivity) {
        mainActivity = inActivity;
    }

    public void addTaskCategory() {

    }

    public void addTask() {

    }

    public int getSelectedCategoryPosition() {
        return selectedCategoryPosition;
    }
}
