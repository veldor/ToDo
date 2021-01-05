package net.veldor.todo.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.veldor.todo.R;
import net.veldor.todo.view_models.NewTaskViewModel;

import static androidx.work.WorkInfo.State.FAILED;
import static androidx.work.WorkInfo.State.SUCCEEDED;

public class AddNewTaskActivity extends AppCompatActivity {

    private NewTaskViewModel mMyViewModel;
    private AlertDialog mShowLoadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_task);
        mMyViewModel = new ViewModelProvider(this).get(NewTaskViewModel.class);
        setupUi();
    }

    private void setupUi() {
        FloatingActionButton sendBtn = findViewById(R.id.fab);
        Button addPhotoBtn = findViewById(R.id.addPhotoBtn);
        addPhotoBtn.setOnClickListener(v -> Toast.makeText(AddNewTaskActivity.this, "In work", Toast.LENGTH_SHORT).show());
        Button addFileBtn = findViewById(R.id.addFileBtn);
        addFileBtn.setOnClickListener(v -> Toast.makeText(AddNewTaskActivity.this, "In work", Toast.LENGTH_SHORT).show());

        EditText taskHeaderView = findViewById(R.id.taskName);
        EditText taskBodyView = findViewById(R.id.taskBody);
        Spinner targetView = findViewById(R.id.chooseWorkerView);
        sendBtn.setOnClickListener(v -> {
            if (taskBodyView.getText().toString().isEmpty()) {
                taskBodyView.performClick();
                Toast.makeText(AddNewTaskActivity.this, "Нужно описать суть проблемы", Toast.LENGTH_SHORT).show();
            }
            else if(targetView.getSelectedItem().toString().equals("Выберите исполнителя")){
                Toast.makeText(AddNewTaskActivity.this, "Необходимо выбрать исполнителя",Toast.LENGTH_SHORT).show();
                targetView.performClick();
            }
            else {
                showLoadWaitingDialog();
                LiveData<WorkInfo> data = mMyViewModel.sendTask(taskHeaderView.getText().toString(), taskBodyView.getText().toString(), targetView.getSelectedItem().toString());
                observeRequest(data);
            }
        });
    }

    private void observeRequest(LiveData<WorkInfo> data) {
        data.observe(AddNewTaskActivity.this, workInfo -> {
            if (workInfo != null) {
                if (workInfo.getState() == SUCCEEDED) {
                    // готово, закрою активити
                    Toast.makeText(AddNewTaskActivity.this, "Задача добавлена",Toast.LENGTH_SHORT).show();
                    AddNewTaskActivity.this.finish();
                } else if (workInfo.getState() == FAILED) {
                    Toast.makeText(AddNewTaskActivity.this, "Не удалось добавить задачу, попробуйте ещё раз",Toast.LENGTH_SHORT).show();
                    hideWaitingDialog();
                }
            }
        });
    }

    private void showLoadWaitingDialog() {
        if (mShowLoadDialog == null) {
                mShowLoadDialog = new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.wait_title))
                        .setMessage(getString(R.string.wait_message))
                        .create();
        }
        if (!AddNewTaskActivity.this.isFinishing()) {
            mShowLoadDialog.show();
        }

    }

    private void hideWaitingDialog(){
        if(mShowLoadDialog != null){
            mShowLoadDialog.dismiss();
        }
    }
}