/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.terminal1;

import java.util.List;

import jp.ohwada.android.bluetooth.lib.BtToastMaster;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * TerminalView
 */
public class TerminalView {

    // callback
    private OnChangedListener mOnListener;

    // class object 
    private Context mContext;
    private View mContentView;

    // UI
    private EditText mEditTextSend;
    private ListView mListViewReport;
    private ArrayAdapter<String> mAdapter;

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
        Button btnSend = (Button) mContentView.findViewById( R.id.Button_send );
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
     * execSend
     */
    private void execSend() {
        String str = mEditTextSend.getText().toString();
        if ( str.length() > 0 ) {
            notifySend( str );
        } else {
            toast_short( R.string.msg_enter_text );
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
     * @param List<String> list
     */
    public void execRead( List<String> list ) {
        if ( list.size() == 0 ) return;
        for ( String str: list ) {
            mAdapter.add( str );
        }
    }

    /**
     * show toast
     * @param int res_id
     */				
    private void toast_short( int res_id ) {
    	BtToastMaster.showShort( mContext, res_id );
    }
}
