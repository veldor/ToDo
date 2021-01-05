package net.veldor.todo.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import net.veldor.todo.App;
import net.veldor.todo.BR;
import net.veldor.todo.R;
import net.veldor.todo.databinding.WorkTaskItemBinding;
import net.veldor.todo.selections.TaskItem;
import net.veldor.todo.ui.TaskDetailActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder> {

    private ArrayList<TaskItem> mRawTasks = new ArrayList<>();
    private ArrayList<TaskItem> mTasks = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private int lastSortOption = -1;

    public TasksAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(viewGroup.getContext());
        }
        WorkTaskItemBinding binding = DataBindingUtil.inflate(mLayoutInflater, R.layout.work_task_item, viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TasksAdapter.ViewHolder holder, int position) {
        holder.bind(mTasks.get(position));
    }

    @Override
    public int getItemCount() {
        if (mTasks != null) {
            return mTasks.size();
        }
        return 0;
    }

    public void setItems(ArrayList<TaskItem> list, boolean[] filter) {
        mRawTasks = list;
        applyFilter(filter);
    }

    public void applyFilter(boolean[] filter) {
        mTasks = new ArrayList<>();
        HashMap<String, Boolean> currentFilter = new HashMap<>();
        currentFilter.put("Ожидает подтвержения", filter[0]);
        currentFilter.put("В работе", filter[1]);
        currentFilter.put("Завершено", filter[2]);
        currentFilter.put("Отменено", filter[3]);
        if (mRawTasks.size() > 0) {
            for (TaskItem t :
                    mRawTasks) {
                if (currentFilter.get(t.task_status)) {
                    mTasks.add(t);
                }
            }
            notifyDataSetChanged();
        }
    }

    public void sort(int which) {
        Log.d("surprise", "TasksAdapter sort 84: " + which);
        Collections.sort(mTasks, new Comparator<TaskItem>() {
            @Override
            public int compare(TaskItem lhs, TaskItem rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                //return lhs.getId() > rhs.getId() ? -1 : (lhs.customInt < rhs.customInt ) ? 1 : 0;
                if (which == 0) {
                    if (which == lastSortOption) {
                        return rhs.task_status.compareTo(lhs.task_status);
                    }
                    return lhs.task_status.compareTo(rhs.task_status);

                } else if (which == 1) {
                    if (which == lastSortOption) {
                        return lhs.task_header.compareTo(rhs.task_header);
                    }
                    return rhs.task_header.compareTo(lhs.task_header);
                } else if (which == 2) {
                    if (which == lastSortOption) {
                        return Long.compare(lhs.task_creation_time, rhs.task_creation_time);
                    }
                    return Long.compare(rhs.task_creation_time, lhs.task_creation_time);
                } else if (which == 3) {
                    if (which == lastSortOption) {
                        return rhs.target.compareTo(lhs.target);
                    }
                    return lhs.target.compareTo(rhs.target);
                } else if (which == 4) {
                    if (which == lastSortOption) {
                        return Long.compare(lhs.task_finish_time, rhs.task_finish_time);
                    }
                    return Long.compare(rhs.task_finish_time, lhs.task_finish_time);
                }
                return 0;
            }
        });
        notifyDataSetChanged();
        lastSortOption = which;
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private final WorkTaskItemBinding mBinding;
        private final View mRoot;
        private TaskItem mWork;

        public ViewHolder(WorkTaskItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mRoot = mBinding.getRoot();
            Intent intent = new Intent(App.getInstance(), TaskDetailActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            mRoot.setOnClickListener(v -> {
                intent.putExtra("data", mWork);
                App.getInstance().startActivity(intent);
            });
        }

        public void bind(TaskItem workingTask) {
            mWork = workingTask;
            mBinding.setVariable(BR.task, mWork);
            mBinding.executePendingBindings();
            TextView worker = mRoot.findViewById(R.id.worker);
            if (worker != null) {
                worker.setText(String.format(Locale.ENGLISH, "(%s) %s", workingTask.target, workingTask.executor));
            }
        }
    }
}
