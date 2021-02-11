package net.veldor.todo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import net.veldor.todo.selections.Role;
import net.veldor.todo.ui.LoginActivity;
import net.veldor.todo.utils.MyNotify;
import net.veldor.todo.utils.Preferences;

public class MainActivity extends AppCompatActivity {

    private static final int LOGIN_RESULT = 1;
    public static final String START_FRAGMENT = "start fragment";
    public static final int INCOMING_FRAGMENT = 1;
    private static final int REQUEST_WRITE_READ = 2;
    private BottomNavigationView mNavView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupInterface();
        checkDose();
        checkSwitchToFragment();
        if(!permissionGranted()){
            showPermissionDialog();
        }
    }

    private boolean permissionGranted() {
        int writeResult;
        int readResult;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            writeResult = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            readResult = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            writeResult = PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            readResult = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        return writeResult == PackageManager.PERMISSION_GRANTED && readResult == PackageManager.PERMISSION_GRANTED;
    }

    private void showPermissionDialog() {
        if (!MainActivity.this.isFinishing()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("Необходимо предоставить разрешения")
                    .setMessage("Для загрузки файлов необходимо предоставить доступ к памяти устройства")
                    .setCancelable(false)
                    .setPositiveButton("Предоставить разрешение", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_READ))
                    .setNegativeButton("Нет, закрыть приложение", (dialog, which) -> finish());
            dialogBuilder.create().show();
        }
    }

    private void checkSwitchToFragment() {
        int startFragment = getIntent().getIntExtra(START_FRAGMENT, -1);
        if(startFragment > 0){
            switchToIncoming();
            MyNotify.getInstance().hideMessage(MyNotify.NEW_TASKS_NOTIFICATION);
        }
    }

    private void checkDose() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                showDisableDoseDialog();
            }
        }
    }

    @SuppressLint("BatteryLife")
    private void showDisableDoseDialog() {
        // создам диалоговое окно
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.disable_dose_dialog_title)
                .setMessage(getString(R.string.disable_dose_dialog_message))
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    Intent intent = new Intent();
                    String packageName = getPackageName();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + packageName));
                        startActivity(intent);
                    }
                }).create().show();
    }

    private void setupInterface() {
        mNavView = findViewById(R.id.nav_view);
        if (Preferences.getInstance().getRole() == Role.ROLE_USER) {
            mNavView.setVisibility(View.GONE);
        } else {
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_incoming, R.id.navigation_outgoing)
                    .build();
            NavHostFragment fragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if(fragment != null){
                NavController navController = fragment.getNavController();
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
                NavigationUI.setupWithNavController(mNavView, navController);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Preferences.getInstance().isUserUnknown()) {
            startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_RESULT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == LOGIN_RESULT) {
            if (resultCode == RESULT_OK) {
                setupInterface();
            } else {
                startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_RESULT);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void switchToIncoming() {
        Log.d("surprise", "MainActivity switchToStatistics: switch stat");
        mNavView.setSelectedItemId(R.id.navigation_incoming);
    }
}