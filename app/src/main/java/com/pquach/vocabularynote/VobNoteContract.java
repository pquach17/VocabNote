package com.pquach.vocabularynote;

import android.provider.BaseColumns;

public class VobNoteContract {
	
	public static abstract class Word implements BaseColumns {
	    public static final String TABLE_NAME = "word";
	    public static final String COLUMN_NAME_WORD_ID = "id";
	    public static final String COLUMN_NAME_WORD = "word";
	    public static final String COLUMN_NAME_TYPE = "type";
	    public static final String COLUMN_NAME_DEFINITION = "definition";
	    public static final String COLUMN_NAME_EXAMPLE = "example";
	}
	
	private VobNoteContract(){}
}
