/**
 * 2015-03-01 K.OHWADA
 */ 

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.ohwada.android.bluetooth.chatsample2;

import jp.ohwada.android.bluetooth.lib.BtV4Fragment;

import android.app.Activity;
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

        // Get local Bluetooth adapter
        boolean ret = bt_initAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if ( !ret ) {
            bt_toast_long( R.string.bt_toast_not_available );
            getActivity().finish();
        }
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
        log_d( "onActivityResult " + resultCode );
        switch (requestCode) {
            case REQUEST_DEVICE_LIST_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    bt_execActivityResultDeviceListSecure( data );
                }
                break;
            case REQUEST_DEVICE_LIST_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    bt_execActivityResultDeviceListInsecure( data );
                }
                break;
            case REQUEST_ADAPTER_ENABLE:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                	bt_execActivityResultAdapterEnable( data );
                    setupChat();    // ****
                } else {
                    // User did not enable Bluetooth or an error occurred
                    log_d( "BT not enabled" );
                    bt_toast_short( R.string.bt_not_enabled_leaving );
                    getActivity().finish();
                }
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
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                bt_execMenuConnectSecure();
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                bt_execMenuConnectInsecure();
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                bt_execMenuDiscoverable();
                return true;
            }
        }
        return false;
    }

    /** 
     * log DEBUG
     */
    private void log_d( String msg ) {
        if(D) Log.d(TAG, msg);
    }
}
