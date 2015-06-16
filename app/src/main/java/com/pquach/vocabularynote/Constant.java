package com.pquach.vocabularynote;

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
}
