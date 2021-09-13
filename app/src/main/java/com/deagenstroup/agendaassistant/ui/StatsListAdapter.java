package com.deagenstroup.agendaassistant.ui;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deagenstroup.agendaassistant.R;
import com.deagenstroup.agendaassistant.fragments.StatisticsFragment;
import com.deagenstroup.agendaassistant.logic.statistics.Report;
import com.deagenstroup.agendaassistant.logic.statistics.ReportListInterface;

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
