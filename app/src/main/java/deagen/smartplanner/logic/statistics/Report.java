package deagen.smartplanner.logic.statistics;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import deagen.smartplanner.logic.taskscheduling.ListCalendar;

/**
 * A collection of statistics about time spent on different tasks over a period of provided dates.
 */
public class Report {

    /**
     * The dates which specify the time period which this report applies to, including the end date.
     */
    private LocalDate startDate, endDate;

    /**
     * The calendar of completed tasks which is being analyzed to compile the report.
     */
    private ListCalendar calendar;

    /**
     * The total amount of time spent on all tasks over the specified time period.
     */
    private Duration totalTime;

    /**
     * The average amount of time spent on a single task over the specified time period.
     */
    private Duration averageTaskTime;

    /**
     * The list of associated reports for each of the individual categories within the time period.
     */
    private List<CategoryReport> categoryReports;

    public Report(LocalDate startDate, LocalDate endDate, ListCalendar calendar) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.calendar = calendar;
        this.calculateReport();
    }

    /**
     * Calculates the data in the report from the calendar.
     */
    public void calculateReport() {

    }

    public Duration getTotalTime() {
        return this.totalTime;
    }

    public Duration getAverageTaskTime() {
        return this.averageTaskTime;
    }

    public List<CategoryReport> getCategoryReports() {
        return categoryReports;
    }

}
