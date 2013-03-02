package com.zerolinux5.tagger;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SlidingDrawer;
import android.widget.Toast;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;

public class MenuActivity extends Activity implements OnGestureListener {
	private static final String LOG_TAG = "ChooserActivity";
	private static final String PREFS_NAME = "BasicPreferences";
	private RadioGroup r1;
	public static String NEWSTRING = " ";
	public static final String NEWSTRING_POINTER = "NEWSTRING";
	SlidingDrawer slidingDrawer;
	
	private GestureDetector myGesture;
	private static final int SWIPE_MIN_DISTANCE = 200;
	private static final int SWIPE_MAX_OFF_PATH = 200;
	private static final int SWIPE_THRESHOLD_VELOCITY = 150;
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent e){
	    super.dispatchTouchEvent(e);
	    return myGesture.onTouchEvent(e);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View v;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		Button b1 = (Button) findViewById(R.id.button1);
	    r1 = (RadioGroup) findViewById(R.id.radioGroup1);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		NEWSTRING = settings.getString(NEWSTRING_POINTER, " ");
		myGesture = new GestureDetector(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}
	
	public void causeReturn(View v) {
		EditText simpleEditText = (EditText) findViewById(R.id.editText1);
		String newString = simpleEditText.getText().toString();
			String buttonlabel = Integer.toString(getRadioButton());
			Intent result = new Intent();
			result.putExtra(MainActivity.NEW_STRING, newString);
			NEWSTRING = newString;
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			final SharedPreferences.Editor editor = settings.edit(); 
			editor.putString("NEWSTRING", newString);
			editor.commit();
			Log.d(LOG_TAG, "Chosen: " +  newString);
			result.putExtra(MainActivity.LABEL_NUMBER, buttonlabel);
			setResult(RESULT_OK, result);
			finish();
	}

	   private int getRadioButton() {
		      int checkedRadioId = r1.getCheckedRadioButtonId();
		      switch (checkedRadioId) {
		      case R.id.radio0:
		         return 1;
		      case R.id.radio1:
		         return 2;
		      case R.id.radio2:
		         return 3;
		      default:
		    	  return -1;
		      }
		   }

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		float dX = e2.getX()-e1.getX();
		float dY = e1.getY()-e2.getY();
		if (Math.abs(dY)<SWIPE_MAX_OFF_PATH && Math.abs(velocityX)>=SWIPE_THRESHOLD_VELOCITY && Math.abs(dX)>=SWIPE_MIN_DISTANCE ) {
			if (dX>0) {
				causeReturn(null);
			} else {
			}
			return true;
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
}