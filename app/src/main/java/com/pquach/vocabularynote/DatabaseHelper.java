package com.pquach.vocabularynote;
import android.content.Context;
import android.database.sqlite.*;


public class DatabaseHelper extends SQLiteOpenHelper {
	
	//==========================================================================//
    //***-----------------------------PROPERTIES-----------------------------***//
    //==========================================================================//
	
	//-------------Creating-table variables-----------------//
	private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
	// Word table
	private static final String SQL_CREATE_TABLE_WORD = 
			"CREATE TABLE "+ VobNoteContract.Word.TABLE_NAME + "(" + 
			VobNoteContract.Word.COLUMN_NAME_WORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
			VobNoteContract.Word.COLUMN_NAME_WORD + TEXT_TYPE + COMMA_SEP +
			VobNoteContract.Word.COLUMN_NAME_TYPE + TEXT_TYPE  + COMMA_SEP +
			VobNoteContract.Word.COLUMN_NAME_DEFINITION + TEXT_TYPE  + COMMA_SEP +
			VobNoteContract.Word.COLUMN_NAME_EXAMPLE + TEXT_TYPE  + COMMA_SEP +
			VobNoteContract.Word.COLUMN_NAME_CATEGORY + INTEGER_TYPE + ")";
	
	
	// Category table
    private static final String SQL_CREATE_TABLE_CATEGORY =
            "CREATE TABLE "+ VobNoteContract.Category.TABLE_NAME + "(" +
                    VobNoteContract.Category.COLUMN_NAME_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME + TEXT_TYPE + ")";

	//-----------------Creating-database variables--------------------//
	 // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 10;
    public static final String DATABASE_NAME = "VobNote.db";
	
	//==========================================================================//
    //***------------------------------METHODS-------------------------------***//
    //==========================================================================//
    
    // Constructor
    public  DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create Word table
		db.execSQL(SQL_CREATE_TABLE_WORD);
        // Create Category table
        db.execSQL(SQL_CREATE_TABLE_CATEGORY);
	}
	
	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + VobNoteContract.Word.TABLE_NAME );
        db.execSQL("DROP TABLE IF EXISTS " + VobNoteContract.Category.TABLE_NAME );
		onCreate(db);
	}
}