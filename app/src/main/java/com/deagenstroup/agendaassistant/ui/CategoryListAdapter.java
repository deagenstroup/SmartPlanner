package com.deagenstroup.agendaassistant.ui;

import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deagenstroup.agendaassistant.R;
import com.deagenstroup.agendaassistant.fragments.ActivityPlannerFragment;
import com.deagenstroup.agendaassistant.logic.Planner;

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
        ((CategoryHolder)holder).categoryText.setText(planner.getActivityPlanner().getCategories()[position]);
    }

    @Override
    public void selectHolder(SelectionHolder selectionHolder) {
        super.selectHolder(selectionHolder);
        ((ActivityPlannerFragment)fragment).setSelectedCategoryPosition(this.getSelectedHolderPosition());
        ((ActivityPlannerFragment)fragment).setDeleteButtonVisible(true);
    }

    public void doubleClickHolder() {
        int position = selectedHolder.getAdapterPosition();
        unselectHolder();
        ((ActivityPlannerFragment)fragment).openCategory(position);
    }

    public SelectionHolder unselectHolder() {
        SelectionHolder holder = super.unselectHolder();
        ((ActivityPlannerFragment)fragment).setSelectedCategoryPosition(-1);
        ((ActivityPlannerFragment)fragment).setDeleteButtonVisible(false);
        return holder;
    }

    public void moveItem(int fromPos, int toPos) {
        planner.getActivityPlanner().moveActivityCategory(fromPos, toPos);
    }

    @Override
    public int getItemCount() {
        return planner.getActivityPlanner().getCategories().length;
    }

    @Override
    public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.category_container, parent, false);
        CategoryHolder vh = new CategoryHolder(v);
        return vh;
    }

}
