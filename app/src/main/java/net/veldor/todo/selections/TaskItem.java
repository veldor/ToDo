package net.veldor.todo.selections;

import java.io.Serializable;

public class TaskItem implements Serializable {
    public String id;
    public String initiator;
    public String initiatorPhone;
    public String initiatorEmail;
    public String target;
    public String executor;
    public String executorPhone;
    public String executorEmail;
    public long task_creation_time;
    public String task_creation_time_formatted;
    public long task_accept_time;
    public String task_accept_time_formatted;
    public long task_planned_finish_time;
    public String task_planned_finish_time_formatted;
    public long task_finish_time;
    public String task_finish_time_formatted;
    public String task_header;
    public String task_body;
    public String task_status;
    public int task_status_code;
    public String executor_comment;
    public int sideColor;
    public boolean imageFile;
    public boolean attachmentFile;
    public String shortCreateTime;
    public String shortAcceptTime;
    public String shortFinishTime;
}
