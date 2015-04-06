/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.os.SystemClock;
import android.util.Log;

/**
 *  String Utility
 */
public class BtStringUtility {
    /** Debug */
    private boolean isDebug = BtConstant.DEBUG_LOG_STRING;
    private boolean isDebugRead = BtConstant.DEBUG_LOG_STRING_READ;
    private static final boolean D = BtConstant.DEBUG_LOG_EXCEPTION;
    private static final String TAG_SUB = "StringUtility";

    // char
    private static final byte LF = 0x0a;
    private static final String CHARSET_NAME = "UTF-8";
    
    // param
    private int mInterval = 100; 
    private int mMaxSize = 256;

    // recv
    private String mPrevBuf = "";
    private long mPrevTime = 0;

    /**
     * === constructor ===
     */	
    public BtStringUtility() {
        mPrevBuf = "";
        mPrevTime = System.currentTimeMillis();
    } 

    /**
     * setInterval
     */	
    public void setInterval( int n ) {
        mInterval = n;
    }

    /**
     * setMaxSize
     */
    public void setMaxSize( int n ) {
        mMaxSize = n;
    }

    /**
     * getListString
     * @param byte[] buffer
     */
    public List<String> getListString( byte[] bytes ) {
        List<String> list = new ArrayList<String>();
        if ( bytes == null ) return list;
        int length = bytes.length;
        if ( length == 0 ) return list;
        if ( isDebugRead ) {
            log_bytes( bytes );
        }
        int offset = 0;
        String str = "";
        String buf = "";
        boolean is_prev = true;
        long time = SystemClock.elapsedRealtime() - mPrevTime;
        if ( time > mInterval ) {
            if ( mPrevBuf.length() > 0 ) {
                list.add( mPrevBuf );
                log_debug( "too long interval " + time );
            }
            is_prev = false;
            mPrevBuf = "";            
        }
        // search LF
        for ( int i=0; i<length; i++ ) {
            if ( bytes[i] == LF ) {
                // 
                if ( i == offset ) {
                    // set empty, if no valid byte
                    str = "";
                } else {
                    // convert string
                    str = bytesToString( bytes, offset, i - offset );
                }
                // if remain the prevous string, add this.
                if ( is_prev == true ) {
                    str = mPrevBuf + str;
                    is_prev = false;
                    mPrevBuf = "";
                }
                // set in list
                list.add( str );
                // set next offset
                offset = i + 1;
            }
        }
        if ( length == offset ) {
            // set empty, if search at end
            buf = "";
        } else {
            // convert string
            buf = bytesToString( bytes, offset, length - offset );
        }
        if ( is_prev == true ) {
            // if remain the prevous string, add this.
            mPrevBuf += buf;
        } else {
            // set new buf
            mPrevBuf = buf;
        }
        // clear recv buffer, if buffer size is over limit
        if ( mPrevBuf.length() > mMaxSize ) {
            mPrevBuf = "";
            log_debug( "buffer overflow" );
        }
        mPrevTime = SystemClock.elapsedRealtime();
        return list;
    }

    /**
     * bytesToString
     */
    private String bytesToString( byte[] bytes, int offset, int count ) {
        String str= "";
            try {
                str = new String( bytes, offset, count, CHARSET_NAME );
                str = str.trim();
            } catch ( UnsupportedEncodingException e) {
                if (D) e.printStackTrace();
            }
    	return str;
    }

    /**
     * getBuffer
     */
    public String getBuffer() {
        return mPrevBuf ;  
    }

    /**
     * clearBuffer
     */
    public void clearBuffer() {
        mPrevBuf = "" ;
    }

// --- debug log ---
    /**
     * setDebug
     * @param boolean flag
     */	
    public void setDebug( boolean flag ) {
        isDebug = flag;
    }

    /**
     * setDebug
     * @param boolean flag
     */	
    public void setDebugRead( boolean flag ) {
        isDebugRead = flag;
    }

    /**
     * log_bytes
     * @param String str
     * @param byte[] bytes
     */
    private void log_bytes( byte[] bytes ) {
        String msg = "";
        for ( int i=0; i<bytes.length ; i++ ) {
            msg += String.format( "%02X", bytes[ i ] );
            msg += " ";
        }
        log_d( msg );
    }

    /**
     * write log
     * @param String msg
     */ 
    private void log_debug( String msg ) {
        if (isDebug) log_d( msg ); 
    } 
	    					
    /**
     * write log
     * @param String msg
     */ 
    private void log_d( String msg ) {
        Log.d( 
            BtConstant.TAG, 
            TAG_SUB + BtConstant.LOG_COLON + msg );
    }    
}
