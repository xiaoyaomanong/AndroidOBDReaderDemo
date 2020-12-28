/*
 * TODO put header
 */
package com.example.obdreader.io;

import com.github.pires.obd.commands.ObdCommand;

/**
 * 此类表示ObdGatewayService必须执行的工作，
 * 保持直到工作完成。 因此，是应用程序
 * ObdCommand实例的表示形式以及状态为
 * 由ObdGatewayService解释和操纵。
 */
public class ObdCommandJob {

    private Long _id;
    private ObdCommand _command;
    private ObdCommandJobState _state;

    /**
     * Default ctor.
     *
     * @param command ObCommand进行封装。
     */
    public ObdCommandJob(ObdCommand command) {
        _command = command;
        _state = ObdCommandJobState.NEW;
    }

    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        _id = id;
    }

    public ObdCommand getCommand() {
        return _command;
    }

    /**
     * @return 工作当前状态。
     */
    public ObdCommandJobState getState() {
        return _state;
    }

    /**
     * 设置新的作业状态。
     *
     * @param state the new job state.
     */
    public void setState(ObdCommandJobState state) {
        _state = state;
    }

    /**
     * 命令状态。
     */
    public enum ObdCommandJobState {
        NEW,//新的
        RUNNING,//正在执行
        FINISHED,//完成
        EXECUTION_ERROR,//执行错误
        BROKEN_PIPE,//破坏
        QUEUE_ERROR,//队列错误
        NOT_SUPPORTED//不支持
    }

}
