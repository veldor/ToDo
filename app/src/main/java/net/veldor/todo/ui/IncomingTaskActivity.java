package net.veldor.todo.ui;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import net.veldor.todo.R;
import net.veldor.todo.utils.ShowWaitingDialog;

public class IncomingTaskActivity extends AppCompatActivity {


    public static final String TASK_ID = "task id";
    public static final String NOTIFICATION_ID = "notification id";
    private ShowWaitingDialog mWaitingDialog;
    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_task);

        applyShowWindow();

        setupUI();
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
        mWaitingDialog = new ShowWaitingDialog();
        mWaitingDialog.show(getSupportFragmentManager(), ShowWaitingDialog.NAME);
    }
}