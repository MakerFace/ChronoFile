package id.ac.ui.clab.dchronochat.resources.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import id.ac.ui.clab.dchronochat.R;
import id.ac.ui.clab.dchronochat.resources.entity.Video;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private List<Video> videoList;
    private Context context;

    public VideoAdapter(List<Video> videoList, Context context) {
        this.videoList = videoList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Video video = videoList.get(position);
        holder.iconImage.setImageBitmap(video.getVideoIcon());
        holder.nameText.setText(video.getVideoName());
        holder.timeText.setText(video.getVideoTime());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.click();
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView iconImage;
        private AppCompatTextView nameText;
        private AppCompatTextView timeText;
        private AppCompatCheckBox checkBox;
        private boolean isChecked = false;

        public ViewHolder(View itemView) {
            super(itemView);
            iconImage = (AppCompatImageView) itemView.findViewById(R.id.video_image);
            nameText = (AppCompatTextView) itemView.findViewById(R.id.video_name);
            timeText = (AppCompatTextView) itemView.findViewById(R.id.video_time);
            checkBox = (AppCompatCheckBox) itemView.findViewById(R.id.video_is_checked);
        }

        public void click() {
            isChecked = !isChecked;
            checkBox.setChecked(isChecked);
            if (isChecked)
                checkBox.setVisibility(View.VISIBLE);
            else
                checkBox.setVisibility(View.INVISIBLE);
        }
    }
}
