package deagen.smartplanner.ui;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
        public ImageButton checkButton;
        public ScheduledHolder(ConstraintLayout inLayout) {
            super(inLayout);
            layoutView = inLayout;
            task = layoutView.findViewById(R.id.task_text);
            category = layoutView.findViewById(R.id.category_text);
            time = layoutView.findViewById(R.id.time_text);
            checkButton = layoutView.findViewById(R.id.check_button);
        }
    }



    // Provide a suitable constructor (depends on the kind of dataset)
    public ScheduledListAdapter(Planner inPlanner, DailyPlannerFragment inFragment) {
        super(inPlanner, inFragment);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ScheduledHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.task_container, parent, false);
        ScheduledHolder vh = new ScheduledHolder(v);
        this.setCompleteButtonVisible(vh, false);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final SelectionHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ScheduledToDoTask schTask = planner.getScheduledTask(position);
        ScheduledHolder scheduledHolder = (ScheduledHolder) holder;

        String currentTaskName = planner.getTaskManager().getCurrentTask().getName();
        Log.d("ScheduledListAdapter", "adapterposition: " + holder.getAdapterPosition() + " position: " + position + " holder text: " + scheduledHolder.task.getText().toString());
        //if(position == 0 && scheduledHolder.task.getText().toString().equals(currentTaskName)) {

        if(schTask.equals(planner.getTaskManager().getCurrentTask())) {
            if(((DailyPlannerFragment)fragment).getPlanner().getTaskManager().isActive())
                holder.layoutView.setBackgroundColor(fragment.getResources().getColor(R.color.blue_selected));
        }

        scheduledHolder.task.setText(schTask.getName());
        scheduledHolder.category.setText(schTask.getCategory());

        // If the scheduled task does not have a specified time period of completion
        if(schTask.isUntimedTask()) {
            // If the task has not been worked on yet
            if(schTask.getTimeSpentString().equals("0:00")) {
                // Remove the time text from the view
                setTimeTextVisible(scheduledHolder, false);
                // Otherwise if it has been worked on and the view is not currently in the container
            } else {
                setTimeTextVisible(scheduledHolder, true);
                scheduledHolder.time.setText(schTask.getTimeSpentString());
            }
        } else {
            setTimeTextVisible(scheduledHolder, true);
            scheduledHolder.time.setText(schTask.getTimeRemainingString());
        }
        scheduledHolder.checkButton.setOnClickListener(((DailyPlannerFragment)fragment).getCompleteButtonListener());
//        this.resetTextConstraints((ScheduledHolder)holder);
//        scheduledHolder.task.setMaxWidth(this.getMaxTextWidth((ScheduledHolder)holder));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(planner.getScheduledTasks() == null)
            return 0;
        return planner.getScheduledTasks().size();
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
                dailyFragment.changeTaskTime(planner.getScheduledTask(holderPosition), false);
                ((MainActivity)dailyFragment.getActivity()).saveToFile();
                Log.d("CLICK DEBUG", "You have clicked the time text");
            }
        });
        dailyFragment.setRemoveButtonsVisible(true);
        setCompleteButtonVisible( (ScheduledHolder) inHolder, true);
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
        ((DailyPlannerFragment) fragment).setRemoveButtonsVisible(false);
        setCompleteButtonVisible( (ScheduledHolder) returnHolder, false);
        return returnHolder;
    }

    public void moveItem(int fromPos, int toPos) {
        this.setCompleteButtonVisible((ScheduledHolder)selectedHolder, false);
        this.setCompleteButtonVisible(((ScheduledHolder)clickedHolder), false);

        // move the selected task to this position
        planner.moveScheduledTask(fromPos, toPos);
        ((MainActivity)fragment.getActivity()).saveToFile();
        planner.printScheduledTasks();

    }

    public ScheduledToDoTask deleteCurrentItem() {
        int itemPosition = selectedHolder.getAdapterPosition();
        unselectHolder();
        ScheduledToDoTask task = planner.removeTask(itemPosition);
        notifyDataSetChanged();
        return task;
    }

    /**
     * Sets a view that is a child of the provided holder, such as the time text or complete button,
     * to be visible or invisible.
     * @param holder The ScheduledHolder which contains the child view.
     * @param viewId The ID of the child view within the ScheduledHolder whose visibility is to be set.
     * @param visible If true, the child view is set to visible. Otherwise, it is set to be invisible.
     */
    public void setChildViewVisiblity(ScheduledHolder holder, int viewId, boolean visible) {
        if(holder == null)
            return;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(holder.layoutView);
        if(visible) {
            constraintSet.setVisibility(viewId, ConstraintSet.VISIBLE);
        } else {
            constraintSet.setVisibility(viewId, ConstraintSet.GONE);
        }
        constraintSet.applyTo(holder.layoutView);
    }

    public void setTimeTextVisible(ScheduledHolder holder, boolean visible) {
        setChildViewVisiblity(holder, R.id.time_text, visible);
    }

    public void setCompleteButtonVisible(ScheduledHolder holder, boolean visible) {
        setChildViewVisiblity(holder, R.id.check_button, visible);
    }

//    public void resetTextConstraints(ScheduledHolder holder) {
//        ConstraintSet constraintSet = new ConstraintSet();
//        constraintSet.clone(holder.layoutView);
//        if(holder.layoutView.findViewById(R.id.check_button) != null) {
//            constraintSet.connect(R.id.task_text, ConstraintSet.END, R.id.check_button, ConstraintSet.START);
//            constraintSet.connect(R.id.category_text, ConstraintSet.END, R.id.check_button, ConstraintSet.START);
//        } else {
//            constraintSet.connect(R.id.task_text, ConstraintSet.END, R.id.time_text, ConstraintSet.START);
//            constraintSet.connect(R.id.category_text, ConstraintSet.END, R.id.time_text, ConstraintSet.START);
//        }
//        constraintSet.applyTo(holder.layoutView);
//    }
//    public int getMaxTextWidth(ScheduledHolder inHolder) {
//        return super.getMaxTextWidth(inHolder) - this.getExtraComponentWidth(inHolder);
//    }
//
//    public int getExtraComponentWidth(ScheduledHolder inHolder) {
//        int extraWidth = 0;
//        View extraView = inHolder.layoutView.findViewById(R.id.check_button);
//        if(extraView != null)
//            extraWidth += extraView.getWidth();
//        extraView = inHolder.layoutView.findViewById(R.id.time_text);
//        if(extraView != null)
//            extraWidth += extraView.getWidth();
//        return extraWidth + 64;
//    }

}
