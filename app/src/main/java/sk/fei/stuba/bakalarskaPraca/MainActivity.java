package sk.fei.stuba.bakalarskaPraca;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sk.fei.stuba.bakalarskaPraca.RecyclerViewVideos.MediaObject;
import sk.fei.stuba.bakalarskaPraca.RecyclerViewVideos.RecyclerAdapter;
import sk.fei.stuba.bakalarskaPraca.RecyclerViewVideos.RecyclerViewVideos;
import sk.fei.stuba.bakalarskaPraca.await.FirebaseCallback;

public class MainActivity extends AppCompatActivity {

    private RecyclerViewVideos mRecyclerView;
    private ImageView recordVideo;
    private final Context context = this;
    private List<MediaObject> mediaObjects = null;
    private ValueEventListener valueEventListener;
    private DatabaseReference ref;
    private ProgressBar progressBar;
    private ConstraintLayout constraintLayout;
    private TextView swipeForPlayFirstVideo;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
        setTheme(R.style.Theme_AppCompat_NoActionBar);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        recordVideo = findViewById(R.id.createVideo);
        progressBar = findViewById(R.id.videoProgressBar);
        constraintLayout = findViewById(R.id.mainLayout);
        swipeForPlayFirstVideo = findViewById(R.id.swipeForPlay);

    }

    @Override
    protected void onStart() {
        super.onStart();
        recordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                startActivity(intent);
                finish();
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        if(mRecyclerView != null){
            mRecyclerView.videoPlayer.play();
        }
        getData(new FirebaseCallback() {
            @Override
            public void onCallback(List<MediaObject> list) {
                ProgressBar p = new ProgressBar(context);
                if(mRecyclerView != null){
                    p.setVisibility(View.VISIBLE);
                    mediaObjects = list;
                    initRecyclerView(mediaObjects);
                    mRecyclerView.setMediaObjects(mediaObjects);
                }else{
                    p.setVisibility(View.INVISIBLE);
                    mediaObjects = list;
                    initRecyclerView(mediaObjects);
                    mRecyclerView.setMediaObjects(mediaObjects);
                    mRecyclerView.disableScroll();
                }

                try {
                    mRecyclerView.createVideoList();
                    mRecyclerView.videoPlayer.prepare();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                ref.removeEventListener(valueEventListener);
                mRecyclerView.setProgressBar(progressBar);
                mRecyclerView.setInitText(swipeForPlayFirstVideo);
                mRecyclerView.setConstraintLayout(constraintLayout);

            }

        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mRecyclerView != null){
            if(mRecyclerView.videoPlayer != null){
                mRecyclerView.videoPlayer.pause();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mRecyclerView!=null) {
            mRecyclerView.releasePlayer();
        }
    }

    public void getData(FirebaseCallback firebaseCallback){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference("videos");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MediaObject>  tmpMediaObjects =  collectAllVideos((Map<String,Object>) snapshot.getValue());
                firebaseCallback.onCallback(tmpMediaObjects);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref.addValueEventListener(valueEventListener);
    }

    private void initRecyclerView(List<MediaObject> list){
        if(mRecyclerView == null){
        mRecyclerView = findViewById(R.id.videoRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        SnapHelper mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(mRecyclerView);
        RecyclerAdapter adapter = new RecyclerAdapter(list);
        mRecyclerView.setAdapter(adapter);
        }
    }


    private List<MediaObject> collectAllVideos(Map<String,Object> users) {
        List<MediaObject> sounds = new ArrayList<>();

        for (Map.Entry<String, Object> entry : users.entrySet()){
            Map nodes = (Map) entry.getValue();
            MediaObject mediaObject = new MediaObject((String) nodes.get("videoUrl"));
            sounds.add(mediaObject);
        }
        sounds.add(sounds.get(sounds.size()-1)) ;
        return sounds;
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

}