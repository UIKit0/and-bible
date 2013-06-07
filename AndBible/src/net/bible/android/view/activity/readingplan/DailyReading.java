package net.bible.android.view.activity.readingplan;

import java.util.ArrayList;
import java.util.List;

import net.bible.android.activity.R;
import net.bible.android.control.ControlFactory;
import net.bible.android.control.readingplan.ReadingPlanControl;
import net.bible.android.control.readingplan.ReadingStatus;
import net.bible.android.view.activity.base.CustomTitlebarActivityBase;
import net.bible.android.view.activity.base.Dialogs;
import net.bible.android.view.activity.base.toolbar.Toolbar;
import net.bible.android.view.activity.readingplan.toolbar.ReadingPlanToolbar;
import net.bible.service.common.CommonUtils;
import net.bible.service.readingplan.OneDaysReadingsDto;

import org.crosswire.jsword.passage.Key;
import org.crosswire.jsword.versification.BookName;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/** Allow user to enter search criteria
 * 
 * @author Martin Denham [mjdenham at gmail dot com]
 * @see gnu.lgpl.License for license details.<br>
 *      The copyright to this program is held by it's author.
 */
public class DailyReading extends CustomTitlebarActivityBase {
	
	private static final String TAG = "DailyReading";
	
	private TextView mDescriptionView;
	private TextView mDayView;
	private TextView mDateView;
	private List<ImageView> mImageTickList;

	private MenuItem mMenuItemAllDone;
	
	private int mDay;
	
	public static final String PLAN = "net.bible.android.view.activity.readingplan.Plan";
	public static final String DAY = "net.bible.android.view.activity.readingplan.Day";
	
	private OneDaysReadingsDto mReadings;

	private Toolbar mToolbar;

	private ReadingPlanControl mReadingPlanControl = ControlFactory.getInstance().getReadingPlanControl();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, true);
        Log.i(TAG, "Displaying one day reading plan");
        setContentView(R.layout.reading_plan_one_day);
        
        try {
			// may not be for current day if user presses forward or backward
	        mDay = mReadingPlanControl.getCurrentPlanDay();
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				if (extras.containsKey(PLAN)) {
					mReadingPlanControl.setReadingPlan(extras.getString(PLAN));
				}
				if (extras.containsKey(DAY)) {
					mDay = extras.getInt(DAY, mDay);
				}
			}

			// get readings for chosen day
	        mReadings = mReadingPlanControl.getDaysReading(mDay);
	        
	        // Populate view
	        mDescriptionView =  (TextView)findViewById(R.id.description);
	        mDescriptionView.setText(mReadings.getReadingPlanInfo().getDescription());
	
	        // date display
	        mDayView =  (TextView)findViewById(R.id.day);
	        mDayView.setText(mReadings.getDayDesc());
	        mDateView =  (TextView)findViewById(R.id.date);
	        mDateView.setText(mReadings.getReadingDateString());
	        
	        mImageTickList = new ArrayList<ImageView>();

	        // show short book name to save space if Portrait
			boolean fullBookNameSave = BookName.isFullBookName();
			BookName.setFullBookName(!CommonUtils.isPortrait());
	        
	        TableLayout layout = (TableLayout)findViewById(R.id.reading_container);
	        for (int i=0; i<mReadings.getNumReadings(); i++) {
	        	final int readingNo = i;
	            View child = getLayoutInflater().inflate(R.layout.reading_plan_one_reading, null);
	
	            // Ticks
	            mImageTickList.add((ImageView)child.findViewById(R.id.tick));
	            
	            // Passage description
	            TextView rdgText = (TextView)child.findViewById(R.id.passage);
	            rdgText.setText(mReadings.getReadingKey(readingNo).getName());

	            // handle read button clicks
	            Button readBtn = (Button)child.findViewById(R.id.readButton);
	            readBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onRead(readingNo);
					}
				});

	            // handle speak button clicks
	            Button speakBtn = (Button)child.findViewById(R.id.speakButton);
	            speakBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onSpeak(readingNo);
					}
				});
	            
	            layout.addView(child, readingNo);
	        }
	
			// restore full book name setting
			BookName.setFullBookName(fullBookNameSave);

	        updateTicksAndDone();
	        
	        // Speak All
	        if (mReadings.getNumReadings()>1) {
		        View child = getLayoutInflater().inflate(R.layout.reading_plan_one_reading, null);
		
		        // hide the tick
		        ImageView tick = (ImageView)child.findViewById(R.id.tick);
		        tick.setVisibility(View.INVISIBLE);
		        
		        // Passage description
		        TextView rdgText = (TextView)child.findViewById(R.id.passage);
		        rdgText.setText(getResources().getString(R.string.all));
		
		        Button passageBtn = (Button)child.findViewById(R.id.readButton);
		        passageBtn.setVisibility(View.INVISIBLE);
		        
		        Button speakBtn = (Button)child.findViewById(R.id.speakButton);
		        speakBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						onSpeakAll(null);
					}
				});
		        layout.addView(child, mReadings.getNumReadings());
	        }
	        // end All
	        
	        Log.d(TAG, "Finished displaying Reading view");
        } catch (Exception e) {
        	Log.e(TAG, "Error showing daily readings", e);
        	Dialogs.getInstance().showErrorMsg(R.string.error_occurred);
        }

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    /** user pressed read button by 1 reading
	 */
    public void onRead(int readingNo) {
    	Log.i(TAG, "Read "+readingNo);
    	Key readingKey = mReadings.getReadingKey(readingNo);
    	mReadingPlanControl.read(mDay, readingNo, readingKey);
    	
    	finish();
    }

    /** user pressed speak button by 1 reading
	 */
    public void onSpeak(int readingNo) {
    	Log.i(TAG, "Speak "+readingNo);
    	Key readingKey = mReadings.getReadingKey(readingNo);
    	mReadingPlanControl.speak(mDay, readingNo, readingKey);
    	
    	updateTicksAndDone();
    }
    /** user pressed speak button by All
	 */
    public void onSpeakAll(View view) {
    	Log.i(TAG, "Speak all");
    	mReadingPlanControl.speak(mDay, mReadings.getReadingKeys());
    	
    	updateTicksAndDone();
    }

    // the button that called this has been removed
    public void onNext(View view) {
    	Log.i(TAG, "Next");
    	if (mDay<mReadings.getReadingPlanInfo().getNumberOfPlanDays()) {
    		showDay(mDay+1);
    	}
    }

    // the button that called this has been removed
    public void onPrevious(View view) {
    	Log.i(TAG, "Previous");
    	if (mDay>1) {
    		showDay(mDay-1);
    	}
    }
    
    /** user pressed Done button so must have read currently displayed readings
     */
	private void onDone() {
    	Log.i(TAG, "Done");
    	try {
	    	// do not add to History list because it will just redisplay same page
	    	setIntegrateWithHistoryManager(false);
	    	
	    	// all readings must be ticked for this to be enabled
	    	int nextDayToShow = mReadingPlanControl.done(mReadings.getReadingPlanInfo(), mDay, false);
	    	
	    	//if user is behind then go to next days readings
	    	if (nextDayToShow>0) {
	    		showDay(nextDayToShow);
	    	} else {
	    		// else exit
	        	finish();
	    	}
	
	    	// if we move away then add to history list
	    	setIntegrateWithHistoryManager(true);
        } catch (Exception e) {
        	Log.e(TAG, "Error when Done daily reading", e);
        	Dialogs.getInstance().showErrorMsg(R.string.error_occurred);
        }
    }
    
    /** allow activity to enhance intent to correctly restore state */
	public Intent getIntentForHistoryList() {
		Intent intent = getIntent();
		
		intent.putExtra(DailyReading.PLAN, mReadings.getReadingPlanInfo().getCode());
		intent.putExtra(DailyReading.DAY, mReadings.getDay());

		return intent;
	}


    /** I don't think this is used because of hte finish() in onSearch()
     */
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode==Activity.RESULT_OK) {
    		returnToPreviousScreen();
    	}
    }

	private void showDay(int dayNo) {
    	Log.i(TAG, "ShowDay "+dayNo);
    	Intent handlerIntent = new Intent(this, DailyReading.class);
    	handlerIntent.putExtra(DAY, dayNo);
    	startActivity(handlerIntent);
    	finish();
    }

	private void updateTicksAndDone() {
		ReadingStatus status = mReadingPlanControl.getReadingStatus(mDay);
		
		for (int i=0; i<mImageTickList.size(); i++) {
			ImageView imageTick = mImageTickList.get(i);
			if (status.isRead(i)) {
				imageTick.setImageResource(R.drawable.btn_check_buttonless_on);
			} else {
				imageTick.setImageResource(R.drawable.btn_check_buttonless_off);
			}
		}
		
		if (mMenuItemAllDone != null) {
			mMenuItemAllDone.setEnabled(status.isAllRead());
			supportInvalidateOptionsMenu();
		}
	}
	
	
	
    @Override
	protected void onScreenTurnedOn() {
		super.onScreenTurnedOn();
		// use reload to ensure colour is correct
		reload();
	}

    /** Could possibly push this reload up to a higher level
     * See: http://stackoverflow.com/questions/1397361/how-do-i-restart-an-android-activity
     */
    private void reload() {
    	// do not save current page to history because it is being reloaded
    	boolean wasIntegrateWithhistory = isIntegrateWithHistoryManager();
    	setIntegrateWithHistoryManager(false);
    	
    	// reload page to refresh if screen colour change
    	Intent intent = getIntent();
    	finish();
    	startActivity(intent);
    	
    	setIntegrateWithHistoryManager(wasIntegrateWithhistory);
    }

	protected Toolbar getToolbar() {
    	if (mToolbar==null) {
    		mToolbar = new ReadingPlanToolbar();
    	}
    	return mToolbar;
    }

	@Override
	protected void preferenceSettingsChanged() {
		//NOOP
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.reading_plan, menu);
		mMenuItemAllDone = menu.findItem(R.id.alldone);
        return true;
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		ReadingStatus status = mReadingPlanControl.getReadingStatus(mDay);
		mMenuItemAllDone.setEnabled(status.isAllRead());
		return true;
	}

	/** 
     * on Click handlers
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean isHandled = false;
        
        switch (item.getItemId()) {
		// action bar up
		case (android.R.id.home):
			onBackPressed();
		break;
        // selected to allow jump to a certain day
		case (R.id.done):
	    	Log.i(TAG, "Force Done");
	    	try {
		    	mReadingPlanControl.done(mReadings.getReadingPlanInfo(), mDay, true);
		    	updateTicksAndDone();
	        } catch (Exception e) {
	        	Log.e(TAG, "Error when Done daily reading", e);
	        	Dialogs.getInstance().showErrorMsg(R.string.error_occurred);
	        }

			break;
		case (R.id.reset):
			mReadingPlanControl.reset(mReadings.getReadingPlanInfo());
			finish();
			isHandled = true;
			break;
		case (R.id.setStartToJan1):
			mReadingPlanControl.setStartToJan1(mReadings.getReadingPlanInfo());
		
			// refetch readings for chosen day
	        mReadings = mReadingPlanControl.getDaysReading(mDay);
	        
	        // update date and day no
	        mDateView.setText(mReadings.getReadingDateString());
			mDayView.setText(mReadings.getDayDesc());
			
			isHandled = true;
			break;
		// all done button
		case (R.id.alldone):
			onDone();
			isHandled = true;
			break;
        }
        
		if (!isHandled) {
            isHandled = super.onOptionsItemSelected(item);
        }
        
     	return isHandled;
    }
}
