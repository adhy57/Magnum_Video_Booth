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
//        mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(localUUID);
        mBluetoothSocket = mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(localUUID);
        mBluetoothSocket.connect();
//        mBluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("btspp", localUUID2);
//        mBluetoothServerSocket.accept();

        mOutputStream = mBluetoothSocket.getOutputStream();
        mInputStream = mBluetoothSocket.getInputStream();

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
//    protected void listen() {
//        try {
//
//			/* accept client request */
//            BluetoothSocket socket = mBluetoothServerSocket.accept();
////            Log.d("EF-BTBee", ">>Accept Client Request");
//
//			/* Processing the request content*/
//            if (socket != null) {
//                InputStream inputStream = socket.getInputStream();
//                int read = -1;
//                final byte[] bytes = new byte[2048];
//                for (; (read = inputStream.read(bytes)) > -1;) {
//                    final int count = read;
//                    _handler.post(new Runnable() {
//                        public void run() {
//                            StringBuilder b = new StringBuilder();
//                            for (int i = 0; i < count; ++i) {
//                                if (i > 0) {
//                                    b.append(' ');
//                                }
//                                String s = Integer.toHexString(bytes[i] & 0xFF);
//                                if (s.length() < 2) {
//
//                                    b.append('0');
//                                }
//                                b.append(s);
//                            }
//                            String s = b.toString();
//                            Toast.makeText(getApplicationContext(), s ,Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
////                Log.d("EF-BTBee", ">>Server is over!!");
//            }
//        } catch (IOException e) {
////            Log.e("EF-BTBee", "", e);
//        } finally {
//
//        }
//    }
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
                while(true)
                {
                    try
                    {
                        int bytesAvailable = mInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            Toast.makeText(getApplicationContext(), data ,Toast.LENGTH_SHORT).show();
                                            Log.d("EF-BTBee", ">>Server is over!!"+data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
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

    public void run() {
        byte[] buffer = new byte[1024]; // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mInputStream.read(buffer);
                // Send the obtained bytes to the UI activity
                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget();
            }
            catch (IOException e) {
                Log.e("bluetooth", "Error reading from btInputStream");
                break;
            }
        }
    }
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_WRITE:
                    //Do something when writing
                    break;
                case MESSAGE_READ:
                    //Get the bytes from the msg.obj
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
