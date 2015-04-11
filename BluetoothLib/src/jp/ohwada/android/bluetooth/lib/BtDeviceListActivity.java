/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * DeviceListActivity
 *
 * base on DeviceListActivity
 * https://android.googlesource.com/platform/development/+/master/samples/BluetoothChat/
 */
public class BtDeviceListActivity extends Activity{
    // Debugging
    private boolean isDebug = BtConstant.DEBUG_LOG_LIST;
    private static final String TAG_SUB = "DeviceList";	

    // Return Intent extra
    public static final String EXTRA_DEVICE_ADDRESS =  "bt_device_address";

    // Get the device MAC address, which is the last 17 chars in the View
    private static final int MAC_ADDR_OFFSET = 17;

    // Member fields
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
   
    // UI 
    private TextView mTextViewPaired;
    private TextView mTextViewNew;

// --- onCreate ---
    /**
     * === onCreate ===
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        execCreate();
    }

    /**
     * --- execCreate ---
     */
    public void execCreate() {
        // Setup the window
        requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
        setContentView( R.layout.bt_activity_device_list );

        // Set result CANCELED incase the user backs out
        setResult( Activity.RESULT_CANCELED );

        mTextViewPaired = (TextView) findViewById( R.id.bt_TextView_list_title_paired_devices );
        mTextViewNew = (TextView) findViewById( R.id.bt_TextView_list_title_new_devices );

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>( this, R.layout.bt_device_name );
        mNewDevicesArrayAdapter = new ArrayAdapter<String>( this, R.layout.bt_device_name );

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById( R.id.bt_ListView_list_paired_devices );
        pairedListView.setAdapter( mPairedDevicesArrayAdapter );
        pairedListView.setOnItemClickListener( mDeviceClickListener );

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById( R.id.bt_ListView_list_new_devices );
        newDevicesListView.setAdapter( mNewDevicesArrayAdapter );
        newDevicesListView.setOnItemClickListener( mDeviceClickListener );

        // Initialize the button to perform device discovery
        Button scanButton = (Button) findViewById( R.id.bt_Button_list_scan );
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick( View view ) {
                doDiscovery();
                view.setVisibility( View.GONE );
            }
        });

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            mTextViewPaired.setVisibility( View.VISIBLE );
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            mPairedDevicesArrayAdapter.add( 
                getString( R.string.bt_list_no_paired ) );
        }
    }

    /**
     * === onDestroy ===
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        execDestroy();
    }

    /**
     * execDestroy
     */
    public void execDestroy() {
        // Make sure we're not doing discovery anymore
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // Unregister broadcast listeners
        unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility( true );
        setTitle( R.string.bt_list_scanning );

        // Turn on sub-title for new devices
        mTextViewNew.setVisibility( View.VISIBLE );

        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBluetoothAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            int length = info.length();
            int resultCode = Activity.RESULT_CANCELED;
            String address = "";
            if ( length >= MAC_ADDR_OFFSET ) {
                resultCode = Activity.RESULT_OK;
                address = info.substring( length - MAC_ADDR_OFFSET );
                log_debug( "address; "  + address );
            } else {
                log_debug( "not get address" );
            }
            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra( EXTRA_DEVICE_ADDRESS, address );
            // Set result and finish this Activity
            setResult( resultCode, intent ) ;
            finish();
        }
    };

    /**
     * The BroadcastReceiver that listens for discovered devices and
     *changes the title when discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle( R.string.bt_list_select_device );
                if ( mNewDevicesArrayAdapter.getCount() == 0 ) {
                    mNewDevicesArrayAdapter.add( 
                        getString( R.string.bt_list_not_found ) );
                }
            }
        }
    };

// --- debug log ---
    /**
     * initDebug
     * @param boolean flag
     */	
    public void initDebug( boolean flag ) {
        isDebug = flag;
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
