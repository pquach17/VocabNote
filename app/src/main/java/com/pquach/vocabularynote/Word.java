package com.pquach.vocabularynote;

import java.util.Comparator;


public class Word {
	/*--------PROPERTIES----------*/
	private long mId;
	private String mWord;
	private String mType;
	private String mDefinition;
	//String mTranslation;
	private String mExample;
	//Date mCreatedDate;
	
	
	/*--------METHODS----------*/
	public Word(){
		
	}
    public Word(String word){
        mWord = word;
    }
	public long getId(){
		return mId;
	}
	public void setId(long value){
		mId = value;
	}
	

	
	public String getWord(){
		return mWord;
	}
	public void setWord(String value){
		mWord = value;
	}
	
	public String getType(){
		return mType;
	}
	public void setType(String value){
		mType = value;
	}
	
	public String getDefinition()
	{
		return mDefinition;
	}
	public void setDefinition(String value){
		mDefinition = value;
	}
	
	public String getExample()
	{
		return mExample;
	}
	public void setExample(String value)
	{
		mExample = value;
	}
	
	public static class WordComparator implements Comparator<Word>{
		@Override
		public int compare(Word lhs, Word rhs) {
			// TODO Auto-generated method stub
			return lhs.getWord().compareToIgnoreCase(rhs.getWord());
		}
	}
	
	public static class IdComparator implements Comparator<Word>{
		@Override
		public int compare(Word lhs, Word rhs) {
			// TODO Auto-generated method stub
			return  (int)(lhs.getId() - rhs.getId());
		}
	}
}