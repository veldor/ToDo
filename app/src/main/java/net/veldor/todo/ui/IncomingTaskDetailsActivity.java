package net.veldor.todo.ui;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.veldor.todo.App;
import net.veldor.todo.R;
import net.veldor.todo.selections.GetTaskInfoResponse;
import net.veldor.todo.selections.TaskItem;
import net.veldor.todo.utils.MyNotify;
import net.veldor.todo.utils.ShowWaitingDialog;
import net.veldor.todo.view_models.IncomingTaskViewModel;

import static androidx.work.WorkInfo.State.FAILED;
import static androidx.work.WorkInfo.State.SUCCEEDED;

public class IncomingTaskDetailsActivity extends AppCompatActivity {


    public static final String TASK_ID = "task id";
    public static final String NOTIFICATION_ID = "notification id";
    public static final String FULL_DATA = "full data";
    private ShowWaitingDialog mWaitingDialog;
    private PowerManager.WakeLock mWakeLock;
    private IncomingTaskViewModel mViewModel;
    private TextView mTaskNameView, mTaskTextView, mTaskInitiatorView, mTaskStateView;
    private NumberPicker numberPicker;
    private Button mCallInitiatorBtn, mCancelTaskBtn;
    private TaskItem mData;
    private View mPickerLabel;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_task);
        App.getInstance().mTaskInfo.setValue(null);
        mViewModel = new ViewModelProvider(this).get(IncomingTaskViewModel.class);

        setupObservers();
        setupUI();
        handleContentLoading();
    }

    private void setupObservers() {
        LiveData<GetTaskInfoResponse> item = App.getInstance().mTaskInfo;
        item.observe(this, response -> {
            if (response != null && response.task_info != null) {
                mData = response.task_info;
                fillInfo(mData);
            }
        });
    }

    private void fillInfo(TaskItem taskInfo) {
        hideWaiter();
        switch (taskInfo.task_status) {
            case "created":
                taskInfo.task_status = "Ожидает подтвержения";
                taskInfo.task_status_code = 1;
                taskInfo.sideColor = Color.parseColor("#FFC107");
                break;
            case "accepted":
                taskInfo.task_status = "В работе";
                taskInfo.task_status_code = 2;
                taskInfo.sideColor = Color.parseColor("#03A9F4");
                break;
            case "finished":
                taskInfo.task_status = "Завершено";
                taskInfo.task_status_code = 3;
                taskInfo.sideColor = Color.parseColor("#8BC34A");
                break;
            case "cancelled_by_initiator":
                taskInfo.task_status = "Отменено пользователем";
                taskInfo.task_status_code = 4;
                taskInfo.sideColor = Color.parseColor("#FF5722");
                break;
            case "cancelled_by_executor":
                taskInfo.task_status = "Отменено исполнителем";
                taskInfo.task_status_code = 5;
                taskInfo.sideColor = Color.parseColor("#FF5722");
                break;
        }
        if (taskInfo.task_status_code == 1) {
            // покажу нужные кнопки
            mPickerLabel.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> acceptTask());
        } else if (taskInfo.task_status_code == 2) {
            fab.setVisibility(View.VISIBLE);
            mPickerLabel.setVisibility(View.GONE);
            fab.setOnClickListener(v -> finishTask());
        } else {
            fab.setVisibility(View.GONE);
            mPickerLabel.setVisibility(View.GONE);
            mCancelTaskBtn.setVisibility(View.GONE);
        }
        mTaskNameView.setText(taskInfo.task_header);
        mTaskTextView.setText(taskInfo.task_body);
        mTaskStateView.setText(taskInfo.task_status);
        mTaskStateView.setTextColor(taskInfo.sideColor);
        mTaskInitiatorView.setText(taskInfo.initiator);
        mCallInitiatorBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "+79308184347"));
            startActivity(intent);
        });
    }

    private void finishTask() {
        // покажу диалог завершения задачи
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.finish_task_title)
                .setMessage(getString(R.string.finish_task_message))
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    showWaiter();
                    handleAction(mViewModel.finishTask(mData));
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    private void handleContentLoading() {
        // проверю, если сведения о задаче переданы полностью- загружу их в просмотр. Иначе- запущу загрузку их с сервера
        Intent intent = getIntent();
        mData = (TaskItem) intent.getSerializableExtra(FULL_DATA);
        Log.d("surprise", "IncomingTaskDetailsActivity handleContentLoading 98: data is " + mData);
        if (mData == null) {
            applyShowWindow();
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
        }
    }

    private void applyShowWindow() {
        final Window win = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            km.requestDismissKeyguard(this, null);
        } else {
            win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            win.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        /* This code together with the one in onDestroy()
         * will make the screen be always on until this Activity gets destroyed. */
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "myapp:surprise");
        mWakeLock.acquire(360000);
    }

    private void setupUI() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.my_task_title));
        mPickerLabel = findViewById(R.id.pickerLabel);
        mTaskNameView = findViewById(R.id.taskName);
        mTaskTextView = findViewById(R.id.taskDetails);
        mTaskStateView = findViewById(R.id.taskState);
        mTaskInitiatorView = findViewById(R.id.taskInitiator);
        mCallInitiatorBtn = findViewById(R.id.call_initiator);
        mCancelTaskBtn = findViewById(R.id.cancelTaskBtn);
        mCancelTaskBtn.setOnClickListener(v -> {
            Intent intent = new Intent(IncomingTaskDetailsActivity.this, ExecutorCancelTaskActivity.class);
            intent.putExtra(ExecutorCancelTaskActivity.TASK_ID, mData.id);
            startActivity(intent);
        });

        numberPicker = findViewById(R.id.requiredPeriodPicker);
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setMaxValue(365);
        numberPicker.setMinValue(0);

        fab = findViewById(R.id.fab);
    }

    private void acceptTask() {
        // проверю, заполнено ли поле необходимого времени для решения проблемы
        if (numberPicker.getValue() == 0) {
            Toast.makeText(IncomingTaskDetailsActivity.this, "Выберите время, необходимое для решения проблемы", Toast.LENGTH_SHORT).show();
        } else {
            // покажу окно загрузки и отправлю подтверждение на сервер
            showWaiter();
            handleAction(mViewModel.confirmTask(mData, numberPicker.getValue()));
        }
    }

    private void handleAction(LiveData<WorkInfo> confirmTask) {
        // отслежу выполнение задачи, после чего обновлю информацию
        confirmTask.observe(this, workInfo -> {
            if (workInfo != null) {
                if (workInfo.getState() == SUCCEEDED) {
                    Log.d("surprise", "IncomingTaskDetailsActivity handleAction 183: data changed!");
                } else if (workInfo.getState() == FAILED) {
                    hideWaiter();
                    Toast.makeText(IncomingTaskDetailsActivity.this, "Не удалось выполнить операцию, попробуйте ещё раз", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWakeLock != null) {
            mWakeLock.release();
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

    @Override
    protected void onResume() {
        super.onResume();
        if (mData != null) {
            mViewModel.getTaskInfo(mData.id);
        }
    }
}