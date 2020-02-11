package id.ac.ui.clab.dchronochat.file;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import net.named_data.jndn.Name;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by LittleBoy on 2018/5/11.
 */

public class FileState implements Cloneable {
    private int fileSize;
    private int position;
    private float progress;
    private boolean finish;
    private Name name;
    private String fileName;
    private String filePath;
    private State state;
    private boolean isSender = false;

    private FileState(Builder builder) {
        this.fileName = builder.fileName;
        this.filePath = builder.filePath;
        this.state = builder.state;
        this.fileSize = builder.size;
        this.position = builder.position;
        this.finish = builder.finish;
        this.name = builder.name;
    }

    public String getFileName() {
        return fileName;
    }

    void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setPosition(int pos) {
        this.position = pos;
    }

    public int getPosition() {
        return position;
    }

    public int getFileSize() {
        return fileSize;
    }

    public State getState() {
        return state;
    }

    void setState(State state) {
        this.state = state;
    }

    void setFilePath(String path) {
        this.filePath = path;
    }

    void setFileSize(int size) {
        this.fileSize = size;
    }

    boolean isFinish() {
        return finish;
    }

    void setFinish() {
        this.finish = true;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    float getProgress() {
        return this.progress;
    }

    float addProgress(float pro) {
        progress += pro;
        return progress;
    }

    void setSender() {
        isSender = true;
    }

    boolean getSender() {
        return isSender;
    }

    public enum State {
        NONE,
        RECEIVE,
        RECEIVING,
        SENDING,
        FINISH
    }

    public static class Builder {
        private int size;
        private int position;
        private boolean finish;
        private Name name;
        private String fileName;
        private String filePath;
        private State state;

        public Builder() {
            finish = false;
        }

        public Builder(FileState state) {
            this.size = state.fileSize;
            this.position = state.position;
            this.finish = state.finish;
            this.name = state.name;
            this.fileName = state.fileName;
            this.filePath = state.filePath;
            this.state = state.state;
        }

        public FileState build() {
            return new FileState(this);
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder setState(State state) {
            this.state = state;
            return this;
        }

        public Builder setSize(int size) {
            this.size = size;
            return this;
        }

        public Builder setFinish(boolean finish) {
            this.finish = finish;
            return this;
        }

        public Builder setName(Name name) {
            this.name = name;
            return this;
        }

        public Builder setPosition(int pos) {
            this.position = pos;
            return this;
        }
    }

    public static String[] getFileArgs(String filePath) {
        File file = new File(filePath);
        String[] args = new String[3];
        if (file.exists()) {
            args[0] = file.getName();
            args[1] = file.getPath();
            args[2] = String.valueOf(file.length());
        }
        return args;
    }

    @Override
    protected FileState clone() {
        try {
            return (FileState) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
