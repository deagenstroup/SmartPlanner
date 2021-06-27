package deagen.smartplanner.ui;

import android.graphics.Color;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import deagen.smartplanner.R;
import deagen.smartplanner.fragments.DailyPlannerFragment;
import deagen.smartplanner.logic.Planner;

/**
 * A class for deriving a RecyclerView Adapter which has holder selection and holder moving
 * functionality.
 * @param <T> A Holder class within the subclass which derives from this classes Holder class.
 */
public abstract class SelectionListAdapter<T extends SelectionListAdapter.SelectionHolder> extends RecyclerView.Adapter<T> {

    protected Fragment fragment;

    protected Planner planner;

    /**
     * If set to true, they then selection and holder moving operations are performed. If set to false,
     * no selection or holder moving operations are performed.
     */
    protected boolean allowOperations = true;

    /**
     * The SelectedHolder object which is currently selected by the user.
     */
    protected SelectionHolder selectedHolder = null;

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

    public SelectionListAdapter(Planner inPlanner, Fragment inFragment) {
        planner = inPlanner;
        fragment = inFragment;
    }

    @Override
    public void onBindViewHolder(final SelectionHolder holder, int position) {
        final int holderPosition = holder.getAdapterPosition();
        holder.layoutView.setOnClickListener(new HolderListener(holder));
//        holder.layoutView.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                if(!allowOperations)
//                    return;
//
//                // if this task is already selected, unselect it
//                if (holder == selectedHolder) {
//                    doubleClickHolder();
//                    unselectHolder();
//                } else {
//                    // if there is a task selected already, that is different, move selected task to
//                    // position of the item which was clicked
//                    if(selectedHolder != null) {
//                        moveItem(selectedHolder.getAdapterPosition(), holderPosition);
//                        // reload the items
//                        notifyDataSetChanged();
//                        Log.d("SELECTION DEBUG", "deselected position: " + selectedHolder.getAdapterPosition());
//                        unselectHolder();
//                    } else {
//                        // otherwise, simply select this task
//                        selectHolder(holder);
//                    }
//                }
//            }
//        });
    }

    /**
     * Method which indicates that a item in the RecyclerView has been moved.
     * @param fromPos The position of the item which is being moved.
     * @param toPos The position where the item is being moved to.
     */
    public abstract void moveItem(int fromPos, int toPos);

    public void selectHolder(SelectionHolder inHolder) {
        selectedHolder = inHolder;
        inHolder.layoutView.setBackgroundColor(fragment.getResources().getColor(R.color.green_selected));
    }

    public SelectionHolder unselectHolder() {
        SelectionHolder returnHolder = selectedHolder;
        if(selectedHolder != null) {
            selectedHolder.layoutView.setBackgroundColor(Color.TRANSPARENT);
            selectedHolder.layoutView.setOnClickListener(new HolderListener(selectedHolder));
            selectedHolder = null;
        }
        return returnHolder;
    }

    public void doubleClickHolder() {

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

    protected class HolderListener implements View.OnClickListener {

        private SelectionHolder holder;

        public HolderListener(SelectionHolder inHolder) {
            holder=inHolder;
        }

        @Override
        public void onClick(View view) {
            int holderPosition = holder.getAdapterPosition();

            if(!allowOperations)
                return;

            // if this task is already selected, unselect it
            if (holder == selectedHolder) {
                doubleClickHolder();
                unselectHolder();
            } else {
                // if there is a task selected already, that is different, move selected task to
                // position of the item which was clicked
                if(selectedHolder != null) {
                    moveItem(selectedHolder.getAdapterPosition(), holderPosition);
                    // reload the items
                    notifyDataSetChanged();
                    Log.d("SELECTION DEBUG", "deselected position: " + selectedHolder.getAdapterPosition());
                    unselectHolder();
                } else {
                    // otherwise, simply select this task
                    selectHolder(holder);
                }
            }
        }
    }

}
