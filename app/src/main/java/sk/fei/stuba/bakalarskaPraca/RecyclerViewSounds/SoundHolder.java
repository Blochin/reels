package sk.fei.stuba.bakalarskaPraca.RecyclerViewSounds;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sk.fei.stuba.bakalarskaPraca.R;

public class SoundHolder extends RecyclerView.ViewHolder {

    ImageView imageView;
    ImageView download;
    TextView title, description;

    public SoundHolder(@NonNull View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.cardViewImage);
        title = itemView.findViewById(R.id.cardTitle);
        description = itemView.findViewById(R.id.cardViewDescription);
        download = itemView.findViewById(R.id.download_music);

    }


}
