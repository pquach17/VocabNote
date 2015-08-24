package com.pquach.vocabularynote;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;


public class DatabaseHelper extends SQLiteOpenHelper {
	
	//==========================================================================//
    //***-----------------------------PROPERTIES-----------------------------***//
    //==========================================================================//
	Context context;
	//-------------Creating-table variables-----------------//
	private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
	private static final String CATEGORY_DEFAULT_ITEM = "List 1";
	// Word table
	private static final String SQL_CREATE_TABLE_WORD = 
			"CREATE TABLE "+ VobNoteContract.Word.TABLE_NAME + "(" + 
			VobNoteContract.Word.COLUMN_NAME_WORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
			VobNoteContract.Word.COLUMN_NAME_WORD + TEXT_TYPE + COMMA_SEP +
			VobNoteContract.Word.COLUMN_NAME_TYPE + TEXT_TYPE  + COMMA_SEP +
			VobNoteContract.Word.COLUMN_NAME_DEFINITION + TEXT_TYPE  + COMMA_SEP +
			VobNoteContract.Word.COLUMN_NAME_EXAMPLE + TEXT_TYPE  + COMMA_SEP +
			VobNoteContract.Word.COLUMN_NAME_CATEGORY + INTEGER_TYPE + " NOT NULL)";
	// Add column Category to table Word
	/*private static final  String SQL_ADD_CAT_COLUMN_TO_TABLE_WORD =
			"ALTER TABLE "+ VobNoteContract.Word.TABLE_NAME + " ADD COLUMN " + VobNoteContract.Word.COLUMN_NAME_CATEGORY +
			" " + INTEGER_TYPE;*/

	// Category table
    private static final String SQL_CREATE_TABLE_CATEGORY =
            "CREATE TABLE "+ VobNoteContract.Category.TABLE_NAME + "(" +
                    VobNoteContract.Category.COLUMN_NAME_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME + TEXT_TYPE + ")";

	private static final String SQL_INSERT_DEFAULT_CATEGORY =
			"INSERT INTO " + VobNoteContract.Category.TABLE_NAME + " ("+VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME+") " +
					"VALUES ('"+CATEGORY_DEFAULT_ITEM+"')";

	//-----------------Creating-database variables--------------------//
	 // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 8;
    public static final String DATABASE_NAME = "VobNote.db";
	
	//==========================================================================//
    //***------------------------------METHODS-------------------------------***//
    //==========================================================================//
    
    // Constructor
    public  DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create Category table
		db.execSQL(SQL_CREATE_TABLE_CATEGORY);
		ContentValues values = new ContentValues();
		values.put(VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME, CATEGORY_DEFAULT_ITEM);
		db.insert(VobNoteContract.Category.TABLE_NAME, "", values);
		// Create Word table
		db.execSQL(SQL_CREATE_TABLE_WORD);
	}
	
	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//db.execSQL("DROP TABLE IF EXISTS " + VobNoteContract.Word.TABLE_NAME );
        //db.execSQL("DROP TABLE IF EXISTS " + VobNoteContract.Category.TABLE_NAME );
		//onCreate(db);
		int i=0;
		switch(oldVersion){
			case 7:
				// Create Category table
				db.execSQL(SQL_CREATE_TABLE_CATEGORY);
				ContentValues values = new ContentValues();
				values.put(VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME, CATEGORY_DEFAULT_ITEM);
				long id = db.insert(VobNoteContract.Category.TABLE_NAME, "", values);

				// Alter Word table and insert Category's default item into table Word
				String SQL_ADD_CAT_COLUMN_TO_TABLE_WORD =
						"ALTER TABLE "+ VobNoteContract.Word.TABLE_NAME + " ADD COLUMN " + VobNoteContract.Word.COLUMN_NAME_CATEGORY +
								INTEGER_TYPE + " NOT NULL DEFAULT " + id;
				db.execSQL(SQL_ADD_CAT_COLUMN_TO_TABLE_WORD);
		}
	}

	private void upgradeDBFromVersionSeven(SQLiteDatabase db){
		// Create table Category
		db.execSQL(SQL_CREATE_TABLE_CATEGORY);
		// Add column Category into table Word
		db.execSQL("ALTER TABLE "+VobNoteContract.Word.TABLE_NAME+" ADD COLUMN "+
				VobNoteContract.Word.COLUMN_NAME_CATEGORY+" "+ INTEGER_TYPE);

	}
}