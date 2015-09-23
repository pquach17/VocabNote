package com.pquach.vocabularynote;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;


/*
/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WordDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WordDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WordDetailFragment extends BaseFragment{

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String TAG = "WordDetailFragment";

    private final String DELETE_CONFIRM_MESSAGE = "Do you want to delete this word?";
    private final String DELETE_DIALOG_TITLE = "Delete";

    private long mWordId;
    private Word mWord;
    private long mCategory;

    private TextView tv_word ;
    private TextView tv_word_type;
    private TextView tv_definition ;
    private TextView tv_example  ;
    private TextView label_definition;
    private TextView label_example;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param wordId Id of the selected word from the word list fragment.
     * @return A new instance of fragment WordDetailFragment.
     */
    public static WordDetailFragment newInstance(long wordId, long category) {
        WordDetailFragment fragment = new WordDetailFragment();
        Bundle args = new Bundle();
        args.putLong(Constant.ARG_WORD_ID, wordId);
        args.putLong(Constant.ARG_CATEGORY, category);
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
            mWordId = getArguments().getLong(Constant.ARG_WORD_ID);
            mCategory = getArguments().getLong(Constant.ARG_CATEGORY);
        }
        setHasOptionsMenu(true);

        Log.v("WordDetailFragment", "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("WordDetailFragment", "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_word_detail, container, false);

        WordDataSource worddds = new WordDataSource(getActivity());
        mWord = worddds.getWord(mWordId);

        // Get controls' reference
        tv_word = (TextView) view.findViewById(R.id.tv_word);
        tv_word_type = (TextView) view.findViewById(R.id.tv_word_type);
        tv_definition = (TextView) view.findViewById(R.id.tv_definition);
        tv_example = (TextView) view .findViewById(R.id.tv_example);
        label_definition = (TextView) view.findViewById(R.id.label_definition);
        label_example = (TextView) view.findViewById(R.id.label_example);

        // Bind data to controls
        if(mWord != null){
            tv_word.setText(mWord.getWord());
            tv_word_type.setText(mWord.getType());
            tv_definition.setText(mWord.getDefinition());
            tv_example.setText(mWord.getExample());
            // set labels' visibility
            /*
            if(mWord.getDefinition().length() > 0)
                label_definition.setVisibility(View.VISIBLE);
            if(mWord.getExample().length() > 0)
                label_example.setVisibility(View.VISIBLE);
                */
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v("WordDetailFragment", "onActivityCreated");
        MainActivity activity = (MainActivity) getActivity();
        Spinner spinner = (Spinner) activity.findViewById(R.id.spinner_nav);
        spinner.setVisibility(View.INVISIBLE);

        activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        activity.getSupportActionBar().setLogo(null);
        //Set title
        String title = getActivity().getResources().getString(R.string.str_label_word_detail);
        activity.getSupportActionBar().setTitle(title);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v("WordDetailFragment", "onStart");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
                Bundle args = new Bundle();
                args.putLong(Constant.ARG_WORD_ID, mWordId);
                args.putLong(Constant.ARG_CATEGORY, mCategory);
                startFragment(NewWordFragment.EDIT_WORD_TAG, args);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void delete(long wordId){
        WordDataSource wordds = new WordDataSource(getActivity());
        wordds.delete(String.valueOf(wordId));
        wordds.close();
    }
    private void showDeleteDialog(){
        AlertDialog deleteDlg = new AlertDialog.Builder(getActivity())
                                              .setMessage(DELETE_CONFIRM_MESSAGE)
                                              .create();
        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // Delete the word and go back to word list.
                        delete(mWordId);
                        getActivity().getSupportFragmentManager().popBackStack();
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

    public interface OnFragmentInteractionListener{
        public void onFragmentInteraction(String id);
    }
}
