package com.pquach.vocabularynote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

/**
 * Created by SONY on 8/27/2015.
 */
public class JSONProcessor {

    private Context mContext;
    private Word[] mArrayWords;
    private String[] mArrayCategoryName;


    public JSONProcessor(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
    }

    public boolean exportData(OutputStream outstr){
        //--Load data
        CategoryDataSource catds = new CategoryDataSource(mContext);
        WordDataSource wordds = new WordDataSource(mContext);
        Cursor cats = catds.getAllCategories();
        Cursor words = wordds.selectWordJoinCategory();
        return writeFile(outstr, cats, words);
    }

    public boolean importData(BufferedReader buffer){

        // Read file content to mArrayWords and mArrayCategoryName
        readFiles(buffer);

        // Insert mArrayWords into database
        for(int i=0; i<mArrayWords.length; i++){
            WordDataSource dataSource = new WordDataSource(mContext);
            if(dataSource.insert(mArrayWords[i])==-1){
                return false;
            }
        }
        return true;
    }


    private String createJSONString(Cursor categories, Cursor words){
        String catName;
        String word, type, def, ex;
        String temp;
        String jsonRoot = null;
        String jsonCategories = "\""+ VobNoteContract.Category.TABLE_NAME + "\":[";
        String jsonWords = "\""+ VobNoteContract.Word.TABLE_NAME + "\":[";

        // write Category table data
        while(categories.moveToNext()){
            catName = categories.getString(categories.getColumnIndex(VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME));
            temp = "{ \""+VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME+"\":\"" + catName+"\"},";
            jsonCategories+=temp;
        }

        jsonCategories = jsonCategories.substring(0, jsonCategories.length()-1);// remove the last comma(,)
        jsonCategories+= "]";

        // write Word table data
        while(words.moveToNext()){
            word =  words.getString(words.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_WORD));
            type =  words.getString(words.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_TYPE));
            def =  words.getString(words.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_DEFINITION));
            ex =  words.getString(words.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_EXAMPLE));
            catName = words.getString(words.getColumnIndex(VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME));
            temp = "{ " +
                    "\""+VobNoteContract.Word.COLUMN_NAME_WORD+"\":\"" + word+"\"," +
                    "\""+VobNoteContract.Word.COLUMN_NAME_TYPE+"\":\"" + type+"\"," +
                    "\""+VobNoteContract.Word.COLUMN_NAME_DEFINITION+"\":\"" + def+"\"," +
                    "\""+VobNoteContract.Word.COLUMN_NAME_EXAMPLE+"\":\"" + ex+"\"," +
                    "\""+VobNoteContract.Word.COLUMN_NAME_CATEGORY+"\":\"" + catName+"\"" +
                    "},";
            jsonWords+=temp;
        }
        jsonWords = jsonWords.substring(0, jsonWords.length()-1); // remove the last comma(,)
        jsonWords+="]";

        jsonRoot = "{" +jsonCategories +","+ jsonWords+"}";

        return jsonRoot;
    }

    private boolean writeFile(OutputStream outstr, Cursor categories, Cursor words){
        try{

            OutputStreamWriter osw = new OutputStreamWriter(outstr);
            String jsonString;

            osw.write("<application/json version=1>\n");
            jsonString = createJSONString(categories, words);
            if(jsonString==null){
               return false;
            }
            osw.write(jsonString);
			/* ensure that everything is
			* really written out and close */
            osw.flush();
            osw.close();
            return true;

        }catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
    }

    private void readFiles(BufferedReader reader){
        String jsonString;
        String fileHeader;

        // Read content from file
        try{
            // Read file header
            fileHeader = reader.readLine();

            // Read JSON string
            jsonString = reader.readLine();

        }catch (IOException e){
            e.printStackTrace();
            return ;
        }

        // Read data from JSON string
        try{
            JSONObject jsonRootObject = new JSONObject(jsonString);

            // Get Category array's instance
            JSONArray jsonCategoryArray = jsonRootObject.optJSONArray(VobNoteContract.Category.TABLE_NAME);
            // Read data from jsonCategoryArray to mArrayCategoryName
            if(jsonCategoryArray != null){
                mArrayCategoryName = new String[jsonCategoryArray.length()];
                for(int i=0; i< jsonCategoryArray.length(); i++){
                    JSONObject jsonObject = jsonCategoryArray.getJSONObject(i);
                    mArrayCategoryName[i] = jsonObject.getString(VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME);
                }
            }

            // Insert categories into Category table and return an array of catrgory ids
            HashMap hashMap = updateCategoryTable(mArrayCategoryName);
            if(hashMap == null)
                return;

            // Get Word array's instance
            JSONArray jsonWordArray = jsonRootObject.optJSONArray(VobNoteContract.Word.TABLE_NAME);
            // Read data from jsonWordArray to mArrayWords
            if(jsonWordArray != null){
                mArrayWords = new Word[jsonWordArray.length()];
                for(int i=0; i< jsonWordArray.length(); i++){
                    JSONObject jsonObject = jsonWordArray.getJSONObject(i);
                    mArrayWords[i] = new Word();
                    mArrayWords[i].setWord(jsonObject.getString(VobNoteContract.Word.COLUMN_NAME_WORD));
                    mArrayWords[i].setType(jsonObject.getString(VobNoteContract.Word.COLUMN_NAME_TYPE));
                    mArrayWords[i].setDefinition(jsonObject.getString(VobNoteContract.Word.COLUMN_NAME_DEFINITION));
                    mArrayWords[i].setExample(jsonObject.getString(VobNoteContract.Word.COLUMN_NAME_EXAMPLE));
                    mArrayWords[i].setCategory((long) hashMap.get(jsonObject.getString(VobNoteContract.Word.COLUMN_NAME_CATEGORY)));
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private HashMap updateCategoryTable(String[] cats){
        if(cats.length<=0){
            return null;
        }
        HashMap hashMap = new HashMap();
        CategoryDataSource ds = new CategoryDataSource(mContext);
        Cursor cursor;
        String catName;
        long catId;
        for(int i=0; i<cats.length; i++){
            cursor = ds.getCategoriesByName(cats[i]);
            if(cursor.moveToFirst()){
                catId = cursor.getLong(cursor.getColumnIndex(VobNoteContract.Category.COLUMN_NAME_CATEGORY_ID));
                catName = cursor.getString(cursor.getColumnIndex(VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME));
                hashMap.put(catName,catId);
            }else{
                catId = ds.insert(new Category(cats[i]));
                catName = cats[i];
                hashMap.put(catName, catId);
            }
        }
        return hashMap;
    }
}
