package com.pquach.vocabularynote;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.os.Process;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

public class WordDetailActivity extends ActionBarActivity {


	private long mId;
	TextView tv_word ;
	TextView tv_word_type;
	TextView tv_definition ;
	TextView tv_example  ;
	TextView label_definition;
	TextView label_example;
    ImageButton ib_pronounce;
    TextToSpeech tts;
    final static int MY_DATA_CHECK_CODE = 2;
    private boolean ttsAvailable;
    private int languageAvailable;
    private boolean readyToSpeak = false;
    final String ttsPackageName = "com.google.android.tts";
    final String picoPackageName = "com.svox.pico";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_word_detail);
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// -----Enable navigation arrow on action bar-----
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
			
		//------Get word id from previous activity-----------
		mId = getIntent().getLongExtra("id", -1); // id = -1 if there is no value passed to id from previous activity
		
		//-------Bind data to controls------------
		Word word;
		WordDataSource wordds = new WordDataSource(this);
		word = wordds.getWord((int)mId);
		if(mId != -1 && word != null) {
            // Get controls' reference
            tv_word = (TextView) findViewById(R.id.tv_word);
            tv_word_type = (TextView) findViewById(R.id.tv_word_type);
            tv_definition = (TextView) findViewById(R.id.tv_definition);
            tv_example = (TextView) findViewById(R.id.tv_example);
            label_definition = (TextView) findViewById(R.id.label_definition);
            label_example = (TextView) findViewById(R.id.label_example);
            ib_pronounce = (ImageButton) findViewById(R.id.btn_pronounce);
            ib_pronounce.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!ttsAvailable) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + ttsPackageName)));

                        } catch (android.content.ActivityNotFoundException e) {
                            // If no Play Store installed on device, bring user to Play Store website
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + ttsPackageName)));
                        }
                    }else {
                        new LoadTtsTask().execute();
                    }
                }
            });

		   // Bind data into UI
			tv_word.setText(word.getWord());
			tv_word_type.setText(word.getType());
			tv_definition.setText(word.getDefinition());
			tv_example.setText(word.getExample());
			
			// set labels' visibility
			if(word.getDefinition().length() > 0)
				label_definition.setVisibility(View.VISIBLE);
			if(word.getExample().length() > 0)
				label_example.setVisibility(View.VISIBLE);
		}
		else{
			Toast t = Toast.makeText(this, "No detail available", Toast.LENGTH_LONG);
			t.show();
		}
		wordds.close();
	}

    @Override
    protected void onStart() {
        super.onStart();
        ttsAvailable = isTtsAvailable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts!=null){
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_word_detail_action, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_bar_btn_edit:
	        	Intent intent = new Intent();
				intent.putExtra("id", mId);
				intent.setClassName("com.pquach.vocabularynote", "com.pquach.vocabularynote.EditDetailActivity");
				startActivity(intent);
	            return true;
	        case R.id.action_bar_btn_delete:
	        	showDeleteAlerDialog(this); // show an alert dialog and delete the word (delete function is called inside showAlertDialog())
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
		
	}
	
	void delete(int wordId){
		
		WordDataSource wordds = new WordDataSource(this);
		wordds.delete(String.valueOf(wordId));
		wordds.close();
	}
	
	void showDeleteAlerDialog(Context context){
		AlertDialog dlg = new AlertDialog.Builder(context).create();
		dlg.setMessage("Do you want to delete this word?");
		dlg.setTitle("Delete");
		dlg.setCancelable(true);
		DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch(which){
				case DialogInterface.BUTTON_POSITIVE:
					delete((int) mId);
					// start main activity
		    		Intent intent = new Intent();
		    		intent.setClassName("com.pquach.vocabularynote", "com.pquach.vocabularynote.MainActivity");
		    		startActivity(intent);
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					break;
				}
			}
		};
		dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", dialogOnClickListener );
		dlg.setButton(DialogInterface.BUTTON_NEGATIVE, "No", dialogOnClickListener);
		dlg.show();
	}

    private boolean isTtsAvailable(){
        return isPackageInstalled(getPackageManager(),ttsPackageName);
    }

    private boolean isPackageInstalled(PackageManager pm, String packageName){
        try{
            pm.getPackageInfo(packageName,0);
        }catch(PackageManager.NameNotFoundException e){
            return false;
        }
        return true;
    }
/*
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
              // dataInstalled = true;
                new LoadTtsTask().execute(tts);
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }*/
    private class LoadTtsTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            Log.v("LoadTtsTask", "onPreExecute");
            pd= new ProgressDialog(WordDetailActivity.this,ProgressDialog.STYLE_SPINNER);
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params){
            Log.v("LoadTtsTask", "doInBackground");
            tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status==TextToSpeech.SUCCESS) {
                        Locale defaultLocale = Locale.getDefault();
                        Locale locale = Locale.US;
                        if (defaultLocale == Locale.CANADA) {
                            locale = Locale.CANADA;
                        }
                        if (defaultLocale == Locale.UK) {
                            locale = Locale.UK;
                        }
                        languageAvailable = tts.setLanguage(locale);
                        tts.setSpeechRate((float) 0.8);
                    }else{
                        // Toast.makeText(getApplicationContext(), "Text-to-speech is not properly installed", Toast.LENGTH_LONG).show();
                    }// end of else
                }// end of onInit
            });// end of TextToSpeech constructor
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            if(pd.isShowing())
                pd.dismiss();

            if(languageAvailable == TextToSpeech.LANG_MISSING_DATA
                    || languageAvailable == TextToSpeech.LANG_NOT_SUPPORTED) {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }else {
                tts.speak(tv_word.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }

            Log.v("LoadTtsTask", "onPostExecute");
        }
    }


}