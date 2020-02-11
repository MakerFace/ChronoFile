package id.ac.ui.clab.dchronochat.resources.fragment;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import id.ac.ui.clab.dchronochat.R;
import id.ac.ui.clab.dchronochat.resources.adapter.PhotoAdapter;
import id.ac.ui.clab.dchronochat.resources.entity.Photo;

public class PhotoFragment extends Fragment {

    private List<Photo> photos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.view_pager_photo, container, false);
        initPhotos();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.photo_recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(new PhotoAdapter(getContext(), photos));
        return view;
    }

    private void initPhotos() {
        photos = new ArrayList<>();
        //线程中加载
        Cursor externalCursor = MediaStore.Images.Media.query(getContext().getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media._ID},
                null,
                MediaStore.Images.Media._ID);

        if (externalCursor != null && externalCursor.moveToFirst()) {
            do {
                String name = externalCursor.getString(0);
                String size = externalCursor.getString(1);
                String data = externalCursor.getString(2);
                long _id = externalCursor.getLong(3);
                Bitmap icon;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                icon = MediaStore.Images.Thumbnails.getThumbnail(
                        getContext().getContentResolver(),
                        _id, MediaStore.Images.Thumbnails.MINI_KIND, options);
                Log.i("PhotoFragment", "initPhotos: name = " + name);
                photos.add(new Photo.Builder()
                        .icon(icon).name(name).size(size).path(data).build());
            } while (externalCursor.moveToNext());

        }

        assert externalCursor != null;
        externalCursor.close();

//        for (int i = 0; i < 10; i++) {
//            photos.add(new Photo.Builder()
//                    .name("Launcher".concat(String.valueOf(i)))
//                    .size(String.valueOf(10))
//                    .path("/storage/emulated/0/").build());
//        }
    }
}
