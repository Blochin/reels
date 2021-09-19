package sk.fei.stuba.bakalarskaPraca.RecyclerViewVideos;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;

import sk.fei.stuba.bakalarskaPraca.R;

public class PlayerViewHolder extends RecyclerView.ViewHolder {
    FrameLayout media_container;
    View parent;

    public PlayerViewHolder(@NonNull View itemView){
        super(itemView);
        parent = itemView;
        media_container = itemView.findViewById(R.id.media_container);
    }
    public void onBind(MediaObject mediaObject){
        parent.setTag(this);
    }
}
