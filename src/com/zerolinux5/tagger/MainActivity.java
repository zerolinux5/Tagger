package com.zerolinux5.tagger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class MainActivity extends Activity implements OnGestureListener, LocationListener {
	private static final String PREFS_NAME = "BasicPreferences";
	public static final int BUTTONTRUE = 1;
	static private final String LOG_TAG = "MainActivity";
	static private final String SERVER_URL = "http://crowdlab.soe.ucsc.edu/tagstore/default/";
	static private final int MAX_SETUP_DOWNLOAD_TRIES = 2;
	public static final String LABEL_NUMBER = "";
	public static final String NEW_STRING = "";
	public static String label1 = "pothole";
	public static String label2 = "water";
	public static String label3 = "traffic light";
	ProgressBar progressBar;
	Button slideHandleButton;
	SlidingDrawer slidingDrawer;
	private LocationManager locationManager;
	
	private GestureDetector myGesture;
	private static final int SWIPE_MIN_DISTANCE = 100;
	private static final int SWIPE_MAX_OFF_PATH = 200;
	private static final int SWIPE_THRESHOLD_VELOCITY = 100;
	
	public double latitude = 0;
	public double longitude = 0;
	private Location location;
	private Location currentLocation;
	private Criteria criteria;
	public NearbyTags publicTags;
	
	private class ListElement {
		ListElement() {};
		
		public String textLabel;
		public String bearing;
		public Double distance;
	}
	
	private ArrayList<ListElement> aList;
	
	private class MyAdapter extends ArrayAdapter<ListElement>{

		int resource;
		Context context;
		
		public MyAdapter(Context _context, int _resource, List<ListElement> items) {
			super(_context, _resource, items);
			resource = _resource;
			context = _context;
			this.context = _context;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout newView;
			
			ListElement w = getItem(position);
			
			// Inflate a new view if necessary.
			if (convertView == null) {
				newView = new LinearLayout(getContext());
				String inflater = Context.LAYOUT_INFLATER_SERVICE;
				LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
				vi.inflate(resource,  newView, true);
			} else {
				newView = (LinearLayout) convertView;
			}
			
			// Fills in the view.
			TextView tv = (TextView) newView.findViewById(R.id.listText);
			tv.setText(w.textLabel);
			TextView tv2 = (TextView) findViewById(R.id.textView1);
			tv2.setText(Double.toString(w.distance));
			return newView;
		}		
	}

	private MyAdapter aa;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		aList = new ArrayList<ListElement>();
		aa = new MyAdapter(this, R.layout.list_element, aList);
		ListView myListView = (ListView) findViewById(R.id.listView1);
		myListView.setAdapter(aa);
		aa.notifyDataSetChanged();
		
		myGesture = new GestureDetector(this);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0); 
		//set the labels and urls to what was previously saved if anything was saved
		String temp = settings.getString("label1", "");
		if (temp.length() != 0)
			label1 = settings.getString("label1", "");
		temp = settings.getString("label2", "");
		if (temp.length() != 0)
			label2 = settings.getString("label2", "");
		temp = settings.getString("label3", "");
		if (temp.length() != 0)
			label3 = settings.getString("label3", "");
		//set the buttons to correspond to their id
		Button b1 = (Button) findViewById(R.id.button1);
		b1.setText(label1);
		Button b2 = (Button) findViewById(R.id.button2);
		b2.setText(label2);
		Button b3 = (Button) findViewById(R.id.button3);
		b3.setText(label3);
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		progressBar.setVisibility(View.GONE);
		
		//set the buttons to correspond to their id
		b1 = (Button) findViewById(R.id.button1);
		b2 = (Button) findViewById(R.id.button2);
		b3 = (Button) findViewById(R.id.button3);
		
		//set the sliding bar and its methods
		slideHandleButton = (Button) findViewById(R.id.handle);
		slidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
		
		slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener(){
			@Override
			public void onDrawerOpened() {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		slidingDrawer.setOnDrawerCloseListener( new OnDrawerCloseListener(){
			@Override
			public void onDrawerClosed() {
				// TODO Auto-generated method stub
			}
			
		});
		
		//set button listeners
		b1.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				buttonOne(null);
			}
			
		});
		
		b2.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				buttonTwo(null);
			}
			
		});
		
		b3.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				buttonThree(null);
			}
			
		});
		
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		// Define a set of criteria used to select a location provider.
	    criteria = new Criteria();
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
	    criteria.setAltitudeRequired(false);
	    criteria.setBearingRequired(false);
	    criteria.setCostAllowed(true);
	    criteria.setPowerRequirement(Criteria.POWER_LOW);
	    getTags(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void uploadTag(View v) {
		// Let us build the parameters.
		ServerCallParams serverParams = new ServerCallParams();
		serverParams.url = "add_tagging.json";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("token", "CMPS121_yehaa"));
		params.add(new BasicNameValuePair("user", "luca"));
		params.add(new BasicNameValuePair("lat", "37.2"));
		params.add(new BasicNameValuePair("lng", "120.5"));
		params.add(new BasicNameValuePair("tag", "bridge"));
		serverParams.params = params;
		serverParams.continuation = new ContinuationAddTag();
		ContactServer contacter = new ContactServer();
		contacter.execute(serverParams);
	}
	
	class ContinuationAddTag implements Continuation {
		ContinuationAddTag() {}
		
		public void useString(String s) {
			if (s == null) {
				Log.d(LOG_TAG, "Returned an empty string.");
			} else {
				Log.d(LOG_TAG, "Returned: " + s);
			}
		}
	}

	public void getTags(View v) {
		// Let us build the parameters.
		ServerCallParams serverParams = new ServerCallParams();
	    String provider = locationManager.getBestProvider(criteria, true);
	    Location location = locationManager.getLastKnownLocation(provider);
		serverParams.url = "get_tags.json";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("token", "CMPS121_yehaa"));
		params.add(new BasicNameValuePair("user", "luca"));
		params.add(new BasicNameValuePair("lat_min", Double.toString(location.getLatitude() - .05)));
		params.add(new BasicNameValuePair("lng_min", Double.toString(location.getLongitude() - .05)));
		params.add(new BasicNameValuePair("lat_max", Double.toString(location.getLatitude() + .05)));
		params.add(new BasicNameValuePair("lng_max", Double.toString(location.getLongitude() + .05)));
		params.add(new BasicNameValuePair("n_taggings", "20"));
		serverParams.params = params;
		serverParams.continuation = new ContinuationGetTagList();
		ContactServer contacter = new ContactServer();
		contacter.execute(serverParams);
	}
	
	
	class ContinuationGetTagList implements Continuation {
		public ContinuationGetTagList() {}

		public void useString(String s) {
			// Dejasonize the string.
			if (s == null) {
				Log.d(LOG_TAG, "Returned an empty string.");
			} else {
				Log.d(LOG_TAG, "Returned: " + s);
			    NearbyTags newTags = decodeNearbyTags(s);
			    if (newTags != null) {
			    	ListElement el = new ListElement();
				    for (int i = 0; i < newTags.tags.length; i++){
				    	el.textLabel=publicTags.tags[i].tag;
				    	el.distance=Math.sqrt(Math.pow((latitude - publicTags.tags[i].lat), 2) + Math.pow((longitude - publicTags.tags[i].lng), 2));
				    }
				    aa.notifyDataSetChanged();
			    	// We would have to replace the list in the array adaptor.
			    	Log.d(LOG_TAG, "The dejsonizing succeeded");
			    	Log.d(LOG_TAG, "N. of tags:" + newTags.tags.length);
			    }
			}
		}
	}
	
	interface Continuation {
		void useString(String s);
	}
	
	class ServerCallParams {
		public String url; // for example: get_tags.json
		public List<NameValuePair> params;
		public Continuation continuation;
	}
	
	class FinishInfo {
		public Continuation continuation;
		public String value;
	}

	// This class executed an http call to the server. 
	// You need to pass to it the ServerCallParams, containing the method to call,
	// a list of parameters for the call, and what to do afterwards (the continuation).
    private class ContactServer extends AsyncTask<ServerCallParams, String, FinishInfo> {

    	protected FinishInfo doInBackground(ServerCallParams... callParams) {
    		Log.d(LOG_TAG, "Starting the download.");
    		String downloadedString = null;
    		int numTries = 0;
    		ServerCallParams callInfo = callParams[0];
    		List<NameValuePair> params = callInfo.params;
			FinishInfo info = new FinishInfo();
			info.continuation = callInfo.continuation;
    		while (downloadedString == null && numTries < MAX_SETUP_DOWNLOAD_TRIES && !isCancelled()) {
    			numTries++;
    			HttpClient httpclient = new DefaultHttpClient();
    		    HttpPost httppost = new HttpPost(SERVER_URL + callInfo.url);
    	        HttpResponse response = null; 
    			try {
        	        httppost.setEntity(new UrlEncodedFormEntity(params));
        	        // Execute HTTP Post Request
    				response = httpclient.execute(httppost);
    			} catch (ClientProtocolException ex) {
    				Log.e(LOG_TAG, ex.toString());
    			} catch (IOException ex) {
    				Log.w(LOG_TAG, ex.toString());
    			}
    			if (response != null) {
    				// Checks the status code.
    				int statusCode = response.getStatusLine().getStatusCode();
    				Log.d(LOG_TAG, "Status code: " + statusCode);

    				if (statusCode == HttpURLConnection.HTTP_OK) {
    					// Correct response. Reads the real result.
    					// Extracts the string content of the response.
    					HttpEntity entity = response.getEntity();
    					InputStream iStream = null;
    					try {
    						iStream = entity.getContent();
    					} catch (IOException ex) {
    						Log.e(LOG_TAG, ex.toString());
    					}
    					if (iStream != null) {
    						downloadedString = ConvertStreamToString(iStream);
    						Log.d(LOG_TAG, "Received string: " + downloadedString);
    						// Passes the string, along with the continuation, to onPostExecute.
    						info.value = downloadedString;
    				    	return info;
    					}
    				}
    			}
    		}
    		// Returns null to indicate failure.
    		info.value = null;
    		return info;
    	}
    	
    	protected void onProgressUpdate(String... s) {}
    	
    	protected void onPostExecute(FinishInfo info) {
    		// Do something with what you get. 
    		if (info != null) {
    			info.continuation.useString(info.value);
    		} else {
    			// This is just an example: we can pass back null to the continuation
    			// to indicate that no string was in fact received.
    			info.continuation.useString(null);
    		}
    	}
    }
    
    // Here is an example of how to decode a JSON string.  We will decode the taglist.
    // First, we declare a class for the info on one tag.
    // Note that if you want to have this accessible from multiple activities, as it might be
    // a good idea to do, it might be better to define this as a public class in its own file,
    // rather than here. 
    class TagInfo {
    	public double lat;
    	public double lng;
    	public String nick;
    	public String tag;
    }
    // Now we create a class for the overall message.
    class NearbyTags {
    	public TagInfo[] tags;
    }
    
    // Decoding a received tag string is then simple.
    private NearbyTags decodeNearbyTags(String s) {
    	if (s == null) {
    		// Your choice of what to do; returning null may be a simple way to 
    		// propagate the fact that the call to the server failed.
    		return null;
    	}
    	// Gets a gson object for decoding a string.
    	Gson gson = new Gson();
    	NearbyTags tags = null;
    	try {
    		tags = gson.fromJson(s, NearbyTags.class);
    		publicTags = tags;
    	} catch (JsonSyntaxException ex) {
    		Log.w(LOG_TAG, "Error decoding json: " + s + " exception: " + ex.toString());
    	}
    	return tags;
    }
    
    
    @Override
    public void onStop() {
    	// Cancel what you have to cancel.
    	super.onStop();
    }
    
    public static String ConvertStreamToString(InputStream is) {
    	if (is == null) {
    		return null;
    	}
    	
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append((line + "\n"));
	        }
	    } catch (IOException e) {
	        Log.d(LOG_TAG, e.toString());
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            Log.d(LOG_TAG, e.toString());
	        }
	    }
	    return sb.toString();
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
			} else {
				menu(null);
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
	
	  public void menu(View v){
	    	Intent intent = new Intent(MainActivity.this, MenuActivity.class);
	    	startActivityForResult(intent, BUTTONTRUE);  
	  }

		@Override
		public boolean dispatchTouchEvent(MotionEvent e){
		    super.dispatchTouchEvent(e);
		    return myGesture.onTouchEvent(e);
		}
		
	    // Gets the return value.
	    @Override
	    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
		    String provider = locationManager.getBestProvider(criteria, true);
		    Location location = locationManager.getLastKnownLocation(provider);
	    	if(requestCode == BUTTONTRUE){
	    			if (resultCode == RESULT_OK) {

	    				if(Integer.parseInt(data.getStringExtra(LABEL_NUMBER)) == 1){	
	    					Button b = (Button) findViewById(R.id.button1);
	    					if (MenuActivity.NEWSTRING.length() != 0){
		    					b.setText(MenuActivity.NEWSTRING);
		    					label1 = MenuActivity.NEWSTRING;
	    					}
	    				} 
	    				if(Integer.parseInt(data.getStringExtra(LABEL_NUMBER)) == 2){	
	    					Button b = (Button) findViewById(R.id.button2);
	    					if (MenuActivity.NEWSTRING.length() != 0){
		    					b.setText(MenuActivity.NEWSTRING);
		    					label2 = MenuActivity.NEWSTRING;
	    					}
	    				} 
	    				if(Integer.parseInt(data.getStringExtra(LABEL_NUMBER)) == 3){	
	    					Button b = (Button) findViewById(R.id.button3);
	    					if (MenuActivity.NEWSTRING.length() != 0){
		    					b.setText(MenuActivity.NEWSTRING);
		    					label3 = MenuActivity.NEWSTRING;
	    					}
	    				} 
	    			}
	    	}
		  	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		  	SharedPreferences.Editor editor = settings.edit(); 
		  	editor.putString("label1", label1);
		  	editor.putString("label2", label2);
		  	editor.putString("label3", label3);
		  	editor.commit();
		  	super.onPause();
	    }
	    
		public void buttonOne(View v){
			slidingDrawer.close();
		    String provider = locationManager.getBestProvider(criteria, true);
		    Location location = locationManager.getLastKnownLocation(provider);
		    latitude = location.getLatitude();
		    longitude = location.getLongitude();
			// Let us build the parameters.
			ServerCallParams serverParams = new ServerCallParams();
			serverParams.url = "add_tagging.json";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("token", "CMPS121_yehaa"));
			params.add(new BasicNameValuePair("user", "luca"));
			params.add(new BasicNameValuePair("lat",Double.toString(latitude)));
			params.add(new BasicNameValuePair("lng", Double.toString(longitude)));
			params.add(new BasicNameValuePair("tag", label1));
			serverParams.params = params;
			serverParams.continuation = new ContinuationAddTag();
			ContactServer contacter = new ContactServer();
			contacter.execute(serverParams);
			Log.d(LOG_TAG, "lat: "+latitude+" lng: "+longitude);
		}
		
		public void buttonTwo(View v){
			slidingDrawer.close();
		    String provider = locationManager.getBestProvider(criteria, true);
		    Location location = locationManager.getLastKnownLocation(provider);
		    latitude = location.getLatitude();
		    longitude = location.getLongitude();
			// Let us build the parameters.
			ServerCallParams serverParams = new ServerCallParams();
			serverParams.url = "add_tagging.json";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("token", "CMPS121_yehaa"));
			params.add(new BasicNameValuePair("user", "luca"));
			params.add(new BasicNameValuePair("lat", Double.toString(latitude)));
			params.add(new BasicNameValuePair("lng", Double.toString(longitude)));
			params.add(new BasicNameValuePair("tag", label2));
			serverParams.params = params;
			serverParams.continuation = new ContinuationAddTag();
			ContactServer contacter = new ContactServer();
			contacter.execute(serverParams);
			Log.d(LOG_TAG, "lat: "+latitude+" lng: "+longitude);
		}
		
		public void buttonThree(View v){
			slidingDrawer.close();
		    String provider = locationManager.getBestProvider(criteria, true);
		    Location location = locationManager.getLastKnownLocation(provider);
		    latitude = location.getLatitude();
		    longitude = location.getLongitude();
			// Let us build the parameters.
			ServerCallParams serverParams = new ServerCallParams();
			serverParams.url = "add_tagging.json";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("token", "CMPS121_yehaa"));
			params.add(new BasicNameValuePair("user", "luca"));
			params.add(new BasicNameValuePair("lat", Double.toString(latitude)));
			params.add(new BasicNameValuePair("lng", Double.toString(longitude)));
			params.add(new BasicNameValuePair("tag", label3));
			serverParams.params = params;
			serverParams.continuation = new ContinuationAddTag();
			ContactServer contacter = new ContactServer();
			contacter.execute(serverParams);
			Log.d(LOG_TAG, "lat: "+latitude+" lng: "+longitude);
		}
		
		@Override
		public void onPause(){
		    super.onPause();
		    locationManager.removeUpdates(this);
		}

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			latitude = (double) (location.getLatitude());
			longitude = (double) (location.getLongitude());
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
		  public Location getLocation() {
			    return location;
			  }
		  
		  @Override 
		  public void onResume(){
			  super.onResume();
			  getTags(null);
		  }
		  
}
