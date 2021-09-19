package sk.fei.stuba.bakalarskaPraca.RecyclerViewSounds;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import sk.fei.stuba.bakalarskaPraca.FileStruct.FileStruct;
import sk.fei.stuba.bakalarskaPraca.FileStruct.FileStructConstants;
import sk.fei.stuba.bakalarskaPraca.R;
import sk.fei.stuba.bakalarskaPraca.RecordActivity;
import sk.fei.stuba.bakalarskaPraca.SoundsActivity;
import sk.fei.stuba.bakalarskaPraca.animation.ProgressBarAnimation;

public class SoundAdapter extends RecyclerView.Adapter<SoundHolder> {

    private Context context;
    private ArrayList<SoundMediaObject> soundMediaObjects;
    ProgressDialog progressDialog;



    public SoundAdapter(Context context, ArrayList<SoundMediaObject> soundMediaObjects/*,ProgressBar progressBar*/){
        this.context = context;
        this.soundMediaObjects = soundMediaObjects;

    }

    @NonNull
    @Override
    public SoundHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_recycler_view_item,parent,false);

        return new SoundHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundHolder holder, int position) {
        holder.title.setText(soundMediaObjects.get(position).getTitle());
        holder.description.setText(soundMediaObjects.get(position).getDescription());
        holder.imageView.setImageURI(Uri.parse(soundMediaObjects.get(position).getImageUrl()));
        Picasso.get().load(soundMediaObjects.get(position).getImageUrl()).into(holder.imageView);

        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setTitle("Downloading "+soundMediaObjects.get(position).getTitle());
                progressDialog.show();
                new FileStruct(FileStructConstants.packageName, FileStructConstants.childPackageSounds);
                downloadFiles(view.getContext(),soundMediaObjects.get(position).getTitle() , FileStructConstants.mp3Ext, FileStructConstants.packageName + "/" + FileStructConstants.childPackageSounds, soundMediaObjects.get(position).getSoundUrl(),view);

            }
        });

    }

    @Override
    public int getItemCount() {
        return soundMediaObjects.size();
    }


    public void downloadFiles(Context context, String filename, String fileExtension, String destinationDirectory, String url, View view) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setDestinationInExternalPublicDir(destinationDirectory, filename + fileExtension);
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                Intent newIntent = new Intent(view.getContext(), RecordActivity.class);
                newIntent.putExtra("soundName", filename);
                view.getContext().startActivity(newIntent);
                progressDialog.dismiss();

            }
        };
        view.getContext().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        downloadManager.enqueue(request);
    }
}
