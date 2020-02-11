package id.ac.ui.clab.dchronochat.resources.entity;

import org.litepal.crud.DataSupport;

public class Doc extends DataSupport {
    private String name;
    private int length;
    private String path;
    private boolean isChecked;

    private Doc(Builder builder) {
        name = builder.name;
        length = builder.length;
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

        public Builder isChecked(boolean val) {
            isChecked = val;
            return this;
        }

        public Doc build() {
            return new Doc(this);
        }
    }
}
