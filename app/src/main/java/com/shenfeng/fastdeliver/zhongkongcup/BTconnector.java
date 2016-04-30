package com.shenfeng.fastdeliver.zhongkongcup;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by singleghost on 16-2-19.
 */
public class BTconnector {

    public static final String TAG = "BTconnector";
    private static final String mRemoteAddress = "98:D3:33:80:7B:45"; // 蓝牙串口模块的地址
    private BluetoothSocket mbtSocket = null;
    public OutputStream mOutputStream = null;
    public InputStream mInputStream = null;
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");   //蓝牙串口服务的UUID
    private BluetoothAdapter mBluetoothAdapter;

    private static final int REQUEST_DISCOVERABLE = 0x2;

    public BTconnector() {
    }

    public void enableBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();
    }

    public boolean connectBT() {

        if (!mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth can't be discovered");
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mRemoteAddress);
        try {
            mbtSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Create RFCOMM socket to device ERROR!", e);
            return false;
        }

        mBluetoothAdapter.cancelDiscovery();
        try {
            mbtSocket.connect();
        } catch (IOException e) {
            Log.e(TAG, "Socket connection error.");
            return false;
        }
        try {
            mOutputStream = mbtSocket.getOutputStream();
            mInputStream = mbtSocket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Get outputStream or inputStream error!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public InputStream getInputStream() {
        return mInputStream;
    }

    public OutputStream getOutputStream() {
        return mOutputStream;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    private void closeSocket() {
        if (mbtSocket != null) {
            try {
                mbtSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Unable to close the socket");
            }
        }

    }

    private void disableBT() {
        closeSocket();
        mBluetoothAdapter.disable();
    }

    public boolean isconnected() {
        return mbtSocket.isConnected();
    }
}
