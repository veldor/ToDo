package net.veldor.todo.ui.incoming;

import android.content.Intent;
import android.net.Uri;
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

import net.veldor.todo.App;
import net.veldor.todo.R;
import net.veldor.todo.adapters.IncomingTasksAdapter;
import net.veldor.todo.selections.RefreshDataResponse;
import net.veldor.todo.selections.TaskItem;
import net.veldor.todo.ui.IncomingTaskDetailsActivity;
import net.veldor.todo.ui.OutgoingTaskDetailsActivity;
import net.veldor.todo.utils.Preferences;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class IncomingFragment extends Fragment {

    private FragmentActivity mActivity;
    private IncomingViewModel mViewModel;
    private RecyclerView recycler;

    private final String[] filterOptions = {"Ожидают подтверждения", "В работе", "Завершённые", "Отменённые пользователем", "Отменённые исполнителем"};
    final boolean[] filter = {true, true, false, false, false};
    private static final String[] sortingOptions = new String[]{"По статусу", "По названию", "По времени добавления", "По назначению", "По времени завершения"};

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_incoming_tickets, container, false);
        mViewModel = new ViewModelProvider(this).get(IncomingViewModel.class);
        mActivity = getActivity();
        setupUI(root);
        setHasOptionsMenu(true);
        setupObservers();
        return root;
    }

    private void setupObservers() {
        // буду отслеживать изменение периода, если он меняется- перезапущу таймер
        LiveData<RefreshDataResponse> newTaskData = App.getInstance().mCurrentIncomingList;
        newTaskData.observe(getActivity(), response -> {
            if (response != null) {
                if (response.list != null && response.list.size() > 0) {
                    // обновлю данные
                    ((IncomingTasksAdapter) recycler.getAdapter()).setItems(response.list, filter);
                    ((IncomingTasksAdapter) recycler.getAdapter()).sort(0);

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
        recycler.setAdapter(new IncomingTasksAdapter());
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!Preferences.getInstance().isUserUnknown()) {
            mViewModel.updateTaskList();
        }
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
                .setPositiveButton("Показать", (dialog, which) -> ((IncomingTasksAdapter) recycler.getAdapter()).applyFilter(filter));
        dialogBuilder.create().show();
    }

    private void showSortDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Выберите тип сортировки")
                .setItems(sortingOptions, (dialog1, which) -> ((IncomingTasksAdapter) recycler.getAdapter()).sort(which));
        // покажу список типов сортировки
        if (!getActivity().isFinishing()) {
            dialog.show();
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position;
        try {
            position = ((IncomingTasksAdapter) recycler.getAdapter()).getPosition();
        } catch (Exception e) {
            return super.onContextItemSelected(item);
        }
        TaskItem taskItem = ((IncomingTasksAdapter) recycler.getAdapter()).getItem(position);
        if (item.getTitle().equals(getString(R.string.show_more_menu_item))) {
            // открою просмотр элемента
            Intent intent = new Intent(App.getInstance(), IncomingTaskDetailsActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(OutgoingTaskDetailsActivity.FULL_DATA, taskItem);
            App.getInstance().startActivity(intent);
        }
        else if (item.getTitle().equals(getString(R.string.call_initiator_menu_item))) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "+79308184347"));
            startActivity(intent);
        }
        return super.onContextItemSelected(item);
    }
}
