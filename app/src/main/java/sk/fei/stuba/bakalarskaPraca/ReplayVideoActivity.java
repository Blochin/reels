package sk.fei.stuba.bakalarskaPraca;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;

import sk.fei.stuba.bakalarskaPraca.FFmpegService.FFmpegService;
import sk.fei.stuba.bakalarskaPraca.FileStruct.FileStruct;
import sk.fei.stuba.bakalarskaPraca.FileStruct.FileStructConstants;

public class ReplayVideoActivity extends AppCompatActivity {

    //TODO zkontrolovat ci neprebieha upload videa. ak ano tak spravit nejaky poradovnik na odosielanie

    private FileStruct recordedVideoDestination;
    private VideoView videoView;
    private ImageView removeButton;
    private ImageView sendButton;
   // private StorageReference storageReference;
   // private DatabaseReference databaseReference;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        recordedVideoDestination.deleteDirectory();
        Intent intent = new Intent(ReplayVideoActivity.this, RecordActivity.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_replay_video);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            recordedVideoDestination = new FileStruct(extras.getString("mergedVideoDestinationAbsolutPath"));
        }

        videoView  = findViewById(R.id.vidoview);
        removeButton = findViewById(R.id.remove);
        sendButton = findViewById(R.id.upload);



    }

    @Override
    protected void onStart() {
        super.onStart();

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordedVideoDestination.deleteDirectory();
                Intent intent = new Intent(ReplayVideoActivity.this, RecordActivity.class);
                startActivity(intent);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadVideoFirebase();
                recordedVideoDestination.deleteDirectory();
                Intent intent = new Intent(ReplayVideoActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.setVideoPath(recordedVideoDestination.getFileAbsolutPath());
        videoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recordedVideoDestination.deleteDirectory();
    }

    private String title =" ";

    private void uploadVideoFirebase(){
        String timestamp = "" + System.currentTimeMillis();
        String filePathAndName = "videos/" + "video" +timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);



        storageReference.putFile(recordedVideoDestination.getUriFile()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUri = uriTask.getResult();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("id", "" + timestamp);
                        hashMap.put("title", "" + title);
                        hashMap.put("timestamp", "" + timestamp);
                        hashMap.put("videoUrl", "" + downloadUri);

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("videos");
                        databaseReference.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),e.toString(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        });
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),e.toString(), Toast.LENGTH_LONG).show();

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

}