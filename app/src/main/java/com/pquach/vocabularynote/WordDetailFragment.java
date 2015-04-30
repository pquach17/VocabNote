package com.pquach.vocabularynote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Locale;

/*
/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WordDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WordDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WordDetailFragment extends Fragment implements View.OnClickListener{

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String TAG = "WordDetailFragment";

    private static final String ARG_WORD_ID = "mWordId";
    private final String DELETE_CONFIRM_MESSAGE = "Do you want to delete this word?";
    private final String DELETE_DIALOG_TITLE = "Delete";
    private long mWordId;

    private OnFragmentInteractionListener mListener;
    private Word mWord;

    private TextView tv_word ;
    private TextView tv_word_type;
    private TextView tv_definition ;
    private TextView tv_example  ;
    private TextView label_definition;
    private TextView label_example;
    private ImageButton ib_pronounce;

    private TextToSpeech mTts;
    final static int MY_DATA_CHECK_CODE = 2;
    private boolean mTtsAvailable;
    private int mLanguageAvailable;
    private boolean mReadyToSpeak = false;
    private final String TTS_PACKAGE_NAME = "com.google.android.tts";
    private final String PICO_PACKAGE_NAME = "com.svox.pico";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param wordId Id of the selected word from the word list fragment.
     * @return A new instance of fragment WordDetailFragment.
     */
    public static WordDetailFragment newInstance(long wordId) {
        WordDetailFragment fragment = new WordDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_WORD_ID, wordId);
        fragment.setArguments(args);
        return fragment;
    }

    public WordDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWordId = getArguments().getLong(ARG_WORD_ID);

        }
        setHasOptionsMenu(true);

        Log.v("WordDetailFragment", "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("WordDetailFragment","onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_word_detail, container, false);

        WordDataSource worddds = new WordDataSource(getActivity());
        mWord = worddds.getWord((int)mWordId);

        // Get controls' reference
        tv_word = (TextView) view.findViewById(R.id.tv_word);
        tv_word_type = (TextView) view.findViewById(R.id.tv_word_type);
        tv_definition = (TextView) view.findViewById(R.id.tv_definition);
        tv_example = (TextView) view .findViewById(R.id.tv_example);
        label_definition = (TextView) view.findViewById(R.id.label_definition);
        label_example = (TextView) view.findViewById(R.id.label_example);
        ib_pronounce = (ImageButton) view.findViewById(R.id.btn_pronounce);

        // Bind data to controls
        if(mWord != null){
            tv_word.setText(mWord.getWord());
            tv_word_type.setText(mWord.getType());
            tv_definition.setText(mWord.getDefinition());
            tv_example.setText(mWord.getExample());
            // set labels' visibility
            if(mWord.getDefinition().length() > 0)
                label_definition.setVisibility(View.VISIBLE);
            if(mWord.getExample().length() > 0)
                label_example.setVisibility(View.VISIBLE);
        }
        //Set up Pronounce button
        ib_pronounce.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v("WordDetailFragment", "onActivityCreated");

        //Set title
        String title = getActivity().getResources().getString(R.string.str_label_word_detail);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(title);

        // Set the audio stream which will be adjusted by hardware volume control
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //Initialize TextToSpeech engine if one is available
        mTtsAvailable = isTtsAvailable();
        if(mTtsAvailable){
            new LoadTtsTask().execute();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v("WordDetailFragment", "onStart");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.v("WordDetailFragment", "onAttach");
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mTts!=null){
            mTts.stop();
            mTts.shutdown();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_word_detail_action, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            case R.id.action_bar_btn_delete:
                showDeleteDialog();
                return true;
            case R.id.action_bar_btn_edit:
                mListener.onFragmentInteraction(String.valueOf(mWordId));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void delete(int wordId){
        WordDataSource wordds = new WordDataSource(getActivity());
        wordds.delete(String.valueOf(wordId));
        wordds.close();
    }
    private void showDeleteDialog(){
        AlertDialog deleteDlg = new AlertDialog.Builder(getActivity())
                                              .setMessage(DELETE_CONFIRM_MESSAGE)
                                              .setTitle(DELETE_DIALOG_TITLE)
                                              .create();
        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // Delete the word and go back to word list.
                        delete((int)mWordId);
                        WordFragment wordFragment = new WordFragment();
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, wordFragment, WordFragment.TAG)
                                .commit();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        deleteDlg.setButton(DialogInterface.BUTTON_NEGATIVE,"No", dialogOnClickListener);
        deleteDlg.setButton(DialogInterface.BUTTON_POSITIVE,"Yes", dialogOnClickListener);
        deleteDlg.show();

    }

    private boolean isTtsAvailable(){
        return isPackageInstalled(getActivity().getPackageManager(),TTS_PACKAGE_NAME);
    }

    private boolean isPackageInstalled(PackageManager pm, String packageName){
        try{
            pm.getPackageInfo(packageName,0);
        }catch(PackageManager.NameNotFoundException e){
            return false;
        }
        return true;
    }

    /**
     * This method is implemented from {@link} View.OnClickListener
     * @param v a view which was clicked
     */
    @Override
    public void onClick(View v) {
        if(!mTtsAvailable){
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + TTS_PACKAGE_NAME)));
            } catch (android.content.ActivityNotFoundException e) {
                // If no Play Store installed on device, bring user to Play Store website
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + TTS_PACKAGE_NAME)));
            }
        }else if (mLanguageAvailable == TextToSpeech.LANG_MISSING_DATA
                    || mLanguageAvailable == TextToSpeech.LANG_NOT_SUPPORTED){
            // missing data, install it
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installIntent);
        }else {
            mTts.speak(tv_word.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    /**
     * This class creates a new thread for initializing an instance of TextToSpeech engine
     */
    private class LoadTtsTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            Log.v("LoadTtsTask", "onPreExecute");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.v("LoadTtsTask", "doInBackground");
            mTts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        Locale defaultLocale = Locale.getDefault();
                        Locale locale = Locale.US;
                        if (defaultLocale == Locale.CANADA) {
                            locale = Locale.CANADA;
                        }
                        if (defaultLocale == Locale.UK) {
                            locale = Locale.UK;
                        }
                 //       mLanguageAvailable = mTts.setLanguage(locale);
                 //       mTts.setSpeechRate((float) 0.8);
                    } else {
                        // Toast.makeText(getApplicationContext(), "Text-to-speech is not properly installed", Toast.LENGTH_LONG).show();
                    }// end of else
                }// end of onInit
            });// end of TextToSpeech constructor
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Log.v("LoadTtsTask", "onPostExecute");
        }
    }

    public interface OnFragmentInteractionListener{
        public void onFragmentInteraction(String id);
    }
}
