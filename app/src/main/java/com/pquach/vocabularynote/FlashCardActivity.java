package com.pquach.vocabularynote;

import java.util.ArrayList;
import java.util.Random;
import android.os.Bundle;
import android.graphics.Point;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;
import com.pquach.vocabularynote.R;
import com.tekle.oss.android.animation.AnimationFactory;
import com.tekle.oss.android.animation.AnimationFactory.FlipDirection;



public class FlashCardActivity extends ActionBarActivity implements AnimationListener{

	ViewAnimator mViewAnimator;
	TextView mTextViewWord;
	TextView mTextViewDefinition;
	MenuItem mFlashCardMenuItem;
	ImageButton mButtonStartOver;
	RelativeLayout mFrontCardLayout;
	RelativeLayout mBackCardLayout;
	ArrayList<Word> mWordArray; 
	ArrayList<Integer> mWordIdArray; // this array saves id of words removed from mWordArray so that
	                                 // we know which word was already shown when the activity onCreate reloads
	boolean mIsWordShowing;//if mIsWordShowing = false, it means definition is showing
	boolean mIsOver;
	int mCurrentCard; // this variable saves the current view of the card which is being shown (that is whether front card or back is being shown)
	long mList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flash_card);

		if(getIntent().hasExtra(Constant.ARG_CATEGORY))
			mList = getIntent().getLongExtra(Constant.ARG_CATEGORY, 0);
		//----create cards-----------
		createCardLayout();	
		
		mTextViewWord = (TextView) findViewById(R.id.tv_word);
		mTextViewDefinition = (TextView) findViewById(R.id.tv_def);
		
		//--------Handle ViewAnimator control------------
		mViewAnimator = (ViewAnimator) findViewById(R.id.flipper);
		mViewAnimator.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub	
				if(mIsOver & mIsWordShowing)
					return;
				flipTransition(mViewAnimator, FlipDirection.RIGHT_LEFT);
			}
		});
		
		//------Initialize variables after activity reloads----------
		if(savedInstanceState == null){
			this.InitializeArrays(); // Loads data into mWordArray and initializes mWordIdArray
			mIsWordShowing = true; // if mIsWordShowing = false, it means definition is showing
			if(!mWordArray.isEmpty()){
				mIsOver = false;
				Random random = new Random();
				int index = random.nextInt(mWordArray.size());
				Word word = mWordArray.get(index);
				mWordArray.remove(index);
				mWordIdArray.add((int)word.getId());
				changeText(word);
			}else{
				// When the word list is empty, shows instruction message
				mIsOver = true;
				mTextViewWord.setTextSize((float)16);
				mTextViewWord.setText(R.string.str_instruct_msg);
			}
		}else {
			// reload saved states 
			mIsWordShowing = savedInstanceState.getBoolean("mIsWordShowing");
			mIsOver = savedInstanceState.getBoolean("mIsOver");
			mWordIdArray = savedInstanceState.getIntegerArrayList("mWordIdArray");
			mCurrentCard = savedInstanceState.getInt("mCurrentCard");
			mViewAnimator.setDisplayedChild(mCurrentCard);
			// removes words that were already shown before the activity reloads
			// and reloads the last word that was shown
			for(int i=0;i<mWordIdArray.size();i++){
				for(int j=0;j<mWordArray.size();j++){
					if(mWordIdArray.get(i)== mWordArray.get(j).getId()){
						if(i==mWordIdArray.size()-1){// when it goes to the last id in the mWorIdArray
							changeText(mWordArray.get(j));//reload the last word that was shown before the activity reloads
						}
						mWordArray.remove(j);
						break;
					}	
				}
			}
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState){
		savedInstanceState.putIntegerArrayList("mWordIdArray", mWordIdArray);
		savedInstanceState.putBoolean("mIsWordShowing", mIsWordShowing);
		savedInstanceState.putBoolean("mIsOver", mIsOver);
		savedInstanceState.putInt("mCurrentCard", mViewAnimator.getDisplayedChild());
	}
/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.flash_card_activity, menu);
		return true;
	}*/
	
	protected void InitializeArrays(){
		mWordIdArray = new ArrayList<Integer>();
		// Load data
		WordDataSource wordds = new WordDataSource(this);
		mWordArray = wordds.getWordArray(mList);
	}
	
	private void createCardLayout(){
		
		float width, height, left_right_margins = 0, top_bottom_margins = 0;
		int screen_width = 0, screen_height = 0;
		final float HORIZONTAL_PERCENTAGE = (float) (5.0/100);
		final float VERTICAL_PERCENTAGE = (float) (85.0/100);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		mFrontCardLayout = (RelativeLayout) findViewById(R.id.layout_front_card);
		mBackCardLayout = (RelativeLayout) findViewById(R.id.layout_back_card);
		Display display = getWindowManager().getDefaultDisplay();
		int version = android.os.Build.VERSION.SDK_INT;
		if(version >= 13){
			Point size = new Point();
			display.getSize(size);
			screen_height = size.y;
			screen_width = size.x;
			left_right_margins = (float) (HORIZONTAL_PERCENTAGE*screen_width);
			width =  (float) (screen_width - left_right_margins); // width of the flash card 
			height =  (float) (VERTICAL_PERCENTAGE*width); // height of the flash card
			top_bottom_margins = (float) ((screen_height - height)/2.0);
		} else{
			screen_height = display.getHeight();
			screen_width = display.getWidth();
			left_right_margins = (float) (HORIZONTAL_PERCENTAGE*screen_width);
			width =  (float) (screen_width - left_right_margins); // width of the flash card 
			height =  (float) (VERTICAL_PERCENTAGE*width); // height of the flash card
			top_bottom_margins = (float) ((screen_height - height)/2.0);
			params.gravity = Gravity.TOP;
		}
		
		params.setMargins((int)left_right_margins, (int)top_bottom_margins, (int)left_right_margins, (int)top_bottom_margins);
		mFrontCardLayout.setLayoutParams(params);
		mBackCardLayout.setLayoutParams(params);
	}
	
	private void changeText(Word word){
		
		mTextViewWord.setText(word.getWord());
		mTextViewDefinition.setText(word.getDefinition());
	}
	
	
	public void flipTransition(ViewAnimator viewAnimator, FlipDirection dir){
		
		final View fromView = viewAnimator.getCurrentView();
		final int currentIndex = viewAnimator.getDisplayedChild();
		final int nextIndex = (currentIndex + 1)%viewAnimator.getChildCount();
		
		final View toView = viewAnimator.getChildAt(nextIndex);

	//	Animation[] animc = AnimationFactory.flipAnimation(fromView, toView, (nextIndex < currentIndex?dir.theOtherDirection():dir), 500, null);
		Animation[] animc = AnimationFactory.flipAnimation(fromView, toView, dir, 500, null);
		
		animc[0].setAnimationListener(this);
		
		viewAnimator.setOutAnimation(animc[0]);
		viewAnimator.setInAnimation(animc[1]);
		
		viewAnimator.showNext();
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		
			/*
			mButtonStartOver = (ImageButton) findViewById(R.id.button_refresh);
			mButtonStartOver.setVisibility(View.VISIBLE);
			mButtonStartOver.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					InitializeArrays();
					mButtonStartOver.setVisibility(View.INVISIBLE);
					mTextViewWord.setVisibility(View.INVISIBLE);
				}
			});*/
		if(mWordArray.isEmpty()){
			mIsOver = true;
			mTextViewWord.setClickable(true);
			mTextViewWord.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_action_refresh,	0, 0);
			mTextViewWord.setTextSize((float)16);
			mTextViewWord.setText(R.string.str_flash_card_retry_button_decs);
			mTextViewWord.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//  
					finish();
					startActivity(getIntent());				}
			});
		}
		if(!mWordArray.isEmpty() && mIsWordShowing==false ){
			Random random = new Random();
			int index = random.nextInt(mWordArray.size());
			Word word = mWordArray.get(index);
			mWordArray.remove(index);
			mWordIdArray.add((int)word.getId());
			changeText(word);
		}
		
		mIsWordShowing = !mIsWordShowing;
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		
		}

}
