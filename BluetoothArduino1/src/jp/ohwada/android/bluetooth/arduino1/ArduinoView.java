/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.arduino1;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * ArduinoView
 */
public class ArduinoView {

    // char
    private static final String LF = "\n";

    // PWM value
    private static final int PWM_MAX = 255;
    private static final int PWM_PROGRESS = 128;
    // PWM interval time 0.1 sec
     private static final int PWM_TIME = 100;	

    // callback
    private OnChangedListener mOnListener;

    // class object
    private Context mContext;
    private View mContentView;

    // UI
    private GraphView mGraphView;
    private TextView mTextViewSwitch;
    private Button mButtonLed;
    private ListView mListViewReport;
    private ArrayAdapter<String> mAdapter;

    // message
    private String mLabelOn = "On";
    private String mLabelOff = "Off";

    // PWM value format
    private DecimalFormat mFormatPwm = new DecimalFormat( "000" );
    // Time when send PWM
    private long mTimePwm = 0; 

    // LED Status
    private boolean isLed = false;

    /**
     * interface OnChangedListener
     */
    public interface OnChangedListener {
        void onSend( String str );
    }

    /**
     * === Constractor ===
     * @param Context context
     */
    public ArduinoView( Context context ) {
        mContext = context;
    }

    /**
     * setOnClickListener
     * @param OnButtonsClickListener listener
     */
    public void setOnChangedListener( OnChangedListener listener ) {
        mOnListener = listener;
    }

    /**
     * initView
     * @param View view 
     */
    public void initView( View view ) {
        mContentView = view;
    }

    /**
     * initLabel
     * @param int int id_on
     * @param int int id_off
     */
    public void initLabel( int id_on, int id_off ) {
        mLabelOn = getString( id_on );
        mLabelOff = getString( id_off );
    }

    /**
     * initTextViewSwitch
     * @param int res_id
     */
    public void initTextViewSwitch( int res_id ) {
        mTextViewSwitch = (TextView) mContentView.findViewById( res_id );
        mTextViewSwitch.setText( mLabelOn );
    }

    /**
     * initGraphView
     * @param int res_id
     */
    public void initGraphView( int res_id ) {
        mGraphView = (GraphView) mContentView.findViewById( res_id );
    }

    /**
     * initButtonLed
     * @param int res_id
     */
    public void initButtonLed( int res_id ) {					
        mButtonLed = (Button) mContentView.findViewById( res_id );
        mButtonLed.setText( mLabelOff );
        mButtonLed.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                execLed();
            }
        });
    }

    /**
     * initSeekBarPwm
     * @param int res_id
     */
    public void initSeekBarPwm( int res_id ) {		
        SeekBar sbPwm = (SeekBar) mContentView.findViewById( res_id );
        sbPwm.setMax( PWM_MAX );
        sbPwm.setProgress( PWM_PROGRESS );
        sbPwm.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch( SeekBar seekBar ) {
                // dummy
            }
            @Override
            public void onProgressChanged( SeekBar seekBar, int progress, boolean fromTouch ) {
                execPwm( progress );
            }
            @Override
            public void onStopTrackingTouch( SeekBar seekBar ) {
                // dummy
            }
        });
    }

    /**
     * initAdapter
     * @param int res_layout
     * @param int res_id
     */
    public void initAdapter( int res_layout, int res_id ) {
        mAdapter = new ArrayAdapter<String>( 
            mContext, res_layout, res_id );
    }

    /**
     * initListViewReport
     * @param int res_id
     */
    public void initListViewReport( int res_id ) {
        mListViewReport = (ListView) mContentView.findViewById( res_id );
        mListViewReport.setAdapter( mAdapter );
    }

// --- comand ---
    /**
     * showGraph
     */
    public void showGraph() {
        mGraphView.setVisibility( View.VISIBLE );
        mListViewReport.setVisibility( View.GONE );
    }

    /**
     * showLog
     */
    public void showLog() {
        mGraphView.setVisibility( View.GONE );
        mListViewReport.setVisibility( View.VISIBLE );
    }

    /**
     * execLed
     */
    private void execLed() {
        if ( isLed ) {
            mButtonLed.setText( mLabelOn );
            mButtonLed.setTextColor( Color.RED );
            notifySendLF( "L1" );
        } else {
            mButtonLed.setText( mLabelOff );
            mButtonLed.setTextColor( Color.BLACK );
            notifySendLF( "L0" );
        }
        isLed = !isLed;
    }

    /**
     * execPwm
     * @param int progress
     */
    private void execPwm( int progress ) {
        long time = SystemClock.elapsedRealtime();
        // send command, when 0.1 sec or more has passed since the last sending. 
        if ( time > mTimePwm + PWM_TIME ) {
            mTimePwm = time;
            notifySendLF( "P" + mFormatPwm.format( progress ) );
        }
    }

    /**
     * execRead
     * @param List<String> list
     */
    public void execRead( List<String> list ) {
        if ( list.size() == 0 ) return;
        for ( String str: list ) {
            execRecvMsg( str );
        }
    }

    /**
     * execRecvMsg
     * @param String str
     */
    private void execRecvMsg( String str ) {
        if ( str == null ) return;
        if ( str.startsWith( "B0" )) {
            mTextViewSwitch.setText( mLabelOff );	
            mTextViewSwitch.setTextColor( Color.BLACK );
        } else if ( str.startsWith( "B1" )) {
            mTextViewSwitch.setText( mLabelOff );	
            mTextViewSwitch.setTextColor( Color.RED );
        } else if ( str.startsWith( "A" )) {
            int value = parseInt( str.substring( 1 ) );
            mGraphView.setData( value );
        }
    }

    /**
     * getString From Resources
     * @param int id
     * @param String  
     */	
    private String getString( int id ) {	
        return mContext.getResources().getString( id );
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
           e.printStackTrace();
        }
        return n;
    }

    /**
     * notifySend
     * @param String str
     */
    private void notifySendLF( String str ) {
        if ( mOnListener != null ) {
            mOnListener.onSend( str + LF );
        }
    }    
}
