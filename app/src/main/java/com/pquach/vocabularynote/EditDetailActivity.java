package com.pquach.vocabularynote;



import java.util.Locale;
import com.pquach.vocabularynote.R;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;


public class EditDetailActivity extends ActionBarActivity{

	private long mId;
	private EditText edit_word ;
    private EditText edit_definition ;
    private EditText edit_example;
    private Spinner spinner ;
    private ImageButton btn_web;
    private Word word = new Word();
	private WordDataSource mWordds; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_word);

		//-------Get reference to activity's controls----------------------------
		edit_word = (EditText) findViewById(R.id.et_word);
		edit_definition = (EditText) findViewById(R.id.et_definition);
		edit_example = (EditText) findViewById(R.id.et_example);
		spinner = (Spinner) findViewById(R.id.spinner_type);
		
		//----------Set control's properties------------------
		
		edit_word.setSelectAllOnFocus(true);
		edit_definition.setSelectAllOnFocus(true);
		edit_example.setSelectAllOnFocus(true);
		
		//-----------Bind data to spinner------------------
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_type, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		// ----------Bind data to controls-----------------------
		mId = getIntent().getLongExtra("id", -1);
		word = new Word();
		mWordds = new WordDataSource(this);
		word = mWordds.getWord((int)mId);
		if(mId != -1 && word != null){
			edit_word.setText(word.getWord());
			edit_definition.setText(word.getDefinition());
			edit_example.setText(word.getExample());
			spinner.setSelection(adapter.getPosition(word.getType()));
		}
		
		//------Handle web button-----------
				btn_web = (ImageButton) findViewById(R.id.btn_Web);
				btn_web.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						edit_word = (EditText) findViewById(R.id.et_word);
						Toast toast;
						int networkAvailability = checkNetWorkAvailability();
						String dictionary = getDictionary();
						if(networkAvailability == 1){ // if there is network available
							if(edit_word.getText().length()>0){
								if(dictionary == null){
									DictionaryDialog dialog = new DictionaryDialog(EditDetailActivity.this);
									dialog.show();
								}else{
									Intent intent = getIntent();
									intent.putExtra("word",edit_word.getText().toString());
									intent.putExtra("url", dictionary);
									intent.setClassName(getPackageName(), "com.pquach.vocabularynote.WebViewerActivity");
									startActivity(intent);
								}
							}
							else{
								toast = Toast.makeText(getApplicationContext(), "Please enter your word", Toast.LENGTH_SHORT);
								toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL,0,0);
								toast.show();
							}
						}
						else{
							String msg;
						    if(networkAvailability == -1){
						    	msg = "No Wi-Fi connection available. You can enable mobile data connection by unchecking Wi-Fi Only in Settings";
						    }else{ // this happens when networkAvailability == 0
						    	msg = "No Internet connection available";
						    }
							toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
							toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0,0);
							toast.show();
						}
					}
				});
		
	}
	
	@Override
	public void onPause(){
		mWordds.close();
		super.onPause();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_new_word_action, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_bar_btn_save:
	        	if(Update()){
		        	// start main activity
	        		Intent intent = new Intent();
	    			intent.putExtra("id", mId);
	    			intent.setClassName("com.pquach.vocabularynote", "com.pquach.vocabularynote.WordDetailActivity");
	    			startActivity(intent);
	        	}else{
	        		edit_word.requestFocus();
	        	}
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}   
	
	public boolean Update(){
		//Get data from activity's controls
		edit_word = (EditText) findViewById(R.id.et_word);
		edit_definition = (EditText) findViewById(R.id.et_definition);
		edit_example = (EditText) findViewById(R.id.et_example);
		spinner = (Spinner) findViewById(R.id.spinner_type);
		 
		// If no word entered, return false
		if(edit_word.getText().length()<=0){
			edit_word.requestFocus();
			return false;
		}
		// Add new word into Word table
		Word word = new Word();
		word.setId((int)mId);
		String tempWord = edit_word.getText().toString();
		word.setWord(tempWord.substring(0, 1).toUpperCase(Locale.US) + tempWord.substring(1));
		word.setType(spinner.getSelectedItem().toString());
		word.setDefinition(edit_definition.getText().toString());
		word.setExample(edit_example.getText().toString());
		WordDataSource wordds = new WordDataSource(this);
		if(wordds.update(word)<1) {// if no row gets updated, return false
            return false;
        }
		return true;
	}
	
	private String getDictionary(){
		String dictionary = null;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		dictionary = sharedPref.getString(SettingsActivity.KEY_PREF_DICTIONARY, "");
		if(dictionary.equals("null")){
			return null;
		}
		return dictionary;
	}
	 
	private int checkNetWorkAvailability() {
		if(isWifiOnly()){ // check if wifiOnly is checked in settings
			if(isWifiConnected()){ // if wifiOnly is checked in settings, check if wifi is connected
				return 1; // return 1 if wifi is connected
			}else {
				return -1; // return -1 if wifi is not connected
			}
		}else if (isNetworkConnected()){// this  happens when wifiOnly is not checked in Settings, 
			                            //so the app can use either wifi or mobile data to connect to the Internet
			                            // isNetworkConnected checks if there is any network connection is available, either wifi od mobile data
			return 1; // return 1 if either wifi or mobile data is connected
		}else{
			return 0; // return 0 if no connection is available
		}
	}
    // this method checks if there is any network connection is available, either wifi or mobile data 
	private boolean isNetworkConnected() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	private boolean isWifiConnected(){
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifi != null && wifi.isConnected();
	}
	
	private boolean isWifiOnly(){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean wifiOnly = sharedPref.getBoolean(SettingsActivity.KEY_PREF_WIFI_ONLY,false);
		return wifiOnly;
	}
	
	class DictionaryDialog extends AlertDialog implements DialogInterface.OnClickListener{

		private String mSelectedDictionay;
		private AlertDialog.Builder mBuilder;

		public String getSelectedDictionary(){
			return mSelectedDictionay;
		}
		public DictionaryDialog(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			createDialog(context);
		}
		
		public void createDialog(Context context){
			mBuilder = new Builder(context);
			mBuilder.setTitle("Select a dictionary");
			mBuilder.setItems(R.array.dictionary_entries, this);
			mBuilder.create();
		}
		
		@Override
		public void show(){
			mBuilder.show();
		}
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			//String[] dictionaryList = getResources().getStringArray(R.array.pref_dictionary_entries);
			String[] urls = getResources().getStringArray(R.array.pref_dictionary_values);
			mSelectedDictionay = urls[which];
			
			Intent intent = getIntent();
			intent.putExtra("word",edit_word.getText().toString());
			intent.putExtra("url", mSelectedDictionay);
			intent.setClassName(getPackageName(), "com.pquach.vocabularynote.WebViewerActivity");
			startActivity(intent);
		}
		
	}
	
	
}
