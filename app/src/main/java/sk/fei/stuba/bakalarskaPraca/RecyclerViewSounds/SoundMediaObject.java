package sk.fei.stuba.bakalarskaPraca.RecyclerViewSounds;

public class SoundMediaObject {
    private String SoundUrl, ImageUrl, Title, Description;

    public SoundMediaObject(String soundUrl, String imageUrl, String title, String description) {
        SoundUrl = soundUrl;
        ImageUrl = imageUrl;
        Title = title;
        Description = description;
    }

    public String getSoundUrl() {
        return SoundUrl;
    }

    public void setSoundUrl(String soundUrl) {
        SoundUrl = soundUrl;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}
