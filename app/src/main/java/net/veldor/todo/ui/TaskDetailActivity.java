package net.veldor.todo.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import net.veldor.todo.R;
import net.veldor.todo.selections.TaskItem;
import net.veldor.todo.utils.Grammar;

import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        TaskItem task = (TaskItem) getIntent().getSerializableExtra("data");
        TextView headerView = findViewById(R.id.taskName);
        if(headerView != null){
            headerView.setText(task.task_header);
        }
        TextView bodyView = findViewById(R.id.taskBody);
        if(bodyView != null){
            bodyView.setText(task.task_body);
        }
        TextView stateView = findViewById(R.id.taskState);
        if(stateView != null){
            stateView.setText(task.task_status);
        }
        TextView createDateView = findViewById(R.id.createTime);
        if(createDateView != null){
            createDateView.setText(String.format(Locale.ENGLISH, "Дата подачи заявки: %s",Grammar.timestampToDate(task.task_creation_time * 1000)));
        }
        TextView plannedFinishDateView = findViewById(R.id.plannedFinishTime);
        if(plannedFinishDateView != null){
            if(task.task_planned_finish_time > 0){
                plannedFinishDateView.setText(String.format(Locale.ENGLISH, ":Срок выполнения: %s",Grammar.timestampToDate(task.task_planned_finish_time * 1000)));
            }
        }
    }
}