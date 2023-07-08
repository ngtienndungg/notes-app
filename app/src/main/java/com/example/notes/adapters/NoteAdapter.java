package com.example.notes.adapters;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notes.R;
import com.example.notes.entities.Note;
import com.example.notes.listeners.NoteListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final NoteListener noteListener;
    private final List<Note> noteSource;
    private List<Note> notes;
    private Timer timer;

    public NoteAdapter(List<Note> notes, NoteListener noteListener) {
        this.notes = notes;
        this.noteListener = noteListener;
        noteSource = notes;
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
        holder.llNote.setOnClickListener(v -> noteListener.onNoteClicked(notes.get(holder.getAdapterPosition()), holder.getAdapterPosition()));
        holder.llNote.setOnLongClickListener(v -> {
            noteListener.onNoteLongClicked(notes.get(holder.getAdapterPosition()), holder.getAdapterPosition());
            return false;
        });
        holder.ivMore.setOnClickListener(v -> noteListener.onMoreClicked(notes.get(holder.getAdapterPosition()), holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void searchNote(final String searchKeyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()) {
                    notes = noteSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : noteSource) {
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase()) || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase()) || note.getNoteContent().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }

                new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle, tvSubtitle, tvDateTime;
        private LinearLayout llNote;
        private RoundedImageView rivNoteImage;
        private ImageView ivMore;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            viewMapping();
        }

        private void viewMapping() {
            tvTitle = itemView.findViewById(R.id.item_container_note_tvTitle);
            tvSubtitle = itemView.findViewById(R.id.item_container_note_tvSubtitle);
            tvDateTime = itemView.findViewById(R.id.item_container_note_tvDateTime);
            llNote = itemView.findViewById(R.id.item_container_note_llNote);
            rivNoteImage = itemView.findViewById(R.id.item_container_note_rivNoteImage);
            ivMore = itemView.findViewById(R.id.item_container_note_ivMore);
        }

        private void setNote(Note note) {
            tvTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty()) {
                tvSubtitle.setVisibility(View.GONE);
            } else {
                tvSubtitle.setText(note.getSubtitle());
            }
            tvDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a", Locale.getDefault()).format(note.getDateTime()));

            GradientDrawable gradientDrawable = (GradientDrawable) llNote.getBackground();
            if (note.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if (note.getImagePath() != null) {
                rivNoteImage.setVisibility(View.VISIBLE);
                rivNoteImage.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
            } else {
                rivNoteImage.setVisibility(View.GONE);
            }
        }
    }
}
