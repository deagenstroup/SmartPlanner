package deagen.smartplanner.logic;

public interface DailyPlannerUserInterface {

    /**
     * Method for updating the UI info of the planner periodically as a thread is run.
     */
    public void updateCurrentTask();

    /**
     * Called to update the UI right before the current task is ended.
     */
    public void preEndTaskUpdate();

    /**
     * Called to update the UI right after the current task is ended.
     */
    public void postEndTaskUpdate();

}
