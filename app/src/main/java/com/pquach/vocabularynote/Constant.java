package com.pquach.vocabularynote;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

/**
 * Created by HP on 28/05/2015.
 */
public final class Constant {
    public static final String ARG_WORD_ID = "com.pquach.vocabularynote.constant.wordid";
    public static final String ARG_CATEGORY = "com.pquach.vocabularynote.constant.categoryid";

    public static String capitalizeEachWord(String s){
        String[] temp = s.split(" ");
        String result = new String();
        for(int i=0; i<temp.length; i++){
            result += Character.toUpperCase(temp[i].charAt(0)) + temp[i].substring(1) + " " ;
        }

        return result.trim();
    }

    public static TextView createDialogTitle(Context context, String titleText, int color){
        TextView title = new TextView(context);
        title.setText(titleText);
        title.setTextSize(25);
        title.setPadding(30, 30, 30, 30);
        title.setTextColor(color);
        return title;
    }

    public static void setDialogDividerColor(Context context, AlertDialog dialog, int color){
        int titleDividerId = context.getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = dialog.findViewById(titleDividerId);
        if (titleDivider != null)
            titleDivider.setBackgroundColor(color);
    }
}
