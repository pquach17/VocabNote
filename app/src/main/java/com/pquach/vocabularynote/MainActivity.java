package com.pquach.vocabularynote;

import java.util.ArrayList;
import java.util.Collections;
import com.pquach.vocabularynote.R;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;

public class MainActivity extends  ActionBarActivity {
 
	static final int ALERT_DIALOG_FILTER = 0;
	static final int ALERT_DIALOG_SORT = 1;
	static final int SORT_BY_WORD = 0;
	static final int SORT_BY_ID = 1;
	static final int ASCENDING = 0;
	static final int DESCENDING = 1;
	private WordDataSource mWordDS;
	ListView mListview;
	TextView mTv_instruct_msg;
	ArrayList<Integer> mSelectedItems;
	boolean[] mCheckedItems;
	int[] mSelectedSortingCondition = new int[2];
	int mCheckedCondition;
	AlertDialog mFilterDialog;
	AlertDialog mSortDialog;
	android.support.v4.widget.SimpleCursorAdapter  mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mWordDS = new WordDataSource(this);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		mListview = (ListView) findViewById(R.id.lv_wordlist);
		registerForContextMenu(mListview);
		Cursor cur;
		
		//---Restore saved instance state-----
		if(savedInstanceState != null){
			mSelectedItems = savedInstanceState.getIntegerArrayList("mSelectedItems");
			mCheckedItems = savedInstanceState.getBooleanArray("mCheckedItems");
			mCheckedCondition = savedInstanceState.getInt("mCheckedCondition");
			mSelectedSortingCondition = savedInstanceState.getIntArray("mSelectedSortingCondition");
			cur = filter();
		} else{
			mCheckedItems = new boolean[getResources().getStringArray(R.array.spinner_type).length];
			mCheckedCondition = 2; // initially, data is loaded into list view in "Oldest first" order
								   // this makes sure the ratio button is checked at "Oldest first" 
			mSelectedSortingCondition[0] = SORT_BY_ID;
			mSelectedSortingCondition[1]= ASCENDING;
			mSelectedItems = new ArrayList<Integer>();
			cur = this.loadWordList();
		}						
		
		//-----Create list view----------
		
		// Bind data into List view
		String[] from = {VobNoteContract.Word.COLUMN_NAME_WORD, VobNoteContract.Word.COLUMN_NAME_TYPE};
		int[] to = {R.id.tv_list_row, R.id.tv_list_row_type};
		mAdapter = new android.support.v4.widget.SimpleCursorAdapter(this, R.layout.word_list_layout, cur, from, to, 0);
		if(cur.getCount()>=1) // getCount() return number of rows in the cursor
		{
			//  sort list view
			cur = sortWordList(cur, mSelectedSortingCondition[0], mSelectedSortingCondition[1]);
			mAdapter.swapCursor(cur);	
		}
		mListview.setAdapter(mAdapter);
		mListview.setOnItemClickListener(new WordListViewListener());
		// Display instruction message when the list is empty
		mTv_instruct_msg = (TextView) findViewById(R.id.tv_instruct_msg);
	    mListview.setEmptyView(mTv_instruct_msg);
	    cur.close();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		  super.onSaveInstanceState(savedInstanceState);
		  savedInstanceState.putBooleanArray("mCheckedItems", mCheckedItems);
		  savedInstanceState.putInt("mCheckedCondition", mCheckedCondition);
		  savedInstanceState.putIntegerArrayList("mSelectedItems", mSelectedItems);
		  savedInstanceState.putIntArray("mSelectedSortingCondition", mSelectedSortingCondition);
		}
	
	@Override
	public void onPause(){
		mWordDS.close();
		super.onPause();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.context_menu_listview, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// Handle presses on the action bar items
		Intent intent = new Intent();
	    switch (item.getItemId()) {
	        case R.id.action_new_word:
                intent.setClass(getBaseContext(), NewWordActivity.class);
				startActivity(intent);
	            return true;
	        case R.id.action_sort:
	        	mSortDialog = createDialog(ALERT_DIALOG_SORT);
	        	mSortDialog.show();
	        	return true;
	        case R.id.action_filter:
	        	mFilterDialog = createDialog(ALERT_DIALOG_FILTER);
	        	mFilterDialog.show();
	        	return true;
	        case R.id.action_flash_card:
				intent.setClass(getBaseContext(), FlashCardActivity.class);
				startActivity(intent);
				return true;
	        case R.id.action_settings:
                intent.setClass(getBaseContext(), SettingsActivity.class);
				startActivity(intent);
                return true;
            case R.id.action_backup:
                intent.setClass(getBaseContext(), BackupActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_restore:
                intent.setClass(getBaseContext(), RestoreActivity.class);
                startActivity(intent);
                return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {
	        case R.id.context_delete:
	        	showDeleteAlerDialog(this,(int) info.id);
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	public void delete(int wordId){
		mWordDS.delete(String.valueOf(wordId));
	}
	
	public void showDeleteAlerDialog(Context context, int wordId){
		AlertDialog dlg = new AlertDialog.Builder(context).create();
		dlg.setMessage("Do you want to delete this word?");
		dlg.setTitle("Delete");
		dlg.setCancelable(true);
		AlertDialogListener dialogOnClickListener  = new AlertDialogListener(wordId);
		dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", dialogOnClickListener );
		dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "No", dialogOnClickListener);
		dlg.show();
	}
	
	private AlertDialog createDialog(int id){
		
		//mSelectedItems = new ArrayList<Integer>();
		AlertDialog.Builder builder = new Builder(this);
		switch(id){
		case ALERT_DIALOG_FILTER:
			builder.setTitle("Select types")
			   .setMultiChoiceItems(R.array.spinner_type, mCheckedItems, new OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					// TODO Auto-generated method stub
					if(isChecked){
						mSelectedItems.add(which); // add checked item into an array
						mCheckedItems[which] = true;
					}else if(mSelectedItems.contains(which)){
						mSelectedItems.remove(Integer.valueOf(which));// if item unchecked, remove it from the array
						mCheckedItems[which] = false;
					}
				}
			   })
			   .setPositiveButton("OK", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Cursor cur = filter();
					mAdapter.swapCursor(cur);
					cur = sortWordList(mAdapter.getCursor(), mSelectedSortingCondition[0],mSelectedSortingCondition[1]);
					mAdapter.swapCursor(cur);
					mListview.setAdapter(mAdapter);
					cur.close();
				}
			});
			break;
		case ALERT_DIALOG_SORT:
			builder.setTitle("Sort by")
			   .setSingleChoiceItems(R.array.sort_condition, mCheckedCondition, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mCheckedCondition = which;
					Cursor cur;
					String[] arr = getResources().getStringArray(R.array.sort_condition);
					if(arr[which].equalsIgnoreCase("A to Z")){
						mSelectedSortingCondition[0] = SORT_BY_WORD;
						mSelectedSortingCondition[1] = ASCENDING;
						cur = sortWordList(mAdapter.getCursor(),SORT_BY_WORD, ASCENDING);
					} else if(arr[which].equalsIgnoreCase("Z to A")){
						mSelectedSortingCondition[0] = SORT_BY_WORD;
						mSelectedSortingCondition[1] = DESCENDING;
						cur = sortWordList(mAdapter.getCursor(),SORT_BY_WORD, DESCENDING);
					} else if(arr[which].equalsIgnoreCase("Oldest first")){
						mSelectedSortingCondition[0] = SORT_BY_ID;
						mSelectedSortingCondition[1] = ASCENDING;
						cur = sortWordList(mAdapter.getCursor(),SORT_BY_ID, ASCENDING);
					} else{
						mSelectedSortingCondition[0] = SORT_BY_ID;
						mSelectedSortingCondition[1] = DESCENDING;
						cur = sortWordList(mAdapter.getCursor(),SORT_BY_ID, DESCENDING);
					}
					dialog.dismiss();
					// Bind data to List view
					mAdapter.swapCursor(cur);
					mListview.setAdapter(mAdapter);
					cur.close();
				}
			});
			break;
		default:
			break;
		}
		return builder.create();
	}
	
	private Cursor loadWordList(){
		Cursor cursor = mWordDS.getAll();
		return cursor;
	}
	
	protected void refreshListViewAdapter(){
		Cursor cur = loadWordList();
		cur = filter();
		cur = sortWordList(cur, mSelectedSortingCondition[0], mSelectedSortingCondition[1]);
		mAdapter.swapCursor(cur);
		mListview.setAdapter(mAdapter);
		cur.close();
	}
	private Cursor sortWordList(Cursor cursor, int condition, int direction){
		ArrayList<Word> arr = new ArrayList<Word>();
		arr = copyCursorToArray(cursor);
		switch(condition){
		case SORT_BY_WORD:
			if(direction == ASCENDING){
				Collections.sort(arr, new Word.WordComparator());
			} else{
				Collections.sort(arr, new Word.WordComparator());
				Collections.reverse(arr);
			}
			break;
		case SORT_BY_ID:
			if(direction == ASCENDING){
				Collections.sort(arr, new Word.IdComparator());
			} else{
				Collections.sort(arr, new Word.IdComparator());
				Collections.reverse(arr);
			}
			break;
		default:
			break;
		}

		Cursor cur = copyArrayToCursor(arr, cursor.getColumnNames());
		return cur;
	}
	
	protected Cursor filter(){
		String[] arrSelectedTypes;
		if(mSelectedItems == null)
			arrSelectedTypes = new String[] {};
		else
			arrSelectedTypes= new String[mSelectedItems.size()];
		String[] arrTypes = getResources().getStringArray(R.array.spinner_type);
		int j;
		for(int i=0; i<mSelectedItems.size(); i++){
			j=mSelectedItems.get(i).intValue();
			arrSelectedTypes[i] = arrTypes[j];// get the string value of the selected items
											  // mSelectedItems contains position of selected items in R.array.spinner_type
		}
		Cursor cur = mWordDS.selectByTypes(arrSelectedTypes);
		return cur;
	}
	
	private ArrayList<Word> copyCursorToArray(Cursor cur){
		ArrayList<Word> arr = new ArrayList<Word>();
		if(cur.moveToFirst()){// check if cursor is empty
			do{
				Word word = new Word();
				word.setId(cur.getInt(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_WORD_ID)));
				word.setWord(cur.getString(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_WORD)));
				word.setType(cur.getString(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_TYPE)));
				word.setDefinition(cur.getString(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_DEFINITION)));
				word.setExample(cur.getString(cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_EXAMPLE)));
				arr.add(word);
			} while(cur.moveToNext());
		}
		return arr;
	}
	
	private Cursor copyArrayToCursor(ArrayList<Word> arr, String[] columnNames){
		MatrixCursor cur = new MatrixCursor(columnNames);
		String[] values = new String[columnNames.length];
		for(int i =0; i<arr.size(); i++){
			values[cur.getColumnIndex("_id")] = String.valueOf(arr.get(i).getId());
			values[cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_WORD_ID)] = String.valueOf(arr.get(i).getId());
			values[cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_WORD)] = arr.get(i).getWord();
			values[cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_TYPE)] = arr.get(i).getType();
			values[cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_DEFINITION)] = arr.get(i).getDefinition();
			values[cur.getColumnIndex(VobNoteContract.Word.COLUMN_NAME_EXAMPLE)] = arr.get(i).getExample();
			cur.addRow(values);
		}
		return cur;
	}

	
	class WordListViewListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> listview, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.putExtra("id", id);
			intent.setClassName("com.pquach.vocabularynote", "com.pquach.vocabularynote.WordDetailActivity");
			startActivity(intent);
		}
	}
	
	class AlertDialogListener implements  DialogInterface.OnClickListener{
		
		int id;
		public AlertDialogListener(int wordId) {
			// TODO Auto-generated constructor stub
			id = wordId;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			switch(which){
			case DialogInterface.BUTTON_POSITIVE:
				delete(id);
				refreshListViewAdapter();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
		
	}
}


