package com.example.desktoptext;

import androidx.appcompat.app.AppCompatActivity;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import static android.graphics.Color.rgb;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public int FONT_COLOR = Color.BLACK;
    public int FONT_SIZE = 12;
    private Resources r;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);   // 调用父类的onCreate方法
        setContentView(R.layout.activity_main);

        /*
         *定义颜色选择spinner
         * 定义列表adapter
         */
        Spinner color_spinner = findViewById(R.id.color_spinner);
        if (color_spinner!=null){
            color_spinner.setOnItemSelectedListener(this);
        }
        ArrayAdapter<CharSequence> color_adapter = ArrayAdapter.createFromResource(this,
                R.array.color_array, android.R.layout.simple_spinner_item);
        color_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (color_spinner!=null){
            color_spinner.setAdapter(color_adapter);
        }

        /*
         *定义字体选择spinner
         * 定义列表adapter
         */
        Spinner font_spinner = findViewById(R.id.font_spinner);
        if (font_spinner!=null){
            font_spinner.setOnItemSelectedListener(this);
        }
        ArrayAdapter<CharSequence> font_adapter = ArrayAdapter.createFromResource(this,
                R.array.color_array, android.R.layout.simple_spinner_item);
        font_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (font_spinner!=null){
            font_spinner.setAdapter(font_adapter);
        }

        this.r = getResources();
        this.sharedPref = this.getSharedPreferences(r.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        initSavedSlots();

    }

    public void initSavedSlots(){
        /*
         *初始化存储栏
         */

        String handle = String.format("saved_text_%d", 1);
        String message = sharedPref.getString(handle, "");

        updateSlot(R.id.saveSlots1, message);

        handle = String.format("saved_text_%d", 2);
        message = sharedPref.getString(handle, "");

        updateSlot(R.id.saveSlots2, message);

        handle = String.format("saved_text_%d", 3);
        message = sharedPref.getString(handle, "");

        updateSlot(R.id.saveSlots3, message);

    }

    public void updateTextFly(View view){
        /*
        实时更新
         */
        EditText editText = (EditText) findViewById(R.id.editText);
        // 获取输入的文本
        String message = editText.getText().toString();
        updateText(message);
    }

    public void updateText(String message){
        /*
        启动更新小部件Activity
         */
        
        // 定义intent，与另一个activity连接
        Intent intent = new Intent(this, ConfigureActivity.class);

        // 将文本加入到intent中
        intent.putExtra(EXTRA_MESSAGE, message);
        // 开始activity
        startActivity(intent);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(r.getString(R.string.displaying_text), message);
        editor.apply();

        displayToast(r.getString(R.string.update_text_toast));
    }

    public int saveText(View view, String message, int pos) {

        SharedPreferences.Editor editor = sharedPref.edit();

        Integer totalSaveSlots = r.getInteger(R.integer.total_save_slots);


        int nowSlot = pos;
        int flag = 0;

//        System.out.println(numOccupied);

        if (pos != 0) {
            String handle = String.format("saved_text_%d", pos);
            editor.putString(handle, message);
            flag = 1;
        } else {
            for (int i = 0; i < totalSaveSlots; i++) {
                String handle = String.format("saved_text_%d", i + 1);
                System.out.println(i);System.out.println(sharedPref.getString(handle, ""));
                if (sharedPref.getString(handle, "").length()==0) {

                    editor.putString(handle, message);
                    nowSlot = i + 1;
                    flag = 1;
                    break;
                }
            }
        }

        if (flag != 1){
            System.out.println("hello");
            String handle = String.format("saved_text_1");
            editor.putString(handle, message);
            displayToast(r.getString(R.string.overwrite_text_toast));
            nowSlot = 1;
        }

//        if (message.length() == 0){
//            editor.putInt(occupiedNumberHandel, numOccupied-1);
//        }
        editor.apply();
        String save_text_toast = r.getString(R.string.add_text_to_saved_list_toast);
        displayToast(message);

        updateSlotsFly(nowSlot, message);

        return nowSlot;
    }

    public void updateSlotsFly(int nowSlot, String message){
        switch (nowSlot){
            case 1:
                updateSlot(R.id.saveSlots1, message);
                break;
            case 2:
                updateSlot(R.id.saveSlots2, message);
                break;
            case 3:
                updateSlot(R.id.saveSlots3, message);
                break;
            default:
                break;
        }
    }

    public void updateSlot(int id, String message){
        EditText editText = (EditText) findViewById(id);
        editText.setText(message);
    }



    /*
    各方案更新
     */
    public void saveText0(View view){
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        int nowSlot = saveText(view, message,1);
    }

    public void saveText1(View view){
        EditText editText = (EditText) findViewById(R.id.saveSlots1);
        String message = editText.getText().toString();
        int nowSlot = saveText(view, message,1);
    }

    public void saveText2(View view){
        EditText editText = (EditText) findViewById(R.id.saveSlots2);
        String message = editText.getText().toString();
        int nowSlot = saveText(view, message, 2);
    }

    public void saveText3(View view){
        EditText editText = (EditText) findViewById(R.id.saveSlots3);
        String message = editText.getText().toString();
        int nowSlot = saveText(view, message,3);
    }

    /*
 各方案显示
  */

    public void showText1(View view){
        EditText editText = (EditText) findViewById(R.id.saveSlots1);
        String message = editText.getText().toString();
        updateText(message);
    }

    public void showText2(View view){
        EditText editText = (EditText) findViewById(R.id.saveSlots2);
        String message = editText.getText().toString();
        updateText(message);
    }

    public void showText3(View view){
        EditText editText = (EditText) findViewById(R.id.saveSlots3);
        String message = editText.getText().toString();
        updateText(message);
    }



    public void changeFont(View view){

        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        EditText inputFontSize = findViewById(R.id.fontsize);
        String tmp = inputFontSize.getText().toString();
        if (tmp.length()!=0){
            this.FONT_SIZE = Integer.parseInt(tmp);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName provider = new ComponentName(this, DesktopTextWidget.class);
        RemoteViews views = new RemoteViews(this.getPackageName(),
                R.layout.desktop_text_widget);


        // 字体颜色
        EditText inputColorCodeR = findViewById(R.id.color_code_r);
        EditText inputColorCodeG = findViewById(R.id.color_code_g);
        EditText inputColorCodeB = findViewById(R.id.color_code_b);

        String rCode = inputColorCodeR.getText().toString();
        String gCode = inputColorCodeG.getText().toString();
        String bCode = inputColorCodeB.getText().toString();
        if (rCode.length()==0 | gCode.length()==0 | bCode.length()==0) {

        }else{
            int rCodeInt = Integer.parseInt(rCode);
            int gCodeInt = Integer.parseInt(gCode);
            int bCodeInt = Integer.parseInt(bCode);

            if (rCodeInt<0 | gCodeInt<0 |gCodeInt<0 | rCodeInt>255 | gCodeInt>255 | bCodeInt>255){
                displayToast(r.getString(R.string.input_code_error_message));
            }else{
                this.FONT_COLOR = rgb(rCodeInt, gCodeInt, bCodeInt);
//                System.out.println(String.format("%d-%d-%d", rCodeInt, gCodeInt, bCodeInt));
            }
        }
//        views.setTextColor(R.id.appwidget_text, this.FONT_COLOR);

        // 字体大小
//        views.setTextViewTextSize(R.id.appwidget_text, TypedValue.COMPLEX_UNIT_PT, this.FONT_SIZE);

        Bitmap bmp = getTextBmp("hello world!");
        views.setImageViewBitmap(R.id.appwidget_image, bmp);

        appWidgetManager.updateAppWidget(provider, views);

        displayToast("字体已修改");

    }

    public Bitmap getTextBmp(String message){


        Paint p = new Paint();
        Paint.FontMetricsInt fm = p.getFontMetricsInt();
        int width = (int)p.measureText(message);
        int height = fm.descent - fm.ascent;

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvasTmp = new Canvas(bmp);
        canvasTmp.drawColor(Color.WHITE);

        Typeface plain = r.getFont(R.font.handwriting);
        Typeface font = Typeface.create(plain, Typeface.NORMAL);

        p.setColor(Color.RED);
        p.setTypeface(font);
        p.setTextSize(12);
        canvasTmp.drawText(message, 0, fm.leading - fm.ascent, p);

        return bmp;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String spinnerLabel = parent.getItemAtPosition(position).toString();
        displayToast(String.valueOf(id));
        switch (spinnerLabel){
            case "Black":
                this.FONT_COLOR = Color.BLACK;
                break;
            case "Red":
                this.FONT_COLOR = Color.RED;
                break;
            case "Blue":
                this.FONT_COLOR = Color.BLUE;
                break;
            case "Gray":
                this.FONT_COLOR = Color.CYAN;
                break;
            case "Pink":
                this.FONT_COLOR = rgb(249, 204, 226);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_SHORT).show();
    }
}