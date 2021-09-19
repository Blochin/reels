package sk.fei.stuba.bakalarskaPraca.RecyclerViewVideos;

public class MediaObject {
    private String mediaUrl;

    public MediaObject(String mediaUrl){
        this.mediaUrl = mediaUrl;
    }

    public void setMediaUrl(String mediaUrl){
        this.mediaUrl=mediaUrl;
    }

    public String getMediaUrl(){
        return this.mediaUrl;
    }

}
