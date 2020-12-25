package com.example.desktoptext;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class DesktopTextWidget1 extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.desktop_text_widget);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.desktop_text_widget);

        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

        for (int appWidgetId : appWidgetIds) {

            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key_widget1), Context.MODE_PRIVATE);

            //初始化文字，大小，颜色
            int fontSize = sharedPref.getInt(context.getString(R.string.last_font_size), 12);
            int fontColor = sharedPref.getInt(context.getString(R.string.last_font_color), Color.BLACK);
            String message = sharedPref.getString(context.getString(R.string.displaying_text), context.getString(R.string.default_display_text));

            views.setTextViewText(R.id.appwidget_text, message);
            views.setTextViewTextSize(R.id.appwidget_text, TypedValue.COMPLEX_UNIT_PT,fontSize);
            views.setTextColor(R.id.appwidget_text, fontColor);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}