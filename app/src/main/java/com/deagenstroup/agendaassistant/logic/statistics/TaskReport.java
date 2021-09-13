package com.deagenstroup.agendaassistant.logic.statistics;

import java.time.Duration;

/**
 * A collection of statistics about time spent on unique tasks within a greater CategoryReport
 * object.x
 */
public class TaskReport {

    /**
     * The report for the category to which this task belongs.
     */
    private CategoryReport categoryReport;

    /**
     * The name of the task.
     */
    private String name;

    /**
     * The total amount of time spent on this task.
     */
    private Duration totalTime;

    /**
     * The number of individual times this task was done.
     */
    private int taskCount;

    public TaskReport(CategoryReport categoryReport) {
        this.categoryReport = categoryReport;
    }

    public TaskReport(CategoryReport categoryReport, String name, Duration totalTime, int taskCount) {
        this.categoryReport = categoryReport;
        this.name = name;
        this.totalTime = totalTime;
        this.taskCount = taskCount;
    }

    public String getName() {
        return name;
    }

    public Duration getTotalTime() {
        return this.totalTime;
    }

    public Duration getAverageTaskTime() {
        return null;
    }

    /**
     * Calculates the percentage of time spent on this task out of all of the time spent on
     * all the tasks in the Report.
     * @return Floating point number representing a the percentage.
     */
    public float getPercentage() {
        return 0.0f;
    }

}
