package edu.ucsd.cse110.sharednotes.model;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteRepository {
    private final NoteDao dao;
    private OkHttpClient client;
    NoteAPI noteAPI = NoteAPI.provide();
    public NoteRepository(NoteDao dao) {
        this.dao = dao;
        this.client = new OkHttpClient();
    }

    // Synced Methods
    // ==============

    /**
     * This is where the magic happens. This method will return a LiveData object that will be
     * updated when the note is updated either locally or remotely on the server. Our activities
     * however will only need to observe this one LiveData object, and don't need to care where
     * it comes from!
     * <p>
     * This method will always prefer the newest version of the note.
     *
     * @param title the title of the note
     * @return a LiveData object that will be updated when the note is updated locally or remotely.
     */

    public LiveData<Note> getSynced(String title){
        var note = new MediatorLiveData<Note>();

        Observer<Note> updateFromRemote = theirNote -> {
            var ourNote = note.getValue();
            if(theirNote == null)return;
            if (ourNote == null || ourNote.updatedAt < theirNote.updatedAt) {
                upsertLocal(theirNote);
            }
        };

        // If we get a local update, pass it on.
        note.addSource(getLocal(title), note::postValue);
        // If we get a remote update, update the local version (triggering the above observer)
        note.addSource(getRemote(title), updateFromRemote);

        return note;
    }

    public void upsertSynced(Note note) {
        upsertLocal(note);
        upsertRemote(note);
    }

    // Local Methods
    // =============

    public LiveData<Note> getLocal(String title) {
        return dao.get(title);
    }

    public LiveData<List<Note>> getAllLocal() {
        return dao.getAll();

    }

    public void upsertLocal(Note note) {
        note.updatedAt = System.currentTimeMillis();
        dao.upsert(note);
    }

    public void deleteLocal(Note note) {
        dao.delete(note);
    }

    public boolean existsLocal(String title) {
        return dao.exists(title);
    }

    // Remote Methods
    // ==============


//         TODO: Implement getRemote!
//         TODO: Set up polling background thread (MutableLiveData?)
//         TODO: Refer to TimerService from https://github.com/DylanLukes/CSE-110-WI23-Demo5-V2.
public LiveData<Note> getRemote(String title) {
    MutableLiveData<Note> liveData = new MutableLiveData<>();
    NoteAPI noteAPI = NoteAPI.provide();
    Note note = noteAPI.getNote(title);

    if (note != null) {
        liveData.setValue(note);
    } else {
        liveData.setValue(null);
    }

    final Handler handler = new Handler(Looper.getMainLooper());
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Note note = noteAPI.getNote(title);
            liveData.postValue(note);
        }
    };

    TimeService.singleton().getTimeData().observeForever(time -> {
        if (time % 3000 == 0) {
            handler.post(runnable);
        }
    });

    return liveData;
}

    public void upsertRemote(Note note) {

        noteAPI.putNote(note);
    }
}
