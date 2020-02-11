package id.ac.ui.clab.dchronochat.resources.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import id.ac.ui.clab.dchronochat.R;
import id.ac.ui.clab.dchronochat.resources.entity.Doc;

public class DocAdapter extends RecyclerView.Adapter<DocAdapter.ViewHolder> {

    private List<Doc> docs;
    private Context context;

    public DocAdapter(Context context, List<Doc> docs) {
        this.context = context;
        this.docs = docs;
    }

    @Override
    public DocAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.doc_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Doc doc = docs.get(position);
        holder.nameText.setText(doc.getName());
        holder.lengthText.setText(String.valueOf(doc.getLength()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.click();
            }
        });
    }

    @Override
    public int getItemCount() {
        return docs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView nameText;
        private AppCompatTextView lengthText;
        private AppCompatCheckBox checkBox;
        private boolean isCheck = false;

        private ViewHolder(View itemView) {
            super(itemView);
            nameText = (AppCompatTextView) itemView.findViewById(R.id.doc_name);
            lengthText = (AppCompatTextView) itemView.findViewById(R.id.doc_length);
            checkBox = (AppCompatCheckBox) itemView.findViewById(R.id.doc_is_checked);
        }

        public void click() {
            isCheck = !isCheck;
            checkBox.setChecked(isCheck);
        }
    }
}
