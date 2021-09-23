package com.deagenstroup.agendaassistant.ui;

import android.graphics.Color;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.deagenstroup.agendaassistant.R;
import com.deagenstroup.agendaassistant.logic.Planner;

/**
 * A class for deriving a RecyclerView Adapter which has holder selection and holder moving
 * functionality.
 * @param <T> A Holder class within the subclass which derives from this classes Holder class.
 */
public abstract class SelectionListAdapter<T extends SelectionListAdapter.SelectionHolder> extends RecyclerView.Adapter<T> {

    protected Fragment fragment;

    protected Planner planner;

    /**
     * If set to true, then selection and holder moving operations are performed. If set to false,
     * no selection or holder moving operations are performed.
     */
    protected boolean allowOperations = true;

    /**
     * The SelectedHolder object which is currently selected by the user.
     */
    protected SelectionHolder selectedHolder = null;

    /**
     * The SelectionHolder which was just clicked by the user.
     */
    protected SelectionHolder clickedHolder = null;

    /**
     * A class which represents a single item within the RecyclerView.
     */
    public static class SelectionHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout layoutView;

        public SelectionHolder(ConstraintLayout inView) {
            super(inView);
            layoutView = inView;
        }
    }

    private HolderListener holderListener;

    /**
     * Handler for the click of a holder in the recyclerview.
     */
    protected class HolderListener implements View.OnClickListener, View.OnLongClickListener {

        /**
         * The SelectionHolder which this listener belongs to.
         */
        protected SelectionHolder holder;

        public HolderListener(SelectionHolder inHolder) {
            holder = inHolder;
        }

        @Override
        public void onClick(View view) {
            clickedHolder = this.holder;
            if (!allowOperations)
                return;

            if(selectedHolder == clickedHolder) {
                doubleClickHolder();
            }
            // If there is a task selected already, that is different, move selected task to
            // position of the item which was clicked.
            else if(selectedHolder != null) {
                moveItem(selectedHolder.getAdapterPosition(), holder.getAdapterPosition());

                // reload the items
                notifyDataSetChanged();
                unselectHolder();
            } else {
                // otherwise, simply select this task
                selectHolder(holder);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if(!allowOperations)
                return true;
            if(this.holder == selectedHolder)
                unselectHolder();
            return true;
        }
    }



    public SelectionListAdapter(Planner inPlanner, Fragment inFragment) {
        planner = inPlanner;
        fragment = inFragment;


    }

    @Override
    public void onBindViewHolder(final SelectionHolder holder, int position) {
        // Setting the click handler for the container of an individual holder (item in the list)
        HolderListener listener = new HolderListener(holder);
        holder.layoutView.setOnClickListener(listener);
        holder.layoutView.setOnLongClickListener(listener);
    }

    /**
     * Called when a selected holder is clicked again. Used by category view holder.
     */
    public void doubleClickHolder() {

    }

    /**
     * Method which indicates that a item in the RecyclerView has been moved.
     * @param fromPos The position of the item which is being moved.
     * @param toPos The position where the item is being moved to.
     */
    public abstract void moveItem(int fromPos, int toPos);

    public void selectHolder(SelectionHolder inHolder) {
        selectedHolder = inHolder;
        inHolder.layoutView.setBackgroundColor(fragment.getResources().getColor(R.color.blue_selected));
    }

    /**
     * Unselect the currently selected holder.
     * @return The SelectionHolder that was unselected. Null if no SelectionHolder was selected.
     */
    public SelectionHolder unselectHolder() {
        SelectionHolder returnHolder = selectedHolder;
        if(selectedHolder != null) {
            selectedHolder.layoutView.setBackgroundColor(Color.TRANSPARENT);
//            selectedHolder.layoutView.setOnClickListener(new HolderListener(selectedHolder));
            selectedHolder = null;
        }
        return returnHolder;
    }

    public boolean hasSelected() {
        return selectedHolder != null;
    }

    public int getSelectedHolderPosition() {
        return selectedHolder.getAdapterPosition();
    }

    public void setAllowOperations(boolean status) {
        allowOperations = status;
    }

}
