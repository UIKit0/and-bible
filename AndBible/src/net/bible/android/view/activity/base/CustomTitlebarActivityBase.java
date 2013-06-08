package net.bible.android.view.activity.base;

import net.bible.android.activity.R;
import net.bible.android.control.ControlFactory;
import net.bible.android.view.activity.base.toolbar.DefaultToolbar;
import net.bible.android.view.activity.base.toolbar.Toolbar;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

public abstract class CustomTitlebarActivityBase extends ActivityBase {
	
	public enum HeaderButton {DOCUMENT, PAGE, BIBLE, COMMENTARY, DICTIONARY, TOGGLE_STRONGS, SPEAK, SPEAK_STOP, SPEAK_FF, SPEAK_REW};

	private View mTitleBar;
	
	private Toolbar mToolbar;
	private ProgressBar mProgressBarIndeterminate;

	private View mContentView;
	
	private static final String TAG = "CustomTitlebarActivityBase";

	// called whenever something like strong preferences have been changed by the user.  Should refresh the screen
	protected abstract void preferenceSettingsChanged();
	
	/** custom title bar code to add the FEATURE_CUSTOM_TITLE just before setContentView
	 * and set the new titlebar layout just after
	 */
	
    @Override
	public void setContentView(int layoutResID) {
    	// CCHOW CHANGE START
    	// don't ever hide actionbar 
    	//requestWindowFeature(Window.FEATURE_NO_TITLE);
    	// CCHOW CHANGE END
    	
		super.setContentView(layoutResID);

        mTitleBar = findViewById(R.id.titleBar);
        mContentView = mTitleBar.getRootView();
        initialiseToolbar(mTitleBar);
        mProgressBarIndeterminate = (ProgressBar)findViewById(R.id.progressCircular);
        
        // force the toolbar buttons to be shown correctly
        getToolbar().updateButtons();
    }
    
    public void toggleFullScreen() {
    	if (isFullScreen()) {
    		Log.d(TAG, "Showing title bar");
    		mTitleBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom));
    		mTitleBar.setVisibility(View.VISIBLE);
    		super.toggleFullScreen();
    	} else {
    		Log.d(TAG, "Hiding title bar");
    		Animation animOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
    		animOut.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationEnd(Animation animation) {
					CustomTitlebarActivityBase.super.toggleFullScreen();
				}
				@Override
				public void onAnimationRepeat(Animation animation) {}
				@Override
				public void onAnimationStart(Animation animation) {}
    		});
    		mTitleBar.startAnimation(animOut);
    		mTitleBar.setVisibility(View.GONE);
    	}
    	mContentView.requestLayout();
    }

    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		updateToolbarButtonText();

//		// the title bar has different widths depending on the orientation
//		int titleBarTitleWidthPixels = getResources().getDimensionPixelSize(R.dimen.title_bar_title_width);
//		Log.d(TAG, "Title bar width:"+titleBarTitleWidthPixels);
//		mPageTitle.setWidth(titleBarTitleWidthPixels);
	}

	/** update the quick links in the title bar
     */
    public void updateToolbarButtonText() {
        getToolbar().updateButtons();
    }
    
    protected void initialiseToolbar(View toolBarContainer) {
    	getToolbar().initialise(toolBarContainer);
    }
    
    protected Toolbar getToolbar() {
    	if (mToolbar==null) {
    		mToolbar = new DefaultToolbar();
    	}
    	return mToolbar;
    }

    //TODO move this somewhere appropriate or call PageControl directly
	/** return true if Strongs numbers are shown */
	public boolean isStrongsShown() {
		return ControlFactory.getInstance().getPageControl().isStrongsShown();
	}

	public void setProgressBar(boolean on) {
		mProgressBarIndeterminate.setVisibility(on ? View.VISIBLE : View.GONE);
	}
}
