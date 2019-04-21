package com.chamelon.sabadmin.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.chamelon.sabadmin.fragments.FragmentLiveContent;
import com.chamelon.sabadmin.fragments.FragmentPendingContent;

public class ViewPagerAdapterAdmin extends FragmentStatePagerAdapter {

    private int mTabsCount;

    public ViewPagerAdapterAdmin(FragmentManager fm, int mTabsCount) {
        super(fm);
        this.mTabsCount = mTabsCount;
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment = null;

        switch (position) {

            case 0:

                fragment = new FragmentPendingContent();
                return fragment;

            case 1:

                fragment = new FragmentLiveContent();
                return fragment;

            default:
                return fragment;
        }
    }

    @Override
    public int getCount() {
        return mTabsCount;
    }
}
