package deagen.smartplanner.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import deagen.smartplanner.R;
import deagen.smartplanner.logic.Planner;
import deagen.smartplanner.logic.statistics.CategoryReport;
import deagen.smartplanner.logic.statistics.Report;
import deagen.smartplanner.logic.statistics.ReportListInterface;
import deagen.smartplanner.ui.StatsListAdapter;


/**
 * A Fragment for providing statistics about completed tasks within a specified period of dates.
 */
public class StatisticsFragment extends Fragment {

    /**
     * The top level view from which all of the GUI objects can be found.
     */
    private View view;

    /**
     * The container for the main logic of the program.
     */
    private Planner planner;

    /**
     * A report of statistics about tasks that were completed within a specified time period.
     */
    private Report report;

    /**
     * The object which interfaces with the GUI components to provide the statstics. Can either
     * be the report object itself or one of the category reports.
     */
    private ReportListInterface reportListInterface;

    private RecyclerView statsListView;
    
    private OnFragmentInteractionListener mListener;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    public static StatisticsFragment newInstance() {
        StatisticsFragment fragment = new StatisticsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        // initializing the report with the past week as the time period
        report = new Report(planner.getDate().minusWeeks(1L), planner.getDate(), planner.getCalendar());

        // the GUI will draw statistics from the report as a whole initially
        reportListInterface = report;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inflating the xml sheet to build the GUI objects
        view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // set date handlers
        ((Button)view.findViewById(R.id.stats_from_date_button)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDate(true);
            }
        });
        ((Button)view.findViewById(R.id.stats_to_date_button)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDate(false);
            }
        });

        // initialize the list to contain the special adapter
        statsListView = view.findViewById(R.id.stats_list_view);
        statsListView.setHasFixedSize(true);
        statsListView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        statsListView.setAdapter(new StatsListAdapter(reportListInterface, this));

        // set the handlers for the labels above the RecyclerView list
        (view.findViewById(R.id.statistics_list_title)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderListByItem();
            }
        });
        // set the handlers for the labels above the RecyclerView list
        (view.findViewById(R.id.statistics_list_time_text)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderListByTime();
            }
        });

        // initialize all of the GUI elements with the relevant information
        this.updateDateButtons();
        this.updateTextViews();
        this.updateList();

        return view;
    }

    public Report getReport() { return report; }

    public void setPlanner(Planner inPlanner) {
        planner = inPlanner;
    }

    public void changeInterface(ReportListInterface inInterface) {
        this.reportListInterface = inInterface;
        this.updateTextViews();
        this.updateList();
    }

    /**
     * Changes the text of the start and end date buttons at the top of the fragment to reflect the
     * start and end dates within the report.
     */
    public void updateDateButtons() {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
        ((Button)(view.findViewById(R.id.stats_from_date_button))).setText(report.getStartDate().format(formatter));
        ((Button)(view.findViewById(R.id.stats_to_date_button))).setText(report.getEndDate().format(formatter));
    }

    /**
     * Changes the two text views in the middle of the fragment to reflect the total time and
     * average time of the current ReportListInterface object (either the report as a whole, or
     * one of the individual categories of tasks).
     */
    public void updateTextViews() {
        ((TextView)view.findViewById(R.id.stats_total_time_text))
                .setText(getContext().getResources().getString(R.string.total_time_text)
                        + " "
                        + formatDuration(reportListInterface.getTotalTime()));
        ((TextView)view.findViewById(R.id.stats_average_time_text))
                .setText(getContext().getResources().getString(R.string.average_time_text)
                        + " "
                        + formatDuration(reportListInterface.getAverageTime()));
    }

    public static String formatDuration(Duration inDuration) {
         final long s = inDuration.toMinutes() * 60;
        return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }

    /**
     * Updates the list contained in the RecyclerView to accurately reflect the data stored in
     * the ReportListInterface object.
     */
    public void updateList() {
        if(reportListInterface instanceof Report) {
            ((TextView)view.findViewById(R.id.statistics_list_title)).setText("Task Categories");
        } else {
            ((TextView)view.findViewById(R.id.statistics_list_title)).setText("Tasks in " +
                    ((CategoryReport)reportListInterface).getCategoryName());
        }
        ((StatsListAdapter)statsListView.getAdapter()).setReportListInterface(reportListInterface);
        statsListView.getAdapter().notifyDataSetChanged();
    }

    /**
     * Serves a dialog to the user asking for a date, changes either the start date or end date within
     * the report, and updates the GUI accordingly.
     */
    public void changeDate(final boolean start) {
        final StatisticsFragment statsFrag = this;
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if(start)
                    report.setStartDate(LocalDate.of(year, ++month, dayOfMonth));
                else
                    report.setEndDate(LocalDate.of(year, ++month, dayOfMonth));
                statsFrag.updateDateButtons();
            }
        };
        LocalDate buttonDate = (start ? report.getStartDate() : report.getEndDate());
        DatePickerDialog dialog = new DatePickerDialog(this.getContext(), listener,
                buttonDate.getYear(), buttonDate.getMonthValue()-1, buttonDate.getDayOfMonth());
        dialog.show();
    }

    /**
     * Sorts the list of either task categories or individual tasks by their name in alphabetical
     * order or reverse alphabetical order if they are already sorted in alphabetical order. Updates
     * all GUI objects accordingly after sorting the list.
     */
    public void orderListByItem() {
        Log.d("DEV", "clicked: category");
        ReportListInterface.ReportSorting sorting = reportListInterface.getSorting();
        if(sorting == ReportListInterface.ReportSorting.ALPHAASC) {
            reportListInterface.sortAlpha(false);
        } else {
            reportListInterface.sortAlpha(true);
        }
    }

    /**
     * Sorts the list of either task categories or individual tasks by the total amount of time spent
     * on them, with the biggest first, or with the smallest first if already sorted biggest first.
     */
    public void orderListByTime() {
        Log.d("DEV", "TIME");
        ReportListInterface.ReportSorting sorting = reportListInterface.getSorting();
        if(sorting == ReportListInterface.ReportSorting.TIMEASC) {
            reportListInterface.sortByTime(false);
        } else {
            reportListInterface.sortByTime(true);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
