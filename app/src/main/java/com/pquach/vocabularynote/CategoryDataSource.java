package com.pquach.vocabularynote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by HP on 30/04/2015.
 */
public class CategoryDataSource {
    private SQLiteDatabase mSQLiteDb;
    private DatabaseHelper mDbHelper;

    public CategoryDataSource(Context context){
        mDbHelper = new DatabaseHelper(context);
    }

    public void close(){
        mSQLiteDb.close();
        mDbHelper.close();
    }

    public long insert(Category category){
        mSQLiteDb = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME, category.getCategoryName());
        long isDone = mSQLiteDb.insert(VobNoteContract.Category.TABLE_NAME, "", values);
        mSQLiteDb.close();
        return isDone;
    }

    public long update(Category category){
        mSQLiteDb = mDbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME, category.getCategoryName());
        long isDone = mSQLiteDb.update(VobNoteContract.Category.TABLE_NAME, values, "id=?", new String[]{String.valueOf(category.getId())});
        mSQLiteDb.close();
        return isDone;
    }

    public long delete (long id){
        String[] whereArgs = {String.valueOf(id)};
        mSQLiteDb = mDbHelper.getReadableDatabase();
        long isDone = mSQLiteDb.delete(VobNoteContract.Category.TABLE_NAME, "id = ?", whereArgs);
        mSQLiteDb.close();
        return isDone;
    }

    public Cursor getAllCategories(){
        mSQLiteDb = mDbHelper.getReadableDatabase();
        Cursor cur = mSQLiteDb.rawQuery("SELECT id as _id, * FROM " + VobNoteContract.Category.TABLE_NAME,new String [] {});
        return cur;
    }

    public Category getCategoryById(long id){
        mSQLiteDb = mDbHelper.getReadableDatabase();
        Cursor cur = mSQLiteDb.rawQuery("SELECT  * FROM " + VobNoteContract.Category.TABLE_NAME + " WHERE id = " + id, new String [] {});
        if(cur.moveToFirst())
        {
            Category category = new Category();
            category.setId(cur.getLong(cur.getColumnIndex(VobNoteContract.Category.COLUMN_NAME_CATEGORY_ID)));
            category.setCategoryName(cur.getString(cur.getColumnIndex(VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME)));
            return category;
        }
        cur.close();
        mSQLiteDb.close();
        return null;
    }

    public Cursor  getCategoriesByName(String categoryName){
        mSQLiteDb = mDbHelper.getReadableDatabase();
        Cursor cur = mSQLiteDb.rawQuery("SELECT  * FROM " + VobNoteContract.Category.TABLE_NAME +
                " WHERE "+ VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME +" = '" + categoryName +"'", new String [] {});
        return cur;
    }

    public boolean isCategoryNameUsed(String categoryName){
        Cursor cur = getCategoriesByName(categoryName);
        if(cur.getCount()>0)
            return true;
        return false;
    }
}
