/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.chatsample2;

import jp.ohwada.android.bluetooth.lib.BtV4Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.common.logger.Log;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends BtV4Fragment {

    // Debugging
    private static final String TAG = "BluetoothChatFragment";
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

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
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        boolean ret = bt_enableService();
        if (ret) {
            setupChat();
        }
    }

    /**
     * onResume
     */
    @Override
    public void onResume() {
        super.onResume();
        bt_startService();
    }

    /**
     * onDestroy
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        bt_stopService();
    }

    /**
     * onCreateView
     */
    @Override
    public View onCreateView( 
        LayoutInflater inflater, 
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState ) {
        return inflater.inflate( R.layout.fragment_bluetooth_chat, container, false );
    }

    /**
     * onViewCreated
     */
    @Override
    public void onViewCreated(
        View view, 
        @Nullable Bundle savedInstanceState ) {
        mConversationView = (ListView) view.findViewById(R.id.in);
        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        mSendButton = (Button) view.findViewById(R.id.button_send);
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        log_d( "setupChat()" );

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message);
                }
            }
        });

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }


    /**
     * Sends a message.
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        if (message.length() > 0) {
            bt_send( message );
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

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
     * onActivityResult
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean ret = bt_execActivityResultAndFinish( requestCode, resultCode, data );
        if (ret) {
            setupChat();
        }
    }

    /**
     * onCreateOptionsMenu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    /**
     * onOptionsItemSelected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return bt_execOptionsItemSelected( item );
    }

    /** 
     * log DEBUG
     */
    private void log_d( String msg ) {
        if(D) Log.d(TAG, msg);
    }
}
