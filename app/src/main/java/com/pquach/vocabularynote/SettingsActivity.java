package com.pquach.vocabularynote;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	public static final String KEY_PREF_DICTIONARY = "pref_dictionary";
	public static final String KEY_PREF_WIFI_ONLY = "pref_wifiOnly";
	private String mDictionarySummary;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		ListPreference listPreference = (ListPreference) findPreference(KEY_PREF_DICTIONARY);
		listPreference.setSummary(listPreference.getEntry());
	}
	
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState){
		 super.onSaveInstanceState(savedInstanceState);
		 savedInstanceState.putString("DictionarySummary", mDictionarySummary);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		if (key.equals(KEY_PREF_DICTIONARY)) {
			// For list preferences, look up the correct display value in
			// the preference's 'entries' list.
			ListPreference listPreference = (ListPreference) findPreference(key);
			// Set the summary to reflect the new value.
			listPreference.setSummary(listPreference.getEntry());
			mDictionarySummary = listPreference.getEntry().toString();
		}
		
	}
}
