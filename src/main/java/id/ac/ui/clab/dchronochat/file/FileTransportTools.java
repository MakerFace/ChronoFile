package id.ac.ui.clab.dchronochat.file;

import android.util.Log;

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.util.Blob;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import id.ac.ui.clab.dchronochat.chat.ChatbufProto;


/**
 * Created by LittleBoy on 2018/3/19.
 */

public class FileTransportTools {

    public static String FILE = "file";
    public static String START = "start";
    public static String END = "end";
//    private static String LOG_TAG = "FileTransportTools";

    public static int isFileData(Data data) {
        return 1;
    }

    public static int isFileInterest(Interest interest) {
        String component = interest.getName().getSubName(7, 1).toString();
//        Log.i(LOG_TAG, component);
        if ("/file".equals(component))
            return 1;
        else if ("/start".equals(component))
            return 2;
        else if ("/end".equals(component))
            return 3;
        return 0;
    }

    public static Interest packInterest(String type, Name name, String filename) {
        Name _name;
        if (type.equals(END)) {
            String nameStr = name.toString();
            nameStr = nameStr.substring(0, nameStr.lastIndexOf("/"));
            nameStr = nameStr.substring(0, nameStr.lastIndexOf("/"));
            _name = new Name(nameStr);
        } else {
            _name = new Name(name);
        }
        _name.append(type);
        _name.append(filename);
        //        Log.i(LOG_TAG, interest.toUri());
        return new Interest(_name);
    }

    //该函数有问题，Google协议尾不标准，猜测：应该是没有SequenceNo
    public static Data packData(Name name, String screenName, String chatRoom, byte[] bytes) {
//        Log.i(LOG_TAG, name.toUri());
        ChatbufProto.ChatMessage.Builder builder = ChatbufProto.ChatMessage.newBuilder(); //builder to build chatmessage instance
        builder.setFrom(screenName);
        builder.setTo(chatRoom);
        builder.setType(ChatbufProto.ChatMessage.ChatMessageType.FILE_INFO);
        try {
            builder.setData(new String(bytes, "iso-8859-1"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        Log.i(LOG_TAG, "bytes to string :" + Arrays.toString(bytes));
        builder.setTimestamp(0);
        builder.setSequence(0);
        Data data = new Data(name);
        data.setContent(new Blob(builder.build().toByteArray(), false));
        return data;
    }

    static void unpackInterest(Interest interest, String[] fileInfo) {
        Name name = interest.getName();
        fileInfo[0] = name.getSubName(6, 1).toString().substring(1);
        fileInfo[1] = name.getSubName(7, 1).toString().substring(1);
        fileInfo[2] = name.getSubName(8, 1).toString().substring(1);
        fileInfo[3] = name.getSubName(9, 1).toString().substring(1);
//        Log.i(LOG_TAG, "file : " + fileInfo[0] + "|" +
//                "file name : " + fileInfo[1] + "|" +
//                "file type : " + fileInfo[2] + "|" +
//                "file index : " + fileInfo[3]);
    }

    static String unpackData(ChatbufProto.ChatMessage content) {
        String fileData = content.getData();
        //bytes = content.getData().getBytes("iso-8859-1");
        //Log.i(LOG_TAG, new String(bytes,"iso-8859-1"));
        Log.e("FTT", "file data length = " + fileData.length());
        return fileData;
    }
}
