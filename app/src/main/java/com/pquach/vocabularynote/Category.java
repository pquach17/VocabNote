package com.pquach.vocabularynote;

import android.content.Context;

/**
 * Created by HP on 30/04/2015.
 */
public class Category {
    private long mId;
    private String mCategoryName;

    public Category(){}
    public Category(String categoryName){
        mCategoryName = categoryName;
    }

    public long getId(){
        return mId;
    }
    public void setId(long id) { mId = id;}

    public String getCategoryName(){
        return mCategoryName;
    }
    public void setCategoryName(String value){
        mCategoryName = value;
    }

    public static long delete(Context context, long categoryId){
        long result = 0;
        // delete all data in this category
        WordDataSource wordDataSource = new WordDataSource(context);
        wordDataSource.deleteWordsInCategory(categoryId);
        long i = wordDataSource.getWordsInCategory(categoryId).getCount();
        if( i < 1){
            // delete this category
            CategoryDataSource categoryDataSource = new CategoryDataSource(context);
            result = categoryDataSource.delete(categoryId);
        }
        return result;
    }


}
