package id.ac.ui.clab.dchronochat.resources.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class FmPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private List<Fragment> fragments;
    private List<String> titles;

    public FmPagerAdapter(List<Fragment> fragments, FragmentManager fragmentManager,
                          Context context, List<String> titles){
        super(fragmentManager);
        this.context = context;
        this.fragments = fragments;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
