package com.novus.navigo.uihelper;

/**
 * Created by sahajbedi on 11-Jan-16.
 */
public interface ItemTouchHelperAdapter {
    boolean onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}
