package com.example.obdreader.io;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothManager {

    private static final String TAG = BluetoothManager.class.getName();
    private static ConnBluetoothSocketListener listeners;
    /*
     * http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
     * #createRfcommSocketToServiceRecord(java.util.UUID)
     * 提示：如果您要连接到蓝牙串行板，请尝试使用众所周知的SPP UUID 00001101-0000-1000-8000-00805F9B34FB。
     * 但是如果你正在连接到Android对等设备，然后请生成您自己的唯一UUID。”
     */
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * 实例化远程设备的BluetoothSocket并将其连接。
     * <p/>
     * See http://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3/18786701#18786701
     *
     * @param dev 要连接的远程设备
     * @return The BluetoothSocket
     * @throws IOException
     */
    public static BluetoothSocket connect(BluetoothDevice dev, ConnBluetoothSocketListener listener) throws IOException {
        BluetoothSocket sock = null;
        BluetoothSocket sockFallback = null;
        listeners = listener;

        android.util.Log.d(TAG, "正在启动蓝牙连接..");
        try {
            sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
            sock.connect();
            listeners.connectMsg(1, "蓝牙连接成功");//连接成功
        } catch (Exception e1) {
            android.util.Log.e(TAG, "建立蓝牙连接时出错", e1);
            listeners.connectMsg(2, "建立蓝牙连接时出错");//连接成功
            assert sock != null;
            Class<?> clazz = sock.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
            try {
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{1};
                sockFallback = (BluetoothSocket) m.invoke(sock.getRemoteDevice(), params);
                sockFallback.connect();
                sock = sockFallback;
            } catch (Exception e2) {
                android.util.Log.e(TAG, "建立蓝牙连接时无法回退", e2);
                listeners.connectMsg(3, "建立蓝牙连接时无法回退");//连接成功
                throw new IOException(e2.getMessage());
            }
        }
        return sock;
    }

    public interface ConnBluetoothSocketListener {
        void connectMsg(int code, String msg);
    }
}