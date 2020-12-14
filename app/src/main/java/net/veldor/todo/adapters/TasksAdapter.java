package net.veldor.todo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import net.veldor.todo.BR;
import net.veldor.todo.R;
import net.veldor.todo.WorkingTask;
import net.veldor.todo.databinding.WorkTaskItemBinding;

import java.util.ArrayList;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder> {

    private final ArrayList<WorkingTask> mTasks;
    private LayoutInflater mLayoutInflater;

    public TasksAdapter(ArrayList<WorkingTask> tasksList){
        mTasks = tasksList;
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


    class ViewHolder extends RecyclerView.ViewHolder {

        private final WorkTaskItemBinding mBinding;
        private final View mRoot;
        private WorkingTask mWork;

        public ViewHolder(WorkTaskItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mRoot = mBinding.getRoot();
        }

        public void bind(WorkingTask workingTask) {
            mWork = workingTask;
            mBinding.setVariable(BR.task, mWork);
            mBinding.executePendingBindings();
        }
    }
}
