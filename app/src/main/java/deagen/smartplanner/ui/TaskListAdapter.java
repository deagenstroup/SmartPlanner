package deagen.smartplanner.ui;

import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import deagen.smartplanner.R;
import deagen.smartplanner.fragments.ActivityPlannerFragment;
import deagen.smartplanner.logic.Planner;

public class TaskListAdapter extends SelectionListAdapter {

    private int categoryPosition;

    public static class TaskHolder extends SelectionListAdapter.SelectionHolder {
        public TextView taskText;
        public TaskHolder(ConstraintLayout inLayout) {
            super(inLayout);
            taskText = inLayout.findViewById(R.id.activityplanner_task_text);
        }
    }

    public TaskListAdapter(Planner inPlanner, ActivityPlannerFragment inFragment) {
        super(inPlanner, inFragment);
        categoryPosition = ((ActivityPlannerFragment)fragment).getSelectedCategoryPosition();
    }

    @Override
    public void onBindViewHolder(final SelectionHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ((TaskHolder)holder).taskText.setText(planner.getActivityPlanner().getTaskNameInCategory(categoryPosition, holder.getAdapterPosition()));
    }

    @Override
    public void moveItem(int fromPos, int toPos) {
        planner.moveTaskInCategory(categoryPosition, fromPos, toPos);
    }

    @Override
    public int getItemCount() {
        return planner.getActivityPlanner().getTaskNumberInCategory(categoryPosition);
    }

    @Override
    public void selectHolder(SelectionHolder selectionHolder) {
        super.selectHolder(selectionHolder);
        ((ActivityPlannerFragment)fragment).setDeleteButtonVisible(true);
    }

    public SelectionHolder unselectHolder() {
        SelectionHolder holder = super.unselectHolder();
        ((ActivityPlannerFragment)fragment).setDeleteButtonVisible(false);
        return holder;
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.activityplanner_task_holder, parent, false);
        TaskHolder vh = new TaskHolder(v);
        return vh;
    }

}
