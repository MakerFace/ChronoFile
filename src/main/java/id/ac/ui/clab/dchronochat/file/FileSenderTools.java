package id.ac.ui.clab.dchronochat.file;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import id.ac.ui.clab.dchronochat.activity.MainActivity;

/**
 * 从指定路径读取文件，存入cache中
 */
public final class FileSenderTools extends FileTransport {
    private static final String TAG = "FileSenderTools";
    private RandomAccessFile raf;
    private ArrayList<Integer> sequenceList;
    private String filePath;

    @Override
    public void setFileInfo(String fileName) {
        //state = new FileState.Builder().build();
        filePath = MainActivity.CONTEXT_CACHE_DIRECTORY.concat(File.separator).concat(fileName);
    }

    //Data LENGTH 不超过 4000
    void getData() {
        if (isFinish()) {
            return;
        }
        if (raf == null) {
            try {
                raf = new RandomAccessFile(filePath, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            byte[] bytes = new byte[LENGTH];
            raf.read(bytes, 0, LENGTH);
            if (raf.getFilePointer() == raf.length()) {
                raf.close();
                raf = null;
                state.setFinish();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized byte[] getData(int seq) {
        try {
            if (raf == null)
                raf = new RandomAccessFile(filePath, "r");

            byte[] bytes = new byte[LENGTH];
            raf.seek(seq * LENGTH);
            int len = raf.read(bytes, 0, LENGTH);
            if (raf.getFilePointer() == raf.length()) {
                byte[] tmp = new byte[len];
                System.arraycopy(bytes, 0, tmp, 0, len);
                bytes = tmp;
            }
            return bytes;
        } catch (IOException e) {
//            e.printStackTrace();
            try {
                Log.e(TAG, String.format("getData: want locate to %d/%d", seq * LENGTH, raf.length()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    void setSequenceList(int len) {
        Log.i(TAG, "setSequenceList: len is " + len);
        sequenceList = new ArrayList<>();
        for (int i = 0; i <= len; i++) {
            sequenceList.add(i);
        }
    }

    private boolean checkSequence(int index) {
        if (sequenceList.contains(index)) {
            sequenceList.remove(Integer.valueOf(index));
            return true;
        }
        return false;
    }

    synchronized float onProgress(int index, int progress) {
        float pro;
        if (checkSequence(index)) {
            pro = state.addProgress(progress * 100f / state.getFileSize());
        } else {
            pro = -1.f;
        }
        return pro;
    }

}
