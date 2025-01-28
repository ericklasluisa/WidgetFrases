package com.example.widget_frases;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import java.io.IOException;

import android.appwidget.AppWidgetProvider;

public class WidgetProvider extends AppWidgetProvider {
    private static final String API_URL = "https://api.quotable.io/random";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            fetchQuote(context, appWidgetManager, appWidgetId);
        }
    }

    private void fetchQuote(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        // Ejecutar la solicitud en un hilo de fondo
        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    String quote = jsonObject.getString("content");

                    // Log para verificar que la respuesta es correcta
                    Log.d("MotivationalWidget", "Frase obtenida: " + quote);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                        views.setTextViewText(R.id.widget_text, quote);

                        // Log para confirmar que el widget se está actualizando
                        Log.d("MotivationalWidget", "Widget actualizado con la frase: " + quote);

                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    });
                } else {
                    Log.e("MotivationalWidget", "Error en la respuesta de la API");
                }
            } catch (IOException | org.json.JSONException e) {
                e.printStackTrace();
                Log.e("MotivationalWidget", "Error al procesar la respuesta: " + e.getMessage());
            }
        }).start();
    }
}