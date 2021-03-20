package net.veldor.todo.workers;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.veldor.todo.App;
import net.veldor.todo.selections.RefreshDataResponse;
import net.veldor.todo.selections.TaskItem;
import net.veldor.todo.utils.TimeHandler;

import java.util.Map;

import static java.util.Map.entry;

public class UpdateOutgoingTaskListWorker extends ConnectWorker {

    public static final String ACTION = "update task list";
    public static final String FILTER_DATA = "filter data";
    public static final String SORT_DATA = "sort data";
    public static final String LIMIT_DATA = "limit data";
    public static final String PAGE_DATA = "page data";
    public static final String SORT_REVERSE_DATA = "sort reverse";

    public UpdateOutgoingTaskListWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Data data = getInputData();
            String filter = data.getString(FILTER_DATA);
            if(filter == null){
                filter = "11000";
            }
            int sort = data.getInt(SORT_DATA, 0);
            int limit = data.getInt(LIMIT_DATA, 0);
            int page = data.getInt(PAGE_DATA, 0);
            int sortReverse = data.getInt(SORT_REVERSE_DATA, 0);
            Map<String, String> args = Map.ofEntries(
                    entry("filter", filter),
                    entry("sort", String.valueOf(sort)),
                    entry("limit", String.valueOf(limit)),
                    entry("page", String.valueOf(page)),
                    entry("sortReverse", String.valueOf(sortReverse))
            );
            String answer = handleRequest("getTaskList", args);
            if (answer != null) {
                GsonBuilder builder = new GsonBuilder();
                Gson responseGson = builder.create();
                RefreshDataResponse resp = responseGson.fromJson(answer, RefreshDataResponse.class);
                if (resp.list != null && resp.list.size() > 0) {
                    for (TaskItem i :
                            resp.list
                    ) {
                        if (i.task_creation_time > 0) {
                            i.task_creation_time_formatted = TimeHandler.formatTime(i.task_creation_time);
                            i.shortCreateTime = TimeHandler.shortFormatTime(i.task_creation_time);
                        } else {
                            i.task_creation_time_formatted = "Ещё не назначено";
                            i.shortCreateTime = "--";
                        }
                        if (i.task_accept_time > 0) {
                            i.task_accept_time_formatted = TimeHandler.formatTime(i.task_accept_time);
                            i.shortAcceptTime = TimeHandler.shortFormatTime(i.task_accept_time);
                        } else {
                            i.task_accept_time_formatted = "Ещё не назначено";
                            i.shortAcceptTime = "--";
                        }
                        if (i.task_planned_finish_time > 0) {
                            i.task_planned_finish_time_formatted = TimeHandler.formatTime(i.task_planned_finish_time);
                        } else {
                            i.task_planned_finish_time_formatted = "Ещё не назначено";
                        }
                        if (i.task_finish_time > 0) {
                            i.task_finish_time_formatted = TimeHandler.formatTime(i.task_finish_time);
                            i.shortFinishTime = TimeHandler.shortFormatTime(i.task_finish_time);
                        } else {
                            i.task_finish_time_formatted = "Ещё не назначено";
                            i.shortFinishTime = "--";
                        }
                        switch (i.target) {
                            case "office":
                                i.target = "Офис";
                                break;
                            case "it":
                                i.target = "IT";
                                break;
                            case "engeneer":
                                i.target = "Инженерная служба";
                                break;
                        }

                        switch (i.task_status) {
                            case "created":
                                i.task_status = "Ожидает подтвержения";
                                i.task_status_code = 1;
                                i.sideColor = Color.parseColor("#FFC107");
                                break;
                            case "accepted":
                                i.task_status = "В работе";
                                i.task_status_code = 2;
                                i.sideColor = Color.parseColor("#03A9F4");
                                break;
                            case "finished":
                                i.task_status = "Завершено";
                                i.task_status_code = 3;
                                i.sideColor = Color.parseColor("#8BC34A");
                                break;
                            case "cancelled_by_initiator":
                                i.task_status = "Отменено пользователем";
                                i.task_status_code = 4;
                                i.sideColor = Color.parseColor("#FF5722");
                                break;
                            case "cancelled_by_executor":
                                i.task_status = "Отменено исполнителем";
                                i.task_status_code = 5;
                                i.sideColor = Color.parseColor("#FF5722");
                                break;
                        }
                    }
                }
                App.getInstance().mCurrentList.postValue(resp);
                return Result.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.failure();
    }
}
