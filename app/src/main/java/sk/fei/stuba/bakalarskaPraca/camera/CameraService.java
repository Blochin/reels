package sk.fei.stuba.bakalarskaPraca.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.util.Log;

import java.io.IOException;
import java.util.Arrays;

import sk.fei.stuba.bakalarskaPraca.FileStruct.FileStruct;
import sk.fei.stuba.bakalarskaPraca.FileStruct.FileStructConstants;
import sk.fei.stuba.bakalarskaPraca.R;

import static android.content.ContentValues.TAG;
import static androidx.core.app.ActivityCompat.requestPermissions;
import static androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale;
import static sk.fei.stuba.bakalarskaPraca.camera.CameraConstants.REQUEST_CAMERA_PERMISSION_RESULT;
import static sk.fei.stuba.bakalarskaPraca.camera.CameraConstants.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT;
import static sk.fei.stuba.bakalarskaPraca.camera.CameraConstants.chooseOptimalSize;
import static sk.fei.stuba.bakalarskaPraca.camera.CameraConstants.sensorToDeviceRotation;

public class CameraService  {

    private FileStruct recordedVideoDestination;

    private String cameraId;
    private int totalRotation;
    private Size previewSize;
    private Size videoSize;
    private HandlerThread backgroundHandlerThread;
    private Handler backgroundHandler;
    private CameraDevice cameraDevice;
    private int duration = 15000;

    private final TextureView textureView;
    private final CameraManager cameraManager;
    private final Activity targetActivity;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;


    private CameraDevice.StateCallback cameraDeviceStateCallback;
    private CaptureRequest.Builder captureRequestBuilder;
    private MediaRecorder.OnInfoListener onInfoListener;

    public CameraService(Activity activity,
                         TextureView textureView,
                         CameraManager cameraManager,
                         MediaPlayer mediaPlayer){

        this.targetActivity = activity;
        this.textureView    = textureView;
        this.cameraManager  = cameraManager;
        this.mediaPlayer    = mediaPlayer;
    }



    public void setSurfaceTextureListener(TextureView.SurfaceTextureListener surfaceTextureListener){
        this.textureView.setSurfaceTextureListener(surfaceTextureListener);
    }
    public void setCameraDeviceStateCallback(CameraDevice.StateCallback cameraDeviceStateCallback){
        this.cameraDeviceStateCallback = cameraDeviceStateCallback;
    }
    public void setOnInfoListener(MediaRecorder.OnInfoListener onInfoListener){
        this.onInfoListener = onInfoListener;
    }
    public void setCameraDevice(CameraDevice cameraDevice) {
        this.cameraDevice = cameraDevice;
    }
    public void setDuration(int duration){
        this.duration = duration;
    }

    public FileStruct getRecordedVideoDestination(){
        return recordedVideoDestination;
    }

    public void connectCamera(){
        try {
            if (ContextCompat.checkSelfPermission(targetActivity.getApplicationContext(), Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, cameraDeviceStateCallback, backgroundHandler);
                this.mediaRecorder = new MediaRecorder();
            } else {
                if (shouldShowRequestPermissionRationale(targetActivity,Manifest.permission.CAMERA)) {
                    Toast.makeText(targetActivity.getApplicationContext(), "Access Camera", Toast.LENGTH_SHORT).show();
                }
               requestPermissions(targetActivity,new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION_RESULT);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void closeCamera(){
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    public void closeCamera(CameraDevice camera){
        if (camera != null) {
            camera.close();
            this.cameraDevice = null;
        }
    }

    public void setupCamera(int width, int height){
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = targetActivity.getWindowManager().getDefaultDisplay().getRotation();
                totalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                videoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), width, height);
                this.cameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void startPreview(){
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.setRepeatingRequest(captureRequestBuilder.build(),
                                null, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(targetActivity.getApplicationContext(), "Unable to setup camera preview", Toast.LENGTH_SHORT).show();
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void setupRecorder() throws IOException {
        mediaRecorder.reset();
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        if (mediaPlayer == null) {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        }
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(recordedVideoDestination.getFileAbsolutPath());
        mediaRecorder.setVideoEncodingBitRate(20000000);
        mediaRecorder.setVideoFrameRate(60);
        mediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setMaxDuration(duration);
        mediaRecorder.setOrientationHint(totalRotation);
        if (mediaPlayer == null) {
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioChannels(1);
        }
        mediaRecorder.setMaxDuration(duration);
        mediaRecorder.setOnInfoListener(onInfoListener);
        mediaRecorder.prepare();
    }

    public void startRecord(){
        try {
            setupRecorder();
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mediaRecorder.getSurface();
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            captureRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.addTarget(recordSurface);

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try {
                                session.setRepeatingRequest(
                                        captureRequestBuilder.build(), null, null
                                );
                            } catch(CameraAccessException e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        }

                    }, null);

        } catch (IOException | CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public Boolean checkWriteStoragePermission() {
        if (ContextCompat.checkSelfPermission(targetActivity.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                new FileStruct(FileStructConstants.packageName, FileStructConstants.childPackageVideos);
                recordedVideoDestination = new FileStruct(FileStructConstants.packageName, FileStructConstants.childPackageVideos, FileStruct.createFileName(), FileStructConstants.mp4Ext);
            } catch (IOException e) {
                e.printStackTrace();
            }
            startRecord();
            mediaRecorder.start();
            return true;
        } else {

            if (shouldShowRequestPermissionRationale(targetActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(targetActivity.getApplicationContext(), "app need to be able to save vidos", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(targetActivity ,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);

            return false;

        }
    }

    public void startBackgroundThread() {
        backgroundHandlerThread = new HandlerThread("RecordActivity");
        backgroundHandlerThread.start();
        backgroundHandler = new Handler(backgroundHandlerThread.getLooper());
    }

    public void stopBackgroundThread() {
        backgroundHandlerThread.quitSafely();
        try {
            backgroundHandlerThread.join();
            backgroundHandlerThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void releaseMediaRecorder(){
        this.mediaRecorder.release();
        this.mediaRecorder = null;
    }

    public void resetMediaRecorder(){
        this.mediaRecorder.stop();
        this.mediaRecorder.reset();
    }

    public void startRecordVideo(){


    }
    public void stopRecordVideo(){

    }



}
