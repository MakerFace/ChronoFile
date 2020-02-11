package id.ac.ui.clab.dchronochat.resources.entity;

import org.litepal.crud.DataSupport;

public class Music extends DataSupport {
    private String name;
    private int length;
    private String path;
    private boolean isChecked;

    private Music(Builder builder) {
        name = builder.name;
        length = builder.length;
        path = builder.path;
        isChecked = builder.isChecked;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public static final class Builder {
        private String name;
        private int length;
        private String path;
        private boolean isChecked;

        public Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder length(int val) {
            length = val;
            return this;
        }

        public Builder path(String val) {
            path = val;
            return this;
        }

        public Builder isChecked(boolean val) {
            isChecked = val;
            return this;
        }

        public Music build() {
            return new Music(this);
        }
    }
}
