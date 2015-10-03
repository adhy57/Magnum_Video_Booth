package doab.uin.mvb;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.UUID;

/**
 * Created by adhy57 on 29/09/2015.
 */
public class SettingActivity extends Activity {

    ListView listView;
    ArrayAdapter<String> listAdapter;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Set<BluetoothDevice> devicesArray;
    Button bOn, bOff, bPutar;
    TextView tvMac;
    boolean mConnected = ((MyApplication)getApplication()).ismConnected();
    String namaPerangkat, macAddress;
    BluetoothSocket mBluetoothSocket;
    OutputStream mOutputStream;
    boolean bicara, aktifBluetooth = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setting);

        listView = (ListView)findViewById(R.id.listBluetooth);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
        listView.setAdapter(listAdapter);
        bOn = (Button)findViewById(R.id.bOn);
        bOff = (Button)findViewById(R.id.bOff);
        bPutar = (Button)findViewById(R.id.bPutar);
        tvMac = (TextView)findViewById(R.id.textView2);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                if (!mConnected) {
                    for (BluetoothDevice device : devicesArray) {
                        if (device.getName().equals(arg0.getItemAtPosition(arg2))) {
                            macAddress = String.valueOf(device.getAddress());
                            namaPerangkat = device.getName();
                        }
                    }
                    try {
                        connect(macAddress); // Perintah untuk mengkoneksikan
                        tvMac.setText("Terkoneksi dengan '" + namaPerangkat + "'");
                    } catch (NoSuchMethodException e) {
                        // TODO Auto-generated catch block
                        Toast.makeText(getApplicationContext(), "Koneksi gagal.", Toast.LENGTH_SHORT).show();
                        bukaPerangkatBluetooth();
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        Toast.makeText(getApplicationContext(), "Koneksi gagal.", Toast.LENGTH_SHORT).show();
                        bukaPerangkatBluetooth();
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        Toast.makeText(getApplicationContext(), "Koneksi gagal.", Toast.LENGTH_SHORT).show();
                        bukaPerangkatBluetooth();
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        Toast.makeText(getApplicationContext(), "Koneksi gagal.", Toast.LENGTH_SHORT).show();
                        bukaPerangkatBluetooth();
                        e.printStackTrace();
                    }
                } else {
                    diskonekPerangkat();
                    bukaPerangkatBluetooth();
                    tvMac.setText("Pilih Perangkat:");
                }
            }
        });

        bPutar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(mConnected){
                    // TODO Auto-generated method stub
                    try {
                        send("putar\n");
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Koneksikan perangkat dahulu!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if(!mConnected){
                    Toast.makeText(getApplicationContext(), "Koneksikan perangkat dahulu!", Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        send("hidupkan\n"); // change this value
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        bOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if(!mConnected){
                    Toast.makeText(getApplicationContext(), "Koneksikan perangkat dahulu!", Toast.LENGTH_SHORT).show();
                }else{
                    try {
                        send("matikan\n"); // change this value
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        if(!mBluetoothAdapter.isEnabled()){
            Intent aktifkanBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            aktifBluetooth = true;
            startActivityForResult(aktifkanBluetooth, 1);
        }

        bukaPerangkatBluetooth();
    }

    private void bukaPerangkatBluetooth() {
        // TODO Auto-generated method stub
        devicesArray = mBluetoothAdapter.getBondedDevices();
        listAdapter.clear();
        if(devicesArray.size() > 0){
            for(BluetoothDevice perangkat:devicesArray){
                listAdapter.add(perangkat.getName());
            }
        }
    }

    private void diskonekPerangkat() {
        // TODO Auto-generated method stub
        if (mConnected) {
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
    }

    protected boolean connect(String macAddress2) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException{
        // TODO Auto-generated method stub

        BluetoothDevice mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddress2);
        //Method mMethod = mBluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
        UUID localUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(localUUID);
        mBluetoothSocket.connect();

        mOutputStream = mBluetoothSocket.getOutputStream();

        if(!mConnected){
            listAdapter.clear();
            listAdapter.add("Putuskan Perangkat");
        }
        ((MyApplication)getApplication()).setmConnected(true);
        return true;
    }

    private void send(final String message) throws IOException {
        if (mConnected) {
            if (mBluetoothSocket != null) {
                mOutputStream.write(message.getBytes());
            }
        }
    }
}
