package com.pquach.vocabularynote;
import android.content.Context;
import android.database.sqlite.*;


public class DatabaseHelper extends SQLiteOpenHelper {
	
	//==========================================================================//
    //***-----------------------------PROPERTIES-----------------------------***//
    //==========================================================================//
	
	//-------------Creating-table variables-----------------//
	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	// Word table
	private static final String SQL_CREATE_TABLE_WORD = 
			"CREATE TABLE "+ VobNoteContract.Word.TABLE_NAME + "(" + 
			VobNoteContract.Word.COLUMN_NAME_WORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
			VobNoteContract.Word.COLUMN_NAME_WORD + TEXT_TYPE + COMMA_SEP +
			VobNoteContract.Word.COLUMN_NAME_TYPE + TEXT_TYPE  + COMMA_SEP +
			VobNoteContract.Word.COLUMN_NAME_DEFINITION + TEXT_TYPE  + COMMA_SEP +
			VobNoteContract.Word.COLUMN_NAME_EXAMPLE + TEXT_TYPE + ")";
	
	
	
	//-----------------Creating-database variables--------------------//
	 // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "VobNote.db";
	
	//==========================================================================//
    //***------------------------------METHODS-------------------------------***//
    //==========================================================================//
    
    // Constructor
    public  DatabaseHelper(Context context) {
		// TODO Auto-generated constructor stub
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// Create Word table
		db.execSQL(SQL_CREATE_TABLE_WORD);
	}
	
	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + VobNoteContract.Word.TABLE_NAME );
		onCreate(db);
	}
}