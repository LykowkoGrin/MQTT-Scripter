package com.example.mqttscripter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

public class LuaScriptGrabber {
    public interface LuaScriptResultCallback {
        void onScriptLoaded(LuaScript script);
        void onError(String message);
    }

    private final WeakReference<Context> contextRef;
    private ActivityResultLauncher<String> filePickerLauncher;
    private LuaScriptResultCallback callback;

    public LuaScriptGrabber(@NonNull FragmentActivity activity) {
        this.contextRef = new WeakReference<>(activity);
        this.filePickerLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handleFileResult
        );
    }


    public LuaScriptGrabber(@NonNull Fragment fragment) {
        this.contextRef = new WeakReference<>(fragment.getContext());
        try{
            this.filePickerLauncher = fragment.registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    this::handleFileResult
            );
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void startChoose(LuaScriptResultCallback callback) {
        this.callback = callback;
        Context context = contextRef.get();
        if (context == null) {
            callback.onError("Context is not available");
            return;
        }

        filePickerLauncher.launch("application/x-lua");
    }

    private void handleFileResult(Uri uri) {
        Context context = contextRef.get();
        if (context == null || uri == null) {
            notifyError("Invalid context or file URI");
            return;
        }

        try {
            String fileName = getFileName(context, uri);
            String content = readFileContent(context, uri);

            if (fileName != null && fileName.endsWith(".lua")) {
                LuaScript script = new LuaScript(fileName, content, null);
                callback.onScriptLoaded(script);
            } else {
                notifyError("Invalid Lua file");
            }
        } catch (Exception e) {
            notifyError("Error reading file: " + e.getMessage());
        }
    }

    private String getFileName(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                return nameIndex != -1 ? cursor.getString(nameIndex) : null;
            }
        }
        return null;
    }

    private String readFileContent(Context context, Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    private void notifyError(String message) {
        if (callback != null) {
            callback.onError(message);
        }
    }
}