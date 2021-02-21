package net.veldor.todo.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import net.veldor.todo.App;
import net.veldor.todo.BR;
import net.veldor.todo.R;
import net.veldor.todo.databinding.IncomingWorkTaskItemBinding;
import net.veldor.todo.selections.TaskItem;
import net.veldor.todo.ui.IncomingTaskDetailsActivity;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class IncomingTasksAdapter extends RecyclerView.Adapter<IncomingTasksAdapter.ViewHolder> {
    private ArrayList<TaskItem> mTasks = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private int lastSortOption = -1;
    private int position;

    public IncomingTasksAdapter() {
    }

    @NonNull
    @Override
    public IncomingTasksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(viewGroup.getContext());
        }
        IncomingWorkTaskItemBinding binding = DataBindingUtil.inflate(mLayoutInflater, R.layout.incoming_work_task_item, viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomingTasksAdapter.ViewHolder holder, int position) {
        holder.bind(mTasks.get(position));
        holder.itemView.setOnLongClickListener(v -> {
            Log.d("surprise", "OutgoingTasksAdapter onBindViewHolder 55: long c");
            setPosition(holder.getAdapterPosition());
            return false;
        });
    }

    private void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int getItemCount() {
        if (mTasks != null) {
            return mTasks.size();
        }
        return 0;
    }

    public void setItems(ArrayList<TaskItem> list, int thisPageCount) {
        if(thisPageCount > 0){
            mTasks.addAll(list);
        }
        else{
            mTasks = list;
        }
        notifyDataSetChanged();
    }

//    public void applyFilter(boolean[] filter) {
//        mTasks = new ArrayList<>();
//        HashMap<String, Boolean> currentFilter = new HashMap<>();
//        currentFilter.put("Ожидает подтвержения", filter[0]);
//        currentFilter.put("В работе", filter[1]);
//        currentFilter.put("Завершено", filter[2]);
//        currentFilter.put("Отменено пользователем", filter[3]);
//        currentFilter.put("Отменено исполнителем", filter[4]);
//        if (mRawTasks.size() > 0) {
//            for (TaskItem t :
//                    mRawTasks) {
//                if (currentFilter.get(t.task_status)) {
//                    mTasks.add(t);
//                }
//            }
//            notifyDataSetChanged();
//        }
//    }

    public void sort(int which) {
        Collections.sort(mTasks, (lhs, rhs) -> {
            // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
            //return lhs.getId() > rhs.getId() ? -1 : (lhs.customInt < rhs.customInt ) ? 1 : 0;
            if (which == 0) {
                if (which == lastSortOption) {
                    return Long.compare(rhs.task_status_code, lhs.task_status_code);
                }
                return Long.compare(lhs.task_status_code, rhs.task_status_code);

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
        });
        notifyDataSetChanged();
        if (lastSortOption == which) {
            lastSortOption = -1;
        } else {
            lastSortOption = which;
        }
    }

    public int getPosition() {
        return position;
    }

    public TaskItem getItem(int position) {
        return mTasks.get(position);
    }


    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        private final IncomingWorkTaskItemBinding mBinding;
        private TaskItem mWork;
        private final View mRoot;

        public ViewHolder(IncomingWorkTaskItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mRoot = mBinding.getRoot();
            Intent intent = new Intent(App.getInstance(), IncomingTaskDetailsActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            mRoot.setOnClickListener(v -> {
                Log.d("surprise", "ViewHolder ViewHolder 159: click to go to " + mWork.task_header);
                App.getInstance().mTaskInfo.setValue(null);
                intent.putExtra(IncomingTaskDetailsActivity.FULL_DATA, mWork);
                App.getInstance().startActivity(intent);
            });
            mRoot.setOnCreateContextMenuListener(this);
        }

        public void bind(TaskItem workingTask) {
            mWork = workingTask;
            mBinding.setVariable(BR.task, mWork);
            mBinding.executePendingBindings();
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            Log.d("surprise", "ViewHolder onCreateContextMenu 154: context menu created");
            menu.setHeaderTitle(App.getInstance().getString(R.string.outgoing_task_context_label));
            menu.add(0, v.getId(), 0, App.getInstance().getString(R.string.show_more_menu_item));
            menu.add(0, v.getId(), 0, App.getInstance().getString(R.string.call_initiator_menu_item));
            menu.add(0, v.getId(), 0, App.getInstance().getString(R.string.claim_menu_item));
        }
    }
}
