package edu.ucsd.cse110.sharednotes.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import edu.ucsd.cse110.sharednotes.model.Note;
import edu.ucsd.cse110.sharednotes.model.NoteDatabase;
import edu.ucsd.cse110.sharednotes.model.NoteRepository;

public class ListViewModel extends AndroidViewModel {
    private LiveData<List<Note>> notes;
    private final NoteRepository repo;

    public ListViewModel(@NonNull Application application) {
        super(application);
        var context = application.getApplicationContext();
        var db = NoteDatabase.provide(context);
        var dao = db.getDao();
        this.repo = new NoteRepository(dao);
    }

    /**
     * Load all notes from the database.
     * @return a LiveData object that will be updated when any notes change.
     */
    public LiveData<List<Note>> getNotes() {
        if (notes == null) {
            notes = repo.getAllLocal();
        }
        return notes;
    }

    /**
     * Open a note in the database. If the note does not exist, create it.
     * @param title the title of the note
     * @return a LiveData object that will be updated when this note changes.
     */
    public LiveData<Note> getOrCreateNote(String title) {
        if (!repo.existsLocal(title)) {
            var note = new Note(title, "");
            repo.upsertLocal(note);
        }

        return repo.getLocal(title);
    }

    public void delete(Note note) {
        repo.deleteLocal(note);
    }
}