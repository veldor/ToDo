package net.veldor.todo.ui;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
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
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import net.veldor.todo.App;
import net.veldor.todo.R;
import net.veldor.todo.databinding.ActivityIncomingTaskBinding;
import net.veldor.todo.selections.GetTaskInfoResponse;
import net.veldor.todo.selections.TaskItem;
import net.veldor.todo.utils.MyNotify;
import net.veldor.todo.view_models.IncomingTaskViewModel;

import static androidx.work.WorkInfo.State.FAILED;
import static androidx.work.WorkInfo.State.SUCCEEDED;

public class IncomingTaskDetailsActivity extends AppCompatActivity {


    public static final String TASK_ID = "task id";
    public static final String NOTIFICATION_ID = "notification id";
    public static final String FULL_DATA = "full data";
    private AlertDialog mWaitingDialog;
    private PowerManager.WakeLock mWakeLock;
    private IncomingTaskViewModel mViewModel;
    private TextView mTaskStateView;
    private TaskItem mData;
    private ActivityIncomingTaskBinding mRootBinding;
    private Button mCallExecutorBtn, mEmailExecutorBtn, showAttachedPhotoBtn, downloadAttachedFileBtn;
    private Button mActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyShowWindow();
        mRootBinding = DataBindingUtil.setContentView(this, R.layout.activity_incoming_task);
        mRootBinding.setLifecycleOwner(IncomingTaskDetailsActivity.this);
        mRootBinding.setTask(mData);
        mRootBinding.invalidateAll();
        Log.d("surprise", "IncomingTaskDetailsActivity onCreate 65: i create new task window!");
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
                mRootBinding.setLifecycleOwner(IncomingTaskDetailsActivity.this);
                mRootBinding.setTask(mData);
                mRootBinding.invalidateAll();
                fillInfo(mData);
            }
        });

        LiveData<String> taskAcceptedData = App.getInstance().mExecutorAcceptedTask;
        taskAcceptedData.observe(this, s -> {
            if (s != null && !s.isEmpty() && mData != null && mData.id.equals(s)) {
                App.getInstance().mExecutorAcceptedTask.setValue(null);
                // Похоже, задачу кто-то перехватил, скрою экран
                App.getInstance().updateIncomingTaskList();
                Log.d("surprise", "IncomingTaskDetailsActivity setupObservers 88: somebody stole this task!");
                finish();
            }
        });
    }

    private void fillInfo(TaskItem taskInfo) {
        if (taskInfo.task_planned_finish_time == 0 && taskInfo.task_status.equals("В работе")) {
            showAcceptTaskDialog();
        }
        invalidateOptionsMenu();
        hideWaiter();
        if (taskInfo.task_status_code == 1) {
            mActionButton.setText(getString(R.string.accept_action_titile));
            mActionButton.setOnClickListener(v -> showAcceptTaskDialog());
        } else if (taskInfo.task_status_code == 2) {
            mActionButton.setText(getString(R.string.finish_task_title));
            mActionButton.setOnClickListener(v -> showFinishTaskDialog());
        }
        if (taskInfo.imageFile) {
            showAttachedPhotoBtn.setVisibility(View.VISIBLE);
            showAttachedPhotoBtn.setOnClickListener(v -> {
                showWaiter();
                handleAction(mViewModel.downloadPhoto(taskInfo.id));
            });
        } else {
            showAttachedPhotoBtn.setVisibility(View.GONE);
        }
        if (taskInfo.attachmentFile) {
            downloadAttachedFileBtn.setVisibility(View.VISIBLE);
            downloadAttachedFileBtn.setOnClickListener(v -> {
                showWaiter();
                handleAction(mViewModel.downloadZip(taskInfo.id));
            });
        } else {
            downloadAttachedFileBtn.setVisibility(View.GONE);
        }
        if (taskInfo.executor == null || taskInfo.executor.isEmpty()) {
            taskInfo.executor = getString(R.string.executor_not_set_message);
        }
        if (taskInfo.task_status_code == 1 || taskInfo.task_status_code == 2) {
            mActionButton.setVisibility(View.VISIBLE);
        } else {
            mActionButton.setVisibility(View.GONE);
        }
        if (taskInfo.initiator.isEmpty() || taskInfo.task_status_code == 4) {
            mCallExecutorBtn.setVisibility(View.GONE);
            mEmailExecutorBtn.setVisibility(View.GONE);
        } else {
            if (taskInfo.initiatorPhone != null && !taskInfo.initiatorPhone.isEmpty()) {
                mCallExecutorBtn.setVisibility(View.VISIBLE);
                mCallExecutorBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + taskInfo.initiatorPhone));
                    startActivity(intent);
                });
            } else {
                mCallExecutorBtn.setVisibility(View.GONE);
            }
            if (taskInfo.initiatorEmail != null && !taskInfo.initiatorEmail.isEmpty()) {
                mEmailExecutorBtn.setVisibility(View.VISIBLE);
                mEmailExecutorBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + taskInfo.initiatorEmail));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    startActivity(intent);
                });
            } else {
                mEmailExecutorBtn.setVisibility(View.GONE);
            }
        }
        mTaskStateView.setTextColor(taskInfo.sideColor);
        mTaskStateView.setText(taskInfo.task_status);
        //mTaskNameView.setText(taskInfo.task_header);
        //mTaskBodyView.setText(taskInfo.task_body);
    }

    private void showFinishTaskDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Task finishing")
                .setPositiveButton(getString(R.string.finish_task_request_titile), (dialog, which) -> handleAction(mViewModel.finishTask(mData)))
                .create()
                .show();
    }

    private void showAcceptTaskDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialod_accept_task_layout, null, false);
        NumberPicker np = view.findViewById(R.id.requiredPeriodPicker);
        np.setMinValue(1);
        np.setValue(1);
        np.setMaxValue(999);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.accept_task_title))
                .setView(view)
                .setPositiveButton(R.string.accept_action_titile, (dialog, which) -> {
                    handleAction(mViewModel.confirmTask(mData, np.getValue()));
                    dialog.dismiss();
                })
                .create().show();
    }

    private void handleContentLoading() {
        // проверю, если сведения о задаче переданы полностью- загружу их в просмотр. Иначе- запущу загрузку их с сервера
        Intent intent = getIntent();
        mData = (TaskItem) intent.getSerializableExtra(FULL_DATA);
        if (mData == null) {
            Log.d("surprise", "IncomingTaskDetailsActivity handleContentLoading 177: no data");
            showWaiter();
            // скрою оповещение, по которому перешли сюда
            int callingMessageId = intent.getIntExtra(NOTIFICATION_ID, -1);
            if (callingMessageId > 0) {
                MyNotify.getInstance().hideMessage(callingMessageId);
            }
            String taskId = intent.getStringExtra(TASK_ID);
            handleAction(mViewModel.getTaskInfo(taskId));
        } else {
            Log.d("surprise", "IncomingTaskDetailsActivity handleContentLoading 188: have data");
            mRootBinding.setLifecycleOwner(IncomingTaskDetailsActivity.this);
            mRootBinding.setTask(mData);
            mRootBinding.invalidateAll();
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
        getSupportActionBar().setTitle(getString(R.string.outgoing_task_title));
        Button okButton = findViewById(R.id.okBtn);
        okButton.setOnClickListener(v -> finish());
        mActionButton = findViewById(R.id.actionButton);
        mTaskStateView = findViewById(R.id.taskState);
        mCallExecutorBtn = findViewById(R.id.callExecutor);
        mEmailExecutorBtn = findViewById(R.id.mailExecutor);
        showAttachedPhotoBtn = findViewById(R.id.showAttachedPhoto);
        downloadAttachedFileBtn = findViewById(R.id.downloadAttachedFile);
    }

    private void handleAction(LiveData<WorkInfo> confirmTask) {
        // отслежу выполнение задачи, после чего обновлю информацию
        confirmTask.observe(this, workInfo -> {
            if (workInfo != null) {
                if (workInfo.getState() == SUCCEEDED) {
                    Log.d("surprise", "IncomingTaskDetailsActivity handleAction 183: data changed!");
                    mViewModel.getTaskInfo(mData.id);
                } else if (workInfo.getState() == FAILED) {
                    hideWaiter();
                    Toast.makeText(IncomingTaskDetailsActivity.this, "Не удалось выполнить операцию, попробуйте ещё раз", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showWaiter() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(IncomingTaskDetailsActivity.this);
        View view = getLayoutInflater().inflate(R.layout.loading_dialog_layout, null, false);
        alertDialogBuilder
                .setTitle("loading...")
                .setView(view)
                .setCancelable(false);
        mWaitingDialog = alertDialogBuilder.create();
        mWaitingDialog.show();
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
        Log.d("surprise", "IncomingTaskDetailsActivity onDestroy 283: i destroyed(");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        } else if (item.getItemId() == R.id.dismissTaskMenu) {
            Intent intent = new Intent(IncomingTaskDetailsActivity.this, ExecutorCancelTaskActivity.class);
            intent.putExtra(ExecutorCancelTaskActivity.TASK_ID, mData.id);
            startActivity(intent);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mData != null && mData.task_status_code == 2) {
            getMenuInflater().inflate(R.menu.incoming_task_menu, menu);
            return true;
        }
        return false;
    }
}