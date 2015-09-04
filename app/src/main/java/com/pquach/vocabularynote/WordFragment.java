package com.pquach.vocabularynote;


import android.app.AlertDialog;;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.melnykov.fab.FloatingActionButton;
import java.util.ArrayList;
import java.util.Collections;
import android.view.ActionMode;


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
    private final String ARG_LIST = "mList";

    // these two variables are used by showDeleteWordDialog function to determine what kind of contextual menu is calling
    private final boolean ARG_CONTEXTUAL_ACTION_MODE = true;
    private final boolean ARG_CONTEXTUAL_FLOATING_MENU = false;

    /**
     *  the selected word list (category) that will be displayed
     */
    private long mCategory;

    /**
     * the selected item in word list spinner
     */
    private int mList = 1;


    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * Spinner for selecting word list (category)
     */
    private Spinner mNavigationSpinner;
    private SimpleCursorAdapter mSpinnerAdapter;

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

    /**
     * mCABCheckedItems saves checked items when in contextual action bar (CAB) mode
     */
    private ArrayList mCABCheckedItems;


    public static WordFragment newInstance() {
        WordFragment fragment = new WordFragment();
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

        // register Option Menu to container activity
        setHasOptionsMenu(true);

        if(savedInstanceState != null){
            mSortByWord = savedInstanceState.getBoolean(ARG_SORT_BY_WORD);
            mAscending = savedInstanceState.getBoolean(ARG_ASCENDING);
            mCheckedSortCondition = savedInstanceState.getInt(ARG_CHECKED_SORT_CONDITION);
            mCheckedFilterConditions = savedInstanceState.getBooleanArray(ARG_CHECKED_FILTER_CONDITIONS);
            mCategory = savedInstanceState.getLong(Constant.ARG_CATEGORY);
            mList = savedInstanceState.getInt(ARG_LIST);
        }else{
            setSortingAndFilterToDefault();
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
        outState.putInt(ARG_LIST, mList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word, container, false);

        // Set up list navigation spinner
        mNavigationSpinner = (Spinner) getActivity().findViewById(R.id.spinner_nav);
        initNavigationSpinner();

        mCategory = mNavigationSpinner.getSelectedItemId();

        // Load data to the Cursor
        mCursor = filter();
        mCursor = sortWordList();

        // Setup SimpleCursorAdapter
        String[] from = {VobNoteContract.Word.COLUMN_NAME_WORD, VobNoteContract.Word.COLUMN_NAME_TYPE};
        int[] to = {R.id.tv_list_row, R.id.tv_list_row_type};
        mAdapter = new SimpleCursorAdapter(getActivity(),R.layout.word_list_layout, mCursor, from, to, 0);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        int version = Build.VERSION.SDK_INT;
        mCABCheckedItems = new ArrayList();
        if(version > 11){
            // Setup Contextual Action Bar Mode
            mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                int defaultColor = mListView.getSolidColor();
                int selectedColor = getResources().getColor(android.R.color.holo_blue_light);

                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    mode.setTitle(mListView.getCheckedItemCount() + " selected");
                    if(checked){
                        mCABCheckedItems.add(id);
                        mListView.getChildAt(position).setBackgroundColor(selectedColor); // seleted state color
                    }else {
                        mListView.getChildAt(position).setBackgroundColor(defaultColor); // default color
                        mCABCheckedItems.remove(id);
                    }
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.context_menu_listview, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                   return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.context_delete:
                            showDeleteWordDialog(mode, mCABCheckedItems, ARG_CONTEXTUAL_ACTION_MODE);
                            return true;
                        default:
                            return false;
                    }
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    mCABCheckedItems.clear();
                    // Set background color back to default color
                    for (int i = 0; i < mListView.getChildCount(); i++) {
                        mListView.getChildAt(i).setBackgroundColor(defaultColor);
                    }
                }
            });
        }else {
            // Setup Contextual Floating Menu
            registerForContextMenu(mListView);

        }

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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu_listview, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.context_delete:
                showDeleteWordDialog(null, mCABCheckedItems, ARG_CONTEXTUAL_FLOATING_MENU);
                return true;
            default:
                return super.onContextItemSelected(item);
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

    private void setSortingAndFilterToDefault(){
        // Initial state of the list will be sorting by "Oldest first",
        // and filtering by full list of word types
        mSortByWord = false;
        mAscending = true;
        mCheckedSortCondition = 2;
        mCheckedFilterConditions = new boolean[getResources().getStringArray(R.array.spinner_type).length];
    }

    private void showActivity(Class activityClass){
        Intent intent = new Intent();
        intent.putExtra(Constant.ARG_CATEGORY, mCategory);
        intent.setClass(getActivity(), activityClass);
        startActivity(intent);
    }

    /**
     *
     * @param mode
     * @param wordIds Array of word's id that will be deleted
     * @param menuType True, if this is Contextual Action Mode. False, if this is Contextual Floating Menu
     */
    private void showDeleteWordDialog(final ActionMode mode, final ArrayList wordIds, final boolean menuType){
        TextView title = Constant.createDialogTitle(getActivity(), "Delete", getResources().getColor(R.color.color_accent));
        String message;
        if(menuType){
            message = "Delete "+wordIds.size()+" word(s)?";
        }else {
            message = "Delete this word?";
        }
        AlertDialog  dialog = new AlertDialog.Builder(getActivity())
                .setCustomTitle(title)
                .setMessage(message)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WordDataSource wordDataSource = new WordDataSource(getActivity());
                        for (int i = 0; i < wordIds.size(); i++) {
                            wordDataSource.delete(String.valueOf(wordIds.get(i)));
                        }
                        mAdapter.swapCursor(wordDataSource.getWordsInCategory(mCategory));
                        mAdapter.notifyDataSetChanged();
                        if (menuType) {
                            mode.finish();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
        dialog.show();
        Constant.setDialogDividerColor(getActivity(), dialog, getResources().getColor(R.color.color_accent));

    }

    private void initNavigationSpinner(){
        Cursor cursor = loadCategoryData();
        mSpinnerAdapter = new SimpleCursorAdapter(getActivity(), R.layout.spinner_textview, cursor,
                new String[]{VobNoteContract.Category.COLUMN_NAME_CATEGORY_NAME}, new int[]{ R.id.spinner_textview}, 0);
        mSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_custom);
        mNavigationSpinner.setAdapter(mSpinnerAdapter);
        mNavigationSpinner.setSelection(mList);
        mNavigationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    showCreateCategoryDialog();
                    return;
                }
                if(i!=mList){
                    mList = i;
                    mCategory = l;
                    setSortingAndFilterToDefault();
                    mCursor = filter();
                    mCursor = sortWordList();
                    mAdapter.swapCursor(mCursor);
                    mAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private Cursor loadCategoryData(){
        CategoryDataSource dataSource = new CategoryDataSource(getActivity());
        Cursor cursor = dataSource.getAllCategories();
        MatrixCursor extras = new MatrixCursor(cursor.getColumnNames());
        extras.addRow(new String[]{"-1", "-1", "New List"});
        Cursor[] cursors = { extras, cursor };
        Cursor extendedCursor = new MergeCursor(cursors);
        return  extendedCursor;
    }

    public void updateSpinner(){
        mSpinnerAdapter.changeCursor(loadCategoryData());
        mSpinnerAdapter.notifyDataSetChanged();
    }

    private void showCreateCategoryDialog(){
        TextView title = Constant.createDialogTitle(getActivity(), "Create word list", getResources().getColor(R.color.color_accent));
        LayoutInflater inflater = getLayoutInflater(null);
        View view = inflater.inflate(R.layout.dialog_edit_text_layout, null);
        final EditText input = (EditText) view.findViewById(R.id.et_dialog);
        final AlertDialog  dialog = new AlertDialog.Builder(getActivity())
                .setCustomTitle(title)
                .setView(view)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
        dialog.show();
        Constant.setDialogDividerColor(getActivity(), dialog, getResources().getColor(R.color.color_accent));
        Button createButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.getText().length() > 0) {
                    Category category = new Category();
                    category.setCategoryName(input.getText().toString());
                    CategoryDataSource categoryDataSource = new CategoryDataSource(getActivity());
                    if (categoryDataSource.isCategoryNameUsed(category.getCategoryName())) {
                        // This name is used, show error message
                        Toast.makeText(getActivity(), "This name is used, please enter another name", Toast.LENGTH_LONG).show();
                        mNavigationSpinner.setSelection(mList);
                        return;
                    }
                    if (categoryDataSource.insert(category) != -1) {
                        updateSpinner();
                        mList = mNavigationSpinner.getCount() - 1;
                        mNavigationSpinner.setSelection(mList);

                        // start newly created word list
                        mCategory = mNavigationSpinner.getSelectedItemId();
                        setSortingAndFilterToDefault();
                        mCursor = filter();
                        mCursor = sortWordList();
                        mAdapter.swapCursor(mCursor);
                        mAdapter.notifyDataSetChanged();
                    }
                    dialog.dismiss();
                }
            }
        });
        mNavigationSpinner.setSelection(mList);
    }

    private void showRenameListDialog(){
        final CategoryDataSource categoryDataSource = new CategoryDataSource(getActivity());
        final Category category = categoryDataSource.getCategoryById(mCategory);
        TextView title = Constant.createDialogTitle(getActivity(), "Rename word list", getResources().getColor(R.color.color_accent));
        LayoutInflater inflater = getLayoutInflater(null);
        View view = inflater.inflate(R.layout.dialog_edit_text_layout, null);
        final EditText input = (EditText) view.findViewById(R.id.et_dialog);
        input.setText(category.getCategoryName());
        final String oldText = input.getText().toString();
        final AlertDialog  dialog = new AlertDialog.Builder(getActivity())
                .setCustomTitle(title)
                .setView(view)
                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
        dialog.show();

        Constant.setDialogDividerColor(getActivity(), dialog, getResources().getColor(R.color.color_accent));
        Button renameButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.getText().length() > 0) {
                    category.setCategoryName(input.getText().toString());
                    if (categoryDataSource.isCategoryNameUsed(category.getCategoryName())
                            && !category.getCategoryName().equals(oldText)) {
                        // This name is used, show error message
                        Toast toast = Toast.makeText(getActivity(), "You already have a \""+input.getText().toString()
                                +"\" list, please choose another name", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 10);
                        toast.show();
                        return;
                    }
                    categoryDataSource.update(category);
                    updateSpinner();
                    dialog.dismiss();
                }
            }
        });
    }

    private void showDeleteListDialog(){
        TextView title = Constant.createDialogTitle(getActivity(), "Delete word list", getResources().getColor(R.color.color_accent));
        final CategoryDataSource categoryDataSource = new CategoryDataSource(getActivity());
        final Category category = categoryDataSource.getCategoryById(mCategory);
        AlertDialog  dialog = new AlertDialog.Builder(getActivity())
                .setCustomTitle(title)
                .setMessage("Deleting " + category.getCategoryName() + " will also DELETE ALL WORDS in it, continue?")
                .create();
        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case DialogInterface.BUTTON_POSITIVE:
                        TextView title = Constant.createDialogTitle(getActivity(), "Delete " + category.getCategoryName(), getResources().getColor(R.color.color_accent));
                        AlertDialog  dialog = new AlertDialog.Builder(getActivity())
                                .setCustomTitle(title)
                                .setMessage("Delete " + category.getCategoryName() + " anyway?")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Category.delete(getActivity(), mCategory);
                                        updateSpinner();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .create();
                        dialog.show();
                        Constant.setDialogDividerColor(getActivity(), dialog, getResources().getColor(R.color.color_accent));
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", dialogOnClickListener);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Continue", dialogOnClickListener);
        dialog.show();
        Constant.setDialogDividerColor(getActivity(), dialog, getResources().getColor(R.color.color_accent));
    }



    private void showSortDialog(){
        TextView title = Constant.createDialogTitle( getActivity(), "Sort by", getResources().getColor(R.color.color_accent));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCustomTitle(title)
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
                })
                .create();
        AlertDialog dialog = builder.show();
        // Set title divider color
        Constant.setDialogDividerColor(getActivity(), dialog, getResources().getColor(R.color.color_accent));

    }


    private void showFilterDialog(){
        TextView title = Constant.createDialogTitle(getActivity(), "Select type(s)", getResources().getColor(R.color.color_accent));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCustomTitle(title)
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
               .create();
        AlertDialog dialog = builder.show();
        // Set title divider color
        Constant.setDialogDividerColor(getActivity(), dialog, getResources().getColor(R.color.color_accent));
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
        WordDataSource wordds = new WordDataSource(getActivity());
        ArrayList<String> selectedTypes = new ArrayList<String>();
        String[] arrTypes = getResources().getStringArray(R.array.spinner_type);

        for(int i=0; i<mCheckedFilterConditions.length; i++) {
            if (mCheckedFilterConditions[i]) {
                selectedTypes.add(arrTypes[i]);
            }
        }
        return wordds.selectByTypes(selectedTypes.toArray(new String[selectedTypes.size()]), mCategory);
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
