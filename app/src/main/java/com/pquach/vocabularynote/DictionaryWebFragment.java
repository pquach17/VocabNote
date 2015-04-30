package com.pquach.vocabularynote;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;
import android.widget.Toast;



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DictionaryWebFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DictionaryWebFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_WORD = "mWord";
    private static final String ARG_URL = "mUrl";

    // TODO: Rename and change types of parameters
    private String mWord;
    private String mUrl;



    private WebView mWebView;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param word Parameter 1.
     * @param url Parameter 2.
     * @return A new instance of fragment DictionaryWebFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DictionaryWebFragment newInstance(String word, String url) {
        DictionaryWebFragment fragment = new DictionaryWebFragment();
        Bundle args = new Bundle();
        args.putString(ARG_WORD, word);
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    public DictionaryWebFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWord = getArguments().getString(ARG_WORD);
            mUrl = getArguments().getString(ARG_URL);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dictionary_web, container, false);
        mWebView = (WebView) view.findViewById(R.id.webviewer);
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getActivity(), description, Toast.LENGTH_LONG).show();
            }
        });

        //-------Set the web page fit the screen and make the webview zoomable--------
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.loadUrl(mUrl + mWord.trim());
        return  view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v("WordDetailFragment", "onActivityCreated");

        //Set title
        String title = getActivity().getResources().getString(R.string.str_label_dictionary);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(title);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.web_viewer, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().getSupportFragmentManager().popBackStack();
                return true;
            case R.id.action_refresh:
                mWebView.reload();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

   public boolean canGoBack(){
       return mWebView != null && mWebView.canGoBack();
   }

   public void goBack(){
       if(mWebView != null)
           mWebView.goBack();
   }


}
