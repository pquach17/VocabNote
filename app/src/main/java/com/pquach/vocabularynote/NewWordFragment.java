package com.pquach.vocabularynote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewWordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewWordFragment extends BaseFragment implements View.OnClickListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public static final String NEW_WORD_TAG = "NewWordFragment";
    public static final String EDIT_WORD_TAG = "EditWordFragment";
    private final String DICTIONARY_DIALOG_TITLE = "Select a dictionary";



    private long mWordId;
    private long mCategoryId;

    protected EditText edit_word;
    protected EditText edit_definition ;
    protected EditText edit_example ;
    protected Spinner spin_type;
    protected ImageButton btn_web;
    protected ArrayAdapter<CharSequence> mAdapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param wordId The id of the word which is being edited. If it's null, it means a new word is being created
     * @return A new instance of fragment NewWordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewWordFragment newInstance(long wordId, long categoryId) {
        NewWordFragment fragment = new NewWordFragment();
        Bundle args = new Bundle();
        args.putLong(Constant.ARG_WORD_ID, wordId);
        args.putLong(Constant.ARG_CATEGORY, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    public NewWordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWordId = getArguments().getLong(Constant.ARG_WORD_ID);
            mCategoryId = getArguments().getLong(Constant.ARG_CATEGORY);
        }
        // register Option Menu to container activity
        setHasOptionsMenu(true);

        mAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.spinner_type, android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_new_word, container, false);
        edit_word = (EditText) view.findViewById(R.id.et_word);
        edit_definition = (EditText) view.findViewById(R.id.et_definition);
        edit_example = (EditText) view.findViewById(R.id.et_example);
        btn_web = (ImageButton) view.findViewById(R.id.btn_Web);
        btn_web.setOnClickListener(this);
        // --------Bind data to spinner------------------
        spin_type = (Spinner) view.findViewById(R.id.spinner_type);
        spin_type.setAdapter(mAdapter);

        // Check if Edit mode is selected
        // mWordId >= 0 only when user selected Edit, otherwise New Word was selected
        if(mWordId >= 0){
            WordDataSource wordds = new WordDataSource(getActivity(), mCategoryId);
            Word word = wordds.getWord(mWordId);
            if(word != null){
                edit_word.setText(word.getWord());
                edit_definition.setText(word.getDefinition());
                edit_example.setText(word.getExample());
                spin_type.setSelection(mAdapter.getPosition(word.getType()));

                //Set focus when user selects an EditText
                edit_word.setSelectAllOnFocus(true);
                edit_definition.setSelectAllOnFocus(true);
                edit_example.setSelectAllOnFocus(true);
            }else {
                Toast.makeText(getActivity(),"No such word in database", Toast.LENGTH_LONG).show();
            }
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        Spinner spinner = (Spinner) activity.findViewById(R.id.spinner_nav);
        spinner.setVisibility(View.INVISIBLE);

        activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        String title;
        if(mWordId >= 0) // Edit mode
            title = getActivity().getResources().getString(R.string.str_label_word_edit);
        else // New word mode
            title = getActivity().getResources().getString(R.string.str_label_new_word);
        activity.getSupportActionBar().setTitle(title);
    }


    @Override
    public void onPause() {
        super.onPause();
        closeKeyBoard();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_new_word_action, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            case R.id.action_bar_btn_save:
                if(save()){
                    getActivity().getSupportFragmentManager().popBackStack();
                }else{
                    edit_word.requestFocus();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDictionaryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(DICTIONARY_DIALOG_TITLE)
               .setItems(R.array.dictionary_entries, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       String[] dictionaryUrls = getResources().getStringArray(R.array.pref_dictionary_values);
                       Bundle args = new Bundle();
                       args.putString(DictionaryWebFragment.ARG_WORD, edit_word.getText().toString());
                       args.putString(DictionaryWebFragment.ARG_URL, dictionaryUrls[which]);
                       startFragment(DictionaryWebFragment.TAG, args);
                   }
               })
               .create().show();
    }

    private void closeKeyBoard(){
        View view = getActivity().getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public boolean save(){
        // Add new word into Word table
        if(edit_word.getText().length()<=0){
            return false;
        }
        Word word = new Word();
        String tempWord = edit_word.getText().toString();
        word.setWord(tempWord.substring(0, 1).toUpperCase(Locale.US) + tempWord.substring(1));
        word.setType(spin_type.getSelectedItem().toString());
        word.setDefinition(edit_definition.getText().toString());
        word.setExample(edit_example.getText().toString());
        WordDataSource wordds =  new WordDataSource(getActivity(), mCategoryId);
        if(mWordId >= 0){
            // In Edit mode
            word.setId(mWordId);
            if(wordds.update(word)>=1){
                return true;
            }
        }else if(wordds.insert(word)>=1){
            // In New Word mode and inserted successfully
            return true;
        }
        return false;
    }

    private String getDictionary(){
        /*
        String dictionary = null;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dictionary = sharedPref.getString(SettingsActivity.KEY_PREF_DICTIONARY, "");
        if(dictionary.equals("null")){
            return null;
        }*/
        String dictionary;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dictionary = sharedPref.getString(SettingsActivity.KEY_PREF_DICTIONARY, null);
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
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean isWifiConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi != null && wifi.isConnected();
    }

    private boolean isWifiOnly(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean wifiOnly = sharedPref.getBoolean(SettingsActivity.KEY_PREF_WIFI_ONLY,false);
        return wifiOnly;
    }

    @Override
    public void onClick(View v) {
        Toast toast;
        int networkAvailability = checkNetWorkAvailability();
        String dictionaryUrl = getDictionary();
        if(networkAvailability == 1){ // if there is network available
            if(edit_word != null && edit_word.getText().length()>0){
                if(dictionaryUrl.equals("null")){
                   showDictionaryDialog();
                }else{
                    Bundle args = new Bundle();
                    args.putString(DictionaryWebFragment.ARG_WORD, edit_word.getText().toString());
                    args.putString(DictionaryWebFragment.ARG_URL, dictionaryUrl);
                    startFragment(DictionaryWebFragment.TAG, args);
                }
            }
            else{
                toast = Toast.makeText(getActivity(), "Please enter your word", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL,0,0);
                toast.show();
            }
        }
        else{
            String msg;
            if(networkAvailability == -1){
                msg = "No WiFi connection available. You can enable mobile data connection by unchecking WifiOnly in Settings";
            }else{ // this happens when networkAvailability == 0
                msg = "No Internet connection available";
            }
            toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0,0);
            toast.show();
        }
    }

}
