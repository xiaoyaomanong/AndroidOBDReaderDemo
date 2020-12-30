package com.example.obdreader.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.obdreader.R;
import com.example.obdreader.io.BluetoothManager;
import com.github.pires.obd.commands.control.TroubleCodesCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.ResetTroubleCodesCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.MisunderstoodCommandException;
import com.github.pires.obd.exceptions.NoDataException;
import com.github.pires.obd.exceptions.UnableToConnectException;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class TroubleCodesActivity extends Activity {

    private static final String TAG = TroubleCodesActivity.class.getName();
    private static final int NO_BLUETOOTH_DEVICE_SELECTED = 0;
    private static final int CANNOT_CONNECT_TO_DEVICE = 1;
    private static final int NO_DATA = 3;
    private static final int DATA_OK = 4;
    private static final int CLEAR_DTC = 5;
    private static final int OBD_COMMAND_FAILURE = 10;
    private static final int OBD_COMMAND_FAILURE_IO = 11;
    private static final int OBD_COMMAND_FAILURE_UTC = 12;
    private static final int OBD_COMMAND_FAILURE_IE = 13;
    private static final int OBD_COMMAND_FAILURE_MIS = 14;
    private static final int OBD_COMMAND_FAILURE_NODATA = 15;
    @Inject
    SharedPreferences prefs;
    private ProgressDialog progressDialog;
    private String remoteDevice;
    private GetTroubleCodesTask gtct;
    private BluetoothDevice dev = null;
    private BluetoothSocket sock = null;
    private final Handler mHandler = new Handler(new Handler.Callback() {


        public boolean handleMessage(Message msg) {
            android.util.Log.d(TAG, "在处理程序上收到的消息");
            switch (msg.what) {
                case NO_BLUETOOTH_DEVICE_SELECTED:
                    makeToast(getString(R.string.text_bluetooth_nodevice));
                    finish();
                    break;
                case CANNOT_CONNECT_TO_DEVICE:
                    makeToast(getString(R.string.text_bluetooth_error_connecting));
                    finish();
                    break;

                case OBD_COMMAND_FAILURE:
                    makeToast(getString(R.string.text_obd_command_failure));
                    finish();
                    break;
                case OBD_COMMAND_FAILURE_IO:
                    makeToast(getString(R.string.text_obd_command_failure) + " IO");
                    finish();
                    break;
                case OBD_COMMAND_FAILURE_IE:
                    makeToast(getString(R.string.text_obd_command_failure) + " IE");
                    finish();
                    break;
                case OBD_COMMAND_FAILURE_MIS:
                    makeToast(getString(R.string.text_obd_command_failure) + " MIS");
                    finish();
                    break;
                case OBD_COMMAND_FAILURE_UTC:
                    makeToast(getString(R.string.text_obd_command_failure) + " UTC");
                    finish();
                    break;
                case OBD_COMMAND_FAILURE_NODATA:
                    makeToastLong(getString(R.string.text_noerrors));
                    //finish();
                    break;

                case NO_DATA:
                    makeToast(getString(R.string.text_dtc_no_data));
                    ///finish();
                    break;
                case DATA_OK:
                    dataOk((String) msg.obj);
                    break;

            }
            return false;
        }
    });

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        remoteDevice = prefs.getString(ConfigActivity.BLUETOOTH_LIST_KEY, null);
        if (remoteDevice == null || "".equals(remoteDevice)) {
            android.util.Log.e(TAG, "尚未选择蓝牙设备。");
            mHandler.obtainMessage(NO_BLUETOOTH_DEVICE_SELECTED).sendToTarget();
        } else {
            gtct = new GetTroubleCodesTask();
            gtct.execute(remoteDevice);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trouble_codes, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理操作栏项目上的按下
        if (item.getItemId() == R.id.action_clear_codes) {
            try {
                sock = BluetoothManager.connect(dev, new BluetoothManager.ConnBluetoothSocketListener() {
                    @Override
                    public void connectMsg(int code, String msg) {
                        android.util.Log.d(TAG, msg);
                    }
                });
            } catch (Exception e) {
                android.util.Log.e(TAG, "建立连接时出错。 -> " + e.getMessage());
                android.util.Log.d(TAG, "此处在处理程序上收到的消息");
                mHandler.obtainMessage(CANNOT_CONNECT_TO_DEVICE).sendToTarget();
                return true;
            }
            try {

                android.util.Log.d("测试复位", "尝试重置");
                //new ObdResetCommand().run(sock.getInputStream(), sock.getOutputStream());
                ResetTroubleCodesCommand clear = new ResetTroubleCodesCommand();
                clear.run(sock.getInputStream(), sock.getOutputStream());
                String result = clear.getFormattedResult();
                android.util.Log.d("测试复位", "尝试重置结果: " + result);
            } catch (Exception e) {
                android.util.Log.e(TAG, "建立连接时出错。 -> " + e.getMessage());
            }
            gtct.closeSocket(sock);
            //关闭对话框时刷新主要活动
            Intent refresh = new Intent(this, TroubleCodesActivity.class);
            startActivity(refresh);
            this.finish(); //
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    java.util.Map<String, String> getDict(int keyId, int valId) {
        String[] keys = getResources().getStringArray(keyId);
        String[] vals = getResources().getStringArray(valId);

        java.util.Map<String, String> dict = new HashMap<String, String>();
        for (int i = 0, l = keys.length; i < l; i++) {
            dict.put(keys[i], vals[i]);
        }
        return dict;
    }

    public void makeToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void makeToastLong(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        toast.show();
    }

    private void dataOk(String res) {
        android.widget.ListView lv = (android.widget.ListView) findViewById(R.id.listView);
        java.util.Map<String, String> dtcVals = getDict(R.array.dtc_keys, R.array.dtc_values);
        //TODO replace below codes (res) with aboce dtcVals
        //String tmpVal = dtcVals.get(res.split("\n"));
        //String[] dtcCodes = new String[]{};
        ArrayList<String> dtcCodes = new ArrayList<String>();
        //int i =1;
        if (res != null) {
            for (String dtcCode : res.split("\n")) {
                dtcCodes.add(dtcCode + " : " + dtcVals.get(dtcCode));
                android.util.Log.d("测试", dtcCode + " : " + dtcVals.get(dtcCode));
            }
        } else {
            dtcCodes.add("没有错误");
        }
        ArrayAdapter<String> myarrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dtcCodes);
        lv.setAdapter(myarrayAdapter);
        lv.setTextFilterEnabled(true);
    }


    public static class ModifiedTroubleCodesObdCommand extends TroubleCodesCommand {
        @Override
        public String getResult() {
            //输出中删除不必要的响应，因为这会导致错误的错误代码
            return rawData.replace("SEARCHING...", "").replace("NODATA", "");
        }
    }

    public class ClearDTC extends ResetTroubleCodesCommand {
        @Override
        public String getResult() {
            return rawData;
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class GetTroubleCodesTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            //创建一个新的进度对话框
            progressDialog = new ProgressDialog(TroubleCodesActivity.this);
            //设置进度对话框以显示水平进度条
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //将对话框标题设置为“正在加载...”
            progressDialog.setTitle(getString(R.string.dialog_loading_title));
            //将对话框消息设置为“正在加载应用程序视图，请稍候...”
            progressDialog.setMessage(getString(R.string.dialog_loading_body));
            //按下返回键无法取消此对话框
            progressDialog.setCancelable(false);
            //该对话框不是不确定的
            progressDialog.setIndeterminate(false);
            //物品数量上限为100
            progressDialog.setMax(5);
            //将当前进度设置为零
            progressDialog.setProgress(0);
            //显示进度对话框
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";

            //获取当前线程的令牌
            synchronized (this) {
                android.util.Log.d(TAG, "正在启动服务..");
                // 获取远程蓝牙设备
                final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                dev = btAdapter.getRemoteDevice(params[0]);
                android.util.Log.d(TAG, "停止蓝牙搜索。");
                btAdapter.cancelDiscovery();
                android.util.Log.d(TAG, "正在启动OBD连接。");
                // 实例化远程设备的BluetoothSocket并连接它。
                try {
                    sock = BluetoothManager.connect(dev, new BluetoothManager.ConnBluetoothSocketListener() {
                        @Override
                        public void connectMsg(int code, String msg) {
                            android.util.Log.d(TAG, msg);
                        }
                    });
                } catch (Exception e) {
                    android.util.Log.e(TAG, "建立连接时出错。 -> " + e.getMessage());
                    android.util.Log.d(TAG, "此处在处理程序上收到的消息");
                    mHandler.obtainMessage(CANNOT_CONNECT_TO_DEVICE).sendToTarget();
                    return null;
                }

                try {
                    // 让我们配置连接。
                    android.util.Log.d(TAG, "为连接配置排队作业。");
                    onProgressUpdate(1);
                    new ObdResetCommand().run(sock.getInputStream(), sock.getOutputStream());
                    onProgressUpdate(2);
                    new EchoOffCommand().run(sock.getInputStream(), sock.getOutputStream());
                    onProgressUpdate(3);
                    new LineFeedOffCommand().run(sock.getInputStream(), sock.getOutputStream());
                    onProgressUpdate(4);
                    new SelectProtocolCommand(ObdProtocols.AUTO).run(sock.getInputStream(), sock.getOutputStream());
                    onProgressUpdate(5);
                    ModifiedTroubleCodesObdCommand tcoc = new ModifiedTroubleCodesObdCommand();
                    tcoc.run(sock.getInputStream(), sock.getOutputStream());
                    result = tcoc.getFormattedResult();
                    onProgressUpdate(6);
                } catch (IOException e) {
                    e.printStackTrace();
                    android.util.Log.e("DTCERR", e.getMessage());
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE_IO).sendToTarget();
                    return null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    android.util.Log.e("DTCERR", e.getMessage());
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE_IE).sendToTarget();
                    return null;
                } catch (UnableToConnectException e) {
                    e.printStackTrace();
                    android.util.Log.e("DTCERR", e.getMessage());
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE_UTC).sendToTarget();
                    return null;
                } catch (MisunderstoodCommandException e) {
                    e.printStackTrace();
                    android.util.Log.e("DTCERR", e.getMessage());
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE_MIS).sendToTarget();
                    return null;
                } catch (NoDataException e) {
                    android.util.Log.e("DTCERR", e.getMessage());
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE_NODATA).sendToTarget();
                    return null;
                } catch (Exception e) {
                    android.util.Log.e("DTCERR", e.getMessage());
                    mHandler.obtainMessage(OBD_COMMAND_FAILURE).sendToTarget();
                } finally {
                    closeSocket(sock);
                }
            }

            return result;
        }

        public void closeSocket(BluetoothSocket sock) {
            if (sock != null)
                // close socket
                try {
                    sock.close();
                } catch (IOException e) {
                    android.util.Log.e(TAG, e.getMessage());
                }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();


            mHandler.obtainMessage(DATA_OK, result).sendToTarget();
            setContentView(R.layout.trouble_codes);

        }
    }

}
