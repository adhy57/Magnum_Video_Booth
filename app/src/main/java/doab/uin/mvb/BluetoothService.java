package doab.uin.mvb;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by adhy57 on 07/10/2015.
 */
public class BluetoothService extends Service {

    BluetoothSocket mBluetoothSocket;
    OutputStream mOutputStream;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "The Service Started", Toast.LENGTH_LONG).show();
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
            int id = extras.getInt("id");
            if (id==0){
                String macAddress = extras.getString("mac");
                try {
                    connectBluetooth(macAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                String msg = extras.getString("msg");
                try {
                    send(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        diskonekPerangkat();
        super.onDestroy();
    }

    private void send(final String message) throws IOException {
        if (((MyApplication)getApplication()).ismConnected()) {
            if (mBluetoothSocket != null) {
                mOutputStream.write(message.getBytes());
            }
        }else{
            Toast.makeText(getApplicationContext(), "Koneksikan perangkat dahulu!", Toast.LENGTH_SHORT).show();
        }
    }

    public void connectBluetooth(String macAddress) throws IOException {
        // TODO Auto-generated method stub

        BluetoothDevice mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddress);
        //Method mMethod = mBluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
        UUID localUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(localUUID);
        mBluetoothSocket.connect();

        mOutputStream = mBluetoothSocket.getOutputStream();

//        if(!((MyApplication)getApplication()).ismConnected()){
//            listAdapter.clear();
//            listAdapter.add("Putuskan Perangkat");
//        }
        ((MyApplication)getApplication()).setmConnected(true);
//        mConnected = true;
//        return true;
    }

    public void diskonekPerangkat() {
        // TODO Auto-generated method stub
        if (((MyApplication)getApplication()).ismConnected()) {
            try {
                if (mBluetoothSocket != null) {
                    mBluetoothSocket.close();
                    mBluetoothSocket = null;
                }
            } catch (IOException e) {
                mBluetoothSocket = null;
            }
        }
        ((MyApplication)getApplication()).setmConnected(false);
//        mConnected = false;
    }
}
