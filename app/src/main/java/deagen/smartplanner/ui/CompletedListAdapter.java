package deagen.smartplanner.ui;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import deagen.smartplanner.R;
import deagen.smartplanner.fragments.DailyPlannerFragment;
import deagen.smartplanner.logic.Planner;
import deagen.smartplanner.logic.tasks.CompletedToDoTask;

public class CompletedListAdapter extends ListAdapter {

    public CompletedListAdapter(Planner inPlanner, DailyPlannerFragment inFragment) {
        super(inPlanner, inFragment);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ListAdapter.ToDoTaskHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        CompletedToDoTask comTask = planner.getCompletedTask(position);
        holder.task.setText(comTask.getName());
        holder.category.setText(comTask.getCategory());
        holder.time.setText(comTask.getTimeSpentString());

        // Changing the constraints on the time text and category text to be constrained to the
        // right of the time text
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(holder.layoutView);
        constraintSet.connect(R.id.task_text, ConstraintSet.END, R.id.time_text, ConstraintSet.START);
        constraintSet.connect(R.id.category_text, ConstraintSet.END, R.id.time_text, ConstraintSet.START);
        if(comTask.getTimeSpentString().equals("0:00"))
            constraintSet.setVisibility(R.id.time_text, ConstraintSet.GONE);
        constraintSet.applyTo(holder.layoutView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(planner.getCompletedTasks() == null)
            return 0;
        return planner.getCompletedTasks().size();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ListAdapter.ToDoTaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.task_container, parent, false);

        // Creating a task holder object from the inflated layout
        ListAdapter.ToDoTaskHolder vh = new ListAdapter.ToDoTaskHolder(v);
        vh.layoutView.removeView(vh.layoutView.findViewById(R.id.check_button));

        // Changing the constraints on the time text and category text to be constrained to the
        // right of the time text
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(vh.layoutView);
        constraintSet.connect(R.id.task_text, ConstraintSet.END, R.id.time_text, ConstraintSet.START);
        constraintSet.connect(R.id.category_text, ConstraintSet.END, R.id.time_text, ConstraintSet.START);
        constraintSet.applyTo(vh.layoutView);

        return vh;
    }
}
