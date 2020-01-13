package deagen.smartplanner.logic.statistics;

import java.time.Duration;
import java.util.List;

/**
 * A collection of statistics about time spent on individual categories of tasks within a greater
 * Report object.
 */
public class CategoryReport {

    /**
     * The report which this individual category report is part of.
     */
    private Report report;

    /**
     * The name of this category.
     */
    private String name;

    /**
     * The total amount of time spent on all of the tasks of this category.
     */
    private Duration totalTime;

    /**
     * A list of associated reports for each unique task within this category.
     */
    private List<TaskReport> taskReports;

    public CategoryReport(Report report) {
        this.report = report;
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
        return null;
    }

    public List<TaskReport> getTaskReports() {
        return taskReports;
    }

    /**
     * Calculates the percentage of time spent on this category out of all of the time spent on
     * all the tasks in the Report.
     * @return Floating point number representing a the percentage.
     */
    public float getPercentage() {
        return 0.0f;
    }

}
