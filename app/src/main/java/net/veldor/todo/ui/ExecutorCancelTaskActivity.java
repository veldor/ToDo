package net.veldor.todo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.veldor.todo.R;
import net.veldor.todo.utils.ShowWaitingDialog;
import net.veldor.todo.view_models.DismissTaskViewModel;

import static androidx.work.WorkInfo.State.FAILED;
import static androidx.work.WorkInfo.State.SUCCEEDED;

public class ExecutorCancelTaskActivity extends AppCompatActivity {

    public static final String TASK_ID = "task id";
    private EditText dismissReasonInput;
    private ShowWaitingDialog mWaitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executor_cancel_task);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        String taskId = intent.getStringExtra(TASK_ID);
        Log.d("surprise", "ExecutorCancelTaskActivity onCreate 31: task id is " + taskId);
        DismissTaskViewModel viewModel = new ViewModelProvider(this).get(DismissTaskViewModel.class);
        dismissReasonInput = findViewById(R.id.dismissReasonInput);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(v -> {
            if(dismissReasonInput.getText().toString().isEmpty()){
                Toast.makeText(ExecutorCancelTaskActivity.this, "Reason required!", Toast.LENGTH_LONG).show();
                dismissReasonInput.performClick();
            }
            else{
                handleAction(viewModel.dismissTask(taskId, dismissReasonInput.getText().toString()));
                showWaiter();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        dismissReasonInput.performClick();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleAction(LiveData<WorkInfo> confirmTask) {
        // отслежу выполнение задачи, после чего обновлю информацию
        confirmTask.observe(this, workInfo -> {
            if (workInfo != null) {
                if (workInfo.getState() == SUCCEEDED) {
                    Toast.makeText(ExecutorCancelTaskActivity.this, getString(R.string.task_cancelled_message), Toast.LENGTH_SHORT).show();
                    finish();
                } else if (workInfo.getState() == FAILED) {
                    hideWaiter();
                    Toast.makeText(ExecutorCancelTaskActivity.this, "Не удалось выполнить операцию, попробуйте ещё раз", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void showWaiter() {
        if (mWaitingDialog == null) {
            mWaitingDialog = new ShowWaitingDialog();
        }
        mWaitingDialog.show(getSupportFragmentManager(), ShowWaitingDialog.NAME);
    }

    private void hideWaiter() {
        if (mWaitingDialog != null) {
            mWaitingDialog.dismiss();
        }
    }
}