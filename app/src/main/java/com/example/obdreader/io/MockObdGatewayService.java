package com.example.obdreader.io;

import android.util.Log;

import com.example.obdreader.activity.MainActivity;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 该服务主要负责建立和维护应用程序运行所在的设备与其他设备之间的永久连接OBD蓝牙接口。
 * <p/>
 * 其次，它将用作ObdCommandJobs的存储库时间应用程序状态机。
 */
public class MockObdGatewayService extends AbstractGatewayService {

    private static final String TAG = MockObdGatewayService.class.getName();

    public void startService() {
        Log.d(TAG, "Starting " + this.getClass().getName() + " service..");

        // 让我们配置连接。
        Log.d(TAG, "为连接配置排队作业。");
        queueJob(new ObdCommandJob(new ObdResetCommand()));
        queueJob(new ObdCommandJob(new EchoOffCommand()));

        /*
         * 将根据测试结果第二次发送。
         *
         * TODO 这可以通过仅发出而无需排队作业来完成
         * command.run(), command.getResult() and validate the result.
         */
        queueJob(new ObdCommandJob(new EchoOffCommand()));
        queueJob(new ObdCommandJob(new LineFeedOffCommand()));
        queueJob(new ObdCommandJob(new TimeoutCommand(62)));

        //现在将协议设置为AUTO
        queueJob(new ObdCommandJob(new SelectProtocolCommand(ObdProtocols.AUTO)));

        //返回伪数据的作业
        queueJob(new ObdCommandJob(new AmbientAirTemperatureCommand()));
        queueCounter = 0L;
        Log.d(TAG, "初始化作业已排队。");
        isRunning = true;
    }


    /**
     * 运行队列，直到服务停止
     */
    protected void executeQueue() {
        Log.d(TAG, "正在执行队列");
        while (!Thread.currentThread().isInterrupted()) {
            ObdCommandJob job = null;
            try {
                job = jobsQueue.take();

                Log.d(TAG, "Taking job[" + job.getId() + "] from queue..");

                if (job.getState().equals(ObdCommandJob.ObdCommandJobState.NEW)) {
                    Log.d(TAG, "作业状态为新。 运行..");
                    job.setState(ObdCommandJob.ObdCommandJobState.RUNNING);
                    Log.d(TAG, job.getCommand().getName());
                    job.getCommand().run(new ByteArrayInputStream("41 00 00 00>41 00 00 00>41 00 00 00>".getBytes()), new ByteArrayOutputStream());
                } else {
                    Log.e(TAG, "作业状态不是新的，因此它不应该在队列中。 错误提示！");
                }
            } catch (InterruptedException i) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
                if (job != null) {
                    job.setState(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR);
                }
                Log.e(TAG, "运行命令失败. -> " + e.getMessage());
            }

            if (job != null) {
                Log.d(TAG, "工作完成.");
                job.setState(ObdCommandJob.ObdCommandJobState.FINISHED);
                final ObdCommandJob job2 = job;
                ((MainActivity) ctx).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) ctx).stateUpdate(job2);
                    }
                });

            }
        }
    }

    /**
     * 停止OBD连接和队列处理。
     */
    public void stopService() {
        Log.d(TAG, "Stopping service..");
        notificationManager.cancel(NOTIFICATION_ID);
        jobsQueue.clear();
        isRunning = false;
        // 杀死服务
        stopSelf();
    }

}
