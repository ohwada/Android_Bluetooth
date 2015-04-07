/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * TextViewDebug
 */
public class BtTextViewDebug {

    /* debug */
    private static final String DEBUG_GLUE = " ";
    
    /* view component */
    private ScrollView mScrollViewDebug; 
    private TextView mTextViewDebug;

    /* List for message */
    private List<String> mListMsg  = new ArrayList<String>();

    /* local variable */
    private int mMaxMsg = 100;	

    /*
     * === Constractor ===
     * @param Activity activity
     */
    public BtTextViewDebug() {
        // dummy
    }

   /**
     * init TextView Debug when onCreate
     * @param View view
     * @param int id
     */	
    public void initScrollViewDebug( View view, int id ) {
        mScrollViewDebug = (ScrollView) view.findViewById( id );
    }

    /**
     * init TextView Debug when onCreate
     * @param View view
     * @param int id
     */	
    public void initTextViewDebug( View view, int id ) {
        mTextViewDebug = (TextView) view.findViewById( id );
    }

    /**
     * setTextViewDebugVisibility
     */
    public void setVisibility( int visibility ) {
        if ( mScrollViewDebug != null ) { 
            mScrollViewDebug.setVisibility( visibility );		 
        }
        if ( mTextViewDebug != null ) { 
            mTextViewDebug.setVisibility( visibility );		 
        }
    }

    /**
     * setMaxMsg
     * @param int max
     */	
    public void setMaxMsg( int max ) {
        mMaxMsg = max;
    }

   /**
     * showMessage
     * @param byte[] bytes
     */
    public void showMessage( String str, byte[] bytes ) {
        if ( bytes == null ) return;
        if ( bytes.length == 0 ) return;
        String msg = buildMsgBytes(  str, bytes );
        addAndShowText( msg );
    }

    /**
     * buildMsgBytes
     * @param String str
     * @param byte[] bytes
     * @return String 
     */	
    private String buildMsgBytes( String str, byte[] bytes ) {
        String msg = str + " ";
        for ( int i=0; i<bytes.length ; i++ ) {
            msg += String.format( "%02X", bytes[ i ] );
            msg += " ";
        }
        return msg;
    }
	
    /**
     * show dubug msg
     * @param String str
     */	
    private void addAndShowText( String str ) {
        addList( str );
        String msg = buildList( mMaxMsg, DEBUG_GLUE );
        setText( msg );
    }

    /**
     * show dubug msg
     * @param String str
     */				
    private void setText( String str ) {
        if ( mTextViewDebug != null ) {
            mTextViewDebug.setText( str );
        }	
    }

// message builder
    /**
     * add
     * @param String msg
     */				
    private void addList( String msg ) {
        mListMsg.add( msg );
    }

    /**
     * build the messages
     * @param max  : max of showing messages
     * @param glue : glue of messages
     * @return String
     */
    private String buildList( int max, String glue ) {
        return buildList( mListMsg, max, glue );
    }
	
    /**
     * build the messages
     * @param list : message list
     * @param max  : max of showing messages
     * @param glue : glue of messages
     * @return String
     */
    private String buildList( List<String> list, int max, String glue ) {
        // set 'start' and 'end' , showing only 'max' messages
        int start = 0;
        int end = list.size();
        if ( end > max ) {
            start = end - max;
        }
        /* combine the messages */
        String msg = "";
        for ( int i = start; i < end; i++ ) {
            msg += list.get( i );
            msg += glue;
        }
        return msg;
    }	

}
