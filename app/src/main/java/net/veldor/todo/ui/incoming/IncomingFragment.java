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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import net.veldor.todo.App;
import net.veldor.todo.R;
import net.veldor.todo.adapters.IncomingTasksAdapter;
import net.veldor.todo.selections.RefreshDataResponse;
import net.veldor.todo.selections.TaskItem;
import net.veldor.todo.ui.IncomingTaskDetailsActivity;
import net.veldor.todo.ui.LoginActivity;
import net.veldor.todo.ui.OutgoingTaskDetailsActivity;
import net.veldor.todo.ui.SettingsActivity;
import net.veldor.todo.utils.Preferences;

import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static net.veldor.todo.MainActivity.LOGIN_RESULT;


public class IncomingFragment extends Fragment {

    private FragmentActivity mActivity;
    private IncomingViewModel mViewModel;
    private RecyclerView recycler;
    private int mSortingOption;
    private int mSortReverse;
    private int mPage = 0;
    private SwipeRefreshLayout mRootView;
    private int mTotalTasksCount;
    private TextView mTotalTasksView, mShowedTasksView;

    private final String[] filterOptions = {"Ожидают подтверждения", "В работе", "Завершённые", "Отменённые пользователем", "Отменённые исполнителем"};
    final boolean[] filter = {true, true, false, false, false};
    private static final int LIMIT_TASKS_FOR_PAGE = 20;
    private static final String[] sortingOptions = new String[]{"По статусу", "По названию", "По времени добавления", "По назначению", "По времени завершения"};
    private AlertDialog mWaitingDialog;

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
        newTaskData.observe(mActivity, response -> {
            if (response != null) {
                mRootView.setRefreshing(false);
                hideWaiter();
                if (response.list != null) {
                    mTotalTasksCount = response.totalTasksCount;
                    mTotalTasksView.setText(String.format(Locale.ENGLISH, "Всего: %d", mTotalTasksCount));
                    mShowedTasksView.setText(String.format(Locale.ENGLISH, "Показано: %d", (mPage * LIMIT_TASKS_FOR_PAGE + response.list.size())));
                    if (response.list.size() > 0) {
                        mTotalTasksCount = response.totalTasksCount;
                        mTotalTasksView.setText(String.format(Locale.ENGLISH, "Всего: %d", mTotalTasksCount));
                        // обновлю данные
                        IncomingTasksAdapter adapter = ((IncomingTasksAdapter) recycler.getAdapter());
                        if (adapter != null) {
                            adapter.setItems(response.list, mPage);
                        }
                    }

                } else {
                    if (mActivity != null && !mActivity.isFinishing()) {
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
        mRootView = root.findViewById(R.id.rootView);
        mTotalTasksView = root.findViewById(R.id.totalTasksCount);
        mShowedTasksView = root.findViewById(R.id.showedTasks);
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // проверю последний видимый элемент
                LinearLayoutManager manager = (LinearLayoutManager) recycler.getLayoutManager();
                if (manager != null) {
                    @SuppressWarnings("rawtypes") RecyclerView.Adapter adapter = recycler.getAdapter();
                    if (adapter != null) {
                        int position = manager.findLastCompletelyVisibleItemPosition();
                        if (position == adapter.getItemCount() - 1 && ((mPage + 1) * LIMIT_TASKS_FOR_PAGE < mTotalTasksCount)) {
                            showLoadMoreDialog();
                        }
                    }
                }
            }
        });
        if (mRootView != null) {
            mRootView.setOnRefreshListener(() -> {
                mRootView.setRefreshing(true);
                mViewModel.updateTaskList(filter, mSortingOption, mSortReverse, LIMIT_TASKS_FOR_PAGE, 0);
            });
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!Preferences.getInstance().isUserUnknown()) {
            mViewModel.updateTaskList(filter, mSortingOption, mSortReverse, LIMIT_TASKS_FOR_PAGE, mPage);
        }
        if (Preferences.getInstance().isNotDisturb()) {
            Snackbar snackbar = Snackbar.make(mRootView, "Активирован тихий режим!", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Отменить", v -> {
                snackbar.dismiss();
                Preferences.getInstance().setSilence(false);
            });
            snackbar.setAnchorView(mActivity.findViewById(R.id.nav_view));
            snackbar.show();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        mActivity.getMenuInflater().inflate(R.menu.outgoing_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(getString(R.string.filter_message))) {
            // покажу диалог выбора элементов для отображения
            showFilterDialog();
        } else if (item.getTitle().equals(getString(R.string.sort_message))) {
            showSortDialog();
        } else if (item.getTitle().equals(getString(R.string.logout_menu_item))) {
            // удалю токен аутентификации и направлю на экран входа
            Preferences.getInstance().removeToken();
            startActivityForResult(new Intent(mActivity, LoginActivity.class), LOGIN_RESULT);
            return true;
        } else if (item.getTitle().equals(getString(R.string.show_preferences_menu_item))) {
            startActivity(new Intent(mActivity, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);
        dialogBuilder.setTitle("Отображать задачи")
                .setMultiChoiceItems(filterOptions, filter, (dialog, which, isChecked) -> filter[which] = isChecked)
                .setPositiveButton("Показать", (dialog, which) -> {
                            showWaiter();
                            mPage = 0;
                            mViewModel.updateTaskList(filter, mSortingOption, mSortReverse, LIMIT_TASKS_FOR_PAGE, mPage);
                        }
                );
        dialogBuilder.create().show();
    }

    private void showSortDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
        dialog.setTitle("Выберите тип сортировки")
                .setItems(sortingOptions, (dialog1, which) -> {
                    showWaiter();
                    mPage = 0;
                    if (mSortingOption == which) {
                        if (mSortReverse == 1) {
                            mSortReverse = 0;
                        } else {
                            mSortReverse = 1;
                        }
                    } else {
                        mSortReverse = 0;
                    }
                    mSortingOption = which;
                    mViewModel.updateTaskList(filter, mSortingOption, mSortReverse, LIMIT_TASKS_FOR_PAGE, mPage);
                });
        // покажу список типов сортировки
        if (!mActivity.isFinishing()) {
            dialog.show();
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position;
        IncomingTasksAdapter adapter = (IncomingTasksAdapter) recycler.getAdapter();
        if (adapter != null) {
            try {
                position = adapter.getPosition();
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
            } else if (item.getTitle().equals(getString(R.string.call_initiator_menu_item))) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "+79308184347"));
                startActivity(intent);
            }
        }
        return super.onContextItemSelected(item);
    }

    private void showWaiter() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
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

    private void showLoadMoreDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);
        dialogBuilder.setTitle("Добавка")
                .setMessage("Загрузить следующую страницу?")
                .setPositiveButton("Да", (dialog, which) -> {
                            showWaiter();
                            mPage++;
                            mViewModel.updateTaskList(filter, mSortingOption, mSortReverse, LIMIT_TASKS_FOR_PAGE, mPage);
                        }
                )
                .setNegativeButton("Нет", null);
        dialogBuilder.create().show();
    }
}
