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
        initLaunchers(activity);
    }

    public LuaScriptGrabber(@NonNull Fragment fragment) {
        this.contextRef = new WeakReference<>(fragment.getContext());
        initLaunchers(fragment);
    }

    private void initLaunchers(FragmentActivity activity) {
        this.filePickerLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handleFileResult
        );
    }

    private void initLaunchers(Fragment fragment) {
        this.filePickerLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handleFileResult
        );
    }

    /**
     * Запускает диалог выбора файлов без фильтров.
     */
    public void startChoose(LuaScriptResultCallback callback) {
        this.callback = callback;
        Context context = contextRef.get();
        if (context == null) {
            callback.onError("Context is not available");
            return;
        }
        // Запуск выбора с универсальным фильтром "*/*", показывающим все файлы.
        filePickerLauncher.launch("*/*");
    }

    /**
     * Обработка выбранного файла.
     */
    private void handleFileResult(Uri uri) {
        Context context = contextRef.get();
        if (context == null || uri == null) {
            notifyError("Invalid context or file URI");
            return;
        }

        try {
            String fileName = getFileName(context, uri);
            if (fileName == null || !(fileName.endsWith(".lua") || fileName.endsWith(".txt"))) {
                notifyError("Неверное расширение файла. Допустимы только .lua или .txt");
                return;
            }
            String content = readFileContent(context, uri);
            LuaScript script = new LuaScript(fileName, content, null);
            callback.onScriptLoaded(script);
        } catch (Exception e) {
            notifyError("Error reading file: " + e.getMessage());
        }
    }

    private String getFileName(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                return nameIndex != -1 ? cursor.getString(nameIndex) : "Unnamed file";
            }
        }
        return "Unnamed file";
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
