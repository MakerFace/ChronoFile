package id.ac.ui.clab.dchronochat.resources.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import id.ac.ui.clab.dchronochat.R;
import id.ac.ui.clab.dchronochat.resources.adapter.VideoAdapter;
import id.ac.ui.clab.dchronochat.resources.entity.Video;

public class VideoFragment extends Fragment {

    private List<Video> videos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.view_pager_video, container, false);
        initVideos();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.video_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new VideoAdapter(videos, getContext()));
        return view;
    }

    private void initVideos() {
        videos = new ArrayList<>();
        videos.add(new Video.Builder().
                videoName("G.E.M.邓紫棋 - 光年之外.mp4").
                videoPath("/storage/emulated/0/WLAN Direct/").build());
//        for (int i = 0; i < 10; i++) {
//            videos.add(new Video.Builder().
//                    videoName("video".concat(String.valueOf(i)).concat(".mp4")).
//                    videoTime("5:30").build());
//        }
    }
}
