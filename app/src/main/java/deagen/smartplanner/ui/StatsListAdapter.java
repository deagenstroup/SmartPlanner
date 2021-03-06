package deagen.smartplanner.ui;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import deagen.smartplanner.R;
import deagen.smartplanner.fragments.StatisticsFragment;
import deagen.smartplanner.logic.statistics.Report;
import deagen.smartplanner.logic.statistics.ReportListInterface;

public class StatsListAdapter extends RecyclerView.Adapter<StatsListAdapter.StatsItemHolder> {
    private StatisticsFragment fragment;
    private ReportListInterface listInterface;

    public void setReportListInterface(ReportListInterface inInterface) {
        listInterface = inInterface;
    }

    public static class StatsItemHolder extends RecyclerView.ViewHolder {
        public TextView itemText, timeText, percentText;
        public StatsItemHolder(ConstraintLayout inLayout) {
            super(inLayout);
            itemText = inLayout.findViewById(R.id.statistics_item_text);
            timeText = inLayout.findViewById(R.id.statistics_item_time);
            percentText = inLayout.findViewById(R.id.statistics_item_percent_text);
        }
    }

    public StatsListAdapter(ReportListInterface inInterface, StatisticsFragment statisticsFragment) {
        this.listInterface = inInterface;
        this.fragment = statisticsFragment;
    }

    @Override
    public StatsListAdapter.StatsItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.statistics_list_holder, parent, false);
        return new StatsItemHolder(layout);
    }

    @Override
    public void onBindViewHolder(StatsItemHolder itemHolder, int position) {
        itemHolder.itemText.setText(listInterface.getName(position));
        final int pos = position;
        if(listInterface instanceof Report) {
            itemHolder.itemText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fragment.changeInterface(fragment.getReport().getCategoryReport(pos));
                }
            });
        }
        itemHolder.timeText.setText(StatisticsFragment
                .formatDuration(listInterface.getTime(position)));
        itemHolder.percentText.setText(""+listInterface.getPercentage(position));
    }

    @Override
    public int getItemCount() {
        return listInterface.getNumberOfItems();
    }
}
