package id.ac.ui.clab.dchronochat.resources.fragment;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import id.ac.ui.clab.dchronochat.R;
import id.ac.ui.clab.dchronochat.resources.adapter.MusicAdapter;
import id.ac.ui.clab.dchronochat.resources.entity.Music;

public class MusicFragment extends Fragment {

    private List<Music> musics;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.view_pager_music, container, false);
        initMusics();
        RecyclerView musicRecyclerView = (RecyclerView) view.findViewById(R.id.music_recycler_view);
        musicRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        musicRecyclerView.setAdapter(new MusicAdapter(this.getContext(), musics));
        musicRecyclerView.addItemDecoration(new MyDecoration(getContext(),
                MyDecoration.VERTICAL_LIST));
        return view;
    }

    private void initMusics() {
        musics = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            musics.add(new Music.Builder()
                    .name("海阔天空".concat(String.valueOf(i)))
                    .path("/storage/emulated/0/music")
                    .length(100).build());
        }
    }

    private void addMusic(Music music) {
        musics.add(music);
    }
}
