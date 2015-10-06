package doab.uin.mvb;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.IOException;
import java.io.OutputStream;

import doab.uin.mvb.camera.VideoCaptureActivity;
import doab.uin.mvb.camera.configuration.CaptureConfiguration;
import doab.uin.mvb.camera.configuration.PredefinedCaptureConfigurations.CaptureQuality;
import doab.uin.mvb.camera.configuration.PredefinedCaptureConfigurations.CaptureResolution;

public class CameraFragment extends Fragment implements View.OnClickListener {


    private final String	KEY_STATUSMESSAGE		= "doab.uin.mvb.camera.statusmessage";
    private final String	KEY_FILENAME			= "doab.uin.mvb.camera.outputfilename";


    private String			statusMessage			= null;
    private String			filename				= null;

    private ImageView       thumbnailIv;
    Button captureBtn;
    Button uploadBtn;
    CallbackManager mCallbackManager;
    Bundle savedInBundle;
    ShareDialog shareDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(getActivity());
        shareDialog.registerCallback(mCallbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Toast.makeText(getActivity(), "video",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.fragment_camera, container,  false);
        //deklarasi view
        captureBtn = (Button) rootView.findViewById(R.id.btn_capturevideo);
        thumbnailIv = (ImageView) rootView.findViewById(R.id.iv_thumbnail);
        uploadBtn = (Button) rootView.findViewById(R.id.btn_uploadvideo);
        //handler click
        captureBtn.setOnClickListener(this);
        thumbnailIv.setOnClickListener(this);
        uploadBtn.setOnClickListener(this);

        savedInBundle = savedInstanceState;

        if (savedInstanceState != null) {
            statusMessage = savedInstanceState.getString(KEY_STATUSMESSAGE);
            filename = savedInstanceState.getString(KEY_FILENAME);
        }

        updateStatusAndThumbnail();
        return rootView;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String name = ((MyApplication)getActivity().getApplication()).getUriVideo();
        if (name==null){
            startVideoCaptureActivity();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_STATUSMESSAGE, statusMessage);
        outState.putString(KEY_FILENAME, filename);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_capturevideo) {
//            prepareSend();
            startVideoCaptureActivity();
        } else if (v.getId() == R.id.iv_thumbnail) {
            playVideo();
            Toast.makeText(getActivity(), "video",Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.btn_uploadvideo){
            uploadVideo();
        } else {
            Toast.makeText(getActivity(), "keluar",Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadVideo() {
//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        fragmentManager.beginTransaction().replace(R.id.container, new MainFragment()).commit();
            shareDialog.canShow(ShareVideoContent.class);
            Uri videoFileUri = Uri.parse(filename);
            ShareVideo shareVideo = new ShareVideo.Builder()
                    .setLocalUrl(videoFileUri)
                    .build();
            ShareVideoContent content = new ShareVideoContent.Builder()
                    .setContentDescription("I'm at Magnum 360 Video Booth")
                    .setVideo(shareVideo)
                    .build();
            shareDialog.show(content);


//            ShareLinkContent linkContent = new ShareLinkContent.Builder()
//                    .setContentTitle("Hello Facebook")
//                    .setContentDescription(
//                            "The 'Hello Facebook' sample showcases simple Facebook integration")
//                                    .setContentUrl(Uri.parse("http://developers.facebook.com/android"))
//                                    .build();
//                            shareDialog.show(linkContent);

        //((MyApplication)getActivity().getApplication()).setUriVideo(null);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.capture_demo, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_advanced :
//                toggleAdvancedSettings();
//                break;
//            case R.id.menu_github:
//                openGitHub();
//                break;
//        }
        return true;
    }

    private void startVideoCaptureActivity() {
        final CaptureConfiguration config = createCaptureConfiguration();
        final Intent intent = new Intent(getActivity(), VideoCaptureActivity.class);
        intent.putExtra(VideoCaptureActivity.EXTRA_CAPTURE_CONFIGURATION, config);
        intent.putExtra(VideoCaptureActivity.EXTRA_OUTPUT_FILENAME, filename);
        startActivityForResult(intent, 101);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            filename = data.getStringExtra(VideoCaptureActivity.EXTRA_OUTPUT_FILENAME);
            int take = ((MyApplication)getActivity().getApplication()).getTake();
            ((MyApplication)getActivity().getApplication()).setUriVideo(filename);
            ((MyApplication)getActivity().getApplication()).setTake(take+1);
            if (take+1 >= 2){
                captureBtn.setVisibility(View.GONE);
            }else {
                captureBtn.setVisibility(View.VISIBLE);
            }
            statusMessage = String.format(getString(R.string.status_capturesuccess), filename);
//            setContent();

        } else if (resultCode == Activity.RESULT_CANCELED) {
            //filename = null;
            statusMessage = getString(R.string.status_capturecancelled);
        } else if (resultCode == VideoCaptureActivity.RESULT_ERROR) {
            //filename = null;
            statusMessage = getString(R.string.status_capturefailed);
        }
        updateStatusAndThumbnail();

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateStatusAndThumbnail() {
        if (statusMessage == null) {
            statusMessage = getString(R.string.status_nocapture);
        }
//        statusTv.setText(statusMessage);

        final Bitmap thumbnail = getThumbnail();

        if (thumbnail != null) {
            thumbnailIv.setImageBitmap(thumbnail);
        } else {
            thumbnailIv.setImageResource(R.drawable.thumbnail_placeholder);
        }
    }

    private Bitmap getThumbnail() {
        if (filename == null) return null;
        return ThumbnailUtils.createVideoThumbnail(filename, Thumbnails.FULL_SCREEN_KIND);
    }

    private CaptureConfiguration createCaptureConfiguration() {
//        final CaptureResolution resolution = getResolution(resolutionSp.getSelectedItemPosition());
//        final CaptureQuality quality = getQuality(qualitySp.getSelectedItemPosition());
        int fileDuration = 6;
        int filesize = 10;
        return new CaptureConfiguration(CaptureResolution.RES_480P, CaptureQuality.HIGH, fileDuration, filesize);
    }

    public void playVideo() {
        if (filename == null) return;

        final Intent videoIntent = new Intent(Intent.ACTION_VIEW);
        videoIntent.setDataAndType(Uri.parse(filename), "video/*");
        try {
            startActivity(videoIntent);
        } catch (ActivityNotFoundException e) {

        }
    }
}
