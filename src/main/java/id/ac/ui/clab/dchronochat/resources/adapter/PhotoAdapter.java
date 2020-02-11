package id.ac.ui.clab.dchronochat.resources.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import java.util.List;

import id.ac.ui.clab.dchronochat.R;
import id.ac.ui.clab.dchronochat.resources.entity.Photo;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private Context context;
    private List<Photo> photoList;

    public PhotoAdapter(Context context, List<Photo> photoList) {
        this.context = context;
        this.photoList = photoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.photo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Photo photo = photoList.get(position);
        holder.photoImage.setImageBitmap(photo.getIcon());
        holder.photoText.setText(photo.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.click();
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView photoImage;
        private AppCompatTextView photoText;
        private CheckBox checkBox;
        private boolean isCheck = false;

        private ViewHolder(View itemView) {
            super(itemView);
            photoImage = (AppCompatImageView) itemView.findViewById(R.id.photo_img);
            photoText = (AppCompatTextView) itemView.findViewById(R.id.photo_name);
            checkBox = (CheckBox) itemView.findViewById(R.id.photo_is_checked);
        }

        public void click() {
            isCheck = !isCheck;
            checkBox.setChecked(isCheck);
            if (isCheck)
                checkBox.setVisibility(View.VISIBLE);
            else
                checkBox.setVisibility(View.INVISIBLE);
        }
    }
}
