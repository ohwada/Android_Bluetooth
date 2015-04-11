/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.chatsample1;

import jp.ohwada.android.bluetooth.lib.BtActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends BtActivity {

    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /** 
     * onCreate
     */
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        log_e( "+++ ON CREATE +++" );

        // Set up the window layout
        setContentView( R.layout.main );
		
        /* bluetooth */
        bt_init();
        bt_initManager();
        bt_initDeviceListClass( DeviceListActivity.class );
        bt_setServiceProfileChat();
        bt_setServiceHandlerWrite( true );
        bt_setUsePrefAddress( false );
        bt_setUseListString( false );

        // Get local Bluetooth adapter
        // If the adapter is null, then Bluetooth is not supported
        bt_initAdapterAndFinish(); 
    }

    /** 
     * onStart
     */
    @Override
    public void onStart() {
        super.onStart();
        log_e( "++ ON START ++" );
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        boolean ret = bt_enableService();
        if ( ret ) {
            setupChat();
        }
    }

    /** 
     * onResume
     */
    @Override
    public synchronized void onResume() {
        super.onResume();
        log_e( "+ ON RESUME +" );
        bt_startService();
    }

    /** 
     * onPause
     */
    @Override
    public synchronized void onPause() {
        super.onPause();
        log_e( "- ON PAUSE -" );
    }

    /** 
     * onStop
     */
    @Override
    public void onStop() {
        super.onStop();
        log_e( "-- ON STOP --" );
    }

    /** 
     * onDestroy
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        log_e( "--- ON DESTROY ---" );
        // Stop the Bluetooth chat services
        bt_stopService();
    }

    /** 
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        log_d( "setupChat()" );

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                log_d("onClick");
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage( String message ) {
        log_d( "sendMessage " + message );
        if (message.length() > 0) {
            bt_send( message );
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            log_i( "END onEditorAction" );
            return true;
        }
    };

    /** 
     * onActivityResult
     */
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        boolean ret = bt_execActivityResultAndFinish( requestCode, resultCode, data );
        if (ret) {
            setupChat();
        }
    }

    /** 
     * onCreateOptionsMenu
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    /** 
     * onOptionsItemSelected
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        return bt_execOptionsItemSelected( item );
    }

    /** 
     * execWrite (override)
     * @param byte[] bytes
     */
    protected void bt_execWrite( byte[] bytes ) {
        String writeMessage = new String( bytes );
        mConversationArrayAdapter.add( "Me:  " + writeMessage );
    }

    /**
     * execRead (override)
     * @param byte[] bytes
     */
    protected void bt_execRead( byte[] bytes ) {
        String readMessage = new String( bytes );
        mConversationArrayAdapter.add( bt_getDeviceName() + ":  " + readMessage );
    }

    /** 
     * log DEBUG
     */
    private void log_d( String msg ) {
        if(D) Log.d(TAG, msg);
    }

    /** 
     * log ERROR
     */
    private void log_e( String msg ) {
        if(D) Log.e(TAG, msg);
    }

    /** 
     * log INFO
     */
    private void log_i( String msg ) {
        if(D) Log.i(TAG, msg);
    }

}
