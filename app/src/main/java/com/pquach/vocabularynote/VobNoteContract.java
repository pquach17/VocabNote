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
		public static final String COLUMN_NAME_CATEGORY = "category";
	}

    public static abstract class Category implements BaseColumns {
        public static final String TABLE_NAME = "category";
        public static final String COLUMN_NAME_CATEGORY_ID = "id";
        public static final String COLUMN_NAME_CATEGORY_NAME = "name";
    }
	private VobNoteContract(){}
}
