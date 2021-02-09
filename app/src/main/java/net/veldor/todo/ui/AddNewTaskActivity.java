package net.veldor.todo.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.veldor.todo.R;
import net.veldor.todo.view_models.NewTaskViewModel;

import java.io.File;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.makeMainActivity;
import static androidx.work.WorkInfo.State.FAILED;
import static androidx.work.WorkInfo.State.SUCCEEDED;

public class AddNewTaskActivity extends AppCompatActivity {

    private static final int TAKE_PHOTO_REQUEST = 1;
    private static final int SELECT_ZIP_REQUEST = 2;
    private NewTaskViewModel mMyViewModel;
    private AlertDialog mShowLoadDialog;
    private boolean mIsPaused = false;
    private DocumentFile mSelectedFile;
    private File mPhotoFile;
    private Button mAddPhotoBtn;
    private Button mAddFileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_task);
        mMyViewModel = new ViewModelProvider(this).get(NewTaskViewModel.class);
        setupUi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPaused = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsPaused = true;
    }

    private void setupUi() {
        FloatingActionButton sendBtn = findViewById(R.id.fab);
        mAddPhotoBtn = findViewById(R.id.takePhotoBtn);
        mAddPhotoBtn.setOnClickListener(v -> {
            // запрошу фото
            mPhotoFile = new File(getFilesDir(), "task_photo.jpg");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoURI = FileProvider.getUriForFile(this,
                    "net.veldor.todo.provider",
                    mPhotoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(intent, TAKE_PHOTO_REQUEST);
        });
        mAddFileBtn = findViewById(R.id.addFileBtn);
        mAddFileBtn.setOnClickListener(v -> {
            // запущу выбор файла
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("application/zip");
                startActivityForResult(intent, SELECT_ZIP_REQUEST);
            }
        });

        EditText taskHeaderView = findViewById(R.id.taskName);
        EditText taskBodyView = findViewById(R.id.taskBody);
        Spinner targetView = findViewById(R.id.chooseWorkerView);
        sendBtn.setOnClickListener(v -> {
            if (taskBodyView.getText().toString().isEmpty()) {
                taskBodyView.performClick();
                Toast.makeText(AddNewTaskActivity.this, "Нужно описать суть проблемы", Toast.LENGTH_SHORT).show();
            } else if (targetView.getSelectedItem().toString().equals("Выберите исполнителя")) {
                Toast.makeText(AddNewTaskActivity.this, "Необходимо выбрать исполнителя", Toast.LENGTH_SHORT).show();
                targetView.performClick();
            } else {
                showLoadWaitingDialog();
                LiveData<WorkInfo> data = mMyViewModel.sendTask(taskHeaderView.getText().toString(), taskBodyView.getText().toString(), targetView.getSelectedItem().toString(), mPhotoFile, mSelectedFile);
                observeRequest(data);
            }
        });
    }

    private void observeRequest(LiveData<WorkInfo> data) {
        data.observe(AddNewTaskActivity.this, workInfo -> {
            if (workInfo != null) {
                if (workInfo.getState() == SUCCEEDED) {
                    // готово, закрою активити
                    Toast.makeText(AddNewTaskActivity.this, "Задача добавлена", Toast.LENGTH_SHORT).show();
                    AddNewTaskActivity.this.finish();
                } else if (workInfo.getState() == FAILED) {
                    Toast.makeText(AddNewTaskActivity.this, "Не удалось добавить задачу, попробуйте ещё раз", Toast.LENGTH_SHORT).show();
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
        if (!AddNewTaskActivity.this.isFinishing() && !mIsPaused) {
            mShowLoadDialog.show();
        }

    }

    private void hideWaitingDialog() {
        if (mShowLoadDialog != null) {
            mShowLoadDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == TAKE_PHOTO_REQUEST && resultCode == Activity.RESULT_OK) {
            Log.d("surprise", "onActivityResult:140 have photo! " + mPhotoFile.length());
            Toast.makeText(this, getString(R.string.photo_added_message), Toast.LENGTH_LONG).show();
            if(mAddPhotoBtn != null){
                mAddPhotoBtn.setVisibility(View.GONE);
            }
        } else if (requestCode == SELECT_ZIP_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri treeUri = data.getData();
                if (treeUri != null) {
                    // проверю наличие файла
                    DocumentFile dl = DocumentFile.fromSingleUri(AddNewTaskActivity.this, treeUri);
                    if (dl != null && dl.isFile() && dl.length() > 0) {
                        mSelectedFile = dl;
                        Toast.makeText(this, getString(R.string.zip_added_message), Toast.LENGTH_LONG).show();
                        if(mAddFileBtn != null){
                            mAddFileBtn.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}