package doab.uin.mvb;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by adhy57 on 07/10/2015.
 */
public class BluetoothService extends Service {

    BluetoothSocket mBluetoothSocket;
    BluetoothServerSocket mBluetoothServerSocket;
    OutputStream mOutputStream;
    InputStream mInputStream;

    public static final int MESSAGE_WRITE = 1;
    public static final int MESSAGE_READ = 2;

    private Handler _handler = new Handler();
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    String STJ;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "The Service Started", Toast.LENGTH_LONG).show();
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
            int id = extras.getInt("id");
            if (id==0){
                String macAddress = extras.getString("mac");
                try {
                    connectBluetooth(macAddress);
                    Toast.makeText(this, "Bluetooth Terhubung", Toast.LENGTH_LONG).show();
//                  listen();
//                  beginListenForData();
//                    run();
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
        UUID localUUID2 = UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666");
        mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(localUUID);
//        mBluetoothSocket = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(localUUID);
        mBluetoothSocket.connect();

        mOutputStream = mBluetoothSocket.getOutputStream();
        mInputStream = mBluetoothSocket.getInputStream();

        beginListenForData();

//        if(!((MyApplication)getApplication()).ismConnected()){
//            listAdapter.clear();
//            listAdapter.add("Putuskan Perangkat");
//        }
        ((MyApplication)getApplication()).setmConnected(true);
//
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

    public void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mInputStream.available();
//                        myLabel.setText("Data AVA");
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
//                            myLabel.setText("Data AVA 1");
                            StringBuilder SB = new StringBuilder();
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                //BARU
                                if (i > 0) {
                                    SB.append(' ');
                                }
                                String s = Integer.toHexString(packetBytes[i] & 0xFF);
                                if (s.length() < 2) {

                                    SB.append('0');
                                }
                                SB.append(s);
                                STJ = SB.toString();
                                //LAMA
//                                byte b = packetBytes[i];
//                                if(b == delimiter)
//                                {
//                                    byte[] encodedBytes = new byte[readBufferPosition];
//                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
//                                    final String data = new String(encodedBytes, "US-ASCII");
//                                    readBufferPosition = 0;
//
                                handler.post(new Runnable() {
                                    public void run()
                                    {
//                                            myLabel.setText(data);
//                                        int i = Integer.parseInt(STJ);
                                        String data = hexToString(STJ);
//                                        myLabel.setText(STJ);
                                        Toast.makeText(getApplicationContext(), "Bluetooth : "+data+" ", Toast.LENGTH_SHORT).show();
                                    }
                                });
//                                }
//                                else
//                                {
//                                    readBuffer[readBufferPosition++] = b;
//                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    public String hexToString(String txtInHex)
    {
        byte [] txtInByte = new byte [txtInHex.length() / 2];
        int j = 0;
        for (int i = 0; i < txtInHex.length(); i += 2)
        {
            txtInByte[j++] = Byte.parseByte(txtInHex.substring(i, i + 2), 16);
        }
        return new String(txtInByte);
    }


}
