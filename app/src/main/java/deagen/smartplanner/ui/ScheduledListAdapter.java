package deagen.smartplanner.ui;

import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import deagen.smartplanner.MainActivity;
import deagen.smartplanner.R;
import deagen.smartplanner.fragments.DailyPlannerFragment;
import deagen.smartplanner.logic.Planner;
import deagen.smartplanner.logic.tasks.ScheduledToDoTask;

/**
 * A container for a list of tasks that have been scheduled into a day within the DailyPlanner
 */
public class ScheduledListAdapter extends SelectionListAdapter {

    public static class ScheduledHolder extends SelectionHolder {
        public TextView task, category, time;
        public ScheduledHolder(ConstraintLayout inLayout) {
            super(inLayout);
            layoutView = inLayout;
            task = layoutView.findViewById(R.id.task_text);
            category = layoutView.findViewById(R.id.category_text);
            time = layoutView.findViewById(R.id.time_text);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ScheduledListAdapter(Planner inPlanner, DailyPlannerFragment inFragment) {
        super(inPlanner, inFragment);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final SelectionHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ScheduledToDoTask schTask = planner.getScheduledTask(position);
        ScheduledHolder scheduledHolder = (ScheduledHolder) holder;
        scheduledHolder.task.setText(schTask.getName());
        scheduledHolder.category.setText(schTask.getCategory());
        if(schTask.getTimeRemaining() == null) {
            scheduledHolder.time.setText(schTask.getTimeSpentString());
        } else {
            scheduledHolder.time.setText(schTask.getTimeRemainingString());
        }
    }

    @Override
    public void selectHolder(SelectionHolder inHolder) {
        super.selectHolder(inHolder);
        ScheduledHolder scheduledHolder = (ScheduledHolder) selectedHolder;
        final DailyPlannerFragment dailyFragment = (DailyPlannerFragment) fragment;
        final int holderPosition = inHolder.getAdapterPosition();
        // add ClickListeners to each view within the Holder to allow modification of each
        // element
        scheduledHolder.task.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dailyFragment.changeTaskName(planner.getScheduledTask(holderPosition));
                ((MainActivity)dailyFragment.getActivity()).saveToFile();
                Log.d("CLICK DEBUG", "You have clicked the task text");
            }
        });
        scheduledHolder.category.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View view) {
               dailyFragment.changeTaskCategory(planner.getScheduledTask(holderPosition));
               ((MainActivity)dailyFragment.getActivity()).saveToFile();
               Log.d("CLICK DEBUG", "You have clicked the category text");
           }
        });
        scheduledHolder.time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dailyFragment.changeTaskTime(planner.getScheduledTask(holderPosition));
                ((MainActivity)dailyFragment.getActivity()).saveToFile();
                Log.d("CLICK DEBUG", "You have clicked the time text");
            }
        });
        dailyFragment.setDeleteButtonVisible(true);
    }

    public SelectionHolder unselectHolder() {
        SelectionHolder returnHolder = super.unselectHolder();
        if(returnHolder != null) {
            HolderListener listener = new HolderListener(returnHolder);
            ScheduledHolder scheduledHolder = (ScheduledHolder) returnHolder;
            scheduledHolder.task.setOnClickListener(listener);
            scheduledHolder.time.setOnClickListener(listener);
            scheduledHolder.category.setOnClickListener(listener);
        }
        ((DailyPlannerFragment) fragment).setDeleteButtonVisible(false);
        return returnHolder;
    }

    public void moveItem(int fromPos, int toPos) {
        // move the selected task to this position
        planner.moveScheduledTask(fromPos, toPos);
        ((MainActivity)fragment.getActivity()).saveToFile();
    }

    public void deleteCurrentItem() {
        int itemPosition = selectedHolder.getAdapterPosition();
        unselectHolder();
        planner.removeTask(itemPosition);
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(planner.getScheduledTasks() == null)
            return 0;
        return planner.getScheduledTasks().size();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ScheduledHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.task_container, parent, false);
        ScheduledHolder vh = new ScheduledHolder(v);
        return vh;
    }

}
