package com.deagenstroup.agendaassistant.ui;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import com.deagenstroup.agendaassistant.R;
import com.deagenstroup.agendaassistant.fragments.DailyPlannerFragment;
import com.deagenstroup.agendaassistant.logic.Planner;

public abstract class ListAdapter extends RecyclerView.Adapter<ListAdapter.ToDoTaskHolder> {
    protected Planner planner;
    protected DailyPlannerFragment fragment;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ToDoTaskHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ConstraintLayout layoutView, taskContainer;
        public TextView task, category, time;

        public ToDoTaskHolder(ConstraintLayout v) {
            super(v);
            layoutView = v;
            taskContainer = (ConstraintLayout) layoutView.findViewById(R.id.task_container);
            task = layoutView.findViewById(R.id.task_text);
            category = layoutView.findViewById(R.id.category_text);
            time = layoutView.findViewById(R.id.time_text);
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ListAdapter(Planner inPlanner, DailyPlannerFragment inFragment) {
        planner = inPlanner;
        fragment = inFragment;
    }


}