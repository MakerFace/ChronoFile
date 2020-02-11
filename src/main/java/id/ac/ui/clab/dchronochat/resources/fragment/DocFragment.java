package id.ac.ui.clab.dchronochat.resources.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import id.ac.ui.clab.dchronochat.R;
import id.ac.ui.clab.dchronochat.resources.adapter.DocAdapter;
import id.ac.ui.clab.dchronochat.resources.entity.Doc;

public class DocFragment extends Fragment {

    private List<Doc> docList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.view_pager_doc, container, false);
        initDocs();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.doc_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new DocAdapter(getContext(), docList));
        recyclerView.addItemDecoration(new MyDecoration(getContext(),
                MyDecoration.VERTICAL_LIST));
        return view;
    }

    private void initDocs() {
        docList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            docList.add(new Doc.Builder().name("document.doc").length(100).build());
        }
    }
}
