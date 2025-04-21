package com.example.devmob;

import android.os.Handler;
import android.os.Looper;
import com.google.gson.*;

import java.io.IOException;

import okhttp3.*;

public class AIRequest {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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 "; // put your real key here

    public interface SuggestionCallback {
        void onSuccess(String suggestion);
        void onFailure(String error);
    }

    public static void getSmartSuggestion(String taskPrompt, SuggestionCallback callback) {
        OkHttpClient client = new OkHttpClient();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "You are a productivity assistant that helps users prioritize their tasks.");

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", taskPrompt);

        JsonArray messages = new JsonArray();
        messages.add(systemMessage);
        messages.add(userMessage);

        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.addProperty("model", "mistralai/mistral-7b-instruct");
        requestBodyJson.add("messages", messages);

        RequestBody requestBody = RequestBody.create(
                requestBodyJson.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Erreur: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String error = response.body() != null ? response.body().string() : "Unknown error";
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Erreur API: " + error));
                    return;
                }

                String responseStr = response.body().string();
                try {
                    JsonObject json = JsonParser.parseString(responseStr).getAsJsonObject();
                    String suggestion = json.getAsJsonArray("choices")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content").getAsString();

                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(suggestion));
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Erreur parsing: " + e.getMessage()));
                }
            }
        });
    }
}
