package deagen.smartplanner.logic.statistics;

import java.time.Duration;

/**
 * Provides all of the relevant methods required to update the Statistics fragment UI with statistics
 * about the collection of task categories or collection of tasks within in a individual category.
 */
public interface ReportListInterface {

    enum ReportSorting {
        ALPHAASC,
        ALPHADES,
        TIMEASC,
        TIMEDES,
        PERASC,
        PERDES
    }

    Duration getTotalTime();

    Duration getAverageTime();

    int getNumberOfItems();

    /**
     * @param pos Position of item within the list to get.
     * @return String representing the name of the item within the Report.
     */
    String getName(int pos);

    /**
     * @param pos Position of item within the list to get.
     * @return Duration of time representing the total time spent on that item in the time period.
     */
    Duration getTime(int pos);

    /**
     * @param pos Position of item within the list
     * @return Floating point value representing the amount of time spent on the item in relation
     * to the total amount of time spent on all of the items in the list.
     */
    float getPercentage(int pos);

    /**
     * Sort the list of items alphabetically.
     * @param ascending If false, items are sorted in reverse alphabetical order.
     */
    void sortAlpha(boolean ascending);

    /**
     * Sort the list of items by total amount of time they had in the time period.
     * @param ascending If true sort in ascending order, descending otherwise.
     */
    void sortByTime(boolean ascending);

    /**
     * Sort the list of items by percentage of time they had.
     * @param ascending If true sort in ascending order, descending otherwise.
     */
    void sortByPercentage(boolean ascending);

    /**
     * @return A variable representing the way in which the list is currently sorted.
     */
    ReportSorting getSorting();
}
