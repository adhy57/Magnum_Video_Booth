/**
 * Copyright 2014 Jeroen Mols
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package doab.uin.mvb.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import doab.uin.mvb.BluetoothService;
import doab.uin.mvb.R;
import doab.uin.mvb.camera.*;
import doab.uin.mvb.camera.camera.CameraWrapper;
import doab.uin.mvb.camera.configuration.CaptureConfiguration;
import doab.uin.mvb.camera.recorder.VideoRecorder;
import doab.uin.mvb.camera.recorder.VideoRecorderInterface;
import doab.uin.mvb.camera.view.RecordingButtonInterface;
import doab.uin.mvb.camera.view.VideoCaptureView;

public class VideoCaptureActivity extends Activity implements RecordingButtonInterface, VideoRecorderInterface {

	public static final int			RESULT_ERROR				= 753245;

	public static final String		EXTRA_OUTPUT_FILENAME		= "doab.uin.mvb.camera.extraoutputfilename";
	public static final String		EXTRA_CAPTURE_CONFIGURATION	= "doab.uin.mvb.camera.extracaptureconfiguration";
	public static final String		EXTRA_ERROR_MESSAGE			= "doab.uin.mvb.camera.extraerrormessage";

	private static final String		SAVED_RECORDED_BOOLEAN		= "doab.uin.mvb.camera.savedrecordedboolean";
	protected static final String	SAVED_OUTPUT_FILENAME		= "doab.uin.mvb.camera.savedoutputfilename";

	private boolean					mVideoRecorded				= false;
	VideoFile mVideoFile					= null;
	private CaptureConfiguration	mCaptureConfiguration;

	private VideoCaptureView		mVideoCaptureView;
	private VideoRecorder			mVideoRecorder;

	//buatan sendiri
	CountDownTimer countDownTimer;
	TextView txt_countTimer;
	int countTimer;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		doab.uin.mvb.camera.CLog.toggleLogging(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_videocapture);

		initializeCaptureConfiguration(savedInstanceState);

		mVideoCaptureView = (VideoCaptureView) findViewById(R.id.videocapture_videocaptureview_vcv);
		if (mVideoCaptureView == null) return; // Wrong orientation

		initializeRecordingUI();
		txt_countTimer = (TextView)findViewById(R.id.txt_countdown);
		countTimer = 5;
		countDownTimer = new CountDownTimer(6000,1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				txt_countTimer.setText(""+countTimer);
				countTimer--;
			}

			@Override
			public void onFinish() {
				txt_countTimer.setVisibility(View.GONE);
				mVideoRecorder.toggleRecording();
				Intent bt = new Intent(VideoCaptureActivity.this, BluetoothService.class);
				bt.putExtra("id", 1);
				bt.putExtra("msg", "START");
				startService(bt);
			}
		};
	}

	private void initializeCaptureConfiguration(final Bundle savedInstanceState) {
		mCaptureConfiguration = generateCaptureConfiguration();
		mVideoRecorded = generateVideoRecorded(savedInstanceState);
		mVideoFile = generateOutputFile(savedInstanceState);
	}

	private void initializeRecordingUI() {
		mVideoRecorder = new VideoRecorder(this, mCaptureConfiguration, mVideoFile, new CameraWrapper(),
				mVideoCaptureView.getPreviewSurfaceHolder());
        mVideoCaptureView.setRecordingButtonInterface(this);

		if (mVideoRecorded) {
			mVideoCaptureView.updateUIRecordingFinished(getVideoThumbnail());
		} else {
			mVideoCaptureView.updateUINotRecording();
		}
	}

	@Override
	protected void onPause() {
		if (mVideoRecorder != null) {
			mVideoRecorder.stopRecording(null);
		}
		releaseAllResources();
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		finishCancelled();
	}

	@Override
	public void onRecordButtonClicked() {
		record();
	}

	private void record() {
		txt_countTimer.setVisibility(View.VISIBLE);
		countDownTimer.start();
	}

	@Override
	public void onAcceptButtonClicked() {
		finishCompleted();
	}

	@Override
	public void onDeclineButtonClicked() {
		finishCancelled();
	}

	@Override
	public void onRecordingStarted() {
		mVideoCaptureView.updateUIRecordingOngoing();
	}

	@Override
	public void onRecordingStopped(String message) {
		if (message != null) {
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}

		mVideoCaptureView.updateUIRecordingFinished(getVideoThumbnail());
		releaseAllResources();
	}

	@Override
	public void onRecordingSuccess() {
		mVideoRecorded = true;
	}

	@Override
	public void onRecordingFailed(String message) {
		finishError(message);
	}

	private void finishCompleted() {
		final Intent result = new Intent();
		result.putExtra(EXTRA_OUTPUT_FILENAME, mVideoFile.getFullPath());
		this.setResult(RESULT_OK, result);
		finish();
	}

	private void finishCancelled() {
		this.setResult(RESULT_CANCELED);
		finish();
	}

	private void finishError(final String message) {
		Toast.makeText(getApplicationContext(), "Can't capture video: " + message, Toast.LENGTH_LONG).show();

		final Intent result = new Intent();
		result.putExtra(EXTRA_ERROR_MESSAGE, message);
		this.setResult(RESULT_ERROR, result);
		finish();
	}

	private void releaseAllResources() {
		if (mVideoRecorder != null) {
			mVideoRecorder.releaseAllResources();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean(SAVED_RECORDED_BOOLEAN, mVideoRecorded);
		savedInstanceState.putString(SAVED_OUTPUT_FILENAME, mVideoFile.getFullPath());
		super.onSaveInstanceState(savedInstanceState);
	}

	protected CaptureConfiguration generateCaptureConfiguration() {
		CaptureConfiguration returnConfiguration = this.getIntent().getParcelableExtra(EXTRA_CAPTURE_CONFIGURATION);
		if (returnConfiguration == null) {
			returnConfiguration = new CaptureConfiguration();
			doab.uin.mvb.camera.CLog.d(doab.uin.mvb.camera.CLog.ACTIVITY, "No captureconfiguration passed - using default configuration");
		}
		return returnConfiguration;
	}

	private boolean generateVideoRecorded(final Bundle savedInstanceState) {
		if (savedInstanceState == null) return false;
		return savedInstanceState.getBoolean(SAVED_RECORDED_BOOLEAN, false);
	}

	protected VideoFile generateOutputFile(Bundle savedInstanceState) {
		VideoFile returnFile = null;
		if (savedInstanceState != null) {
			returnFile = new VideoFile(savedInstanceState.getString(SAVED_OUTPUT_FILENAME));
		} else {
			returnFile = new VideoFile(this.getIntent().getStringExtra(EXTRA_OUTPUT_FILENAME));
		}
		// TODO: add checks to see if outputfile is writeable
		return returnFile;
	}

	public Bitmap getVideoThumbnail() {
		final Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(mVideoFile.getFullPath(),
				Thumbnails.FULL_SCREEN_KIND);
		if (thumbnail == null) {
			doab.uin.mvb.camera.CLog.d(doab.uin.mvb.camera.CLog.ACTIVITY, "Failed to generate video preview");
		}
		return thumbnail;
	}

}
