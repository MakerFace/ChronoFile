package id.ac.ui.clab.dchronochat.resources.entity;

import android.graphics.Bitmap;
import android.system.Os;
import android.util.Log;

import org.litepal.crud.DataSupport;

import java.io.File;

import id.ac.ui.clab.dchronochat.utils.Tools;

public class Video extends DataSupport {
    private Bitmap videoIcon;
    private String videoName;
    private String videoTime;
    private String videoPath;
    private boolean isChecked;

    private Video(Builder builder) {
        videoIcon = builder.videoIcon;
        videoName = builder.videoName;
        videoTime = builder.videoTime;
        videoPath = builder.videoPath;
        isChecked = builder.isChecked;
        Log.e("Video", "Video: " + videoPath);
        String path = videoPath.concat(File.separator).concat(videoName);
        Log.e("Video", "Video: " + path);
        videoIcon = Tools.getVideoPhoto(path);
    }

    public Bitmap getVideoIcon() {
        return videoIcon;
    }

    public String getVideoName() {
        return videoName;
    }

    public String getVideoTime() {
        return videoTime;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public static final class Builder {
        private Bitmap videoIcon;
        private String videoName;
        private String videoTime;
        private String videoPath;
        private boolean isChecked;

        public Builder() {
        }

        public Builder videoIcon(Bitmap val) {
            videoIcon = val;
            return this;
        }

        public Builder videoName(String val) {
            videoName = val;
            return this;
        }

        public Builder videoTime(String val) {
            videoTime = val;
            return this;
        }

        public Builder videoPath(String val) {
            videoPath = val;
            return this;
        }

        public Builder isChecked(boolean val) {
            isChecked = val;
            return this;
        }

        public Video build() {
            return new Video(this);
        }
    }
}
