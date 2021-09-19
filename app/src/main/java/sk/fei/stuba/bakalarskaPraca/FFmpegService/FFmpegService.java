package sk.fei.stuba.bakalarskaPraca.FFmpegService;


import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;


import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;

import sk.fei.stuba.bakalarskaPraca.FileStruct.FileStruct;
import sk.fei.stuba.bakalarskaPraca.MainActivity;
import sk.fei.stuba.bakalarskaPraca.RecordActivity;
import sk.fei.stuba.bakalarskaPraca.ReplayVideoActivity;

public class FFmpegService extends Service {


    FFmpeg fFmpeg;
    String [] command;
    Callbacks activity;
    private FileStruct mergedVideoDestination;
    private FileStruct downloadedSoundDestination;
    private FileStruct recordedVideoDestination;
    private FileStruct videoStream;

    IBinder myBinder = new LocalBinder();

    public FFmpegService(){}


    @Override
    public void onCreate() {
        super.onCreate();
        try {
            loadFFmpegBinary();
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null){
            command = intent.getStringArrayExtra("command");

            if(null != intent.getStringExtra("mergedVideoDestinationAbsolutPath")){
                mergedVideoDestination      = new FileStruct(intent.getStringExtra("mergedVideoDestinationAbsolutPath"));
            }

            if( null != intent.getStringExtra("downloadedSoundDestinationAbsolutPath")){
                downloadedSoundDestination  = new FileStruct(intent.getStringExtra("downloadedSoundDestinationAbsolutPath"));
            }

            if( null != intent.getStringExtra("recordedVideoDestinationAbsolutPath")){
                recordedVideoDestination    = new FileStruct(intent.getStringExtra("recordedVideoDestinationAbsolutPath"));
            }

            try{
                loadFFmpegBinary();
                execFFMpegCommand();
            }catch(FFmpegNotSupportedException | FFmpegCommandAlreadyRunningException e){
                e.printStackTrace();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void execFFMpegCommand() throws FFmpegCommandAlreadyRunningException {
        fFmpeg.execute(command, new ExecuteBinaryResponseHandler(){
            @Override
            public void onSuccess(String message) {
                super.onSuccess(message);
            }

            @Override
            public void onProgress(String message) {
                super.onProgress(message);

            }

            @Override
            public void onFailure(String message) {
                super.onFailure(message);
                Toast.makeText(getApplicationContext(),"in fail",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                 recordedVideoDestination.deleteDirectory();
                 downloadedSoundDestination.deleteDirectory();

                Intent replayVideo = new Intent(getApplicationContext(), ReplayVideoActivity.class);
                replayVideo.putExtra("mergedVideoDestinationAbsolutPath", mergedVideoDestination.getFileAbsolutPath());
                replayVideo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(replayVideo);
            }
        });
    }

    private void loadFFmpegBinary() throws FFmpegNotSupportedException {
        if(fFmpeg == null){
            fFmpeg = FFmpeg.getInstance(this);
        }

        fFmpeg.loadBinary(new LoadBinaryResponseHandler(){

            @Override
            public void onFailure() {
                super.onFailure();
            }

            @Override
            public void onSuccess() {
                super.onSuccess();
            }
        });


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class LocalBinder extends Binder {

        public FFmpegService getServiceInstance(){
            return FFmpegService.this;
        }
    }

    public void registerClient(Activity activity){
        this.activity = (Callbacks) activity;
    }

    public interface Callbacks{
        void updateClient(float data);
    }
}
