package deagen.smartplanner.ui;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import deagen.smartplanner.R;

public class TaskDialog extends Dialog {

    public TaskDialog(Activity a) {
        super(a);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_dialog);
    }

}
