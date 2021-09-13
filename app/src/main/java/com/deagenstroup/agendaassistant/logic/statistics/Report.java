package com.deagenstroup.agendaassistant.logic.statistics;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import com.deagenstroup.agendaassistant.logic.taskscheduling.ListCalendar;

/**
 * A collection of statistics about time spent on different tasks over a period of provided dates.
 */
public class Report implements ReportListInterface {

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
        categoryReports = new LinkedList<CategoryReport>();
        this.addTestValues();
        this.calculateReport();
    }

    /**
     * Calculates the data in the report from the calendar.
     */
    public void calculateReport() {
        long seconds = 0;
        for(CategoryReport catReport : categoryReports) {
            catReport.calculateReport();
            seconds += catReport.getTotalTime().toMinutes() * 60L;
        }
        totalTime = Duration.ofSeconds(seconds);
        averageTaskTime = Duration.ofSeconds( seconds / categoryReports.size());
    }

    public LocalDate getStartDate() { return startDate; }

    public LocalDate getEndDate() { return endDate; }

    public void setStartDate(LocalDate inDate) {
        this.startDate = inDate;
        this.calculateReport();
    }

    public void setEndDate(LocalDate inDate) {
        this.endDate = inDate;
        this.calculateReport();
    }

    public int getNumberOfItems() {
        return categoryReports.size();
    }

    public Duration getTotalTime() {
        return this.totalTime;
    }

    public Duration getAverageTime() {
        return this.averageTaskTime;
    }

    public CategoryReport getCategoryReport(int pos) { return categoryReports.get(pos); }

    /**
     * @param pos Position of item within the list to get.
     * @return String representing the name of the item within the Report.
     */
    public String getName(int pos) {
        return categoryReports.get(pos).getCategoryName();
    }

    /**
     * @param pos Position of item within the list to get.
     * @return Duration of time representing the total time spent on that item in the time period.
     */
    public Duration getTime(int pos) {
        return categoryReports.get(pos).getTotalTime();
    }

    /**
     * @param pos Position of item within the list
     * @return Floating point value representing the amount of time spent on the item in relation
     * to the total amount of time spent on all of the items in the list.
     */
    public float getPercentage(int pos) {
        long catSeconds = categoryReports.get(pos).getTotalTime().toMinutes() * 60;
        return (float)catSeconds / (float)(totalTime.toMinutes() * 60);
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
    public ReportSorting getSorting() {
        return null;
    }

    private void addTestValues() {
        List<TaskReport> cleanTasks = new LinkedList<TaskReport>();
        CategoryReport cleaning = new CategoryReport(this, "cleaning", cleanTasks);
        cleanTasks.add(new TaskReport(cleaning, "wash clothes", Duration.ofMinutes(120L), 4));
        categoryReports.add(cleaning);

        List<TaskReport> homeworkTasks = new LinkedList<TaskReport>();
        CategoryReport homework = new CategoryReport(this, "taskReports", homeworkTasks);
        homeworkTasks.add(new TaskReport(homework, "do stats problems", Duration.ofMinutes(400L), 6));
        categoryReports.add(homework);
    }
}
