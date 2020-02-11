package id.ac.ui.clab.dchronochat.folder;


import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import id.ac.ui.clab.dchronochat.chat.ChatListFragment;
import id.ac.ui.clab.dchronochat.R;

/**
 * Created by LittleBoy on 2018/5/10.
 */

public class SelectFolder extends DialogFragment {

    private ListView mListView;
    private FolderAdapter mFolderAdapter;
    private EditText editFileName;
    private String defaultPath;
    private String defaultName;
    private ArrayList<File> fileList = new ArrayList<>();
    private File fileNow;
    private ChatListFragment.OnClickEnter onClickEnter;

    public final static int RESULT_CODE = 5;

    public void setDefaultName(String fileName) {
        defaultName = fileName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.select_folder, container);
        try {
            defaultPath = Environment.getExternalStorageDirectory().getCanonicalPath() + "/";
        } catch (IOException e) {
            e.printStackTrace();
        }

        Button mReturn = (Button) view.findViewById(R.id.Return);
        mReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Return(v);
            }
        });

        Button mCancel = (Button) view.findViewById(R.id.Cancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cancel(v);
            }
        });

        Button mEnter = (Button) view.findViewById(R.id.Enter);
        mEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Enter(v);
            }
        });

        editFileName = (EditText) view.findViewById(R.id.editFileName);
        editFileName.setText(defaultName);

        mListView = (ListView) view.findViewById(R.id.FileList);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (fileList.get(position).isDirectory()) {
                    fileNow = fileList.get(position);
                    refreshFileList();
                } else {
                    EditText editFileName = (EditText) view.findViewById(R.id.editFileName);
                    editFileName.setText(fileList.get(position).getName());
                }
            }
        });

        mFolderAdapter = new FolderAdapter(inflater);
        mListView.setAdapter(mFolderAdapter);

        fileNow = new File(defaultPath);
        refreshFileList();

        return view;
    }

    public void refreshFileList() {
        fileList.clear();
        File[] tempFile = fileNow.listFiles();
        if (tempFile != null) {
            for (int i = 0; i < tempFile.length; ++i) {
                if (tempFile[i].isDirectory()) {
                    fileList.add(tempFile[i]);
                }
            }
            List<String> tempString = new ArrayList<>();
            for (int i = 0; i < fileList.size(); ++i) {
                tempString.add(fileList.get(i).getName());
            }
            mFolderAdapter.refreshList(tempString);
        } else {
            Toast.makeText(this.getContext(), "权限不够！", Toast.LENGTH_SHORT).show();
            if (fileNow.getParentFile() != null) {
                fileNow = fileNow.getParentFile();
            } else {
                fileNow = new File(defaultPath);
            }
            refreshFileList();
        }
    }

    public void Return(View view) {
        if (fileNow.getParentFile() != null) {
            fileNow = fileNow.getParentFile();
            refreshFileList();
        }
    }

    public void Cancel(View view) {
        dismiss();
    }

    public void Enter(View view) {
        onClickEnter.onEnter(fileNow.getAbsolutePath(), editFileName.getText().toString());
        dismiss();
    }

    public ChatListFragment.OnClickEnter getOnClickEnter() {
        return onClickEnter;
    }

    public void setOnClickEnter(ChatListFragment.OnClickEnter onClickEnter) {
        this.onClickEnter = onClickEnter;
    }
}

