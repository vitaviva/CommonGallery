package com.vitaviva.commongallery;

public class Constants {

    public static final String KEY_TRANSITION_DRAWABLE = "drawalbe";
    public static final String KEY_LISTENER = "listener";
    public static final int MAX_CACHED_PAGE = 3;
    public static final int TRANS_ANIM_DURATION_MLI = 300;
    public static final String EXTRA_CAN_VIEW_GRID = "CAN_VIEW_GRID";

    public static int getCachedPageSize() {
        return Math.max(2, MAX_CACHED_PAGE);
    }
}
