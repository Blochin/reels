package sk.fei.stuba.bakalarskaPraca;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ProgressBar;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import sk.fei.stuba.bakalarskaPraca.RecyclerViewSounds.SoundAdapter;
import sk.fei.stuba.bakalarskaPraca.RecyclerViewSounds.SoundMediaObject;


public class SoundsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SoundAdapter soundAdapter;
    ArrayList<SoundMediaObject> arrayList = new ArrayList<>();
    Context context = this;
    ProgressDialog progressDialog;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent newIntent = new Intent(SoundsActivity.this, RecordActivity.class);
        startActivity(newIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_BakalarskaPraca);
        setContentView(R.layout.activity_sounds);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("sounds");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList =  getAllSounds((Map<String,Object>) snapshot.getValue());
                recyclerView =findViewById(R.id.soundRecyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                soundAdapter = new SoundAdapter(context,arrayList);
                recyclerView.setAdapter(soundAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private ArrayList<SoundMediaObject> getAllSounds(Map<String,Object> users) {
        ArrayList<SoundMediaObject> sounds = new ArrayList<>();
        for (Map.Entry<String, Object> entry : users.entrySet()){
            Map nodes = (Map) entry.getValue();
            SoundMediaObject soundMediaObject = new SoundMediaObject((String) nodes.get("soundUrl"),(String) nodes.get("imageUrl"),(String) nodes.get("title"), (String) nodes.get("description"));
            sounds.add(soundMediaObject);
        }
        return sounds;
    }
}