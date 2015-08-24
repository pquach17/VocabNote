package com.pquach.vocabularynote;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import com.pquach.vocabularynote.WordDataSource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;


public class FileProcessor {

    private final char WORD = '@';
    private final char TYPE = '%';
    private final char DEFINITION = '$';
    private final char EXAMPLE = '&';
    private final char CATEGORY = '#';
    private final String DIVIDER = "-";
    private Context mContext;
    private long mCategoryId;
    private Word[] mArrayWords;
    private String[] mArrayCatNameInWordTable;
    private String[] mArrayCategoryName;


    public FileProcessor(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
        mCategoryId = 0;
    }

    public boolean exportData(OutputStream outstr){
        //--Load data
        CategoryDataSource catds = new CategoryDataSource(mContext);
        WordDataSource wordds = new WordDataSource(mContext);

        Cursor cats = catds.getAll();
        Cursor words = wordds.getFromAllCategory();
        return writeFile(outstr, cats, words);
    }

    public boolean importData(BufferedReader buffer){

        long[] arrCatIds;
        ContentValues mapping = new ContentValues();


        // Read data from file
        readFiles(buffer);

        // Insert data into table Category
        arrCatIds = updateCategoryTable(mArrayCategoryName);

        // Mapping category's names and ids
        for(int i=0; i<mArrayCategoryName.length; i++){
            mapping.put(mArrayCategoryName[i], arrCatIds[i]);
        }

        // Copy catgory's id to mArrayWords
        for(int i=0; i<mArrayWords.length; i++){
            mArrayWords[i].setCategory(mapping.getAsLong(mArrayCatNameInWordTable[i]));
        }

        // Insert mArrayWords into database
        for(int i=0; i<mArrayWords.length; i++){
            WordDataSource dataSource = new WordDataSource(mContext, mArrayWords[i].getCategory());
            if(dataSource.insert(mArrayWords[i])==-1){
                return false;
            }
        }
        return true;
    }

    private boolean writeFile(OutputStream outstr, Cursor categories, Cursor words){
        try{
            String word, type, def, ex, cat, buffer;
            ContentValues mapping = new ContentValues();
            OutputStreamWriter osw = new OutputStreamWriter(outstr);

            // Write the number of categories and words to the file
            osw.write(String.valueOf(categories.getCount())+"\n");
            osw.write(String.valueOf(words.getCount())+"\n");

            // Write category's names
            while(categories.moveToNext()){
                long id = categories.getLong(categories.getColumnIndex(VobNoteContract.Category.COLUMN_NAME_CATEGORY_ID));
                String name = categories.getString(categories.getColumnIndex(VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME));
                mapping.put(String.valueOf(id), name);
                cat = categories.getString(categories.getColumnIndex(VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME))+ DIVIDER;
                osw.write(cat);
            }
            osw.write("\n");

            // Write words
            while(words.moveToNext()){
                long id = words.getLong(words.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_CATEGORY));

                word = WORD + words.getString(words.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_WORD))+ DIVIDER;
                type = TYPE + words.getString(words.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_TYPE))+DIVIDER;
                def = DEFINITION + words.getString(words.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_DEFINITION))+DIVIDER;
                ex = EXAMPLE + words.getString(words.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_EXAMPLE))+DIVIDER;
                cat = CATEGORY + mapping.getAsString(String.valueOf(id));
                buffer = word+type+def+ex+cat+"\n";

                osw.write(buffer);

            }
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
        int catCount=0, wordCount=0, index=0;
        String readLine, word=null, type=null, def=null, ex=null;
        try{
            try{
                catCount = Integer.parseInt(reader.readLine());
                wordCount = Integer.parseInt(reader.readLine());
            }catch (NumberFormatException e){
                e.printStackTrace();
                reader.close();
                return;
            }
            // Read categories
            mArrayCategoryName = new String[catCount];
            mArrayCategoryName = reader.readLine().split(DIVIDER);

           // Initialize arrays
            mArrayWords = new Word[wordCount];
            for(int i=0; i<mArrayWords.length; i++){
                mArrayWords[i] = new Word();
            }
            mArrayCatNameInWordTable = new String[wordCount];

            // Read words
            while((readLine = reader.readLine())!=null){
                String[] temp = readLine.split(DIVIDER);
                for(int i=0; i<temp.length; i++){
                    switch (temp[i].charAt(0)){
                        case WORD:
                            mArrayWords[index].setWord(temp[i].substring(1));
                            break;
                        case TYPE:
                            mArrayWords[index].setType(temp[i].substring(1));
                            break;
                        case DEFINITION:
                            mArrayWords[index].setDefinition(temp[i].substring(1));
                            break;
                        case EXAMPLE:
                            mArrayWords[index].setExample(temp[i].substring(1));
                            break;
                        case CATEGORY:
                            mArrayCatNameInWordTable[index] = temp[i].substring(1);
                            break;
                    }
                }
                index++;
            }
            //-- Insert Cats into db right here --
            //-- Get CatIds[] --
            /*
            arrCatIds = updateCategoryTable(arrCats);
            for(int i=0; i<arrCats.length; i++){
                nameIdMapping.put(arrCats[i], arrCatIds[i]);
            }

            arrWords = new Word[wordCount];
            while((readLine = reader.readLine())!=null){
                String[] temp = readLine.split(DIVIDER);
                for(int i=0; i<temp.length; i++){
                    switch (temp[i].charAt(0)){
                        case WORD:
                            arrWords[index].setWord(temp[i]);
                            break;
                        case TYPE:
                            arrWords[index].setType(temp[i]);
                            break;
                        case DEFINITION:
                            arrWords[index].setDefinition(temp[i]);
                            break;
                        case EXAMPLE:
                            arrWords[index].setExample(temp[i]);
                            break;
                    }
                }
                index++;
            }
            */
        }catch (IOException e){
            e.printStackTrace();
            return ;
        }

    }

    private long[] updateCategoryTable(String[] cats){
        long[] catIds = new long[cats.length];
        CategoryDataSource ds = new CategoryDataSource(mContext);
        Cursor cursor;
        Category category = new Category();
        for(int i=0; i<cats.length; i++){
            cursor = ds.getCategoriesByName(cats[i]);
            if(cursor.moveToFirst()){
                catIds[i] = cursor.getLong(cursor.getColumnIndex(VobNoteContract.Category.COLUMN_NAME_CATEGORY_ID));
            }else{
                catIds[i] = ds.insert(new Category(cats[i]));
            }
        }
        return catIds;
    }

    /*
    public boolean importData(String fileName){
        // 1. Read file's content => return an array list of words
        // 2. Insert data into database
        Word[] arrWords = readFile(fileName);
        WordDataSource wordds = new WordDataSource(mContext, mCategoryId);
        if(arrWords!=null && arrWords.length>0){
            for(int i=0;i<arrWords.length; i++){
                wordds.insert(arrWords[i]);
            }
            return true;
        }else{
            return false;
        }
    }

    public boolean importData(BufferedReader buffer){
        // 1. Read file's content => return an array list of words
        // 2. Insert data into database
        Word[] arrWords = readFile(buffer);
        WordDataSource wordds = new WordDataSource(mContext, mCategoryId);
        if(arrWords!=null && arrWords.length>0){
            for(int i=0;i<arrWords.length; i++){
                wordds.insert(arrWords[i]);
            }
            return true;
        }else{
            return false;
        }
    }

  private boolean writeFile(OutputStream outstr, Cursor content){
        try{
            OutputStreamWriter osw = new OutputStreamWriter(outstr);

            // Write the string to the file
            String word, type, def, ex, buffer;
            osw.write(String.valueOf(content.getCount())+"\n");
            while(content.moveToNext()){
                word = WORD + content.getString(content.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_WORD))+"\n";
                type = TYPE + content.getString(content.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_TYPE))+"\n";
                def = DEFINITION + content.getString(content.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_DEFINITION))+"\n";
                ex = EXAMPLE + content.getString(content.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_EXAMPLE))+"\n";
                buffer = word+type+def+ex+DIVIDER+"\n";

                osw.write(buffer);

            }
			// ensure that everything is
			//really written out and close
    osw.flush();
    osw.close();
    return true;

}catch (IOException ioe) {
        ioe.printStackTrace();
        return false;
        }
        }

    private Word[] readFile(String fileName){
        int rowCount = 0, i=0;
        String readLine, word=null, type=null, def=null, ex=null;
        Word[] arrWords;

        try{
            File dir = new File(mContext.getExternalFilesDir(null), "Exports");
            File file = new File(dir, fileName);
            FileInputStream fIn = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader reader = new BufferedReader(isr);
            try{
                rowCount = Integer.parseInt(reader.readLine());
            }catch (NumberFormatException nfe){
                nfe.printStackTrace();
                reader.close();
                return null;
            }
            arrWords = new Word[rowCount];
            while((readLine=reader.readLine())!=null){
                Word newWord = new Word();
                switch(readLine.charAt(0)){
                    case WORD:
                        word=readLine.substring(1);
                        break;
                    case TYPE:
                        type=readLine.substring(1);
                        break;
                    case DEFINITION:
                        def=readLine.substring(1);
                        break;
                    case EXAMPLE:
                        ex=readLine.substring(1);
                        break;
                    case DIVIDER:
                        newWord.setWord(word);
                        newWord.setType(type);
                        newWord.setDefinition(def);
                        newWord.setExample(ex);
                        arrWords[i++] = newWord;
                    default:
                        break;
                }
            }
            reader.close();
            return arrWords;

        }catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }



    private Word[] readFile(BufferedReader reader){
        int rowCount = 0, i=0;
        String readLine, word=null, type=null, def=null, ex=null;
        Word[] arrWords;

        try{
            try{
                rowCount = Integer.parseInt(reader.readLine());
            }catch (NumberFormatException nfe){
                nfe.printStackTrace();
                reader.close();
                return null;
            }
            arrWords = new Word[rowCount];
            while((readLine=reader.readLine())!=null){
                Word newWord = new Word();
                switch(readLine.charAt(0)){
                    case WORD:
                        word=readLine.substring(1);
                        break;
                    case TYPE:
                        type=readLine.substring(1);
                        break;
                    case DEFINITION:
                        def=readLine.substring(1);
                        break;
                    case EXAMPLE:
                        ex=readLine.substring(1);
                        break;
                    case DIVIDER:
                        newWord.setWord(word);
                        newWord.setType(type);
                        newWord.setDefinition(def);
                        newWord.setExample(ex);
                        arrWords[i++] = newWord;
                    default:
                        break;
                }
            }
            reader.close();
            return arrWords;

        }catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }
*/

}