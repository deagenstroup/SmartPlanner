package deagen.smartplanner.ui;

import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import deagen.smartplanner.R;
import deagen.smartplanner.fragments.ActivityPlannerFragment;
import deagen.smartplanner.logic.Planner;

public class CategoryListAdapter extends SelectionListAdapter {

    public static class CategoryHolder extends SelectionListAdapter.SelectionHolder {
        public TextView categoryText;
        public CategoryHolder(ConstraintLayout inLayout) {
            super(inLayout);
            categoryText = inLayout.findViewById(R.id.activityplanner_category_text);
        }
    }

    public CategoryListAdapter(Planner inPlanner, ActivityPlannerFragment inFragment) {
        super(inPlanner, inFragment);
    }

    @Override
    public void onBindViewHolder(final SelectionHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ((CategoryHolder)holder).categoryText.setText(planner.getCategories()[position]);
    }

    @Override
    public void selectHolder(SelectionHolder selectionHolder) {
        super.selectHolder(selectionHolder);
        ((ActivityPlannerFragment)fragment).setDeleteButtonVisible(true);
    }

    public void doubleClickHolder() {
        ((ActivityPlannerFragment)fragment).openCategory(selectedHolder.getAdapterPosition());
    }

    public SelectionHolder unselectHolder() {
        SelectionHolder holder = super.unselectHolder();
        ((ActivityPlannerFragment)fragment).setDeleteButtonVisible(false);
        return holder;
    }

    public void moveItem(int fromPos, int toPos) {
        planner.moveCategory(fromPos, toPos);
    }

    @Override
    public int getItemCount() {
        return planner.getCategories().length;
    }

    @Override
    public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.activityplanner_category_holder, parent, false);
        CategoryHolder vh = new CategoryHolder(v);
        return vh;
    }

}
