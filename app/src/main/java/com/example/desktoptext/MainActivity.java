package com.example.desktoptext;

import androidx.appcompat.app.AppCompatActivity;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.regex.Pattern;

import static android.graphics.Color.parseColor;
import static android.graphics.Color.rgb;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public int FONT_COLOR = Color.BLACK;
    public int SPINNER_COLOR = Color.BLACK;
    public int FONT_SIZE = 12;
    public boolean SPINNER_USED = false;
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


        this.r = getResources();
        this.sharedPref = this.getSharedPreferences(r.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        initSavedSlots();

    }


    public void updateText(String message){
        /*
        更新桌面显示文字
         */

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName provider = new ComponentName(this, DesktopTextWidget1.class);
        RemoteViews views = new RemoteViews(this.getPackageName(),
                R.layout.desktop_text_widget);
        views.setTextViewText(R.id.appwidget_text, message);

        appWidgetManager.updateAppWidget(provider, views);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(r.getString(R.string.displaying_text), message);
        editor.apply();
        if (message.length()>0){
            displayToast(r.getString(R.string.update_text_toast));
        }else{
            displayToast(r.getString(R.string.update_empty_text_toast));
        }

    }


    public void changeTextFont(View view){
        /*
         * 修改小部件字体
         */

        int statusCode = getInputFont();

        if (statusCode==0 | statusCode==4){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            ComponentName provider = new ComponentName(this, DesktopTextWidget1.class);
            RemoteViews views = new RemoteViews(this.getPackageName(),
                    R.layout.desktop_text_widget);

            SharedPreferences.Editor editor = sharedPref.edit();

            views.setTextViewTextSize(R.id.appwidget_text, TypedValue.COMPLEX_UNIT_PT, this.FONT_SIZE);
            views.setTextColor(R.id.appwidget_text, this.FONT_COLOR);

            editor.putInt(r.getString(R.string.last_font_size), this.FONT_SIZE);
            editor.putInt(r.getString(R.string.last_font_color), this.FONT_COLOR);
            editor.apply();

            appWidgetManager.updateAppWidget(provider, views);

            displayToast(r.getString(R.string.fininshed_font_altering));
        }else if (statusCode==3){
            displayToast(r.getString(R.string.use_last_saved_font_config));
        }else if (statusCode==1){
            displayToast(r.getString(R.string.input_hex_color_code_error));
        }else if (statusCode==2){
            displayToast(r.getString(R.string.input_rgb_color_code_error));
        }
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

    public void showTextFly(View view){
        /*
        实时更新
         */

        EditText editText = (EditText) findViewById(R.id.editText);
        // 获取输入的文本
        String message = editText.getText().toString();
        updateText(message);
    }

    public int saveText(View view, String message, int pos) {
        /*
         *保存常用文字
         */
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
        EditText editText = (EditText) findViewById(id);
        editText.setText(message);
    }



    /*
    各方案更新
     */
    public void saveText0(View view){
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        int nowSlot = saveText(view, message,0);
        updateSlotsFly(nowSlot, message);
    }

    public void saveText1(View view){
        EditText editText = (EditText) findViewById(R.id.saveSlots1);
        String message = editText.getText().toString();
        int nowSlot = saveText(view, message,1);
        updateSlotsFly(nowSlot, message);
    }

    public void saveText2(View view){
        EditText editText = (EditText) findViewById(R.id.saveSlots2);
        String message = editText.getText().toString();
        int nowSlot = saveText(view, message, 2);
        updateSlotsFly(nowSlot, message);
    }

    public void saveText3(View view){
        EditText editText = (EditText) findViewById(R.id.saveSlots3);
        String message = editText.getText().toString();
        int nowSlot = saveText(view, message,3);
        updateSlotsFly(nowSlot, message);
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

    public int getInputFont(){

        // 读取上次配置
        int fontSize = sharedPref.getInt(r.getString(R.string.last_font_size), 12);
        int fontColor = sharedPref.getInt(r.getString(R.string.last_font_color), Color.BLACK);

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