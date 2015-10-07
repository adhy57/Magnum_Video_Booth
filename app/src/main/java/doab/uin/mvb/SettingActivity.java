package doab.uin.mvb;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity {

    ListView listView;
    ArrayAdapter<String> listAdapter;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Set<BluetoothDevice> devicesArray;
    Button bScan, bOff, bPutar;
    TextView tvMac;
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
        bScan = (Button)findViewById(R.id.bScan);
        bOff = (Button)findViewById(R.id.bOff);
        bPutar = (Button)findViewById(R.id.bPutar);
        tvMac = (TextView)findViewById(R.id.textView2);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                if(!((MyApplication)getApplication()).ismConnected()){
                    for(BluetoothDevice device:devicesArray){
                        if(device.getName().equals(arg0.getItemAtPosition(arg2))){
                            macAddress = String.valueOf(device.getAddress());
                            namaPerangkat = device.getName();
                        }
                    }
//                    try {
                        Intent bt = new Intent(SettingActivity.this, BluetoothService.class);
                        bt.putExtra("mac", macAddress);
                        bt.putExtra("id", 0);
                        startService(bt);
//                        if(!((MyApplication)getApplication()).ismConnected()){
//                            listAdapter.clear();
//                            listAdapter.add("Putuskan Perangkat");
//                            tvMac.setText("Terkoneksi dengan '" + namaPerangkat + "'");
//                        }

                }else{
//                    Intent bt = new Intent(SettingActivity.this, BluetoothService.class);
//                    bt.putExtra("id", 2);
//                    stopService(bt);
                    bukaPerangkatBluetooth();
                    tvMac.setText("Pilih Perangkat:");
                }
            }
        });

        bPutar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if(!((MyApplication)getApplication()).ismConnected()){
                    Toast.makeText(getApplicationContext(), "Koneksikan perangkat dahulu!", Toast.LENGTH_SHORT).show();
                }else{
                    Intent bt = new Intent(SettingActivity.this, BluetoothService.class);
                    bt.putExtra("id", 1);
                    bt.putExtra("msg", "START");
                    startService(bt);
                    if(!((MyApplication)getApplication()).ismConnected()){
                        listAdapter.clear();
                        listAdapter.add("Putuskan Perangkat");
                    }
//                  send("START"); // change this value

                }
            }
        });

        bScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
              bukaPerangkatBluetooth();
            }
        });

        bOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if(!((MyApplication)getApplication()).ismConnected()){
                    Toast.makeText(getApplicationContext(), "Koneksikan perangkat dahulu!", Toast.LENGTH_SHORT).show();
                }else{
//                    try {
//                        send("matikan\n"); // change this value
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
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


//    protected boolean connect(String macAddress2) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException{
//        // TODO Auto-generated method stub
//
//        BluetoothDevice mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddress2);
//        //Method mMethod = mBluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
//        UUID localUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
//        mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(localUUID);
//        mBluetoothSocket.connect();
//
//        mOutputStream = mBluetoothSocket.getOutputStream();
//
//        if(!((MyApplication)getApplication()).ismConnected()){
//            listAdapter.clear();
//            listAdapter.add("Putuskan Perangkat");
//        }
//        ((MyApplication)getApplication()).setmConnected(true);
////        mConnected = true;
//        return true;
//    }


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


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
//        diskonekPerangkat();
        super.onDestroy();
    }


//    private void diskonekPerangkat() {
//        // TODO Auto-generated method stub
//        if (((MyApplication)getApplication()).ismConnected()) {
//            try {
//                if (mBluetoothSocket != null) {
//                    mBluetoothSocket.close();
//                    mBluetoothSocket = null;
//                }
//            } catch (IOException e) {
//                mBluetoothSocket = null;
//            }
//        }
//        ((MyApplication)getApplication()).setmConnected(false);
////        mConnected = false;
//    }

//    private void send(final String message) throws IOException {
//        if (((MyApplication)getApplication()).ismConnected()) {
//            if (mBluetoothSocket != null) {
//                mOutputStream.write(message.getBytes());
//            }
//        }
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // TODO Auto-generated method stub
//        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode == RESULT_OK && aktifBluetooth){
//            bukaPerangkatBluetooth();
//            Toast.makeText(getApplicationContext(), "Bluetooth diaktifkan.", Toast.LENGTH_SHORT).show();
//            aktifBluetooth = false;
//        }
//
//        if(resultCode == RESULT_OK && bicara){
//            ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//            Toast.makeText(getApplicationContext(), "'" + text.get(0) + "'", Toast.LENGTH_SHORT).show();
//
//            try {
//                send(text.get(0) + "\n");
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//
//            //if(text.get(0).equals("hidupkan")){
//            //	try {
//            //		send("1");
//            //	} catch (IOException e) {
//            // TODO Auto-generated catch block
//            //		e.printStackTrace();
//            //	}
//            //}
//            //else if(text.get(0).equals("matikan")){
//            //	try {
//            //		send("0");
//            //	} catch (IOException e) {
//            // TODO Auto-generated catch block
//            //		e.printStackTrace();
//            //	}
//            //}
//            //else{
//            //	Toast.makeText(getApplicationContext(), "Perintah tidak ditemukan.", Toast.LENGTH_SHORT).show();
//            //}
//            bicara = false;
//        }
//
//        if(resultCode == RESULT_CANCELED && aktifBluetooth){
//            Toast.makeText(getApplicationContext(), "Bluetooth harus diaktifkan.", Toast.LENGTH_SHORT).show();
//            finish();
//        }
//
//        if(resultCode == RESULT_CANCELED && bicara){
//            Toast.makeText(getApplicationContext(), "Dibatalkan.", Toast.LENGTH_SHORT).show();
//            bicara = false;
//        }
//


//    }

}
