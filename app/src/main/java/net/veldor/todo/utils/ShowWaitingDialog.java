package net.veldor.todo.utils;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import net.veldor.todo.App;
import net.veldor.todo.R;

public class ShowWaitingDialog extends DialogFragment {
    public static final String NAME = "waiting dialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(App.getInstance())
                .setTitle(R.string.processing_title)
                .setView(R.layout.loading_dialog_layout)
                .setCancelable(false);
        return adb.create();
    }
}
