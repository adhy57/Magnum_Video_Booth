package doab.uin.mvb;

import java.io.IOException;
import java.io.InputStream;
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
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
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

//   

}
