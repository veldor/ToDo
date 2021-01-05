package net.veldor.todo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import net.veldor.todo.selections.Role;
import net.veldor.todo.ui.LoginActivity;
import net.veldor.todo.utils.Preferences;

public class MainActivity extends AppCompatActivity {

    private static final int LOGIN_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupInterface();

    }

    private void setupInterface() {
        Log.d("surprise", "MainActivity setupInterface 45: Role is " + Preferences.getInstance().getRole());
        BottomNavigationView mNnavView = findViewById(R.id.nav_view);
        if (Preferences.getInstance().getRole() == Role.ROLE_USER) {
            mNnavView.setVisibility(View.GONE);
        } else {
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_incoming, R.id.navigation_outgoing)
                    .build();
            NavHostFragment fragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            NavController navController = fragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(mNnavView, navController);
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
}