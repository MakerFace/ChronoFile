package id.ac.ui.clab.dchronochat.folder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import id.ac.ui.clab.dchronochat.R;

/**
 * Created by LittleBoy on 2018/5/11.
 */

public class FolderAdapter extends BaseAdapter {

    private List<String> folderName = new ArrayList<>();
    private LayoutInflater inflater;

    public FolderAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    public void refreshList(List<String> list) {
        folderName = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return folderName == null ? 0 : folderName.size();
    }

    @Override
    public String getItem(int position) {
        return folderName.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_item_folder, null);
        String folder = getItem(position);
        TextView folderText = (TextView) view.findViewById(R.id.folderName);
        folderText.setText(folder);
        return view;
    }
}
