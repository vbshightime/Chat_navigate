package com.example.webczar.chat_navigate.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.webczar.chat_navigate.Fragments.Login_frag;
import com.example.webczar.chat_navigate.Fragments.signUp_frag;

/**
 * Created by webczar on 1/2/2018.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter{
    private static int TAB_COUNTS = 2;
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new Login_frag();
            case 1:
                return new signUp_frag();
        }
        return null;
    }

    @Override
    public int getCount() {
        return TAB_COUNTS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return Login_frag.TITLE;
            case 1:
                return signUp_frag.TITLE;
        }
        return super.getPageTitle(position);
    }
}
