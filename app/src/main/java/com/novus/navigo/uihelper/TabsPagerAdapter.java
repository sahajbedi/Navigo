package com.novus.navigo.uihelper;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.novus.navigo.PlaceFragment;

/**
 * Created by sahajbedi on 11-Jan-16.
 */
public class TabsPagerAdapter extends FragmentStatePagerAdapter {
    int numTabs;

    public TabsPagerAdapter(FragmentManager fm, int numTabs) {
        super(fm);
        this.numTabs = numTabs;
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                return new PlaceFragment();
            /*case 1:
                // Top Rated fragment activity
                return new CardFragment();
            case 2:
                // Games fragment activity
                return new PlanFragment();*/
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return numTabs;
    }
}
