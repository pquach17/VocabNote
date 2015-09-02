package com.pquach.vocabularynote;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.widget.TintCheckBox;
import android.support.v7.internal.widget.TintCheckedTextView;
import android.support.v7.internal.widget.TintEditText;
import android.support.v7.internal.widget.TintRadioButton;
import android.support.v7.internal.widget.TintSpinner;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements BaseFragment.OnFragmentInteractionListener,
android.support.v4.app.FragmentManager.OnBackStackChangedListener{

    /**
     * This constant represents the New-Word mode of the NewWordFragment.
     * WordId starts from 0 and up. So the New-Word mode will have the wordId value -1
     */
    private static final long NEW_WORD_MODE = -1;

    private Toolbar mToolBar = null;
    protected Spinner mNavigationSpinner;
    private SimpleCursorAdapter mSpinnerAdapter;

    /**
     * The current word list
     */
    private int mList = 1;

    private WordFragment mWorkFragment;
    private DictionaryWebFragment mDictionaryWebFragment;

    public Toolbar getToolbar(){
        return mToolBar;
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        mToolBar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolBar);
        shouldDisplayHomeUp();

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if(savedInstanceState == null) {
            mWorkFragment = WordFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, mWorkFragment, WordFragment.TAG)
                    .commit();
        }
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constant.ARG_CATEGORY, mList);

    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        // Allow super to try and create a view first
        final View result = super.onCreateView(name, context, attrs);
        if (result != null) {
            return result;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // If we're running pre-L, we need to 'inject' our tint aware Views in place of the
            // standard framework versions
            switch (name) {
                case "EditText":
                    return new TintEditText(this, attrs);
                case "Spinner":
                    return new TintSpinner(this, attrs);
                case "CheckBox":
                    return new TintCheckBox(this, attrs);
                case "RadioButton":
                    return new TintRadioButton(this, attrs);
                case "CheckedTextView":
                    return new TintCheckedTextView(this, attrs);
            }
        }

        return null;
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
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
		return super.onOptionsItemSelected(item);
	}



    private void shouldDisplayHomeUp(){
        boolean canback = getSupportFragmentManager().getBackStackEntryCount()>0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
    }

    /**
     * Handle Back button press for webview
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK
                && mDictionaryWebFragment != null
                && mDictionaryWebFragment.canGoBack()) {
            mDictionaryWebFragment.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();

    }



    @Override
    public void onFragmentInteraction(String fragmentTag, Bundle args) {
        Fragment fragment = null;
        String tag = null;
        switch (fragmentTag){
            // For the WordFragment is the first screen, we don't add it to the back stack
            case WordFragment.TAG:
                fragment = new WordFragment().newInstance();
                tag = WordFragment.TAG;
                if(fragment!=null){
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment, tag)
                            .commit();
                }
                return;
            case WordDetailFragment.TAG:
                fragment = new WordDetailFragment().newInstance(args.getLong(Constant.ARG_WORD_ID), args.getLong(Constant.ARG_CATEGORY));
                tag = WordDetailFragment.TAG;
                break;
            case NewWordFragment.NEW_WORD_TAG:
                fragment = NewWordFragment.newInstance(this.NEW_WORD_MODE, args.getLong(Constant.ARG_CATEGORY));
                tag = NewWordFragment.NEW_WORD_TAG;
                break;
            case NewWordFragment.EDIT_WORD_TAG:
                fragment = NewWordFragment.newInstance(args.getLong(Constant.ARG_WORD_ID), args.getLong(Constant.ARG_CATEGORY));
                tag = NewWordFragment.EDIT_WORD_TAG;
                break;
            case DictionaryWebFragment.TAG:
                fragment = DictionaryWebFragment.newInstance(args.getString(DictionaryWebFragment.ARG_WORD),
                        args.getString(DictionaryWebFragment.ARG_URL));
                tag = DictionaryWebFragment.TAG;
                break;
        }
        if(fragment!=null){
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(tag)
                    .replace(R.id.fragment_container, fragment, tag)
                    .commit();
        }

    }
}


