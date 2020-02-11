package id.ac.ui.clab.dchronochat.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Locale;

class SplitFile {

    static void getSplitFile(String url, String cacheDir, int count) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(new File(url), "r");
            long length = raf.length();
            long maxSize = length / count;
            long offSet = 0L;
            for (int i = 0; i < count - 1; i++) {
                long begin = offSet;
                long end = (i + 1) * maxSize;
                offSet = getWrite(url, cacheDir, i, begin, end);
            }
            if (length - offSet > 0) {
                getWrite(url, cacheDir, count - 1, offSet, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert raf != null;
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void createEmptyFile(String url, String fileName, int count) {
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        for (int i = 0; i < count; i++) {
            try {
                File file = new File(url, fileName.concat(String.format(Locale.getDefault(), "_%d.tmp", i)));
                file.createNewFile();
                System.out.println(file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static String getTempFileName(String fileName, int index) {
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        return fileName.concat(String.format(Locale.getDefault(), "_%d.tmp", index));
    }

    /**
     * 指定文件每一份的边界，写入不同文件中
     *
     * @param url      源文件
     * @param cacheDir 缓存文件夹
     * @param index    源文件的顺序标识
     * @param begin    开始指针的位置
     * @param end      结束指针的位置
     * @return long
     */
    private static long getWrite(String url, String cacheDir, int index, long begin, long end) {

        String tmp = url.substring(url.lastIndexOf('/'));
        tmp = tmp.substring(1, tmp.lastIndexOf('.')).concat(//去掉后缀
                String.format(Locale.getDefault(), "_%d.tmp", index));//拼接自定义后缀
        long endPointer = 0L;
        try {
            RandomAccessFile in = new RandomAccessFile(new File(url), "r");
            RandomAccessFile out = new RandomAccessFile(new File(cacheDir, tmp), "rw");
            byte[] bytes = new byte[1024];
            int n;
            in.seek(begin);
            while (in.getFilePointer() <= end && (n = in.read(bytes)) != -1) {
                out.write(bytes, 0, n);
            }
            endPointer = in.getFilePointer();
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return endPointer;
    }

    /**
     * 文件合并
     *
     * @param sourceURL 源文件：临时文件
     * @param desURL    目标文件
     * @param tempCount 文件个数
     */
    static boolean merge(String sourceURL, String desURL, int tempCount) {
        boolean res = true;
        String name = sourceURL.substring(0, sourceURL.lastIndexOf("."));
        RandomAccessFile desRaf;
        try {
            File desFile = new File(desURL);
            if (!desFile.exists()) {
                desFile.createNewFile();
            }
            desRaf = new RandomAccessFile(desFile, "rw");
            for (int i = 0; i < tempCount; i++) {
                RandomAccessFile sourceRaf = new RandomAccessFile(
                        new File(name.concat(String.format(Locale.getDefault(), "_%d.tmp", i))),
                        "r");
                byte[] bytes = new byte[1024];
                int n;
                while ((n = sourceRaf.read(bytes)) != -1) {
                    desRaf.write(bytes, 0, n);
                }
                sourceRaf.close();
            }
            desRaf.close();
        } catch (IOException e) {
            e.printStackTrace();
            res = false;
        }
        return res;
    }
}
