package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.google.gson.Gson;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // TODO: Read the docs: https://square.github.io/okhttp/
    // TODO: Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;

    private OkHttpClient client;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final Gson gson;
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
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     *
     * This method should can be called on a background thread (Android
     * disallows network requests on the main thread).
     */
    @WorkerThread
    public String echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        String encodedMsg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + encodedMsg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @AnyThread
    public Future<String> echoAsync(String msg) {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> echo(msg));

        // We can use future.get(1, SECONDS) to wait for the result.
        return future;
    }
    /**
     * Sends a PUT request to the server to update or create the note.
     *
     * @param note the note to be updated or created
     * @return true if the update was successful, false otherwise
     */
    public boolean put(Note note) {
        var json = gson.toJson(note);

        var body = RequestBody.create(json, JSON);
        String temp = note.title.replace(" ", "%20");
        var request = new Request.Builder()
                .header("Content-Type", "application/json")
                .url("https://sharednotes.goto.ucsd.edu/notes/" + temp)//URLEncoder.encode(temp, "UTF-8"))
                .method("PUT", body)
                .build();

        try (var response = client.newCall(request).execute()) {
            int code = response.code();
            String message = response.message();
            System.out.println("Response code: " + code);
            System.out.println("Response message: " + message);
            return response.isSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("fail");
            return false;
        }

    }

    /**
     * Sends a GET request to the server to retrieve the note with the given title.
     *
     * @param title the title of the note to retrieve
     * @return the retrieved note, or null if the retrieval was unsuccessful
     */
    public Note get(String title) {
        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .get()
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            return gson.fromJson(body, Note.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}