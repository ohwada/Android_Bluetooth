/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.tester1;

import java.util.List;

import jp.ohwada.android.bluetooth.lib.BtActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Main Activity
 */
public class MainActivity extends BtActivity {

    // Debugging
    private static final boolean D = true;
    private static final boolean D_NUM = true;
    private static final boolean D_SERVICE_READ = true;
    private static final boolean D_MANAGER_READ = true;

    // mode
    private static final int MODE_ECHO = 0;
    private static final int MODE_TESTER = 1;

    // timer
    private static final int TIMER_DELAY = 1;
    private static final int TIMER_WHAT = 11;

    // test param
    private static final int MAX_COUNT = 1100;

    // char
    private static final String LF =  "\n";

    // class object
    private SequenceChecker mSequenceChecker;

    // UI
    private Button mButtonStart;
    private Button mButtonStop;
    private Button mButtonReset;
    private ArrayAdapter<String> mAdapter;

    // mode
    private int mMode = MODE_ECHO;

    // send param
    private int mCount = 0;
    private boolean isStart = false;
    private long mStartTime = 0;

    // timer
    private boolean isTimerStart;
    private boolean isTimerRunning;
	
    /**
     * === onCreate ===
     */
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        bt_init();
        bt_setDebug( D );
        bt_getManager().setDebugServiceRead( D_SERVICE_READ );
        bt_getManager().setDebugManagerRead( D_MANAGER_READ );
		
        /* bluetooth */
        bt_initManager();
        bt_initContentView( R.layout.activity_main );
        bt_initLinearLayoutConnect( R.id.LinearLayout_connect );
        bt_initButtonConnectSecure( R.id.Button_connect_secure );
        bt_initButtonConnectInsecure( R.id.Button_connect_insecure );
        bt_initScrollViewDebug( R.id.ScrollView_debug );
        bt_initTextViewDebug( R.id.TextView_debug );
        bt_initDeviceListClass( DeviceListActivity.class );
        bt_initSettingsClass( SettingsActivity.class );
        bt_setServiceProfileChat();
        bt_initAdapterAndFinish();

        mSequenceChecker = new SequenceChecker();

        RadioGroup rgProfile = (RadioGroup) findViewById( R.id.RadioGroup_profile );
        rgProfile.check( R.id.RadioButton_profile_chat );
        rgProfile.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged( RadioGroup group, int checkedId ) { 
                execProfileChanged( group, checkedId );
            }
        });
 
        RadioGroup rgMode = (RadioGroup) findViewById( R.id.RadioGroup_mode );
        rgMode.check( R.id.RadioButton_mode_echo );
        rgMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged( RadioGroup group, int checkedId ) { 
                execModeChanged( group, checkedId );
            }
        });

        mButtonStart = (Button) findViewById( R.id.Button_start );
        mButtonStart.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                execStart();
            }
        });

        mButtonStop = (Button) findViewById( R.id.Button_stop );
        mButtonStop.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                execStop();
            }
        });

        mButtonReset = (Button) findViewById( R.id.Button_reset );
        mButtonReset.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                execReset();
            }
        });

        ListView lv = (ListView) findViewById( R.id.ListView_report );
        mAdapter = new ArrayAdapter<String>( 
            this, R.layout.report, R.id.TextView_report );
        lv.setAdapter( mAdapter );

        setModeEcho();
    }

    /**
     * === onStart ===
     */
    @Override
    public void onStart() {
        super.onStart();
        bt_enableService();	
    }

    /**
     * === onResume ===
     */
    @Override
    public void onResume() {
        super.onResume();
        bt_startService();
    }

    /**
     * === onDestroy ===
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
        bt_stopService();
    }

    /**
     * === onCreateOptionsMenu ===
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        bt_execCreateOptionsMenuFull( menu );
        return true;
    }

    /**
     * === onOptionsItemSelected ===
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
         bt_execOptionsItemSelectedFull( item );
        return true;
    }

    /**
     * === onActivityResult ===
     */
    @Override
    public void onActivityResult( int request, int result, Intent data ) {
        bt_execActivityResult( request, result, data );
    }

// --- command ---
    /**
     * execModeChanged
     */
    private void execModeChanged( RadioGroup group, int checkedId ) { 
        RadioButton rb = (RadioButton) findViewById( checkedId );
        int id = rb.getId();
        if ( id == R.id.RadioButton_mode_echo ) {
            setModeEcho();
        } else if ( id == R.id.RadioButton_mode_tester ) {
            setModeTester();
        }
    }

    /**
     * setModeEcho
     */
    private void setModeEcho() { 
        mMode = MODE_ECHO;
        mButtonStart.setVisibility( View.INVISIBLE );
        mButtonStop.setVisibility( View.INVISIBLE );
        bt_hideButtonConnect();
    }

    /**
     * setModeTester
     */
    private void setModeTester() { 
        mMode = MODE_TESTER;
        mButtonStart.setVisibility( View.VISIBLE );
        mButtonStop.setVisibility( View.VISIBLE );
        if ( !bt_isServiceConnected() ) {
            bt_showButtonConnect();
        }
    }

    /**
     * execProfileChanged
     */
    private void execProfileChanged( RadioGroup group, int checkedId ) { 
        RadioButton rb = (RadioButton) findViewById( checkedId );
        int id = rb.getId();
        if ( id == R.id.RadioButton_profile_chat ) {
            bt_setServiceProfileChat();
        } else if ( id == R.id.RadioButton_profile_serial ) {
            bt_setServiceProfileSerial();
        }
    }

    /**
     * execStart
     */
    private void execStart() {
        isStart = true;
        mStartTime = SystemClock.elapsedRealtime();
        mCount = 0;
        mSequenceChecker.init();
        mAdapter.clear();
        startTimer();
    }

    /**
     * execStop
     */

    private void execStop() {
        stopTimer();
        if ( isTestetStart() ) {
            isStart = false;
            long time = SystemClock.elapsedRealtime() - mStartTime;
            String msg = "time " + time + LF;
            msg += mSequenceChecker.getResult();
            mAdapter.add( msg );
            bt_log_d( msg );
        }
    }

    /**
     * execReset
     */
    private void execReset() {
        mCount = 0;
        mSequenceChecker.init();
        mAdapter.clear();
    }

    /**
     * execRead (override)
     * @param byte[] bytes
     */
    protected void bt_execRead( byte[] bytes ) {
        if ( mMode == MODE_ECHO ) {
        	bt_send( bytes );
        }
    }

    /**
     * execRead (override)
     * @param List<String> list
     */
    protected void bt_execRead( List<String> list ) {
        if ( list.size() == 0 ) return;
        int num = 0;
        boolean flag = false;
        String mark = "";
        String msg = "";
        for ( String str: list ) {
            if ( str.length() == 0 ) continue;
            num = parseInt( str );
            flag = mSequenceChecker.check( num );
            if ( isTestetStart() || isEcho() ) {
                mark = " ";
                if ( !flag ) {
                    mark = "*";
                }
                msg = mark +  " " + str;
                if (D_NUM) mAdapter.add( msg );
                bt_log_d( msg );
            }
            if ( num > MAX_COUNT ) {
                execStop();
            }
        }
    }

    /**
     * isTestetStart
     */
    private boolean isTestetStart() {
        if (( mMode == MODE_TESTER ) && isStart ) return true;
        return false; 
    }

    /**
     * isEcho
     */
    private boolean isEcho() {
        if ( mMode == MODE_ECHO ) return true;
        return false; 
    }

    /**
     * parseInt
     * @param String str
     * @return int
     */
    private int parseInt( String str ) {
        int n = 0;
        try {
            n = Integer.parseInt( str );
        } catch ( Exception e ) {
           if (D) e.printStackTrace();
        }
        return n;
    } 

    /**
     * startTimer
     */
    private void startTimer() {
        if ( mMode != MODE_TESTER ) return;
        isTimerStart = true;
        updateTimerRunning();
    }

    /**
     * stopTimer
     */	
    private void stopTimer() {
        if ( mMode != MODE_TESTER ) return;
        isTimerStart = false;
        updateTimerRunning();
    }

    /**
     * updateTimerRunning
     */	
    private void updateTimerRunning() {
        boolean running = isTimerStart;
        if (running != isTimerRunning) {
            if (running) {
                updateSend();
                timerHandler.sendMessageDelayed(
                    Message.obtain(timerHandler, TIMER_WHAT), TIMER_DELAY );               
             } else {
                timerHandler.removeMessages(TIMER_WHAT);
            }
            isTimerRunning = running;
        }
    }

    /**
     * --- timerHandler ---
     */	    
    private Handler timerHandler = new Handler() {
        public void handleMessage(Message m) {
            if (isTimerRunning) {
                updateSend();
                sendMessageDelayed(
                    Message.obtain(this, TIMER_WHAT), TIMER_DELAY);
            }
        }
    };

    /**
     * updateSend
     */	
    private synchronized void updateSend() {
        bt_sendLF( Integer.toString( mCount )  );
        mCount ++;
    }

}
