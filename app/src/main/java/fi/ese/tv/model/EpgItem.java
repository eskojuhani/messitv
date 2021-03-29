package fi.ese.tv.model;

public class EpgItem {
    public static final int ICON_NONE = -1;

    private String mTitle;
    private int mIconResId = ICON_NONE;

    public EpgItem(String title) {
        this.mTitle = title;
    }

    public EpgItem(String title, int iconResId) {
        this.mTitle = title;
        this.mIconResId = iconResId;
    }

    public int getIconResId() {
        return mIconResId;
    }

    public String getTitle() {
        return mTitle;
    }
}
