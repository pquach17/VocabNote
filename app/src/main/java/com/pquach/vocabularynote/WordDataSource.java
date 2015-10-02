package com.pquach.vocabularynote;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WordDataSource {
	
	private SQLiteDatabase mSQLiteDb;
	private DatabaseHelper mDbHelper;

	public WordDataSource(Context context){
		mDbHelper = new DatabaseHelper(context);
	}
	

	
	public void close(){
		mSQLiteDb.close();
		mDbHelper.close();
	}
	public long insert(Word word){
		mSQLiteDb = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(VobNoteContract.Word.COLUMN_NAME_WORD, word.getWord());
		values.put(VobNoteContract.Word.COLUMN_NAME_TYPE, word.getType());
		values.put(VobNoteContract.Word.COLUMN_NAME_DEFINITION, word.getDefinition());
		values.put(VobNoteContract.Word.COLUMN_NAME_EXAMPLE, word.getExample());
		values.put(VobNoteContract.Word.COLUMN_NAME_CATEGORY, word.getCategory());
		long isDone = mSQLiteDb.insert(VobNoteContract.Word.TABLE_NAME, "", values);
		mSQLiteDb.close();
		return isDone;
	}
	
	public long update(Word word){
		mSQLiteDb = mDbHelper.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put(VobNoteContract.Word.COLUMN_NAME_WORD, word.getWord());
		values.put(VobNoteContract.Word.COLUMN_NAME_TYPE, word.getType());
		values.put(VobNoteContract.Word.COLUMN_NAME_DEFINITION, word.getDefinition());
		values.put(VobNoteContract.Word.COLUMN_NAME_EXAMPLE, word.getExample());
		values.put(VobNoteContract.Word.COLUMN_NAME_CATEGORY, word.getCategory());
		long isDone = mSQLiteDb.update(VobNoteContract.Word.TABLE_NAME, values, "id=?", new String[]{String.valueOf(word.getId())});
		mSQLiteDb.close();
		return isDone;
	}
	
	public long delete (String id){
		String[] whereArgs = {id};
		mSQLiteDb = mDbHelper.getReadableDatabase();
		long isDone = mSQLiteDb.delete(VobNoteContract.Word.TABLE_NAME, "id = ?", whereArgs);
		mSQLiteDb.close();
		return isDone;
	}

	public  long deleteWordsInCategory(long categoryId){
		String[] whereArgs = {String.valueOf(categoryId)};
		mSQLiteDb = mDbHelper.getReadableDatabase();
		long isDone = mSQLiteDb.delete(VobNoteContract.Word.TABLE_NAME, VobNoteContract.Word.COLUMN_NAME_CATEGORY + " = ?", whereArgs);
		mSQLiteDb.close();
		return isDone;
	}

	public Cursor getWordsFromAllCategories(){
		mSQLiteDb = mDbHelper.getReadableDatabase();
		Cursor cur = mSQLiteDb.rawQuery("SELECT id as _id, * FROM " + VobNoteContract.Word.TABLE_NAME,new String [] {});
		return cur;
	}

	public Cursor selectWordJoinCategory(){
		mSQLiteDb = mDbHelper.getReadableDatabase();
		Cursor cur = mSQLiteDb.rawQuery("SELECT * FROM " + VobNoteContract.Word.TABLE_NAME +
				" INNER JOIN " + VobNoteContract.Category.TABLE_NAME +
				" ON "+VobNoteContract.Word.TABLE_NAME + "." + VobNoteContract.Word.COLUMN_NAME_CATEGORY + "=" +
				VobNoteContract.Category.TABLE_NAME + "." + VobNoteContract.Category.COLUMN_NAME_CATEGORY_ID,new String [] {});
		return cur;
	}

	/**
	 * select Word table with a category id in the WHERE clause
	 * @param categoryId
	 * @return a cursor contains words in a selected category
	 */
	public  Cursor getWordsInCategory(long categoryId){
		mSQLiteDb = mDbHelper.getReadableDatabase();
		Cursor cur = mSQLiteDb.rawQuery("SELECT id as _id, * FROM " + VobNoteContract.Word.TABLE_NAME +
				" WHERE " + VobNoteContract.Word.COLUMN_NAME_CATEGORY + "=" + categoryId, new String [] {});
		return cur;
	}

	public Cursor sortWordsInCategory(String direction, long categoryId){
		mSQLiteDb = mDbHelper.getReadableDatabase();
		String queryString = "SELECT id as _id, * FROM " + VobNoteContract.Word.TABLE_NAME +
				" WHERE " + VobNoteContract.Word.COLUMN_NAME_CATEGORY + "=" + categoryId +
				" ORDER BY " + VobNoteContract.Word.COLUMN_NAME_WORD + " " + direction ;
		Cursor cur = mSQLiteDb.rawQuery(queryString,new String [] {});
		return cur;
	}

    /**
     * Select rows based on the provided word types
     * @param types an array of word types that will be in the WHERE clause of the SELECT statement
     * @return a Cursor which is the result of the SELECT statement. If the array types is empty, the SELECT statement will return all rows in the table
     */
	public Cursor selectByTypes(String[] types, long categoryId){
		Cursor cur;
		if(types.length==0){
			cur = this.getWordsInCategory(categoryId);
		} else{
			mSQLiteDb = mDbHelper.getReadableDatabase();
			String queryString = "SELECT id as _id, * FROM " + VobNoteContract.Word.TABLE_NAME + " WHERE " +
					VobNoteContract.Word.COLUMN_NAME_CATEGORY + " = " + categoryId + " AND (" +
					VobNoteContract.Word.COLUMN_NAME_TYPE + " = ?";
			for(int i=1; i<types.length; i++){
				queryString += " OR " + VobNoteContract.Word.COLUMN_NAME_TYPE + " = ?";
			}
			queryString += ")";
			cur = mSQLiteDb.rawQuery(queryString,types);
		}
		return cur;
	}
	
	public Word getWord(long id){
		mSQLiteDb = mDbHelper.getReadableDatabase();
		Cursor cur = mSQLiteDb.rawQuery("SELECT  * FROM " + VobNoteContract.Word.TABLE_NAME + " WHERE id = " + id,new String [] {});
		if(cur.moveToFirst())
		{
			Word word = new Word();
			word.setId(cur.getLong(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_WORD_ID)));
			word.setWord(cur.getString(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_WORD)));
			word.setType(cur.getString(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_TYPE)));
			word.setDefinition(cur.getString(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_DEFINITION)));
			word.setExample(cur.getString(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_EXAMPLE)));
			word.setCategory(cur.getLong(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_CATEGORY)));
			return word;
		}
		cur.close();
		mSQLiteDb.close();
		return null;
	}
	
	public ArrayList<Word> getWordArray(long categoryId){
		ArrayList<Word> arrWords = new ArrayList<Word>();
		Cursor cur = this.getWordsInCategory(categoryId);
		if(cur.moveToFirst()){// check if cursor is empty
			do{
				Word word = new Word();
				word.setId(cur.getInt(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_WORD_ID)));
				word.setWord(cur.getString(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_WORD)));
				word.setType(cur.getString(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_TYPE)));
				word.setDefinition(cur.getString(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_DEFINITION)));
				word.setExample(cur.getString(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_EXAMPLE)));
				word.setCategory(cur.getLong(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_CATEGORY)));
				arrWords.add(word);
			} while(cur.moveToNext());
		}
		return arrWords;
	}
	
	
}
