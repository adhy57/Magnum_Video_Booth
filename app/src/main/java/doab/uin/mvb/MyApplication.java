package doab.uin.mvb;

import android.app.Application;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Created by PC on 9/2/2015.
 */
public class MyApplication extends Application {

    public String uriVideo = null;
    public int take = 0 ;
    public boolean mConnected = false;
    public BluetoothSocket mBluetoothSocket;
    public OutputStream mOutputStream;
    public InputStream mInputStream;

    public InputStream getmInputStream() {
        return mInputStream;
    }

    public void setmInputStream(InputStream mInputStream) {
        this.mInputStream = mInputStream;
    }

    public BluetoothSocket getmBluetoothSocket() {
        return mBluetoothSocket;
    }

    public OutputStream getmOutputStream() {
        return mOutputStream;
    }

    public void setmOutputStream(OutputStream mOutputStream) {
        this.mOutputStream = mOutputStream;
    }

    public void setmBluetoothSocket(BluetoothSocket mBluetoothSocket) {
        this.mBluetoothSocket = mBluetoothSocket;
    }

    public void setUriVideo(String uriVideo){
        this.uriVideo = uriVideo;
    }

    public String getUriVideo() {
        return uriVideo;
    }

    public void setTake(int take) {
        this.take = take;
    }

    public int getTake() {
        return take;
    }

    public boolean ismConnected() {
        return mConnected;
    }

    public void setmConnected(boolean mConnected) {
        this.mConnected = mConnected;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "doab.uin.mvb",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
}
