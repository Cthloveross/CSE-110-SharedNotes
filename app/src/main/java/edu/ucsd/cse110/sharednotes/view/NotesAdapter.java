package edu.ucsd.cse110.sharednotes.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import edu.ucsd.cse110.sharednotes.R;
import edu.ucsd.cse110.sharednotes.model.Note;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {
    private List<Note> notes = Collections.emptyList();
    private Consumer<Note> onNoteClicked;
    private Consumer<Note> onNoteDeleteClicked;

    public void setOnNoteClickListener(Consumer<Note> onNoteClicked) {
        this.onNoteClicked = onNoteClicked;
    }

    public void setOnNoteDeleteClickListener(Consumer<Note> onNoteDeleteClicked) {
        this.onNoteDeleteClicked = onNoteDeleteClicked;
    }

    /**
     * This time around, the ViewHolder is much simpler, just data.
     * This is closer to "modern" Kotlin Android conventions.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View itemView;
        public final TextView nameView;
        public final TextView previewView;
        public final View deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            // Populate the text views...
            this.nameView = itemView.findViewById(R.id.note_item_title);
            this.previewView = itemView.findViewById(R.id.note_item_preview);
            this.deleteButton = itemView.findViewById(R.id.note_item_delete);
        }

        public void bind(Note note) {
            nameView.setText(note.title);
            previewView.setText(note.content);
            itemView.setOnClickListener(v -> onNoteClicked.accept(note));
            deleteButton.setOnClickListener(v -> onNoteDeleteClicked.accept(note));
        }
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        var view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        var note = notes.get(position);
        holder.bind(note);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public long getItemId(int position) {
        // We don't actually have a unique int/long ID on the Note object, so instead
        // we generate a unique ID based on the title. It is possible that two notes
        // could have different titles but the same hash code, but it is beyond unlikely.
        return notes.get(position).title.hashCode();
    }
}