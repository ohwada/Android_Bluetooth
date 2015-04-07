/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.terminal1;

import java.util.ArrayList;
import java.util.List;

import jp.ohwada.android.bluetooth.lib.BtToastMaster;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * TerminalView
 */
public class TerminalView {

    // send newline mode
    private static final String SEND_NEWLINE_CR = "cr";
    private static final String SEND_NEWLINE_LF = "lf";
    private static final String SEND_NEWLINE_CR_LF = "crlf";

    // Preference key
    private static final String PREF_DISPLAY_SEND = "display_send";
    private static final String PREF_DISPLAY_HEX = "display_hex";
    private static final String PREF_RECV_NEWLINE = "recv_newline";
    private static final String PREF_SEND_NEWLINE = "send_newline";

    // Preference default value
    private static final boolean DEFAULT_DISPLAY_SEND = true;
    private static final boolean DEFAULT_DISPLAY_HEX = true;
    private static final boolean DEFAULT_RECV_NEWLINE = true;
    private static final String DEFAULT_SEND_NEWLINE = SEND_NEWLINE_LF;

    // char	
    private static final String CR = "\r";
    private static final String LF = "\n";

    // callback
    private OnChangedListener mOnListener;

    // class object 
    private Context mContext;
    private View mContentView;
    private SharedPreferences mPreferences;

    // UI
    private EditText mEditTextSend;
    private ListView mListViewReport;
    private TextAdapter mAdapter;

    private List<Text> mListText = new ArrayList<Text>();

    // param
    private boolean isDisplaySend = false;
    private boolean isRecvNewline = false;
    private String mSendNewline = "";

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
    public TerminalView( Context context ) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences( context );
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
     * initEditTextSend
     * @param int res_id
     */
    public void initEditTextSend( int res_id ) {
        mEditTextSend = (EditText) mContentView.findViewById( res_id );
    }

    /**
     * initButtonSend
     * @param int res_id
     */
    public void initButtonSend( int res_id ) {
        Button btnSend = (Button) mContentView.findViewById( res_id );
        btnSend.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                execSend();
            }
        });
    }

    /**
     * initAdapter
     * @param int res_layout
     * @param int res_id
     */
    public void initAdapter( int res_layout, int res_id ) {
        mAdapter = new TextAdapter( 
            mContext, res_layout, res_id, mListText );
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
     * cleanTerminal
     */
    public void cleanTerminal() {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * execSend
     */
    private void execSend() {
        String str = mEditTextSend.getText().toString();
        if ( str.length() > 0 ) {
            if ( isDisplaySend ) {
                mAdapter.add( TextAdapter.SEND, str );
                mAdapter.notifyDataSetChanged();
            }
            notifySend( str + mSendNewline );
        } else {
            toast_short( R.string.toast_enter_text );
        }
    }

    /**
     * notifySend
     * @param String str
     */
    private void notifySend( String str ) {
        if ( mOnListener != null ) {
            mOnListener.onSend( str );
        }
    }

    /**
     * execRead
     * @param byte[] bytes
     */
    public void execRead( byte[] bytes ) {
        if ( isRecvNewline ) return;
        mAdapter.add( TextAdapter.RECV, bytes );
        mAdapter.notifyDataSetChanged();
    }

    /**
     * execRead
     * @param List<String> list
     */
    public void execRead( List<String> list ) {
        if ( !isRecvNewline ) return;
        if ( list.size() == 0 ) return;
        for ( String str: list ) {
            mAdapter.add( TextAdapter.RECV, str );
        }
        mAdapter.notifyDataSetChanged();
    }

// pref
    /**
     * refreshPref
     */ 
    public void refreshPref() {
        isDisplaySend = isPrefDisplaySend();
        isRecvNewline = isPrefRecvNewline();
        mAdapter.setDisplayHex( isPrefDisplayHex() );
        String code = getPrefSendNewline();
        mSendNewline = "";
        if ( SEND_NEWLINE_CR.equals( code ) ) {
            mSendNewline = CR;
        } else  if ( SEND_NEWLINE_LF.equals( code ) ) {
            mSendNewline = LF;
        } else  if ( SEND_NEWLINE_CR_LF.equals( code ) ) {
            mSendNewline = CR + LF;
        }
    }

    /**
     * isPrefDisplaySend
     * @return boolean
     */ 
    private boolean isPrefDisplaySend() {
        return mPreferences.getBoolean( 
            PREF_DISPLAY_SEND, DEFAULT_DISPLAY_SEND );
    }

    /**
     * isPrefDisplayHex
     * @return boolean
     */ 
    private boolean isPrefDisplayHex() {
        return mPreferences.getBoolean( 
            PREF_DISPLAY_HEX, DEFAULT_DISPLAY_HEX );
    }

   /**
     * isRecvNewline
     * @return boolean
     */ 
    private boolean isPrefRecvNewline() {
        return mPreferences.getBoolean( 
            PREF_RECV_NEWLINE, DEFAULT_RECV_NEWLINE );
    }

   /**
     * getPrefSendNewline
     * @return String
     */
    private String getPrefSendNewline() {
        return mPreferences.getString( 
            PREF_SEND_NEWLINE, DEFAULT_SEND_NEWLINE );
    }

// toast
    /**
     * show toast
     * @param int res_id
     */				
    private void toast_short( int res_id ) {
    	BtToastMaster.showShort( mContext, res_id );
    }
}
