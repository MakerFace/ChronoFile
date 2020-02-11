package id.ac.ui.clab.dchronochat.chat;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.ac.ui.clab.dchronochat.R;
import id.ac.ui.clab.dchronochat.file.Entity;
import id.ac.ui.clab.dchronochat.utils.Tools;

/**
 * Created by yudiandreanp on 25/05/16.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {
    private LayoutInflater inflater;
    private int count = -1;
    private OnItemClickListener mOnItemClickListener;
    private List<Entity> mMessageList;

    ChatAdapter(Context context, List<Entity> messageList) {
        this.inflater = LayoutInflater.from(context);
        this.mMessageList = messageList;
    }

    @Override
    public int getItemViewType(int pos) {
        if (mMessageList.get(pos).getType().equals(ChatbufProto.ChatMessage.ChatMessageType.FILE_INFO)) {
            return ITEM_TYPE.ITEM_FILE.ordinal();
        }
        return ITEM_TYPE.ITEM_CHAT.ordinal();
    }

    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int pos) {
        ++count;
        ChatHolder holder = null;
        if (pos == ITEM_TYPE.ITEM_CHAT.ordinal()) {
            View chatView = inflater.inflate(R.layout.list_item_chat, parent, false);
            holder = new ChatHolder(chatView);
        } else if (pos == ITEM_TYPE.ITEM_FILE.ordinal()) {
            View fileView = inflater.inflate(R.layout.list_item_file, parent, false);
            holder = new FileHolder(fileView);
            final FileHolder _holder = (FileHolder) holder;
            if (mOnItemClickListener != null) {
//                Log.i("ChatAdapter", "set click listener");
                holder.itemView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onClick(_holder.getFileName());
                    }
                });
            }
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(final ChatHolder holder, final int pos) {
        Entity chat = mMessageList.get(pos);
        holder.bindData(chat);
    }

    @Override
    public int getItemCount() {
        return mMessageList == null ? 0 : mMessageList.size();
    }

    public enum ITEM_TYPE {
        ITEM_CHAT,
        ITEM_FILE,
    }

    void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(String fileName);
    }

    public interface OnChangeData {
        void onAddData();

        void onChangeData(int pos, int index, float... pro);
    }

    static class ChatHolder extends RecyclerView.ViewHolder {
        // Views
//        private View mUserPresence;
        TextView mScreenName;
        TextView mChatTime;
        //        private TextView mChatLatency;
        TextView mMessage;
        ChatbufProto.ChatMessage mChatMessage;

        ChatHolder(View itemView) {
            super(itemView);

            // Get references to image and name.
//            mUserPresence = (View) itemView.findViewById(R.id.user_presence);
            mScreenName = itemView.findViewById(R.id.screenName);
            mChatTime = itemView.findViewById(R.id.chat_time);
            //mChatLatency = (TextView) itemView.findViewById(R.id.chat_latency);
            mMessage = itemView.findViewById(R.id.chat_message);
        }

        void bindData(Entity entity) {
            mChatMessage = entity.getMessage();
            mScreenName.setText(mChatMessage.getFrom());
            Date date = new Date(mChatMessage.getTimestamp());
            DateFormat formatter = SimpleDateFormat.getTimeInstance();//new SimpleDateFormat("dd/mm hh:mm:ss");
            String dateFormatted = formatter.format(date);
            mChatTime.setText(dateFormatted);
            //mChatLatency.setText(String.valueOf((int)Math.round(System.currentTimeMillis()/1000.0) - chatMessage.getTimestamp()) + "ms");
            mMessage.setText(mChatMessage.getData());
        }

    }

    static class FileHolder extends ChatHolder {
        private NumberProgressBar mProgressBar;
        Resources resources;
        private TextView mFileSizeText;
        private ImageView mImage;
        private TextView mTotalTime;
        private TextView mSpeedText;
        private View mDResultView;
        private View mSResultView;

        FileHolder(View itemView) {
            super(itemView);
            mFileSizeText = itemView.findViewById(R.id.file_size_text);
            mProgressBar = itemView.findViewById(R.id.progressBar);
            mTotalTime = itemView.findViewById(R.id.download_time_text);
            mSpeedText = itemView.findViewById(R.id.speed_text);
            mDResultView = itemView.findViewById(R.id.download_result_text);
            mSResultView = itemView.findViewById(R.id.send_result_contain);
            mImage = itemView.findViewById(R.id.fileImage);
            resources = itemView.getResources();
            mProgressBar.setMax(100);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void bindData(Entity entity) {
            super.bindData(entity);
            String[] strings = entity.getData().split("/");
            mMessage.setText(strings[0]);
            mFileSizeText.setText(Tools.convertB2KB(strings[1]).concat("KB"));
            File file = new File(strings[0]);
            String type = ChatListFragment.getMimeType(file).split("/")[0];
            switch (type) {
                case "image":
                    mImage.setImageResource(R.mipmap.image);
                    break;
                case "audio":
                    mImage.setImageResource(R.mipmap.music);
                    break;
                case "txt":
                    mImage.setImageResource(R.mipmap.txt);
                    break;
                default:
                    mImage.setImageResource(R.mipmap.other);
                    break;
            }
            setProgress(entity.getProgress());
            if (entity.isFinish()) {
                setSpeed(entity.getTime(), entity.getSpeed(), entity.isSender());
            }
        }

        void setProgress(int pro) {
            mProgressBar.setProgress(pro);
            if (pro >= mProgressBar.getMax()) {
                hideProgress();
            }
        }

        void setSpeed(float time, float speed, boolean isSender) {
            mProgressBar.setVisibility(View.GONE);
            if (isSender) {
                mSResultView.setVisibility(View.VISIBLE);
            } else {
                mDResultView.setVisibility(View.VISIBLE);
                mTotalTime.setText(String.format(Locale.getDefault(), "%.1f", time));
                mSpeedText.setText(String.format(Locale.getDefault(), "%.2f", speed));
            }
        }

        private void hideProgress() {
            mProgressBar.setVisibility(View.GONE);
        }

        String getFileName() {
            return mMessage.getText().toString();
        }
    }
}

