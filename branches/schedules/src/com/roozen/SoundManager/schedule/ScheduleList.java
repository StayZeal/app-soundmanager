/**
 * Copyright 2009 Mike Partridge
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed 
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License. 
 */
package com.roozen.SoundManager.schedule;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.roozen.SoundManager.R;
import com.roozen.SoundManager.provider.ScheduleProvider;
import com.roozen.SoundManager.utils.SQLiteDatabaseHelper;

/**
 * Schedule List
 * 
 * @author Mike Partridge
 */
public class ScheduleList extends ListActivity {
    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT   = 1;
    
    public static final String VOLUME_TYPE = "VOLUME_TYPE";
    private int mVolumeType;
    
    private TextView mListHeader;    

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_list);
        
        mVolumeType = savedInstanceState == null ? -1 :
            savedInstanceState.getInt(VOLUME_TYPE);
        
        if (mVolumeType < 0) {
            Bundle extras = getIntent().getExtras();
            mVolumeType = Integer.parseInt(extras.getString(VOLUME_TYPE));
        }
        
        /*
         * set the header text based on volume type
         */
        mListHeader = (TextView) findViewById(R.id.ScheduleType);
        switch (mVolumeType) {
            case AudioManager.STREAM_SYSTEM:
                mListHeader.setText(R.string.SystemVolumeSchedule);
                break;
            case AudioManager.STREAM_RING:
                mListHeader.setText(R.string.RingerVolumeSchedule);
                break;
            case AudioManager.STREAM_MUSIC:
                mListHeader.setText(R.string.MediaVolumeSchedule);
                break;
            case AudioManager.STREAM_ALARM:
                mListHeader.setText(R.string.AlarmVolumeSchedule);
                break;
            case AudioManager.STREAM_VOICE_CALL:
                mListHeader.setText(R.string.InCallVolumeSchedule);
                break;
        }
        
        fillData();
        
        registerForContextMenu(getListView());
	}

	/**
	 * retrieves schedules from the db and populates the list
	 */
	private void fillData() {
	    
	    /*
	     * get all schedules rows for this type
	     */
	    Uri schedulesUri = Uri.withAppendedPath(ScheduleProvider.CONTENT_URI, ScheduleProvider.getMimeType(mVolumeType));
	    Cursor scheduleCursor = managedQuery(schedulesUri, null, null, null, null);
	    
	    ScheduleListAdapter sla = new ScheduleListAdapter(this);
	    
	    if (scheduleCursor.moveToFirst()) {
            
            int idIndex = scheduleCursor.getColumnIndex(SQLiteDatabaseHelper.SCHEDULE_ID);
            int day0Index = scheduleCursor.getColumnIndex(SQLiteDatabaseHelper.SCHEDULE_DAY0);
            int day1Index = scheduleCursor.getColumnIndex(SQLiteDatabaseHelper.SCHEDULE_DAY1);
            int day2Index = scheduleCursor.getColumnIndex(SQLiteDatabaseHelper.SCHEDULE_DAY2);
            int day3Index = scheduleCursor.getColumnIndex(SQLiteDatabaseHelper.SCHEDULE_DAY3);
            int day4Index = scheduleCursor.getColumnIndex(SQLiteDatabaseHelper.SCHEDULE_DAY4);
            int day5Index = scheduleCursor.getColumnIndex(SQLiteDatabaseHelper.SCHEDULE_DAY5);
            int day6Index = scheduleCursor.getColumnIndex(SQLiteDatabaseHelper.SCHEDULE_DAY6);
            int startHourIndex = scheduleCursor.getColumnIndex(SQLiteDatabaseHelper.SCHEDULE_START_HOUR);
            int startMinuteIndex = scheduleCursor.getColumnIndex(SQLiteDatabaseHelper.SCHEDULE_START_MINUTE);
            int volumeIndex = scheduleCursor.getColumnIndex(SQLiteDatabaseHelper.SCHEDULE_VOLUME);
            int vibrateIndex = scheduleCursor.getColumnIndex(SQLiteDatabaseHelper.SCHEDULE_VIBRATE);
            int activeIndex = scheduleCursor.getColumnIndex(SQLiteDatabaseHelper.SCHEDULE_ACTIVE);
            
	        do {
	            
	            Schedule s = new Schedule(scheduleCursor.getInt(idIndex),
                             (scheduleCursor.getInt(day0Index) > 0),
                             (scheduleCursor.getInt(day1Index) > 0),
                             (scheduleCursor.getInt(day2Index) > 0),
                             (scheduleCursor.getInt(day3Index) > 0),
                             (scheduleCursor.getInt(day4Index) > 0),
                             (scheduleCursor.getInt(day5Index) > 0),
                             (scheduleCursor.getInt(day6Index) > 0),
                             scheduleCursor.getInt(startHourIndex),
                             scheduleCursor.getInt(startMinuteIndex),
                             scheduleCursor.getInt(volumeIndex),
                             mVolumeType,
                             (scheduleCursor.getInt(vibrateIndex) > 0),
                             (scheduleCursor.getInt(activeIndex) > 0)
                             );
	        
	            sla.addItem(s);
	            
	        } while(scheduleCursor.moveToNext());
	        	        
	    }
	    
	    setListAdapter(sla);
	}
	
	/* (non-Javadoc)
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {        
        Intent i = new Intent(this, ScheduleEdit.class);
        i.putExtra(SQLiteDatabaseHelper.SCHEDULE_ID, id);
        i.putExtra(VOLUME_TYPE, mVolumeType);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    /* (non-Javadoc)
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.schedulelist_context, menu);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch(item.getItemId()) {
        case R.id.editSchedule:
            Intent i = new Intent(this, ScheduleEdit.class);
            i.putExtra(SQLiteDatabaseHelper.SCHEDULE_ID, info.id);
            i.putExtra(VOLUME_TYPE, mVolumeType);
            startActivityForResult(i, ACTIVITY_EDIT);
        	return true;
        	
        case R.id.deleteSchedule:
            Uri deleteUri = Uri.withAppendedPath(ScheduleProvider.CONTENT_URI, String.valueOf(info.id));
            getContentResolver().delete(deleteUri, null, null);
            fillData();
            return true;
		
		case R.id.toggleSchedule:
		    toggleSchedule(info.id);
		    fillData();
		    return true;
		    
        }
		
		return super.onContextItemSelected(item);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.schedulelist_options, menu);
        
        return result;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    
        switch(item.getItemId()) {
        case R.id.newSchedule:
            Intent i = new Intent(this, ScheduleEdit.class);
            i.putExtra(VOLUME_TYPE, mVolumeType);
            startActivityForResult(i, ACTIVITY_CREATE);
            return true;
        }
       
        return super.onOptionsItemSelected(item);
	}

	/* (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        
        fillData();
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        //save the volume type for re-population on resume
        outState.putInt(VOLUME_TYPE, mVolumeType);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        fillData();
    }
	
    private void toggleSchedule(long scheduleId) {
        
        boolean active = true;
        
        /*
         * retrieve the active state for this schedule
         */
        {
            Uri schedulesUri = Uri.withAppendedPath(ScheduleProvider.CONTENT_URI, String.valueOf(scheduleId));
            Cursor scheduleCursor = managedQuery(schedulesUri, null, null, null, null);

            if (scheduleCursor.moveToFirst()) {
                active = (scheduleCursor.getInt(scheduleCursor.getColumnIndexOrThrow(SQLiteDatabaseHelper.SCHEDULE_ACTIVE)) > 0);
            }
        }
        
        ContentValues values = new ContentValues();

        //flip it
        values.put(SQLiteDatabaseHelper.SCHEDULE_ACTIVE, active ? "0" : "1");
        
        Uri updateUri = Uri.withAppendedPath(ScheduleProvider.CONTENT_URI, String.valueOf(scheduleId));
        getContentResolver().update(updateUri, values, null, null);
        
    }
    
}
