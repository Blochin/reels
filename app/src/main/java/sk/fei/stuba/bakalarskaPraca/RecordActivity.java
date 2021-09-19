package sk.fei.stuba.bakalarskaPraca;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



import java.io.IOException;
import java.security.Permission;

import sk.fei.stuba.bakalarskaPraca.FFmpegService.FFmpegService;
import sk.fei.stuba.bakalarskaPraca.FileStruct.FileStruct;
import sk.fei.stuba.bakalarskaPraca.FileStruct.FileStructConstants;
import sk.fei.stuba.bakalarskaPraca.camera.CameraConstants;
import sk.fei.stuba.bakalarskaPraca.camera.CameraService;
import static sk.fei.stuba.bakalarskaPraca.camera.CameraConstants.REQUEST_CAMERA_PERMISSION_RESULT;
import static sk.fei.stuba.bakalarskaPraca.camera.CameraConstants.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT;
import static sk.fei.stuba.bakalarskaPraca.camera.CameraConstants.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT_GET_SOUND;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class RecordActivity extends AppCompatActivity {

    //pri prvom nahravani sa nevytrvori subor na nahravanie

    private FileStruct mergedVideoDestination;
    private FileStruct downloadedSoundDestination;

    public CameraService cameraService;
    private TextureView mTextureView;
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            cameraService.setupCamera(width, height);
            cameraService.connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            cameraService.closeCamera();
            cameraService.setupCamera(width, height);
            cameraService.connectCamera();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }

    };
    private final CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraService.setCameraDevice(camera);
            cameraService.startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraService.closeCamera(camera);
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraService.closeCamera(camera);
        }
    };
    private boolean mIsRecording = false;
    private ImageView mRecordButton;
    private TextView mGetSoundButton;
    private TextView mGetDuration;
    private Chronometer mChronometer;
    private MediaPlayer mMediaPlayer;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(mIsRecording){
            stopRecordVideo();
            cameraService.getRecordedVideoDestination().deleteDirectory();
        }
        if(mMediaPlayer!= null){
            downloadedSoundDestination.deleteDirectory();
        }
        Intent intent = new Intent(RecordActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mChronometer = findViewById(R.id.chronometer);
        mTextureView = findViewById(R.id.previewTextureView);
        mRecordButton = findViewById(R.id.recordVideo);
        mGetSoundButton = findViewById(R.id.getSounds);
        mGetDuration = findViewById(R.id.duration);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemUI();
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsRecording) {
                    startRecordVideo();
                }
            }
        });

        mGetSoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CameraConstants.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT_GET_SOUND);
                }else{
                    if (mMediaPlayer != null) {
                        if (mIsRecording) {
                            stopRecordVideo();
                        }
                        mMediaPlayer.reset();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                        downloadedSoundDestination.deleteDirectory();
                    }
                    Intent intent = new Intent(RecordActivity.this, SoundsActivity.class);
                    startActivity(intent);
                }
            }

        });

        mGetDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(mMediaPlayer == null){
                    if (mGetDuration.getText().toString().equals("15 SEC")) {
                        mGetDuration.setText("30 SEC");
                        cameraService.setDuration(30000);
                    } else {
                        mGetDuration.setText("15 SEC");
                        cameraService.setDuration(15000);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemUI();
        cameraService = new CameraService(this,
                mTextureView,
                (CameraManager) getSystemService(Context.CAMERA_SERVICE),
                mMediaPlayer);
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getString("soundName") != null) {
            downloadedSoundDestination = new FileStruct(FileStructConstants.packageName,
                    FileStructConstants.childPackageSounds,
                    extras.getString("soundName"),
                    FileStructConstants.mp3Ext);
            mMediaPlayer = MediaPlayer.create(this, downloadedSoundDestination.getUriFile());
            mGetDuration.setVisibility(View.INVISIBLE);
            cameraService.setDuration(mMediaPlayer.getDuration());

        }
        cameraService.setCameraDeviceStateCallback(mCameraDeviceStateCallback);
        cameraService.setOnInfoListener(onInfoListener);
        cameraService.startBackgroundThread();
        if (mTextureView.isAvailable()) {
            cameraService.setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            cameraService.connectCamera();
        } else {
            cameraService.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsRecording) {
            stopRecordVideo();
            cameraService.getRecordedVideoDestination().deleteDirectory();
        }
        cameraService.closeCamera();
        cameraService.stopBackgroundThread();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mIsRecording) {
            stopRecordVideo();
            cameraService.getRecordedVideoDestination().deleteDirectory();
        }
        if(mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        cameraService.releaseMediaRecorder();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer != null) {
            downloadedSoundDestination.deleteDirectory();
        }
    }



    private void startRecordVideo() {
       if( cameraService.checkWriteStoragePermission()) {
           mIsRecording = true;
           mRecordButton.setImageResource(R.drawable.record_prepared);
           mGetDuration.setVisibility(View.INVISIBLE);
           mGetSoundButton.setVisibility(View.INVISIBLE);
           mChronometer.setBase(SystemClock.elapsedRealtime());
           mChronometer.setVisibility(View.VISIBLE);
           mChronometer.start();
           hideButtonsWhileRecord();
           if (mMediaPlayer != null) {
               mMediaPlayer.start();
           }

       }
    }

    private void stopRecordVideo() {
        mChronometer.stop();
        mChronometer.setVisibility(View.INVISIBLE);
        mIsRecording = false;
        mRecordButton.setImageResource(R.drawable.record_bussy);
        mGetDuration.setVisibility(View.VISIBLE);
        mGetSoundButton.setVisibility(View.VISIBLE);
        cameraService.resetMediaRecorder();
        showButtonsAfterRecord();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CameraConstants.REQUEST_CAMERA_PERMISSION_RESULT) {
            if(grantResults.length == 0){
                Toast.makeText(getApplicationContext(), "Application will not rin without camera services", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION_RESULT);
            }else if(grantResults.length == 1 ){
                Toast.makeText(getApplicationContext(), "Application will not rin without camera services", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION_RESULT);
            }else if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_DENIED){
                Toast.makeText(getApplicationContext(), "Application will not rin without camera services", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION_RESULT);
            }
        }
        if (requestCode == CameraConstants.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Application will not run without storage permissions", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        }
        if (requestCode == CameraConstants.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT_GET_SOUND) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Application will not run without storage permissions", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT_GET_SOUND);

            }else{
                Intent intent = new Intent(RecordActivity.this, SoundsActivity.class);
                startActivity(intent);
            }
        }
    }

    MediaRecorder.OnInfoListener onInfoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                stopRecordVideo();
                if (mMediaPlayer != null) {
                    try {
                        mergeVideoAndSound();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Intent ffmpegIntent = new Intent(RecordActivity.this, FFmpegService.class);
                    ffmpegIntent.putExtra("command", command);
                    ffmpegIntent.putExtra("mergedVideoDestinationAbsolutPath", mergedVideoDestination.getFileAbsolutPath());
                    ffmpegIntent.putExtra("downloadedSoundDestinationAbsolutPath", downloadedSoundDestination.getFileAbsolutPath());
                    ffmpegIntent.putExtra("recordedVideoDestinationAbsolutPath", cameraService.getRecordedVideoDestination().getFileAbsolutPath());
                    startService(ffmpegIntent);
                } else {
                    Intent replayVideoIntent = new Intent(getApplicationContext(), ReplayVideoActivity.class);
                    replayVideoIntent.putExtra("mergedVideoDestinationAbsolutPath", cameraService.getRecordedVideoDestination().getFileAbsolutPath());
                    startActivity(replayVideoIntent);
                }

                mServiceConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder iBinder) {
                        FFmpegService fFmpegService;
                        FFmpegService.LocalBinder binder = (FFmpegService.LocalBinder) iBinder;
                        fFmpegService = binder.getServiceInstance();
                        fFmpegService.registerClient(getParent());

                        final Observer<Integer> resultObserver = new Observer<Integer>() {
                            @Override
                            public void onChanged(Integer integer) {

                            }
                        };
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {

                    }
                };
            }
        }
    };

    String[] command;
    ServiceConnection mServiceConnection;
    FFmpegService fFmpegService = new FFmpegService();

    private void mergeVideoAndSound() throws IOException {
        mergedVideoDestination = new FileStruct(FileStructConstants.packageName, FileStructConstants.childPackageMergedVideos, FileStruct.createFileName(), FileStructConstants.mp4Ext);
        command = new String[]{"-i", downloadedSoundDestination.getUriFile().getPath(), "-i", cameraService.getRecordedVideoDestination().getUriFile().getPath(), "-c", "copy", mergedVideoDestination.getFileAbsolutPath()};
    }

    public void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void hideButtonsWhileRecord(){
        mGetSoundButton.setVisibility(View.INVISIBLE);
        mGetDuration.setVisibility(View.INVISIBLE);
    }
    public void showButtonsAfterRecord(){
        mGetSoundButton.setVisibility(View.VISIBLE);
        mGetDuration.setVisibility(View.VISIBLE);
    }
}


