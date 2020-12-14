package net.veldor.todo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import net.veldor.todo.adapters.TasksAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUi();
    }

    private void setupUi() {
        recycler = findViewById(R.id.resultsList);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<WorkingTask> tasks = new ArrayList<>();
        WorkingTask wt;
        int counter = 0;
        while (counter < 10){
            wt = new WorkingTask();
            wt.taskName = "Задача " + counter;
            wt.taskValue = "Нужно сделать что-то " + counter;
            wt.taskStatus = counter % 2 == 0 ? "В работе" : "Ожидает подтверждения";
            wt.worker = counter % 2 == 1 ? "Отдел IT" : "Инженерная служба";
            tasks.add(wt);
            counter++;
        }
        recycler.setAdapter(new TasksAdapter(tasks));
    }
}