package sk.fei.stuba.bakalarskaPraca.RecyclerViewVideos;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import sk.fei.stuba.bakalarskaPraca.Cahce.CacheDataSourcesFactory;
import sk.fei.stuba.bakalarskaPraca.R;

public class RecyclerViewVideos extends RecyclerView {


    int targetPosition;
    public SimpleExoPlayer videoPlayer;
    private View viewHolderParent;
    private FrameLayout frameLayout;
    private PlayerView videoSurfaceView;
    private ProgressBar progressBar;
    private TextView swipeForPlayFirstVideo;
    private ConstraintLayout constraintLayout;
    private boolean firstVideo = true;
    private RecyclerViewVideos recyclerViewVideos = this;

    private List<MediaObject> mediaObjects = new ArrayList<>();
    private Context context;
    private int playPosition = -1;
    private boolean isVideoViewAdded;

    private static final  String TAG = "VideoPlayerRecyclerView";

    public RecyclerViewVideos(@NonNull Context context) {
        super(context);
        init(context);
    }

    public RecyclerViewVideos(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RecyclerViewVideos(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    private void initPlayer(Context context){
        this.context = context.getApplicationContext();
        videoSurfaceView = new PlayerView(this.context);
        videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        DefaultRenderersFactory renderersFactory;
        TrackSelector trackSelector = new DefaultTrackSelector(context);
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder().setBufferDurationsMs(
                250000,
                500000,
                1500,
                2000).build();
        @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER;
        renderersFactory = new DefaultRenderersFactory(context) .setExtensionRendererMode(extensionRendererMode);
        videoPlayer = new SimpleExoPlayer.Builder(context, renderersFactory)
                .setLoadControl(loadControl)
                .setTrackSelector(trackSelector)
                .build();
        videoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        videoSurfaceView.setUseController(false);
        videoSurfaceView.setPlayer(videoPlayer);
        videoPlayer.prepare();
    }
    private void init(Context context){

        initPlayer(context);

        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                swipeForPlayFirstVideo.setVisibility(INVISIBLE);
                progressBar.setVisibility(INVISIBLE);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onScrollStateChanged: called.");

                    if(!recyclerView.canScrollVertically(1)){
                        try {
                            playVideo(true);
                            progressBar.setVisibility(INVISIBLE);
                        } catch (IOException | InterruptedException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                    else{
                        try {
                            playVideo(false);
                        } catch (IOException | InterruptedException ioException) {
                            ioException.printStackTrace();
                        }
                    }

                }
            }
        });

        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                if (viewHolderParent != null && viewHolderParent.equals(view)) {
                    resetVideoView();
                }

            }
        });

        videoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                if(Player.STATE_READY == playbackState){
                    if (!isVideoViewAdded) {
                        addVideoView();
                        progressBar.setVisibility(INVISIBLE);
                    }
                }

            }


            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                videoPlayer.pause();
                 /*Integer tmp = new Integer(reason);
                 Toast.makeText(context,tmp.toString(),Toast.LENGTH_SHORT).show();*/
                if(reason == Player.EVENT_MEDIA_ITEM_TRANSITION){
                    if(!firstVideo){
                        recyclerViewVideos.smoothScrollBy(0, frameLayout.getHeight(), new FastOutLinearInInterpolator());
                    }
                }
                else if(reason == Player.EVENT_TRACKS_CHANGED){
                    if(!firstVideo){
                       videoPlayer.play();
                    }
                }
                // ak by bola niekde chyba jedine tu
            }



            @Override
            public void onIsLoadingChanged(boolean isLoading) {
                if(!isLoading && firstVideo){
                    progressBar.setVisibility(INVISIBLE);
                    swipeForPlayFirstVideo.setVisibility(VISIBLE);
                    constraintLayout.setBackgroundColor(Color.BLACK);
                    enableScroll();
                    firstVideo = false;
                }
            }

        });

    }

    public void playVideo(boolean isEndOfList) throws IOException, InterruptedException {
        targetPosition=0;

        if(!isEndOfList){
            int startPosition = ((LinearLayoutManager) Objects.requireNonNull(getLayoutManager())).findFirstVisibleItemPosition();
            int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1;
            }

            if (startPosition < 0 || endPosition < 0) {
                return;
            }

            targetPosition = startPosition;

        }
        else{
            targetPosition = mediaObjects.size() -1;
        }


        if (targetPosition == playPosition) {
            return;
        }

        playPosition = targetPosition;
        if (videoSurfaceView == null) {
            return;
        }

        videoSurfaceView.setVisibility(INVISIBLE);

        if(targetPosition == 0){
            removeVideoView(videoSurfaceView);
        }


        int currentPosition = targetPosition - ((LinearLayoutManager) Objects.requireNonNull(getLayoutManager())).findFirstVisibleItemPosition();

        View child = getChildAt(currentPosition);
        if (child == null) {
            return;
        }

        PlayerViewHolder holder = (PlayerViewHolder) child.getTag();
        if (holder == null) {
            playPosition = -1;
            return;
        }

        viewHolderParent = holder.itemView;
        frameLayout = holder.itemView.findViewById(R.id.media_container);

        if(targetPosition-1 == -1){
            videoPlayer.seekTo(targetPosition,C.TIME_UNSET);
        }else{
            videoPlayer.seekTo(targetPosition-1,C.TIME_UNSET);
        }

        videoPlayer.play();


    }

    private void removeVideoView(PlayerView videoView) {
        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent == null) {
            return;
        }
        int index = parent.indexOfChild(videoView);
        if (index >= 0) {
            parent.removeViewAt(index);
            isVideoViewAdded = false;
            viewHolderParent.setOnClickListener(null);
        }

    }

    private void addVideoView(){
        frameLayout.addView(videoSurfaceView);
        isVideoViewAdded = true;
        videoSurfaceView.requestFocus();
        videoSurfaceView.setVisibility(VISIBLE);
        videoSurfaceView.setAlpha(1);

    }

    private void resetVideoView(){
        if(isVideoViewAdded){
            removeVideoView(videoSurfaceView);
            playPosition = -1;
            videoSurfaceView.setVisibility(INVISIBLE);

        }
    }

    public void releasePlayer() {

        if (videoPlayer != null) {
            videoPlayer.release();
            videoPlayer = null;
        }

        viewHolderParent = null;
    }

    public void setMediaObjects(List<MediaObject> mediaObjects){
        this.mediaObjects = mediaObjects;
    }

    public void createVideoList() throws IOException, InterruptedException {
        ArrayList<MediaItem> arr = new ArrayList<>();
        ArrayList<MediaSource>  arr2 = new ArrayList<>();
        int i = 0;
        for(MediaObject o : mediaObjects){
            i++;
            arr.add(new MediaItem.Builder().setUri(o.getMediaUrl()).setTag(i).build());
           /* arr2.add( new ExtractorMediaSource(Uri.parse(o.getMediaUrl()),
                    new CacheDataSourcesFactory(context, 100 * 1024 * 1024, 5 * 1024 * 1024), new DefaultExtractorsFactory(), null, null));
           */
        }
        videoPlayer.addMediaItems(arr);
        //videoPlayer.setMediaSources(arr2);

    }

    public void setProgressBar(ProgressBar progressBar){
        this.progressBar = progressBar;
    }

    public void setInitText(TextView swipeForPlayFirstVideo){
        this.swipeForPlayFirstVideo = swipeForPlayFirstVideo;
    }

    public void setConstraintLayout(ConstraintLayout constraintLayout){
        this.constraintLayout = constraintLayout;
    }

    public void disableScroll(){
        this.setLayoutFrozen(true);
    }

    public void enableScroll(){
        this.setLayoutFrozen(false);
    }
}
