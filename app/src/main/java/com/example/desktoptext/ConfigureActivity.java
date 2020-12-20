package com.example.desktoptext;

import androidx.appcompat.app.AppCompatActivity;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.TextView;

public class ConfigureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        final Context context = ConfigureActivity.this;
        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // 获取传到intent的字符
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        System.out.println(context.getPackageName());
        ComponentName provider = new ComponentName(this,DesktopTextWidget.class);
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.desktop_text_widget);
        views.setTextViewText(R.id.appwidget_text, message);

        appWidgetManager.updateAppWidget(provider, views);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();

    }
}