package com.pquach.vocabularynote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;

import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.annotation.Nullable;


import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;

import android.widget.TextView;


import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

import java.util.Collections;
import java.util.jar.Manifest;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class WordFragment extends BaseFragment implements AbsListView.OnItemClickListener {

    // the fragment initialization parameters
    public static final String TAG = "WordFragment";
    private final String ARG_SORT_BY_WORD = "mSortByWord";
    private final String ARG_ASCENDING = "mAscending";
    private final String ARG_CHECKED_SORT_CONDITION = "mCheckedSortCondition";
    private final String ARG_CHECKED_FILTER_CONDITIONS = "mCheckedFilterConditions";

    /**
     *  the selected word list (category) that will be displayed
     */
    private long mCategory;


    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    private Spinner mNavigationSpinner;
    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private SimpleCursorAdapter mAdapter;

    /**
     * The Cursor which contains word list retrieved from the database
     */
    private Cursor mCursor;

    /**
     * Sorting condition variables
     * mSortByWord = TRUE, the list will be sorted by word,
     * if it equals FALSE, the list will be sorted by added time of the words,
     * which is in fact sorted by word's id
     * When mAscending = FALSE, the list will be sorted in DESCENDING order
     */
    private boolean mSortByWord;
    private boolean mAscending;

    /**
     * mCheckedSortCondition saves the currently checked item in the Sort dialog
     * mCheckedFilterConditions[] saves the selected items in the Filter dialog
     */
    private int mCheckedSortCondition;
    private boolean[] mCheckedFilterConditions;

    // TODO: Rename and change types of parameters
    public static WordFragment newInstance(long category) {
        WordFragment fragment = new WordFragment();
        Bundle args = new Bundle();
        args.putLong(Constant.ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null){
            mCategory = getArguments().getLong(Constant.ARG_CATEGORY);
        }
        // register Option Menu to container activity
        setHasOptionsMenu(true);

        if(savedInstanceState != null){
            mSortByWord = savedInstanceState.getBoolean(ARG_SORT_BY_WORD);
            mAscending = savedInstanceState.getBoolean(ARG_ASCENDING);
            mCheckedSortCondition = savedInstanceState.getInt(ARG_CHECKED_SORT_CONDITION);
            mCheckedFilterConditions = savedInstanceState.getBooleanArray(ARG_CHECKED_FILTER_CONDITIONS);
            mCategory = savedInstanceState.getLong(Constant.ARG_CATEGORY);
        }else{
            // Initial state of the list will be sorting by "Oldest first",
            // and filtering by full list of word types
            mSortByWord = false;
            mAscending = true;
            mCheckedSortCondition = 2;
            mCheckedFilterConditions = new boolean[getResources().getStringArray(R.array.spinner_type).length];
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_SORT_BY_WORD, mSortByWord);
        outState.putBoolean(ARG_ASCENDING,mAscending);
        outState.putInt(ARG_CHECKED_SORT_CONDITION, mCheckedSortCondition);
        outState.putBooleanArray(ARG_CHECKED_FILTER_CONDITIONS, mCheckedFilterConditions);
        outState.putLong(Constant.ARG_CATEGORY, mCategory);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word, container, false);


        // Load data to the Cursor
        mCursor = filter();
        mCursor = sortWordList();

        // Setup SimpleCursorAdapter
        String[] from = {VobNoteContract.Word.COLUMN_NAME_WORD, VobNoteContract.Word.COLUMN_NAME_TYPE};
        int[] to = {android.R.id.text1, android.R.id.text2};
        mAdapter = new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_2, mCursor, from, to, 0);


        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        // Set up Floating Action Button and add it to List View
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putLong(Constant.ARG_CATEGORY, mCategory);
                startFragment(NewWordFragment.NEW_WORD_TAG, args);
            }
        });
        fab.attachToListView(mListView);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        Spinner spinner = (Spinner) activity.findViewById(R.id.spinner_nav);
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
       inflater.inflate(R.menu.activity_main_actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        // Handle selected Option Menu item
        switch (item.getItemId()) {
            case R.id.action_sort:
                showSortDialog();
                return true;
            case R.id.action_filter:
                showFilterDialog();
                return true;
            case R.id.action_flash_card:
                showActivity(FlashCardActivity.class);
                return true;
            case R.id.action_rename_list:
                showRenameListDialog();
                return true;
            case R.id.action_delete_list:
                showDeleteListDialog();
                return true;
            case R.id.action_backup:
                showActivity(BackupActivity.class);
                return true;
            case R.id.action_restore:
                showActivity(RestoreActivity.class);
                return true;
            case R.id.action_settings:
                showActivity(SettingsActivity.class);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bundle args = new Bundle();
        args.putLong(Constant.ARG_WORD_ID, id);
        args.putLong(Constant.ARG_CATEGORY, mCategory);
        startFragment(WordDetailFragment.TAG, args);
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    private void showActivity(Class activityClass){
        Intent intent = new Intent();
        intent.putExtra(Constant.ARG_CATEGORY, mCategory);
        intent.setClass(getActivity(),activityClass);
        startActivity(intent);
    }

    private void showRenameListDialog(){
        final CategoryDataSource categoryDataSource = new CategoryDataSource(getActivity());
        final Category category = categoryDataSource.getCategoryById(mCategory);
        final EditText input = new EditText(getActivity());
        input.setText(category.getCategoryName());
        AlertDialog  dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Rename word list")
                .setView(input)
                .create();
        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case DialogInterface.BUTTON_POSITIVE:
                        if(input.getText().length()>0) {
                            category.setCategoryName(input.getText().toString());
                            categoryDataSource.update(category);
                            ((MainActivity)getActivity()).updateSpinner();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", dialogOnClickListener);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Rename", dialogOnClickListener);
        dialog.show();
    }

    private void showDeleteListDialog(){
        final CategoryDataSource categoryDataSource = new CategoryDataSource(getActivity());
        final Category category = categoryDataSource.getCategoryById(mCategory);
        AlertDialog  dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Delete word list")
                .setMessage("Deleting")
                .create();
        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case DialogInterface.BUTTON_POSITIVE:
                        Category.delete(getActivity(), mCategory);
                        ((MainActivity)getActivity()).updateSpinner();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel", dialogOnClickListener);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,"Delete", dialogOnClickListener);
        dialog.show();
    }

    private void showSortDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_sort, null))
                .setTitle("Sort by")
                /*
                .setSingleChoiceItems(R.array.sort_condition, mCheckedSortCondition, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCheckedSortCondition = which;
                        String[] arr = getResources().getStringArray(R.array.sort_condition);
                        if (arr[which].equalsIgnoreCase("A to Z")) {
                            mSortByWord = true;
                            mAscending = true;
                        } else if (arr[which].equalsIgnoreCase("Z to A")) {
                            mSortByWord = true;
                            mAscending = false;
                        } else if (arr[which].equalsIgnoreCase("Oldest first")) {
                            mSortByWord = false;
                            mAscending = true;
                        } else {
                            mSortByWord = false;
                            mAscending = false;
                        }
                        dialog.dismiss();
                        mAdapter.changeCursor(sortWordList());
                        mAdapter.notifyDataSetChanged();
                    }
                })*/
                .create().show();
        // Set title divider color
        /*
        int titleDividerId = getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = dialog.findViewById(titleDividerId);
        if (titleDivider != null)
            titleDivider.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
            */
    }

    private void showFilterDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select type(s)")
               .setMultiChoiceItems(R.array.spinner_type, mCheckedFilterConditions, new DialogInterface.OnMultiChoiceClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                       mCheckedFilterConditions[which] = isChecked;
                   }
               })
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       mCursor = filter();
                       mCursor = sortWordList();
                       mAdapter.changeCursor(mCursor);
                       mAdapter.notifyDataSetChanged();
                   }
               })
               .create().show();
    }



    private Cursor sortWordList(){
        ArrayList<Word> arr = new ArrayList<Word>();
        arr = copyCursorToArray(mCursor);
        if(mSortByWord) {
            if (mAscending) {
                Collections.sort(arr, new Word.WordComparator());
            } else {
                Collections.sort(arr, new Word.WordComparator());
                Collections.reverse(arr);
            }
        }else{// sort by id
            if(mAscending){
                Collections.sort(arr, new Word.IdComparator());
                } else {
                Collections.sort(arr, new Word.IdComparator());
                Collections.reverse(arr);
            }
        }
        return copyArrayToCursor(arr, mCursor.getColumnNames());
    }

    private Cursor filter(){
        WordDataSource wordds = new WordDataSource(getActivity(), mCategory);
        ArrayList<String> selectedTypes = new ArrayList<String>();
        String[] arrTypes = getResources().getStringArray(R.array.spinner_type);

        for(int i=0; i<mCheckedFilterConditions.length; i++) {
            if (mCheckedFilterConditions[i]) {
                selectedTypes.add(arrTypes[i]);
            }
        }
        return wordds.selectByTypes(selectedTypes.toArray(new String[selectedTypes.size()]));
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
    public interface OnFragmentInteractionListener{
        public void onFragmentInteraction(long id);
    }
}
