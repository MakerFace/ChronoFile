package id.ac.ui.clab.dchronochat.resources.entity;

import android.graphics.Bitmap;

import org.litepal.crud.DataSupport;

public class Photo extends DataSupport {
    private Bitmap icon;
    private String name;
    private String path;
    private String size;

    private Photo(Builder builder) {
        icon = builder.icon;
        name = builder.name;
        path = builder.path;
        size = builder.size;
    }

    public String getName() {
        return name;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public static final class Builder {
        private String name;
        private String path;
        private String size;
        private Bitmap icon;

        public Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder path(String val) {
            path = val;
            return this;
        }

        public Builder size(String val) {
            size = val;
            return this;
        }

        public Photo build() {
            return new Photo(this);
        }

        public Builder icon(Bitmap val) {
            icon = val;
            return this;
        }
    }
}
