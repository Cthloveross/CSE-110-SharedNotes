package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteAPI {
    private static final String BASE_URL = "https://sharednotes.goto.ucsd.edu/";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private volatile static NoteAPI instance = null;

    private OkHttpClient client;

    private Gson gson;

    public NoteAPI() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }

    /**
     * Sends a GET request to the server to retrieve the note with the specified title.
     *
     * @param title the title of the note to retrieve
     * @return the note object returned by the server, or null if the note does not exist
     * @throws IOException if there was an error sending the request or parsing the response
     */
    public Note getNote(String title) {
        var url = BASE_URL + "notes/" + title;
        var request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try {
            var response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                var responseBody = response.body();
                if (responseBody != null) {
                    return gson.fromJson(responseBody.string(), Note.class);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Sends a PUT request to the server to update or create the note with the specified title.
     *
     * @param note the note object to update or create
     * @throws IOException if there was an error sending the request or parsing the response
     */
    public void putNote(Note note) {
        try {
            var url = BASE_URL + "notes/" + note.title;
            var json = gson.toJson(note);
            var requestBody = RequestBody.create(json, JSON);
            var request = new Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .build();

            try (var response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("Error updating note " + note.title);
                }
            }
        } catch (IOException e) {
            System.out.println("Error updating note " + note.title);
            e.printStackTrace();
        }
    }

    public Future<Boolean> putAsync(Note note) {
        var executor = Executors.newSingleThreadExecutor();
        return (Future<Boolean>) executor.submit(() -> putNote(note));
    }



    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     */
    public void echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        msg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + msg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
