package id.ac.ui.clab.dchronochat.file;

import android.support.annotation.NonNull;
import android.util.Log;

import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnTimeout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import id.ac.ui.clab.dchronochat.activity.MainActivity;
import id.ac.ui.clab.dchronochat.chat.ChatbufProto;

/**
 * Created by LittleBoy on 2018/1/20.
 */

final class FileReceiverTools extends FileTransport {

    private static final String TAG = "FileReceiveTools";

    private RandomAccessFile raf;
    private SlideWindow slideWindow;
    private OnSendInterest sendInterest;
    private Name remoteName;

    FileReceiverTools() {
        slideWindow = new SlideWindow();
    }

    synchronized void setSlideWindow(int lastIndex, Name name, OnSendInterest sendInterest) {
        this.remoteName = name;
        this.sendInterest = sendInterest;
        slideWindow.setEndIndex(lastIndex);
    }

    synchronized void ReceiveFile(int seq, ChatbufProto.ChatMessage content) {
        if (isFinish()) {
            Log.i(TAG, "ReceiveFile: already finished");
            return;
        }

        //如果待接收buffer中有seq，那么raf就要write；如果没有的话，说明已经接收过了
        if (slideWindow.contain(seq)) {

            byte[] bytes = content.getData().getBytes(StandardCharsets.ISO_8859_1);
            try {
//                Log.i("RAF_WRITE", "ReceiveFile: write sequence is " + seq);
                raf.seek(seq * LENGTH);
                raf.write(bytes);
                onProgress(bytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }

            slideWindow.cancel(seq);

            if (slideWindow.isFinish()) {
                state.setFinish();
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        raf = null;
                    }
                }
            }
        }
    }

    @Override
    void setFileInfo(String name) {
//        String str[] = name_size.split("/");
        state.setFileName(name);
//        state.setFileSize(size);

        try {
            File f = new File(MainActivity.CONTEXT_CACHE_DIRECTORY + File.separator + state.getFileName());//缓存路径
            if (f.exists()) {
                f.delete();
//                progress = (int) f.length();
            }
            raf = new RandomAccessFile(f, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void onProgress(int progress) {
        state.addProgress(progress * 100f / state.getFileSize());
    }

    //模拟TCP/IP的滑动窗口
    class SlideWindow {
        int size = 9;//size大小为10，从0开始
        ArrayList<Sequence> buffer;
        int startIndex;
        int nextIndex;
        int endIndex;

        SlideWindow() {
            buffer = new ArrayList<>();
        }

        void setEndIndex(int endIndex) {
            //如果end index没变过、或者要变小，就不用改了
            if (this.endIndex >= endIndex)
                return;

            this.endIndex = endIndex;
            int len = endIndex < size ? endIndex : size;
            if (!buffer.isEmpty() || buffer.size() >= len)
                return;
            nextIndex = startIndex + size + 1;//先发10个，每次窗口滑动多少，再接着从nextIndex发出多少。。。size - 1，这里就要多加1
            for (int i = startIndex + buffer.size(); i <= len; i++) {
                Sequence sequence = new Sequence(i);
                buffer.add(sequence);
            }
        }

        void cancel(int index) {
            //到最后一个，start index超过end index，这里判断不准确
            if (index < startIndex) {
                return;
            }

            buffer.get((int) (index - startIndex)).cancel(index);
//            Log.i(TAG, "cancel: index " + index + " has received");

            //窗口后移，且start index、next index相应后移
            int i = 0;
//            for (int i = 0; i < buffer.size(); i++)//buffer.size在减小，而i在增加，是错的
            while (buffer.size() > 0) {
                //因为每次都remove 0，所以i要变成0
                if (buffer.get(0).isFinish()) {
                    buffer.remove(0);
                    i++;
                    //没有待接收序号了
                    if (nextIndex > endIndex)
                        continue;

                    buffer.add(new Sequence(nextIndex));
                    nextIndex++;
                } else
                    break;
            }
            startIndex += i;
            //防止start index 超出界限
            if (startIndex > endIndex)
                startIndex = endIndex;
        }

        boolean contain(int index) {
            for (Sequence b : buffer) {
                if (b.seq == index) {
                    return true;
                }
            }
            return false;
        }

        //buffer不是空的，则还有序号未收到，如果isEmpty等于true,则finish等于true
        boolean isFinish() {
            return buffer.isEmpty();
        }

        class Sequence {
            private int times = 3;
            private int seq;
            private Timer timer;
            private boolean finish = false;

            Sequence(final int seq) {
                this.seq = seq;
                Name _name = new Name(remoteName).append(String.valueOf(seq));
                final Interest interest = new Interest(FileTransportTools.packInterest(
                        FileTransportTools.FILE, _name, state.getFileName()));
                timer = new Timer();
                //从现在起，每过syncLifeTime执行一次
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (times == 0) {
                            timer.cancel();
//                            Log.i(TAG, "schedule: " + seq + "次数已用完");
                            return;
                        }
                        times--;
                        sendInterest.send(interest);
                    }
                }, 0, 5000);
            }

            void cancel(int seq) {
                if (this.seq == seq && !finish) {
                    this.seq = -1;
                    timer.cancel();
                    finish = true;
                }
            }

            boolean isFinish() {
                return finish;
            }
        }
    }

    public interface OnSendInterest {
        void send(Interest interest);
    }
}
