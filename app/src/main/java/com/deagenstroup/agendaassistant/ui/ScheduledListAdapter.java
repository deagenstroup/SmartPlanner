package com.deagenstroup.agendaassistant.ui;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.deagenstroup.agendaassistant.MainActivity;
import com.deagenstroup.agendaassistant.R;
import com.deagenstroup.agendaassistant.fragments.DailyPlannerFragment;
import com.deagenstroup.agendaassistant.logic.Planner;
import com.deagenstroup.agendaassistant.logic.tasks.ScheduledToDoTask;

/**
 * A container for a list of tasks that have been scheduled into a day within the DailyPlanner
 */
public class ScheduledListAdapter extends SelectionListAdapter {

    public static class ScheduledHolder extends SelectionHolder {
        public TextView task, category, time;
        public ImageButton checkButton, deselectButton;
        public ScheduledHolder(ConstraintLayout inLayout) {
            super(inLayout);
            layoutView = inLayout;
            task = layoutView.findViewById(R.id.task_text);
            category = layoutView.findViewById(R.id.category_text);
            time = layoutView.findViewById(R.id.time_text);
            checkButton = layoutView.findViewById(R.id.check_button);
            deselectButton = layoutView.findViewById(R.id.deselect_button);
        }
    }

    protected class ScheduledHolderListener extends SelectionListAdapter.HolderListener {

        public ScheduledHolderListener(ScheduledHolder inHolder) {
            super(inHolder);
        }

        @Override
        public void onClick(View view) {
            clickedHolder = this.holder;
            if (!allowOperations)
                return;

            if(selectedHolder == clickedHolder) {
                unselectHolder();
            }
            // If there is a task selected already, that is different, move selected task to
            // position of the item which was clicked.
            else if(selectedHolder != null) {
                moveItem(selectedHolder.getAdapterPosition(), holder.getAdapterPosition());

                // reload the items
                notifyDataSetChanged();
                unselectHolder();
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if(!allowOperations)
                return true;
            if(this.holder == selectedHolder)
                unselectHolder();
            else
                selectHolder(this.holder);
            return true;
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
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.scheduled_task_container, parent, false);
        ScheduledHolder vh = new ScheduledHolder(v);
        this.setCompleteButtonVisible(vh, false);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final SelectionHolder holder, int position) {
        // Setting the click handler for the container of an individual holder (item in the list)
        ScheduledHolderListener listener = new ScheduledHolderListener((ScheduledHolder)holder);
        holder.layoutView.setOnClickListener(listener);
        holder.layoutView.setOnLongClickListener(listener);

        ScheduledToDoTask schTask = planner.getScheduledTask(position);
        ScheduledHolder scheduledHolder = (ScheduledHolder) holder;

        String currentTaskName = planner.getTaskManager().getCurrentTask().getName();
        Log.d("ScheduledListAdapter", "adapterposition: " + holder.getAdapterPosition() + " position: " + position + " holder text: " + scheduledHolder.task.getText().toString());
        //if(position == 0 && scheduledHolder.task.getText().toString().equals(currentTaskName)) {

        if(schTask.equals(planner.getTaskManager().getCurrentTask())) {
            if(((DailyPlannerFragment)fragment).getPlanner().getTaskManager().isActive())
                SelectionListAdapter.setHolderHighlight(holder, true, fragment.getContext());
                //holder.layoutView.setBackgroundColor(SelectionListAdapter.getHighlightColor(fragment.getContext()));
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
        scheduledHolder.deselectButton.setOnClickListener(deselectHandler);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(planner.getScheduledTasks() == null)
            return 0;
        return planner.getScheduledTasks().size();
    }



    private ImageButton.OnClickListener deselectHandler = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            unselectHolder();
        }
    };

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
        View.OnLongClickListener listener = new ScheduledHolderListener((ScheduledHolder)inHolder);
        scheduledHolder.task.setOnLongClickListener(listener);
        scheduledHolder.category.setOnLongClickListener(listener);
        scheduledHolder.time.setOnLongClickListener(listener);
        dailyFragment.setRemoveButtonsVisible(true);
        setChildViewVisiblity( inHolder.taskContainer, R.id.check_button, true);
        setChildViewVisiblity( inHolder.layoutView, R.id.deselect_button, true);
    }

    public SelectionHolder unselectHolder() {
        SelectionHolder returnHolder = super.unselectHolder();
        if(returnHolder != null) {
            ScheduledHolderListener listener = new ScheduledHolderListener((ScheduledHolder)returnHolder);
            ScheduledHolder scheduledHolder = (ScheduledHolder) returnHolder;
            scheduledHolder.task.setOnClickListener(listener);
            scheduledHolder.time.setOnClickListener(listener);
            scheduledHolder.category.setOnClickListener(listener);
            setChildViewVisiblity( returnHolder.taskContainer, R.id.check_button, false);
            setChildViewVisiblity( returnHolder.layoutView, R.id.deselect_button, false);
        }
        ((DailyPlannerFragment) fragment).setRemoveButtonsVisible(false);

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
        notifyItemRemoved(itemPosition);
        return task;
    }

    /**
     * Sets a view that is a child of the provided holder, such as the time text or complete button,
     * to be visible or invisible.
     * @param holder The ScheduledHolder which contains the child view.
     * @param viewId The ID of the child view within the ScheduledHolder whose visibility is to be set.
     * @param visible If true, the child view is set to visible. Otherwise, it is set to be invisible.
     */
    public void setChildViewVisiblity(ConstraintLayout holder, int viewId, boolean visible) {
        if(holder == null)
            return;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(holder);
        if(visible) {
            constraintSet.setVisibility(viewId, ConstraintSet.VISIBLE);
        } else {
            constraintSet.setVisibility(viewId, ConstraintSet.GONE);
        }
        constraintSet.applyTo(holder);
    }

    public void setTimeTextVisible(ScheduledHolder holder, boolean visible) {
        setChildViewVisiblity(holder.taskContainer, R.id.time_text, visible);
    }

    public void setCompleteButtonVisible(ScheduledHolder holder, boolean visible) {
        setChildViewVisiblity(holder.taskContainer, R.id.check_button, visible);
    }

}
