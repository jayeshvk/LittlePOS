package com.evinyas.jkotekar.littlepos;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.evinyas.jkotekar.littlepos.model.BackupFile;

import java.util.List;

public class FileRecyclerViewAdapter extends RecyclerView.Adapter<FileRecyclerViewAdapter.MyViewHolder> {

    private List<BackupFile> fileList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, year, genre;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.filename);
            genre = view.findViewById(R.id.date);
            year = view.findViewById(R.id.key);
        }

    }


    public FileRecyclerViewAdapter(List<BackupFile> backupFileList) {
        this.fileList = backupFileList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.filename_rcview_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        BackupFile backupFile = fileList.get(position);
        holder.title.setText(backupFile.getFileName());
        holder.genre.setText(backupFile.getDate());
        holder.year.setText(backupFile.getKey());
    }


    @Override
    public int getItemCount() {
        return fileList.size();
    }
}