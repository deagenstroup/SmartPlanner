package com.deagenstroup.agendaassistant.logic.statistics;

import java.time.Duration;
import java.util.List;

/**
 * A collection of statistics about time spent on individual categories of tasks within a greater
 * Report object.
 */
public class CategoryReport implements ReportListInterface {

    /**
     * The report which this individual category report is part of.
     */
    private Report report;

    /**
     * The name of this category.
     */
    private String name;

    /**
     * A list of associated reports for each unique task within this category.
     */
    private List<TaskReport> taskReports;


    /**
     * The total amount of time spent on all of the tasks of this category.
     */
    private Duration totalTime;

    private Duration averageTaskTime;


    public CategoryReport(Report report) {
        this.report = report;
    }

    public CategoryReport(Report report, String name, List<TaskReport> taskReports) {
        this(report);
        this.name = name;
        this.taskReports = taskReports;
    }

    /**
     * Calculates the data in the report from the calendar.
     */
    public void calculateReport() {
        long seconds = 0;
        for(TaskReport taskReport : taskReports) {
            seconds += taskReport.getTotalTime().toMinutes() * 60;
        }
        totalTime = Duration.ofSeconds(seconds);
        averageTaskTime = Duration.ofSeconds(seconds/taskReports.size());
    }

    public int getNumberOfItems() {
        return taskReports.size();
    }

    public String getCategoryName() { return name; }

    public Duration getTotalTime() {
        return this.totalTime;
    }

    public Duration getAverageTime() {
        return Duration.ofSeconds((totalTime.toMinutes() * 60) / taskReports.size());
    }

    public List<TaskReport> getTaskReports() {
        return taskReports;
    }

    /**
     * @param pos Position of item within the list to get.
     * @return String representing the name of the item within the Report.
     */
    public String getName(int pos) {
        return taskReports.get(pos).getName();
    }

    /**
     * @param pos Position of item within the list to get.
     * @return Duration of time representing the total time spent on that item in the time period.
     */
    public Duration getTime(int pos) {
        return taskReports.get(pos).getTotalTime();
    }

    /**
     * @param pos Position of item within the list
     * @return Floating point value representing the amount of time spent on the item in relation
     * to the total amount of time spent on all of the items in the list.
     */
    public float getPercentage(int pos) {
        return ((float)taskReports.get(pos).getTotalTime().toMinutes() * 60f) / ((float)totalTime.toMinutes()*60f);
    }

    /**
     * Sort the list of items alphabetically.
     * @param ascending If false, items are sorted in reverse alphabetical order.
     */
    public void sortAlpha(boolean ascending) {

    }

    /**
     * Sort the list of items by total amount of time they had in the time period.
     * @param ascending If true sort in ascending order, descending otherwise.
     */
    public void sortByTime(boolean ascending) {

    }

    /**
     * Sort the list of items by percentage of time they had.
     * @param ascending If true sort in ascending order, descending otherwise.
     */
    public void sortByPercentage(boolean ascending) {

    }

    /**
     * @return A variable representing the way in which the list is currently sorted.
     */
    public ReportListInterface.ReportSorting getSorting() {
        return null;
    }

}
