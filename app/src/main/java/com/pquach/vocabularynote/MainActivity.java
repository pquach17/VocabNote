package com.pquach.vocabularynote;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.widget.TintCheckBox;
import android.support.v7.internal.widget.TintCheckedTextView;
import android.support.v7.internal.widget.TintEditText;
import android.support.v7.internal.widget.TintRadioButton;
import android.support.v7.internal.widget.TintSpinner;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.app.FragmentManager;

public class MainActivity extends  ActionBarActivity implements WordFragment.OnFragmentInteractionListener,
WordDetailFragment.OnFragmentInteractionListener, NewWordFragment.OnNewWordFragmentListener,
android.support.v4.app.FragmentManager.OnBackStackChangedListener{
 



    private WordFragment mWorkFragment;
    private DictionaryWebFragment mDictionaryWebFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        shouldDisplayHomeUp();

        if(savedInstanceState == null) {
            mWorkFragment = new WordFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, mWorkFragment, WordFragment.TAG)
                    .commit();
        }else {
            mWorkFragment = (WordFragment) getSupportFragmentManager().findFragmentByTag(WordFragment.TAG);
        }

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
    public void onFragmentInteraction(String id) {
        NewWordFragment newWordFragment = NewWordFragment.newInstance(id);
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fragment_container, newWordFragment, NewWordFragment.EDIT_WORD_TAG)
                .commit();
    }

    @Override
    public void onFragmentInteraction(long id) {
        WordDetailFragment wordDetailFragment = WordDetailFragment.newInstance(id);
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(WordDetailFragment.TAG)
                .replace(R.id.fragment_container, wordDetailFragment, WordDetailFragment.TAG)
                .commit();
    }

    @Override
    public void onNewWordFragmentInteraction(String word, String url) {
        mDictionaryWebFragment = DictionaryWebFragment.newInstance(word, url);
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fragment_container, mDictionaryWebFragment)
                .commit();

    }


    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();

    }
}


