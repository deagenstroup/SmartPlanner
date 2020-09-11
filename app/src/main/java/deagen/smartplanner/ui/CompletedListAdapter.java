package deagen.smartplanner.ui;

import androidx.constraintlayout.widget.ConstraintLayout;
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
        ListAdapter.ToDoTaskHolder vh = new ListAdapter.ToDoTaskHolder(v);
        return vh;
    }
}
