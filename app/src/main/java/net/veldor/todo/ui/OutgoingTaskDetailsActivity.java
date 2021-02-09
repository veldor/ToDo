package net.veldor.todo.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import net.veldor.todo.App;
import net.veldor.todo.R;
import net.veldor.todo.databinding.ActivityTaskDetailBinding;
import net.veldor.todo.selections.GetTaskInfoResponse;
import net.veldor.todo.selections.TaskItem;
import net.veldor.todo.utils.MyNotify;
import net.veldor.todo.utils.ShowWaitingDialog;
import net.veldor.todo.view_models.OutgoingTaskViewModel;

import static androidx.work.WorkInfo.State.FAILED;
import static androidx.work.WorkInfo.State.SUCCEEDED;

public class OutgoingTaskDetailsActivity extends AppCompatActivity {

    public static final String TASK_ID = "task id";
    public static final String FULL_DATA = "full data";
    public static final String NOTIFICATION_ID = "notification id";
    private OutgoingTaskViewModel mViewModel;
    private TaskItem mData = new TaskItem();
    private ShowWaitingDialog mWaitingDialog;
    private TextView mTaskStateView;
    private Button mCancelTaskBtn;
    private Button mCallExecutorBtn;
    private ActivityTaskDetailBinding mRootBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootBinding = DataBindingUtil.setContentView(this, R.layout.activity_task_detail);
        mRootBinding.setTask(mData);
        mViewModel = new ViewModelProvider(this).get(OutgoingTaskViewModel.class);
        App.getInstance().mTaskInfo.setValue(null);
        setupObservers();
        setupUI();
        handleContentLoading();
//        TextView createDateView = findViewById(R.id.createTime);
//        if (createDateView != null) {
//            createDateView.setText(String.format(Locale.ENGLISH, "Дата подачи заявки: %s", Grammar.timestampToDate(task.task_creation_time * 1000)));
//        }
//        TextView plannedFinishDateView = findViewById(R.id.plannedFinishTime);
//        if (plannedFinishDateView != null) {
//            if (task.task_planned_finish_time > 0) {
//                plannedFinishDateView.setText(String.format(Locale.ENGLISH, ":Срок выполнения: %s", Grammar.timestampToDate(task.task_planned_finish_time * 1000)));
//            }
//        }
    }

    private void setupObservers() {
        LiveData<GetTaskInfoResponse> item = App.getInstance().mTaskInfo;
        item.observe(this, response -> {
            if (response != null && response.task_info != null) {
                Log.d("surprise", "OutgoingTaskDetailsActivity setupObservers 74: observer work");
                mData = response.task_info;
                fillInfo(mData);
                mRootBinding.setTask(mData);
                mRootBinding.invalidateAll();
            }
        });
    }

    private void setupUI() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.outgoing_task_title));
        Button okButton = findViewById(R.id.okBtn);
        okButton.setOnClickListener(v -> finish());
        mCancelTaskBtn = findViewById(R.id.notActualYetBtn);
        mCancelTaskBtn.setOnClickListener(v -> showCancelTaskDialog());
        mTaskStateView = findViewById(R.id.taskState);
        mCallExecutorBtn = findViewById(R.id.call_executor);
        mCallExecutorBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "+79308184347"));
            startActivity(intent);
        });
    }

    private void showCancelTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setPositiveButton("Да, отменить", (dialog, which) -> {
                    showWaiter();
                    handleAction(mViewModel.cancelTask(mData));
                })
                .setNegativeButton("Нет", null)
                .setTitle("Отмена задачи")
                .setMessage("Задача больше неактуальна?").create().show();
    }

    private void handleContentLoading() {
        // проверю, если сведения о задаче переданы полностью- загружу их в просмотр. Иначе- запущу загрузку их с сервера
        Intent intent = getIntent();
        mData = (TaskItem) intent.getSerializableExtra(FULL_DATA);
        if (mData == null) {
            showWaiter();
            // скрою оповещение, по которому перешли сюда
            int callingMessageId = intent.getIntExtra(NOTIFICATION_ID, -1);
            if (callingMessageId > 0) {
                MyNotify.getInstance().hideMessage(callingMessageId);
            }
            String taskId = intent.getStringExtra(TASK_ID);
            handleAction(mViewModel.getTaskInfo(taskId));
        } else {
            fillInfo(mData);
            mRootBinding.setTask(mData);
        }
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
                    Log.d("surprise", "OutgoingTaskDetailsActivity handleAction 156: success");
                } else if (workInfo.getState() == FAILED) {
                    hideWaiter();
                    Toast.makeText(OutgoingTaskDetailsActivity.this, "Не удалось выполнить операцию, попробуйте ещё раз", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void fillInfo(TaskItem taskInfo) {
        Log.d("surprise", "OutgoingTaskDetailsActivity fillInfo 165: rewrite " + taskInfo.task_status);
        hideWaiter();
        if(taskInfo.executor == null || taskInfo.executor.isEmpty()){
            taskInfo.executor = getString(R.string.executor_not_set_message);
        }
        if(taskInfo.task_status_code == 1 || taskInfo.task_status_code == 2){
            mCancelTaskBtn.setVisibility(View.VISIBLE);
        }
        else{
            mCancelTaskBtn.setVisibility(View.GONE);
        }
        if(taskInfo.executor.isEmpty() || taskInfo.task_status_code == 4 || taskInfo.executor.equals(getString(R.string.executor_not_set_message))){
            mCallExecutorBtn.setVisibility(View.GONE);
        }
        else{
            mCallExecutorBtn.setVisibility(View.VISIBLE);
        }
        mTaskStateView.setTextColor(taskInfo.sideColor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mData != null) {
            mViewModel.getTaskInfo(mData.id);
        }
    }
}