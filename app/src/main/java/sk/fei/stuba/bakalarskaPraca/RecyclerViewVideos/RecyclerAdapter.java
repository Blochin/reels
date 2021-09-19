package sk.fei.stuba.bakalarskaPraca.RecyclerViewVideos;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import sk.fei.stuba.bakalarskaPraca.R;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<MediaObject> mediaObjects;

    public RecyclerAdapter(List<MediaObject> mediaObjects) {
        this.mediaObjects = mediaObjects;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PlayerViewHolder(
                LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.video_recycler_view_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((PlayerViewHolder)viewHolder).onBind(mediaObjects.get(i));
    }

    @Override
    public int getItemCount() {
        return mediaObjects.size();
    }

}
