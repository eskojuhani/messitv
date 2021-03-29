package fi.ese.tv.model;

import androidx.leanback.widget.HeaderItem;


public class MainHeaderItem extends HeaderItem {

    public static final int ICON_NONE = -1;

    /** Hold an icon resource id */
    private int mIconResId = ICON_NONE;

    public MainHeaderItem(long id, String name, int iconResId) {
        super(id, name);
        mIconResId = iconResId;
    }

    public MainHeaderItem(long id, String name) {
        this(id, name, ICON_NONE);
    }

    public MainHeaderItem(String name, int iconResId) {
        super(name);
        mIconResId = iconResId;
    }

    public MainHeaderItem(String name) {
        super(name);
    }

    public int getIconResId() {
        return mIconResId;
    }

    public void setIconResId(int iconResId) {
        this.mIconResId = iconResId;
    }
}
