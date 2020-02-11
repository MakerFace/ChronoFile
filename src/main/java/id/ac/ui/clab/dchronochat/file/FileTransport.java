package id.ac.ui.clab.dchronochat.file;

import net.named_data.jndn.Name;

abstract class FileTransport {
    protected static int LENGTH = 5300;
    protected FileState state;
    private OnOnePartFinish onOnePartFinish;//子线程调用

    void setFileState(FileState state) {
        this.state = state;
    }

    Name getName() {
        return state.getName();
    }

    boolean isFinish() {
        return state.isFinish();
//        return false;
    }

    float getProgress() {
        return state.getProgress();
    }

    void setOnOnePartFinish(OnOnePartFinish onOnePartFinish) {
        this.onOnePartFinish = onOnePartFinish;
    }

    abstract void setFileInfo(String fileName);

    void onOnePartFinish() {
        onOnePartFinish.onFinish();
    }

    public interface OnOnePartFinish {
        void onFinish();
    }
}
