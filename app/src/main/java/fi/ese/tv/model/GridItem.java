package fi.ese.tv.model;

public class GridItem {
    public static final int ICON_NONE = -1;

    private String mTitle;
    private int mIconResId = ICON_NONE;

    public GridItem(String title) {
        this.mTitle = title;
    }

    public GridItem(String title, int iconResId) {
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
