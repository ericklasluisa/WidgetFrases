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
import java.util.concurrent.TimeUnit;

import android.appwidget.AppWidgetProvider;

public class WidgetProvider extends AppWidgetProvider {
    private static final String API_URL = "https://api.quotable.io/random";
    private static final String[] FRASES_PREDEFINIDAS = {
        "El éxito es la suma de pequeños esfuerzos repetidos día tras día.",
        "La única forma de hacer un gran trabajo es amar lo que haces.",
        "Todo lo que puedas imaginar es real.",
        "El futuro pertenece a quienes creen en la belleza de sus sueños.",
        "La mejor manera de predecir el futuro es creándolo."
    };
    
    private static int currentIndex = 0;
    private static boolean useLocalQuotes = true; // Cambiar a false para usar la API

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            if (useLocalQuotes) {
                updateWidgetWithLocalQuote(context, appWidgetManager, appWidgetId);
            } else {
                fetchQuote(context, appWidgetManager, appWidgetId);
            }
        }
    }

    private void updateWidgetWithLocalQuote(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        String frase = FRASES_PREDEFINIDAS[currentIndex];
        currentIndex = (currentIndex + 1) % FRASES_PREDEFINIDAS.length;
        
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.widget_text, frase);
        appWidgetManager.updateAppWidget(appWidgetId, views);
        
        Log.d("MotivationalWidget", "Widget actualizado con frase local: " + frase);
    }

    private void fetchQuote(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Accept", "application/json")
                .build();

        // Ejecutar la solicitud en un hilo de fondo
        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                String quote;
                
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    quote = jsonObject.getString("content");
                    Log.d("MotivationalWidget", "Frase obtenida: " + quote);
                } else {
                    // Si falla la API, usar una frase predefinida
                    quote = FRASES_PREDEFINIDAS[currentIndex];
                    currentIndex = (currentIndex + 1) % FRASES_PREDEFINIDAS.length;
                    Log.e("MotivationalWidget", "Error en la API, usando frase local");
                }

                final String finalQuote = quote;
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                        views.setTextViewText(R.id.widget_text, finalQuote);
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                        Log.d("MotivationalWidget", "Widget actualizado correctamente");
                    } catch (Exception e) {
                        Log.e("MotivationalWidget", "Error actualizando widget: " + e.getMessage());
                    }
                });
                
            } catch (Exception e) {
                Log.e("MotivationalWidget", "Error general: " + e.getMessage());
                // En caso de error, actualizar con frase predefinida
                new Handler(Looper.getMainLooper()).post(() -> {
                    updateWidgetWithLocalQuote(context, appWidgetManager, appWidgetId);
                });
            }
        }).start();
    }
}