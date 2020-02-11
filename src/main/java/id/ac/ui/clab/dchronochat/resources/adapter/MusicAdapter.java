package id.ac.ui.clab.dchronochat.resources.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import id.ac.ui.clab.dchronochat.R;
import id.ac.ui.clab.dchronochat.resources.entity.Music;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    private static final String TAG = "MusicAdapter";
    private Context context;
    private List<Music> musicList;

    public MusicAdapter(Context context, List<Music> musicList) {
        this.context = context;
        this.musicList = musicList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Music music = musicList.get(position);
        holder.nameText.setText(music.getName());
        holder.lengthText.setText(String.valueOf(music.getLength()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.click();
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private TextView lengthText;
        private CheckBox checkBox;
        private boolean isChecked = false;

        private ViewHolder(View itemView) {
            super(itemView);
            Log.i(TAG, "ViewHolder: create");
            nameText = (TextView) itemView.findViewById(R.id.music_name);
            lengthText = (TextView) itemView.findViewById(R.id.music_length);
            checkBox = (CheckBox) itemView.findViewById(R.id.music_is_checked);
        }

        private void click() {
            isChecked = !isChecked;
            Log.i(TAG, "click: item click" + isChecked);
            checkBox.setChecked(isChecked);
        }
    }
}
