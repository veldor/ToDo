package net.veldor.todo.ui.outgoing;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.veldor.todo.App;
import net.veldor.todo.R;
import net.veldor.todo.adapters.TasksAdapter;
import net.veldor.todo.selections.RefreshDataResponse;
import net.veldor.todo.ui.AddNewTaskActivity;
import net.veldor.todo.utils.Preferences;


public class OutgoingFragment extends Fragment {
    private static final String[] sortingOptions = new String[]{"По статусу", "По названию", "По времени добавления", "По назначению", "По времени завершения"};

    private final String[] filterOptions = {"Ожидают подтверждения", "В работе", "Завершённые", "Отменённые"};
    boolean[] filter = {true, true, false, false};

    private RecyclerView recycler;
    private OutgoingViewModel mViewModel;
    private FragmentActivity mActivity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_outgoing_tickets, container, false);
        mViewModel = new ViewModelProvider(this).get(OutgoingViewModel.class);
        setupUI(root);
        setupObservers();
        setHasOptionsMenu(true);
        mActivity = getActivity();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Preferences.getInstance().isUserUnknown()) {
            mViewModel.updateTaskList();
        }
    }

    private void setupObservers() {
        LiveData<RefreshDataResponse> newTaskData = App.getInstance().mCurrentList;
        newTaskData.observe(getActivity(), response -> {
            if (response != null) {
                if (response.list != null && response.list.size() > 0) {
                    // обновлю данные
                    ((TasksAdapter) recycler.getAdapter()).setItems(response.list, filter);
                } else {
                    if (mActivity != null && mActivity.isFinishing()) {
                        Toast.makeText(mActivity, "Список задач пуст", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void setupUI(View root) {
        recycler = root.findViewById(R.id.resultsList);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(new TasksAdapter());

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(v -> startActivity(new Intent(getContext(), AddNewTaskActivity.class)));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.outgoing_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(getString(R.string.filter_message))) {
            // покажу диалог выбора элементов для отображения
            showFilterDialog();
        } else if (item.getTitle().equals(getString(R.string.sort_message))) {
            showSortDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("Отображать задачи")
                .setMultiChoiceItems(filterOptions, filter, (dialog, which, isChecked) -> filter[which] = isChecked)
                .setPositiveButton("Показать", (dialog, which) -> ((TasksAdapter) recycler.getAdapter()).applyFilter(filter));
        dialogBuilder.create().show();
    }

    private void showSortDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Выберите тип сортировки")
                .setItems(sortingOptions, (dialog1, which) -> ((TasksAdapter) recycler.getAdapter()).sort(which));
        // покажу список типов сортировки
        if (!getActivity().isFinishing()) {
            dialog.show();
        }
    }
}
