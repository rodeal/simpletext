package com.example.desktoptext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RemoteViews;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import static android.graphics.Color.parseColor;
import static android.graphics.Color.rgb;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public int FONT_COLOR = Color.BLACK;
    public int SPINNER_COLOR = Color.BLACK;
    public int FONT_SIZE = 12;
    public boolean SPINNER_USED = false;
    public int NOW_WIDGET = 1;
    private Resources r;
    private SharedPreferences sharedPrefWidget;
    private SharedPreferences sharedPrefSaveSlots;

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


        this.r = getResources();

        initPref();         // 初始化配置

        getPreview();

        initSavedSlots();   // 初始化存储栏

    }

    public void initPref(){
        this.sharedPrefSaveSlots = this.getSharedPreferences(r.getString(R.string.preference_file_key_saved_slots), Context.MODE_PRIVATE);
        switch (this.NOW_WIDGET){
            case 1:
                this.sharedPrefWidget = this.getSharedPreferences(r.getString(R.string.preference_file_key_widget1), Context.MODE_PRIVATE);
                break;
            case 2:
                this.sharedPrefWidget = this.getSharedPreferences(r.getString(R.string.preference_file_key_widget2), Context.MODE_PRIVATE);
                break;
        }
    }

    public ComponentName initProvider(){
        ComponentName provider;
        switch (this.NOW_WIDGET){
            case 1:
                provider = new ComponentName(this, DesktopTextWidget1.class);
                break;
            case 2:
                provider = new ComponentName(this, DesktopTextWidget2.class);
                break;
            default:
                provider = new ComponentName(this, DesktopTextWidget1.class);
        }
        return provider;
    }

    public RemoteViews initRemoteViews(){
        RemoteViews views;
        switch (this.NOW_WIDGET){
            case 1:
                views = new RemoteViews(this.getPackageName(), R.layout.desktop_text_widget);
                break;
            case 2:
                views = new RemoteViews(this.getPackageName(), R.layout.desktop_text_widget2);
                break;
            default:
                views = new RemoteViews(this.getPackageName(), R.layout.desktop_text_widget);
        }
        return views;
    }

    public void getPreview(){
        TextView textPreview = findViewById(R.id.textViewPreview);
        String message;
        if (this.NOW_WIDGET == 1){
            message = sharedPrefWidget.getString(r.getString(R.string.displaying_text), r.getString(R.string.appwidget_text1));
        }else{
            message = sharedPrefWidget.getString(r.getString(R.string.displaying_text), r.getString(R.string.appwidget_text2));
        }


        int font_color = sharedPrefWidget.getInt(r.getString(R.string.last_font_color), Color.BLACK);
        int font_size = sharedPrefWidget.getInt(r.getString(R.string.last_font_size), 12);

        String textToShow;
        if (message.length()>10){
            textToShow = String.join(" ", "当前文本", message.substring(0, 10), "...", String.format("字体大小为：%d", font_size));
        }else{
            textToShow = String.join(" ", "当前文本", message, String.format("字体大小为：%d", font_size));
        }


        if (this.NOW_WIDGET == 1){
            Typeface typeface = ResourcesCompat.getFont(this, R.font.handwriting1);
            textPreview.setTypeface(typeface);
        }else{
            textPreview.setTypeface(null);
        }
        textPreview.setText(textToShow);
        textPreview.setTextColor(font_color);

    }

    public void initSavedSlots(){
        /*
         *初始化存储栏
         */

        String handle = String.format("saved_text_%d", 1);
        String message = sharedPrefSaveSlots.getString(handle, "");

        updateSlot(R.id.saveSlots1, message);

        handle = String.format("saved_text_%d", 2);
        message = sharedPrefSaveSlots.getString(handle, "");

        updateSlot(R.id.saveSlots2, message);

        handle = String.format("saved_text_%d", 3);
        message = sharedPrefSaveSlots.getString(handle, "");

        updateSlot(R.id.saveSlots3, message);

    }

    public void updateText(String message){
        /*
        更新桌面显示文字
         */

        String handle = r.getString(R.string.displaying_text);
        String nowDisplayingText = sharedPrefWidget.getString(handle, "");
        if (message.equals(nowDisplayingText)){
            displayToast(r.getString(R.string.update_text_failed_toast));
            return;
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews views = initRemoteViews();
        ComponentName provider = initProvider();

        views.setTextViewText(R.id.appwidget_text, message);

        appWidgetManager.updateAppWidget(provider, views);

        SharedPreferences.Editor editor = sharedPrefWidget.edit();
        editor.putString(handle, message);


        editor.apply();
        if (message.length()>0){
            displayToast(r.getString(R.string.update_text_toast));
        }else{
            displayToast(r.getString(R.string.update_empty_text_toast));
        }

        getPreview();
    }


    public void changeTextFont(View view){
        /*
         * 修改小部件字体
         */

        int statusCode = getInputFont();

        if (statusCode==0 | statusCode==4){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

            RemoteViews views = initRemoteViews();
            ComponentName provider = initProvider();

            SharedPreferences.Editor editor = sharedPrefWidget.edit();

            views.setTextViewTextSize(R.id.appwidget_text, TypedValue.COMPLEX_UNIT_PT, this.FONT_SIZE);
            views.setTextColor(R.id.appwidget_text, this.FONT_COLOR);

            appWidgetManager.updateAppWidget(provider, views);

            editor.putInt(r.getString(R.string.last_font_size), this.FONT_SIZE);
            editor.putInt(r.getString(R.string.last_font_color), this.FONT_COLOR);
            editor.apply();

            displayToast(r.getString(R.string.fininshed_font_altering));
        }else if (statusCode==3){
            displayToast(r.getString(R.string.use_last_saved_font_config));
        }else if (statusCode==1){
            displayToast(r.getString(R.string.input_hex_color_code_error));
        }else if (statusCode==2){
            displayToast(r.getString(R.string.input_rgb_color_code_error));
        }

        getPreview();
    }

    public void updateCurrentWidget(View view){
        /*
         * 小部件选择回调
         */
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()){
            case R.id.radioButtonWidget1:
                if (checked){
                    this.NOW_WIDGET = 1;
                }
                break;
            case R.id.radioButtonWidget2:
                if (checked){
                    this.NOW_WIDGET = 2;
                }
                break;
        }

        initPref();

        getPreview();
    }

    public int saveText(String message, int pos) {
        /*
         *保存常用文字
         */
        SharedPreferences.Editor editor = sharedPrefSaveSlots.edit();

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
                System.out.println(i);System.out.println(sharedPrefSaveSlots.getString(handle, ""));
                if (sharedPrefSaveSlots.getString(handle, "").length()==0) {

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
        if (message.length() > 0){
            displayToast(String.join(" ", message, save_text_toast));
        }else{
            displayToast(r.getString((R.string.add_empty_text_to_saved_list_toast)));
        }


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
        EditText editText = findViewById(id);
        editText.setText(message);
    }

    public void getMessageToSave(int inputId, int pos){
        /*
         * 获取对应存储栏数据
         */
        EditText editText = (EditText) findViewById(inputId);
        String message = editText.getText().toString();
        int nowSlot = saveText(message, pos);
        updateSlotsFly(nowSlot, message);
    }


    public void onSaveButtonClick(View view){
        /*
         * 方案保存按钮回调
         */
        switch (view.getId()){
            case R.id.button3:
                getMessageToSave(R.id.editText, 0);
                break;
            case R.id.buttonSaveSlot1:
                getMessageToSave(R.id.saveSlots1, 1);
                break;
            case R.id.buttonSaveSlot2:
                getMessageToSave(R.id.saveSlots2, 2);
                break;
            case R.id.buttonSaveSlot3:
                getMessageToSave(R.id.saveSlots3, 3);
                break;
        }
    }

    public void getMessageToShow(int inputId){
        /*
         * 获取对应存储栏数据
         */

        EditText editText = findViewById(inputId);
        // 获取输入的文本
        String message = editText.getText().toString();
        updateText(message);
    }

    public void onDisplayButtonClick(View view) {
        /*
         *各方案显示按钮回调
         */

        switch (view.getId()) {
            case R.id.buttonQuickDisplay:
                getMessageToShow(R.id.editText);
                break;
            case R.id.buttonSlot1:
                getMessageToShow(R.id.saveSlots1);
                break;
            case R.id.buttonSlot2:
                getMessageToShow(R.id.saveSlots2);
                break;
            case R.id.buttonSlot3:
                getMessageToShow(R.id.saveSlots3);
                break;
        }
    }

    public int getInputFont(){

        // 读取上次配置
        int fontSize = sharedPrefWidget.getInt(r.getString(R.string.last_font_size), 12);
        int fontColor = sharedPrefWidget.getInt(r.getString(R.string.last_font_color), Color.BLACK);

        // 记录当前值
        int nowFontSize = fontSize;
        int nowFontColor = fontColor;

        // 字体大小
        EditText inputFontSize = findViewById(R.id.fontsize);
        String tmp = inputFontSize.getText().toString();
        if (tmp.length()!=0){
            fontSize = Integer.parseInt(tmp);
        }

        // 字体颜色
        if (this.SPINNER_USED==true & this.SPINNER_COLOR != nowFontColor){
            fontColor = this.SPINNER_COLOR;
            this.FONT_COLOR = fontColor;
            this.FONT_SIZE = fontSize;
            return 0;
        }

        EditText inputColorCodeR = findViewById(R.id.color_code_r);
        EditText inputColorCodeG = findViewById(R.id.color_code_g);
        EditText inputColorCodeB = findViewById(R.id.color_code_b);

        String rCode = inputColorCodeR.getText().toString();
        String gCode = inputColorCodeG.getText().toString();
        String bCode = inputColorCodeB.getText().toString();

        EditText inputHexColorCode = findViewById((R.id.hex_color_code));
        String hexCode = inputHexColorCode.getText().toString();

        String pattern = "([0-9]|[a-f]|[A-F]){6}";  // 匹配16进制颜色代码格式
        boolean isMatch = Pattern.matches(pattern, hexCode);

        if (isMatch){
            fontColor = parseColor(String.join("", "#", hexCode));
        }else if (hexCode.length()>0){
            return 1;
        }

        if (rCode.length()==0 & gCode.length()==0 & bCode.length()==0) {

        }else{
            int rCodeInt = 0;
            int gCodeInt = 0;
            int bCodeInt = 0;

            if (rCode.length()>0){
                rCodeInt = Integer.parseInt(rCode);
            }

            if (gCode.length()>0){
                gCodeInt = Integer.parseInt(gCode);
            }

            if (bCode.length()>0){
                bCodeInt = Integer.parseInt(bCode);
            }

            if (rCodeInt<0 | gCodeInt<0 |gCodeInt<0 | rCodeInt>255 | gCodeInt>255 | bCodeInt>255){
                return 2;
            }else {
                fontColor = rgb(rCodeInt, gCodeInt, bCodeInt);
            }
        }
        if (nowFontColor==fontColor & nowFontSize==fontSize){
            return 3;
        }else{
            this.FONT_COLOR = fontColor;
            this.FONT_SIZE = fontSize;
            return 4;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String spinnerLabel = parent.getItemAtPosition(position).toString();
//        displayToast(String.valueOf(id));
        switch (spinnerLabel){
            case "选择颜色":
                break;
            case "Black":
                this.SPINNER_COLOR = Color.BLACK;
                this.SPINNER_USED = true;
                break;
            case "Red":
                this.SPINNER_COLOR = Color.RED;
                this.SPINNER_USED = true;
                break;
            case "Blue":
                this.SPINNER_COLOR = Color.BLUE;
                this.SPINNER_USED = true;
                break;
            case "Gray":
                this.SPINNER_COLOR = Color.CYAN;
                this.SPINNER_USED = true;
                break;
            case "Pink":
                this.SPINNER_COLOR = rgb(249, 204, 226);
                this.SPINNER_USED = true;
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