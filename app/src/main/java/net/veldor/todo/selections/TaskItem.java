package net.veldor.todo.selections;

import java.io.Serializable;

public class TaskItem implements Serializable {
    public String id;
    public String initiator;
    public String target;
    public String executor;
    public long task_creation_time;
    public String task_accept_time;
    public long task_planned_finish_time;
    public long task_finish_time;
    public String task_header;
    public String task_body;
    public String task_status;
    public String executor_comment;
    public int sideColor;
}
