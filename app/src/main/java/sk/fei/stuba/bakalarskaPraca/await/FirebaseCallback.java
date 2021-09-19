package sk.fei.stuba.bakalarskaPraca.await;

import java.util.ArrayList;
import java.util.List;

import sk.fei.stuba.bakalarskaPraca.RecyclerViewVideos.MediaObject;

public interface FirebaseCallback {
    void onCallback(List<MediaObject> list);
}
