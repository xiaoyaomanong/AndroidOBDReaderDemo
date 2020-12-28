package com.example.obdreader.io;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import com.example.obdreader.R;
import com.example.obdreader.activity.ConfigActivity;
import com.example.obdreader.activity.MainActivity;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.UnsupportedCommandException;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;

/**
 * 该服务主要负责建立和维护应用程序运行所在的设备与其他设备之间的永久连接OBD蓝牙接口。
 * <p/>
 * 其次，它将用作ObdCommandJobs的存储库时间应用程序状态机。
 */
public class ObdGatewayService extends AbstractGatewayService {

    private static final String TAG = ObdGatewayService.class.getName();
    @Inject
    SharedPreferences prefs;

    private BluetoothDevice dev = null;
    private BluetoothSocket sock = null;

    public void startService() throws IOException {
        android.util.Log.d(TAG, "正在启动服务..");

        // 获取远程蓝牙设备
        final String remoteDevice = prefs.getString(ConfigActivity.BLUETOOTH_LIST_KEY, null);
        if (remoteDevice == null || "".equals(remoteDevice)) {
            Toast.makeText(ctx, getString(R.string.text_bluetooth_nodevice), Toast.LENGTH_LONG).show();

            android.util.Log.e(TAG, "尚未选择蓝牙设备。");

            // TODO 优雅地终止此服务
            stopService();
            throw new IOException();
        } else {

            final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            dev = btAdapter.getRemoteDevice(remoteDevice);


            /*
             * 建立蓝牙连接
             *
             * 因为发现是蓝牙适配器的繁重过程，所以在尝试连接到具有connect（）的远程设备。
             * 发现不是由活动管理的，但作为系统服务运行，因此应用程序应始终调用即使它没有直接请求发现，
             * 也要取消发现，只是为了确定。 如果蓝牙状态不是STATE_ON，则此API将返回false。
             *
             * see
             * http://developer.android.com/reference/android/bluetooth/BluetoothAdapter
             * .html#cancelDiscovery()
             */
            android.util.Log.d(TAG, "停止蓝牙发现。");
            btAdapter.cancelDiscovery();
            showNotification(getString(R.string.notification_action), getString(R.string.service_starting), R.drawable.ic_btcar, true, true, false);
            try {
                startObdConnection();
            } catch (Exception e) {
                android.util.Log.e(TAG, "建立连接时出错。 -> " + e.getMessage());
                //万一发生故障，请停止此服务。
                stopService();
                throw new IOException();
            }
            showNotification(getString(R.string.notification_action), getString(R.string.service_started), R.drawable.ic_btcar, true, true, false);
        }
    }

    /**
     * 启动并配置到OBD接口的连接。
     * <p/>
     * See http://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3/18786701#18786701
     *
     * @throws IOException
     */
    private void startObdConnection() throws IOException {
        android.util.Log.d(TAG, "正在启动OBD连接。");
        isRunning = true;
        try {
            sock = BluetoothManager.connect(dev, new BluetoothManager.ConnBluetoothSocketListener() {
                @Override
                public void connectMsg(int code, String msg) {
                    android.util.Log.e(TAG, "msg");
                }
            });
        } catch (Exception e2) {
            android.util.Log.e(TAG, "建立蓝牙连接时出错。 正在停止应用程式..", e2);
            stopService();
            throw new IOException();
        }

        // 让我们配置连接。
        android.util.Log.d(TAG, "为连接配置排队作业。");
        queueJob(new ObdCommandJob(new ObdResetCommand()));

        //下面是在发送命令之前给适配器足够的时间来重置，否则可以忽略第一个启动命令。
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
        // 从首选项获取协议
        final String protocol = prefs.getString(ConfigActivity.PROTOCOLS_LIST_KEY, "AUTO");
        queueJob(new ObdCommandJob(new SelectProtocolCommand(ObdProtocols.valueOf(protocol))));
        //返回伪数据的作业
        queueJob(new ObdCommandJob(new AmbientAirTemperatureCommand()));
        queueCounter = 0L;
        android.util.Log.d(TAG, "初始化作业已排队。");


    }

    /**
     * 该方法将作业添加到队列，同时将其ID设置为内部队列计数器。
     *
     * @param job 要排队的工作。
     */
    @Override
    public void queueJob(ObdCommandJob job) {
        // 这是执行英制单位选项的好地方
        job.getCommand().useImperialUnits(prefs.getBoolean(ConfigActivity.IMPERIAL_UNITS_KEY, false));
        //现在我们可以通过
        super.queueJob(job);
    }

    /**
     * 运行队列，直到服务停止
     */
    protected void executeQueue() {
        android.util.Log.d(TAG, "Executing queue..");
        while (!Thread.currentThread().isInterrupted()) {
            ObdCommandJob job = null;
            try {
                job = jobsQueue.take();
                //日志作业
                android.util.Log.d(TAG, "Taking job[" + job.getId() + "] from queue..");

                if (job.getState().equals(ObdCommandJob.ObdCommandJobState.NEW)) {
                    android.util.Log.d(TAG, "作业状态为新。 运行..");
                    job.setState(ObdCommandJob.ObdCommandJobState.RUNNING);
                    if (sock.isConnected()) {
                        job.getCommand().run(sock.getInputStream(), sock.getOutputStream());
                    } else {
                        job.setState(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR);
                        android.util.Log.e(TAG, "Can't run command on a closed socket.");
                    }
                } else
                    // 记录不是新工作
                    android.util.Log.e(TAG, "作业状态不是新的，因此它不应该在队列中。 错误提示！");
            } catch (InterruptedException i) {
                Thread.currentThread().interrupt();
            } catch (UnsupportedCommandException u) {
                if (job != null) {
                    job.setState(ObdCommandJob.ObdCommandJobState.NOT_SUPPORTED);
                }
                android.util.Log.d(TAG, "命令不受支持. -> " + u.getMessage());
            } catch (IOException io) {
                if (job != null) {
                    if (io.getMessage().contains("Broken pipe"))
                        job.setState(ObdCommandJob.ObdCommandJobState.BROKEN_PIPE);
                    else
                        job.setState(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR);
                }
                android.util.Log.e(TAG, "IO错误. -> " + io.getMessage());
            } catch (Exception e) {
                if (job != null) {
                    job.setState(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR);
                }
                android.util.Log.e(TAG, "运行命令失败. -> " + e.getMessage());
            }

            if (job != null) {
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
        android.util.Log.d(TAG, "正在停止服务");
        notificationManager.cancel(NOTIFICATION_ID);
        jobsQueue.clear();
        isRunning = false;

        if (sock != null)
            // 关闭socket
            try {
                sock.close();
            } catch (IOException e) {
                android.util.Log.e(TAG, e.getMessage());
            }

        // 杀死服务
        stopSelf();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public static void saveLogcatToFile(Context context, String devemail) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{devemail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "OBD2 Reader Debug Logs");
        StringBuilder sb = new StringBuilder();
        sb.append("\nManufacturer: ").append(Build.MANUFACTURER);
        sb.append("\nModel: ").append(Build.MODEL);
        sb.append("\nRelease: ").append(Build.VERSION.RELEASE);
        emailIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        String fileName = "OBDReader_logcat_" + System.currentTimeMillis() + ".txt";
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + File.separator + "OBD2Logs");
        if (dir.mkdirs()) {
            File outputFile = new File(dir, fileName);
            Uri uri = Uri.fromFile(outputFile);
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);

            android.util.Log.d("savingFile", "Going to save logcat to " + outputFile);
            //emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(emailIntent, "Pick an Email provider").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            try {
                @SuppressWarnings("unused")
                Process process = Runtime.getRuntime().exec("logcat -f " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
