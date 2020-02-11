package id.ac.ui.clab.dchronochat.file;

import android.os.AsyncTask;
import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.InterestFilter;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.KeyType;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.security.policy.NoVerifyPolicyManager;
import net.named_data.jndn.sync.ChronoSync2013;
import net.named_data.jndn.util.Blob;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import id.ac.ui.clab.dchronochat.activity.MainActivity;
import id.ac.ui.clab.dchronochat.chat.CachedMessage;
import id.ac.ui.clab.dchronochat.chat.ChatAdapter;
import id.ac.ui.clab.dchronochat.chat.ChatbufProto;
import id.ac.ui.clab.dchronochat.chat.DChronoChat;
import id.ac.ui.clab.dchronochat.utils.SpeedTest;

import static id.ac.ui.clab.dchronochat.chat.ChatListFragment.DEFAULT_RSA_PRIVATE_KEY_DER;
import static id.ac.ui.clab.dchronochat.chat.ChatListFragment.DEFAULT_RSA_PUBLIC_KEY_DER;

public class ChronoFile {

    private static final String TAG = "ChronoFile";
    private String screenName;
    private String userName;
    private String chatRoom;
    private String hubPrefix;
    private ChatAdapter.OnChangeData onChangeData;
//    private final double syncLifetime = 5000.0; // milliseconds
//    private String tempName;

    //存储未传输的文件
    private static final int TEST_COUNT = 10;
    private static final int SEND_COUNT = 10;
    private ThreadPoolExecutor threads;
    private List<FileWorker> fileWorkers;
    private HashMap<String, FileState> files;

    public ChronoFile(String screenName, String userName, String hubPrefix, String chatRoom, ChatAdapter.OnChangeData onChangeData) {
        this.screenName = screenName;
        this.userName = userName;
        this.hubPrefix = hubPrefix;
        this.chatRoom = chatRoom;
        this.onChangeData = onChangeData;
        files = new HashMap<>();

        threads = new ThreadPoolExecutor(10, 15,
                1,
                TimeUnit.DAYS,
                new LinkedBlockingDeque<Runnable>());
        fileWorkers = new ArrayList<>();
    }

    /**
     * 发送方发送调用，发送方state是NONE，item不能点
     * 接收方接收时调用，接收方state是RECEIVE，item可以点击
     */
    public void addFile(FileState fileState) {
        Log.i(TAG, "addFile: fileName = " + fileState.getFileName() + ";filepath=" + fileState.getFilePath());
        files.put(fileState.getFileName(), fileState);
    }

    public int beginSend(final String fileName) throws UnsupportedEncodingException {
        //require origin file name, not tmp file name
        //中文需要decode才可以用
        final FileState senderState = files.get(URLDecoder.decode(fileName, "utf-8"));
        assert senderState != null;
        if (senderState.getState().equals(FileState.State.SENDING))
            return -1;
        senderState.setState(FileState.State.SENDING);
        senderState.setSender();

        threads.execute(new Runnable() {
            @Override
            public void run() {
                SpeedTest.initialize(senderState.getFileSize());
                SpeedTest.start();
                String filePath = senderState.getFilePath();
                SplitFile.getSplitFile(filePath, MainActivity.CONTEXT_CACHE_DIRECTORY, SEND_COUNT);

                //必须在循环外
                FileTransport.OnOnePartFinish onOnePartFinish = getOnOnePartFinish(senderState, fileName);

                int session = (int) Math.round(System.currentTimeMillis() / 1000.00);
                for (int i = 0; i < TEST_COUNT/*SEND_COUNT*/; i++) {
                    FileSenderTools fileSender = new FileSenderTools();
                    fileSender.setFileState(senderState.clone());
                    fileSender.setOnOnePartFinish(onOnePartFinish);
                    FileWorker fileWorker = new FileWorker(i, session + i, fileSender);
                    fileWorker.prepareWorker(true);
                    threads.execute(fileWorker);
                    fileWorkers.add(fileWorker);
                    try {
                        Thread.sleep(70);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return senderState.getPosition();
    }

    /**
     * @param filePath desc path
     * @param fileName save name
     */
    public void beginDownload(final String filePath, final String fileName, final int fileSize,
                              final Face face, final DChronoChat dChronoChat) {
        //根据文件名获取file state
        final FileState receiverState = files.get(fileName);
        assert receiverState != null;
        if (receiverState.getState().equals(FileState.State.RECEIVING))
            return;

        receiverState.setState(FileState.State.RECEIVING);
        receiverState.setFilePath(filePath);
        receiverState.setFileSize(fileSize);
        Log.i(TAG, "messageEventBus: item position is " + receiverState.getPosition());

        threads.execute(new Runnable() {
            @Override
            public void run() {
                SpeedTest.initialize(fileSize);
                SpeedTest.start();

                FileTransport.OnOnePartFinish onOnePartFinish = getOnOnePartFinish(receiverState, fileName);
                SplitFile.createEmptyFile(MainActivity.CONTEXT_CACHE_DIRECTORY,
                        fileName, SEND_COUNT);
                int session = (int) Math.round(System.currentTimeMillis() / 1000.0);                //one of receiver loop
                for (int i = 0; i < TEST_COUNT/*SEND_COUNT*/; i++) {
                    FileReceiverTools fileReceiver = new FileReceiverTools();
                    fileReceiver.setOnOnePartFinish(onOnePartFinish);
                    fileReceiver.setFileState(receiverState.clone());
                    FileWorker fileWorker = new FileWorker(i, session + i, fileReceiver);
                    fileWorker.prepareWorker(false);
                    threads.execute(fileWorker);
                    fileWorkers.add(fileWorker);
                    try {
                        Thread.sleep(70);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //主线程中不可以做耗时操作
                try {
                    Interest beginInterest = FileTransportTools.packInterest(FileTransportTools.START,
                            receiverState.getName(), fileName);
                    beginInterest.setInterestLifetimeMilliseconds(5000);
                    Log.i(TAG, "beginDownload: begin interest is " + beginInterest.toUri());
                    face.expressInterest(beginInterest, dChronoChat, dChronoChat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private FileTransport.OnOnePartFinish getOnOnePartFinish(final FileState state, final String fileName) {
        return new FileTransport.OnOnePartFinish() {
            @Override
            synchronized public void onFinish() {
                //这里要修改state都是要修改主线程中的
                state.addProgress(1);
                if (state.getProgress() >= 10.f) {
                    SpeedTest.end();
                    Iterator<FileWorker> it = fileWorkers.iterator();
                    while (it.hasNext()) {
                        it.next();
                        it.remove();
                    }
                    float role = -1.f;
                    if (!state.getSender()) {
                        SplitFile.merge(MainActivity.CONTEXT_CACHE_DIRECTORY.concat(File.separator).concat(fileName),
                                state.getFilePath().concat(File.separator).concat(fileName), SEND_COUNT);
                        role = 1.f;
                    }
                    state.setState(FileState.State.FINISH);
                    try {
                        //因为Adapter连续发两次notify，只执行一次，所以我们间隔几秒
                        Thread.sleep(100);
                        onChangeData.onChangeData(state.getPosition(), -1,
                                SpeedTest.getTotalTime(), SpeedTest.getSpeed(), role);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

    }

    public FileState getFileState(String fileName) {
        return files.get(fileName);
    }

    public void stopWork() {
        for (FileWorker work : fileWorkers) {
            if (work.isTransmission) {
                work.stopWorker();
            }
        }
        threads.shutdown();
    }

    /**
     * 最小的传输线程
     * 每个Data最大可以发送5333B，这里取5120B(5KB)为最小发送长度
     * 每个文件分为10个小文件，在十个线程中发送
     */
    private class FileWorker implements Runnable {
        private int workID;
        private String tmpFileName;
        private FileTransport transport;
        private boolean isTransmission = false;
        private boolean isSender = false;
        private int session;

        FileWorker(int id, int sessionID, FileTransport transport) {
            workID = id;
            session = sessionID;
            //transport.state现在还是总文件的名字
            tmpFileName = SplitFile.getTempFileName(transport.state.getFileName(), workID);
            this.transport = transport;
        }

        /**
         * @param isSender default is false
         */
        void prepareWorker(boolean isSender) {
            isTransmission = true;
            this.isSender = isSender;
        }

        void stopWorker() {
            isTransmission = false;
        }

        @Override
        public void run() {
            try {
                Face face = new Face();
                MemoryIdentityStorage identityStorage = new MemoryIdentityStorage();
                MemoryPrivateKeyStorage privateKeyStorage = new MemoryPrivateKeyStorage();
                KeyChain keyChain = new KeyChain
                        (new IdentityManager(identityStorage, privateKeyStorage),
                                new NoVerifyPolicyManager());
                keyChain.setFace(face);
                Name keyName = new Name("/testname/DSK-12345");
                Name certificateName = keyName.getSubName(0, keyName.size() - 1).append
                        ("KEY").append(keyName.get(-1)).append("ID-CERT").append("0");
                identityStorage.addKey(keyName, KeyType.RSA, new Blob(
                        DEFAULT_RSA_PUBLIC_KEY_DER, false));
                privateKeyStorage.setKeyPairForKeyName
                        (keyName, KeyType.RSA, DEFAULT_RSA_PUBLIC_KEY_DER, DEFAULT_RSA_PRIVATE_KEY_DER);
                face.setCommandSigningInfo(keyChain, certificateName);

                //TODO: 用作广播和数据前缀试验
                FileChrono fileChrono = new FileChrono(workID,
                        screenName,
                        tmpFileName,//TODO: 用作广播和数据前缀试验
                        chatRoom,
                        new Name(hubPrefix),
                        face,
                        keyChain,
                        certificateName,
                        session,
                        false);
                Log.i(TAG, "run: id = " + workID);

                if (!isSender) {
                    //这里transport.state改变了文件名，filename_%d.tmp
                    transport.setFileInfo(tmpFileName);
                    fileChrono.beginDownload(transport);
                } else {
                    transport.setFileInfo(tmpFileName);
                    fileChrono.beginSend(transport);
                }

                while (isTransmission) {
                    //TODO:no used
                    if (fileChrono.isOver) {
                        Log.i(TAG, "run: work" + workID + " will dead");
                        new WaitThreeSeconds(this).execute();
                        break;
                    }
                    //TODO: is it necessary? accept it only sync.
                    //TODO: error:chronosync:sequenceNO_ is not the expected value of 0 for first use. 这是发送sync信号，但是没有其他用户接收导致的
                    //TODO: E/ChronoSync2013: null InvalidProtocolBufferException: Protocol message end-group tag did not match expected tag.
                    face.processEvents();
                    Thread.sleep(7);
                }

            } catch (IOException | SecurityException | InterruptedException | EncodingException e) {
                e.printStackTrace();
            }
        }

    }

    private class FileChrono implements ChronoSync2013.OnInitialized, ChronoSync2013.OnReceivedSyncState, OnData, OnInterestCallback, OnTimeout {
        String screenName;
        String userName;
        Name chatPrefix;
        String fileRoom;
        Face face;
        KeyChain keyChain;
        Name certificateName;
        double syncLifetime = 5000.0;
        ChronoSync2013 sync;
        Name identityName;
        //        ArrayList<HashMap<String, String>> mSequence = new ArrayList<>();
        ArrayList<CachedMessage> messageCache = new ArrayList<>();
        //        ArrayList<String> roster = new ArrayList<String>();
        boolean requireVerification;
        String tempName;
        int session;
        int MAX_MESSAGE_CACHE_LENGTH = 250;
        String mNameAndSession;
        long prefixID;
        ChatbufProto.ChatMessage.ChatMessageType messageType = ChatbufProto.ChatMessage.ChatMessageType.FILE_DATA;

        private int workID;
        private FileTransport transport;
        private boolean isOver = false;

        private FileChrono(int workID, String screenName, String fileName, String chatRoom, Name hubPrefix,
                           Face face, KeyChain keyChain, Name certificateName, int sessionID, boolean requireVerification) {
            Log.i(TAG, "FileChrono: onCreate FileChrono");
            this.workID = workID;
            this.screenName = screenName;
            this.fileRoom = fileName;
            this.face = face;
            this.keyChain = keyChain;
            this.certificateName = certificateName;
            this.requireVerification = requireVerification;

            session = sessionID;
            this.userName = screenName;
            this.mNameAndSession = this.userName + session;
            identityName = new Name(hubPrefix).append(this.userName);
            chatPrefix = new Name(identityName).append(fileRoom).append(String.valueOf(session));
//            Name broadcast = new Name("/ndn/broadcast/ChronoFile").append(chatRoom);
            Name broadcast = new Name("/ndn/broadcast/ChronoChat").append(chatRoom).append(fileName);
//            Log.i(TAG, "FileChrono: broadcast = " + broadcast.toString());
            try {
                //TODO: ChronoFile prefix doesn't registered like MainActivity. now it already had been registered.
                sync = new ChronoSync2013(
                        this, this, chatPrefix, broadcast, session,
                        face, keyChain, certificateName, syncLifetime, RegisterFailed.onRegisterFailed);
            } catch (IOException | SecurityException e) {
                e.printStackTrace();
                return;
            }

            try {
                prefixID = face.registerPrefix(chatPrefix, this, RegisterFailed.onRegisterFailed);
//                Log.i(TAG, "FileChrono: chatPrefix = " + chatPrefix);
//                Log.i(TAG, "FileChrono: prefix id = " + prefixID);
            } catch (IOException | SecurityException e) {
                e.printStackTrace();
            }
        }

        private void messageCacheAppend() {
            messageCache.add(new CachedMessage(sync.getSequenceNo(), messageType,
                    "", System.currentTimeMillis()));
            while (messageCache.size() > MAX_MESSAGE_CACHE_LENGTH) {
                messageCache.remove(0);
            }
        }

        //发送兴趣包，告诉对方我要开始接受了
        private void beginDownload(FileTransport receiver) {
            transport = receiver;
        }

        private void beginSend(FileTransport sender) throws IOException, SecurityException {
            transport = sender;
            FileSenderTools senderTools = (FileSenderTools) transport;

            boolean isEnd = false;
            while (!isEnd) {
                senderTools.getData();
                isEnd = senderTools.isFinish();
                sync.publishNextSequenceNo();
                //这里我们什么都不装，等onInterest时再从raf中获取，内存会节省不少
                messageCacheAppend();
            }
            senderTools.setSequenceList((int) sync.getSequenceNo());
        }

        @Override
        public void onInitialized() {
            Interest interest = new Interest(new Name("/local/timeout"));
            interest.setInterestLifetimeMilliseconds(600000);
            try {
                face.expressInterest(interest, DummyOnData.onData);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            Log.i(TAG, "onInitialized: send join message?");
        }

        @Override
        public void onReceivedSyncState(List syncStates, boolean isRecovery) {

            long lastSequenceNo = -1L;
            final ArrayList<String> sendList = new ArrayList<>();
//            ArrayList<Long> sessionNoList = new ArrayList<>();
//            ArrayList<Long> sequenceNoList = new ArrayList<>();
            for (int i = 0; i < syncStates.size(); i++) {
                ChronoSync2013.SyncState syncState = (ChronoSync2013.SyncState) syncStates.get(i);
                Name nameComponents = new Name(syncState.getDataPrefix());
                if (nameComponents.get(-2).toEscapedString().equals(transport.state.getFileName())) {
                    try {
                        tempName = URLDecoder.decode(nameComponents.get(-3).toEscapedString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    long sessionNo = syncState.getSessionNo();
                    if (!tempName.equals(screenName)) {
                        int index = -1;
                        for (int j = 0; j < sendList.size(); j++) {
                            if ((sendList.get(j)).equals(syncState.getDataPrefix())) {
                                index = j;
                                break;
                            }
                        }
                        lastSequenceNo = syncState.getSequenceNo();
//                        if (index != -1) {
//                            sessionNoList.set(index, sessionNo);
//                            sequenceNoList.set(index, syncState.getSequenceNo());
//                        } else {
                        if (index == -1)
                            sendList.add(syncState.getDataPrefix());
//                            sessionNoList.add(sessionNo);
//                            sequenceNoList.add(syncState.getSequenceNo());
//                        }
                    }
                }
            }

            if (sendList.size() <= 0)
                return;

            final OnData onData = this;
            final OnTimeout onTimeout = this;
            ((FileReceiverTools) transport).setSlideWindow((int) lastSequenceNo,
                    new Name(sendList.get(0)),
                    new FileReceiverTools.OnSendInterest() {
                        @Override
                        public void send(Interest interest) {
                            interest.setInterestLifetimeMilliseconds(syncLifetime);
                            try {
                                face.expressInterest(interest, onData, onTimeout);
//                                Log.i(TAG, "Sent Interest : " + interest.getName());
                            } catch (IOException ex) {
                                Log.e(TAG, "Exception : " + ex + " when expressing Interest on Sync!");
                                //Logger.getLogger(DChronoChat.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
        }

        @Override
        public void onInterest(Name prefix, Interest interest, Face face, long interestFilterId, InterestFilter filter) {
            ChatbufProto.ChatMessage.Builder builder = ChatbufProto.ChatMessage.newBuilder(); //builder to build chatmessage instance
            long sequenceNo = Long.parseLong(interest.getName().get(chatPrefix.size()).toEscapedString());

            int interestType = FileTransportTools.isFileInterest(interest);
            switch (interestType) {
                case 1: {
                    boolean gotContent = false;
                    //Find the right chat message in messageCache
                    CachedMessage message = null;
                    for (int i = messageCache.size() - 1; i >= 0; --i) {
                        message = messageCache.get(i);
                        if (message.getSequenceNo() == sequenceNo) {
                            if (message.getMessageType().equals(ChatbufProto.ChatMessage.ChatMessageType.FILE_DATA)) {
                                builder.setFrom(screenName);
                                builder.setTo(chatRoom);
                                builder.setType(message.getMessageType());
//                                builder.setData(message.getMessage());
                                builder.setData(new String(((FileSenderTools) transport).getData((int) sequenceNo), StandardCharsets.ISO_8859_1));
                                builder.setTimestamp(message.getTime());
                                builder.setSequence(message.getSequenceNo());
                            }
                            gotContent = true;
                            break;
                        }
                    }
                    if (gotContent) {
                        ChatbufProto.ChatMessage content = builder.build();
                        byte[] array = content.toByteArray();
                        Data data = new Data(interest.getName());
                        data.setContent(new Blob(array, false));
                        int length = content.getData().length();
                        //TODO get progress 需要修改
                        float progress = ((FileSenderTools) transport).onProgress((int) message.getSequenceNo(), length);
                        if (progress > 0.f) {
//                            Log.i(TAG, "onInterest: work" + workID + " fresh progress " + progress);
                            onChangeData.onChangeData(transport.state.getPosition(), workID, progress);
                        }
                        try {
                            keyChain.sign(data, certificateName);
                        } catch (SecurityException ex) {
                            Log.e(TAG, "Security Exception : " + ex + " when signing Chat Data!");
                            return;
                        }
                        try {
                            face.putData(data);
//                            Log.i(TAG, "Sent Data : " + data.getName());
                        } catch (IOException ex) {
                            Log.e(TAG, "IOException : " + ex + " when putting Chat Data to Face!");
                            //Logger.getLogger(DChronoChat.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    break;
                }
                case 2: {
                    break;
                }
                case 3:
                    isOver = true;
                    transport.onOnePartFinish();
                    break;
            }
        }

        @Override
        public void onData(Interest interest, Data data) {
            ChatbufProto.ChatMessage content;
            //parse data into Chat Message
            try {
                content = ChatbufProto.ChatMessage.parseFrom(data.getContent().getImmutableArray());
            } catch (InvalidProtocolBufferException ex) {
                Log.e(TAG, "Exception : " + ex + " when parsing ChatMessage from Data!");
                return;
            }

            //using 180secs difference
            if (DChronoChat.getNowMilliseconds() - content.getTimestamp() * 1000.0 < 200000.0) {
                String name = content.getFrom(); //get sender's screenName
                String prefix = data.getName().getPrefix(-4).toUri(); //get sender's prefix
                long sessionNo = Long.parseLong(data.getName().get(-4).toEscapedString());
                long sequenceNo = Long.parseLong(data.getName().get(-3).toEscapedString());

                Interest timeout = new Interest(new Name("/local/timeout"));
                timeout.setInterestLifetimeMilliseconds(120000);
                try {
                    face.expressInterest
                            (timeout, DummyOnData.onData,
                                    this.new Alive(sequenceNo, name, sessionNo, prefix));
                } catch (IOException ex) {
                    Log.e(TAG, "IOException when onData!");
                    //Logger.getLogger(DChronoChat.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }

                // isRecoverySyncState_ was set by sendInterest.
                // TODO: If isRecoverySyncState_ changed, this assumes that we won't get data from an interest sent before it changed.
                if (content.getType().equals(ChatbufProto.ChatMessage.ChatMessageType.FILE_DATA) && sessionNo != session) {
//                    Log.i(TAG, "onData: work " + workID + " has ");
                    ((FileReceiverTools) transport).ReceiveFile((int) sequenceNo, content);
                    Log.i(TAG, "onData: fresh position is " + transport.state.getPosition());
                    onChangeData.onChangeData(transport.state.getPosition(), workID, transport.getProgress());
                    if (transport.isFinish()) {
                        Log.i(TAG, "onData: will stop");
                        transport.onOnePartFinish();
                        isOver = true;
                        Interest finishInterest = FileTransportTools.packInterest(FileTransportTools.END,
                                interest.getName(), transport.state.getFileName());
                        finishInterest.setInterestLifetimeMilliseconds(syncLifetime);
                        try {
                            face.expressInterest(finishInterest, sync, sync);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }

        @Override
        public void onTimeout(Interest interest) {
            Log.i(TAG, "onTimeout: " + interest.toUri());
        }

        /**
         * This is called after a timeout to check if the user with prefix has a newer
         * sequence number than the given temp_seq. If not, assume the user is idle and
         * remove from the roster and print a leave message.
         * This is used as the onTimeout for Face.expressInterest.
         */
        private class Alive implements OnTimeout {
            private final long tempSequenceNo;
            private final String name;
            private final long sessionNo;
            private final String prefix;

            Alive(long tempSequenceNo, String name, long sessionNo, String prefix) {
                this.tempSequenceNo = tempSequenceNo;
                this.name = name;
                this.sessionNo = sessionNo;
                this.prefix = prefix;
            }

            public final void
            onTimeout(Interest interest) {
//                long sequenceNo = sync.getProducerSequenceNo(prefix, sessionNo);
//                String nameAndSession = name + sessionNo;
//                int n = roster.indexOf(nameAndSession);
//                if (sequenceNo != -1 && n >= 0) {
//                    if (tempSequenceNo == sequenceNo) {
//                        roster.remove(n);
//                        Log.i(TAG, name + ": Leave");
//                    }
//                }

//                for (int i = 0; i < mSequence.size(); ++i) {
//                    HashMap<String, String> map = mSequence.get(i);
//                    if (map.containsKey(nameAndSession) && map.get(nameAndSession).equals(sequenceNo)) {
//                        mSequence.remove(i);
//                        //sendDebugMsg("  remove the sequence_ item: " + map.toString());
//                        break;
//                    }
//                }
            }
        }
    }

    static class RegisterFailed implements OnRegisterFailed {
        @Override
        public void onRegisterFailed(Name prefix) {
            Log.e(TAG, "onRegisterFailed: Register failed for prefix:" + prefix);
        }

        final static OnRegisterFailed onRegisterFailed = new RegisterFailed();
    }

    static class DummyOnData implements OnData {

        @Override
        public void onData(Interest interest, Data data) {
            Log.i(TAG, "onData: DummyOnData");
        }

        final static OnData onData = new DummyOnData();
    }

    static class WaitThreeSeconds extends AsyncTask<Void, Void, String> {

        private FileWorker worker;

        WaitThreeSeconds(FileWorker worker) {
            this.worker = worker;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                Thread.sleep(1000);
                Log.i(TAG, "doInBackground: stop " + worker.workID);
                worker.stopWorker();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "success";
        }

    }
}
