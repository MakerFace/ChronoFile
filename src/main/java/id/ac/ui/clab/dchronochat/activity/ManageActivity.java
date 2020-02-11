package id.ac.ui.clab.dchronochat.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import id.ac.ui.clab.dchronochat.R;
import id.ac.ui.clab.dchronochat.resources.adapter.FmPagerAdapter;
import id.ac.ui.clab.dchronochat.resources.fragment.DocFragment;
import id.ac.ui.clab.dchronochat.resources.fragment.MusicFragment;
import id.ac.ui.clab.dchronochat.resources.fragment.PhotoFragment;
import id.ac.ui.clab.dchronochat.resources.fragment.VideoFragment;

public class ManageActivity extends AppCompatActivity {

    private List<String> titles;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FmPagerAdapter pagerAdaper;
    private List<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        tabLayout = (TabLayout) findViewById(R.id.music_tab_layout);
        viewPager = (ViewPager) findViewById(R.id.manage_pager);
        initTitles();
//        for (String title : titles) {
//            Fragment fragment = new MusicFragment();
//            fragments.add(fragment);
//            tabLayout.addTab(tabLayout.newTab().setText(title));
//        }
        Fragment docFragment = new DocFragment();
        Fragment musicFragment = new MusicFragment();
        Fragment photoFragment = new PhotoFragment();
        Fragment videoFragment = new VideoFragment();

        fragments.add(photoFragment);
        fragments.add(musicFragment);
        fragments.add(docFragment);
        fragments.add(videoFragment);

        pagerAdaper = new FmPagerAdapter(fragments, getSupportFragmentManager(), this, titles);
        viewPager.setAdapter(pagerAdaper);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void initTitles() {
        String[] args = {"照片", "音乐", "文档", "视频"};
        titles = Arrays.asList(args);
    }
}
