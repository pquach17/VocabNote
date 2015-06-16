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
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;


public class FileProcessor {

    final char WORD = '@';
    final char TYPE = '%';
    final char DEFINITION = '$';
    final char EXAMPLE = '&';
    final char DIVIDER = '^';
    Context mContext;
    long mCategoryId;

    public FileProcessor(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
        mCategoryId = 0;
    }

    public boolean exportData(OutputStream outstr){
        //--Load data
        WordDataSource wordds = new WordDataSource(mContext, mCategoryId);
        Cursor cur = wordds.getFromAllCategory();
        return writeFile(outstr, cur);
    }

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


}