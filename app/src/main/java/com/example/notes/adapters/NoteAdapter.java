package com.example.notes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notes.R;
import com.example.notes.entities.Note;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes;

    public NoteAdapter(List<Note> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_container_note,
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle, tvSubtitle, tvDateTime;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            viewMapping();
        }

        private void viewMapping() {
            tvTitle = itemView.findViewById(R.id.item_container_note_tvTitle);
            tvSubtitle = itemView.findViewById(R.id.item_container_note_tvSubtitle);
            tvDateTime = itemView.findViewById(R.id.item_container_note_tvDateTime);
        }

        void setNote(Note note) {
            tvTitle.setText(note.getTitle());
            if (note.getSubtitle().toString().trim().isEmpty()) {
                tvSubtitle.setVisibility(View.GONE);
            } else {
                tvSubtitle.setText(note.getSubtitle());
            }
            tvDateTime.setText(note.getDateTime());
        }
    }
}
