/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Bluetooth Service Manager
 */
public class BtManager {

    /** Debug */
    private static final boolean D = BtConstant.DEBUG_LOG_EXCEPTION;
    private boolean isDebugManager = BtConstant.DEBUG_LOG_MANAGER;
    private boolean isDebugWriteBefore = BtConstant.DEBUG_LOG_MANAGER_WRITE_BEFORE;
    private boolean isDebugWriteAfter = BtConstant.DEBUG_LOG_MANAGER_WRITE_AFTER;
    private boolean isDebugRead = BtConstant.DEBUG_LOG_MANAGER_READ;
    private boolean isDebugService = BtConstant.DEBUG_LOG_SERVICE;
    private boolean isDebugServiceWrite = BtConstant.DEBUG_LOG_SERVICE_WRITE;
    private boolean isDebugServiceRead = BtConstant.DEBUG_LOG_SERVICE_READ;
    private boolean isDebugEmulator = BtConstant.DEBUG_EMULATOR; 
    private boolean isTextViewDebugSend = BtConstant.DEBUG_TEXT_SEND;
    private boolean isTextViewDebugRecv = BtConstant.DEBUG_TEXT_RECV;
    private static final String TAG_SUB = "BtManager";

    // mode
    private boolean isServiceHandlerWrite = BtConstant.SERVICE_HANDLER_WRITE;

    /* Message types sent from the BtService Handler */
    private static final int WHAT_READ = BtService.WHAT_READ;
    private static final int WHAT_WRITE = BtService.WHAT_WRITE;
    private static final int WHAT_STATE_CHANGE = BtService.WHAT_STATE_CHANGE;
    private static final int WHAT_DEVICE_NAME = BtService.WHAT_DEVICE_NAME;
    private static final int WHAT_FAILED = BtService.WHAT_FAILED;
    private static final int WHAT_LOST = BtService.WHAT_LOST;

    /* Constants that indicate the current connection state */
    private static final int STATE_NONE = BtService.STATE_NONE;
    private static final int STATE_LISTEN = BtService.STATE_LISTEN;
    private static final int STATE_CONNECTING = BtService.STATE_CONNECTING;
    private static final int STATE_CONNECTED = BtService.STATE_CONNECTED;

    // return code of enableAdapter
    public static final int RET_ENABLE_NONE = 0;
    public static final int RET_ENABLE_START = 1;
    public static final int RET_ENABLE_SETUP = 2;

    // return code of execActivityResult
    public static final int RET_RESULT_UNMATCH = 0;
    public static final int RET_RESULT_MATCH = 1;
    public static final int RET_RESULT_ENABEL_FAIL = 2;

    /* Return Intent extra */
    private static final String EXTRA_DEVICE_ADDRESS = BtDeviceListActivity.EXTRA_DEVICE_ADDRESS;
 
    /* Key names received from the Bluetooth Service Handler */
    private static final String BUNDLE_DEVICE_NAME = BtService.BUNDLE_DEVICE_NAME;

    // Unique UUID of Bluetooth Service
    private static final String SERVICE_UUID_CHAT_SECURE = BtConstant.SERVICE_UUID_CHAT_SECURE;
    private static final String SERVICE_UUID_CHAT_INSECURE = BtConstant.SERVICE_UUID_CHAT_INSECURE;
    private static final String SERVICE_UUID_SPP = BtConstant.SERVICE_UUID_SPP;
    private String mServiceUuidSecure = SERVICE_UUID_SPP;
    private String mServiceUuidInsecure = SERVICE_UUID_SPP;

    // Receive buffer of Bluetooth Service
    private int mServiceRecvBufferPlane = BtConstant.SERVICE_RECV_BUFFER_PLANE;
    private int mServiceRecvBufferByte = BtConstant.SERVICE_RECV_BUFFER_BYTE;

    // request code
    private int mRequestEnable = BtConstant.REQUEST_ADAPTER_ENABLE;
    private int mRequestDiscoverable = BtConstant.REQUEST_ADAPTER_DISCOVERABLE; 
    private int mRequestDeviceListSecure = BtConstant.REQUEST_DEVICE_LIST_SECURE;
    private int mRequestDeviceListInsecure = BtConstant.REQUEST_DEVICE_LIST_INSECURE;
    private int mRequestSettings = BtConstant.REQUEST_SETTINGS;

    /* SharedPreferences */
    private boolean isUsePrefAddr = true;
    private String mPrefDeviceName = BtConstant.PREF_DEVICE_NAME; 
    private String mPrefDeviceAddr = BtConstant.PREF_DEVICE_ADDR;
    private String mPrefUseAddr = BtConstant.PREF_USE_ADDR;
    private String mPrefShowDebug = BtConstant.PREF_SHOW_DEBUG;
    private static final String DEFAULT_DEVICE_NAME = "";
    private static final String DEFAULT_DEVICE_ADDR = "";
    private static final boolean DEFAULT_USE_ADDR = true;
    private static final boolean DEFAULT_SHOW_DEBUG = false;

    // connect mode
    private static final boolean MODE_SECURE = BtService.MODE_SECURE;
    private static final boolean MODE_INSECURE = BtService.MODE_INSECURE;

    // discoverable time
    private static final int DISCOVERABLE_DURATION = 300;

    // char	
    private static final String LF = "\n";

    // menu
    private int mMenuConnectSecure = R.id.bt_menu_connect_secure;
    private int mMenuConnectInsecure = R.id.bt_menu_connect_insecure;
    private int mMenuDiscoverable = R.id.bt_menu_discoverable;
    private int mMenuDisconnect = R.id.bt_menu_disconnect;
    private int mMenuClear = R.id.bt_menu_clear;
    private int mMenuSettings = R.id.bt_menu_settings;
    
    /* Title bar */
    private boolean isTitleUse = true;
    private String mTitleConnecting = "Connecting...";
    private String mTitleConnected = "connected to %1$s";
    private String mTitleNotConnected = "Not connected";

    /* Toast */
    private boolean isToastUse = true;
    private String mToastNotAvailable = "Bluetooth is not available";
    private String mToastNotEnabled = "Bluetooth was not enabled";
    private String mToastFailed = "Unable to connect device";
    private String mToastLost = "Device connection was lost";
    private String mToastConnected = "Connected to ";
    private String mToastNotConnected = "You are not connected to a device";
    private String mToastNoAciton = "No Action in debug";

    // button
    private boolean isShowButton  = true;
     
    // callback
    private OnChangedListener mOnListener;
    
    /* Singlton: Local Bluetooth adapter */
    private static BluetoothAdapter mBluetoothAdapter = null;

    /* Singlton: Member object for the chat services */
    private static BtService mBtService = null;

    /* class object */ 
    private Context mContext;
    private SharedPreferences mPreferences;
    private BtStringUtility mStringUtility;

    // UI
    private LinearLayout mLinearLayoutConnect;
    private TextView mTextViewConnect;
    private Button mButtonConnectSecure;
    private Button mButtonConnectInsecure;
    private BtTextViewDebug mTextViewDebug;

    // message handler 
    private Handler mHandler;
    
    // activity class
    private Class<?> mDeviceListClass;
    private Class<?> mSettingsClass;

    // bluetooth param 
    private String mDeviceName = null;

    // ListString
    private boolean isUseListString = true;

    /**
     * interface OnChangedListener
     */
    public interface OnChangedListener {
        void onRead( byte[] bytes );
        void onRead( List<String> list );
        void onWrite( byte[] bytes );
        void onTitle( String title );
        void onStartActivity( Intent intent, int request  );
    }

    /*
     * === Constractor ===
     * @param Context context
     */
    public BtManager( Context context ) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        mTextViewDebug = new BtTextViewDebug();
        mStringUtility = new BtStringUtility();
    }

    /**
     * setOnClickListener
     * @param OnButtonsClickListener listener
     */
    public void setOnChangedListener( OnChangedListener listener ) {
        mOnListener = listener;
    }

    /*
     * getBluetoothAdapter
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    /*
     * getService
     */
    public BtService getService() {
        return mBtService;
    }

    /**
     * getTextViewDebug
     */
    public BtTextViewDebug getTextViewDebug() { 
        return mTextViewDebug;
    }

    /**
     * getStringUtility
     */
    public BtStringUtility getStringUtility() { 
        return mStringUtility;
    }

// --- Manager Control ---
    /**
     * setHandler
     */
    public void setHandler( Handler handler ) {
        mHandler = handler;
    }

    /**
     * init BluetoothAdapter
     * @return boolean : true init adapter, false failed
     */
    public boolean initAdapter() {
        // return true if debug
        if ( isDebugEmulator ) return true;
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if ( mBluetoothAdapter == null ) {
            toast_long( mToastNotAvailable );
            return false;
        }
        return true;
    }

    /**
     * enabled Service when onStart
     * @return int : 
     *      START : adapte is not Enabled and start AdapterEnable, 
     *      SETUP : setupService
     *      NONE : others
     */
    public int enableService() {
        log_debug( "enabledService()" );	
        // no action if debug
        if ( isDebugEmulator ) return RET_ENABLE_SETUP;
        if ( !mBluetoothAdapter.isEnabled() ) {
            // startActivity AdapterEnable
            // if BluetoothAdapte is not Enabled
            startActivityAdapterEnable();
            return RET_ENABLE_START;
        } else {
            // Otherwise, start Bluetooth Service
            if ( mBtService == null ) {
                setupService();
                return RET_ENABLE_SETUP;
            }
        }
        return RET_ENABLE_NONE;
    }

    /**
     * Initialization of Bluetooth service
     */
    public void setupService() {
        log_debug( "setupService()" );
        // no action if debug
        if ( isDebugEmulator ) return;
        // Initialize the BluetoothChatService to perform bluetooth connections
        if ( mBtService == null ) {
            log_debug( "new BtService" );
            mBtService = new BtService( mContext );
            mBtService.setUuid( mServiceUuidSecure, mServiceUuidInsecure );
            mBtService.setRevBuffer( mServiceRecvBufferPlane, mServiceRecvBufferByte );
            mBtService.setDebugService( isDebugService );
            mBtService.setDebugWrite( isDebugServiceWrite );
            mBtService.setDebugRead( isDebugServiceRead );
            mBtService.setHandlerWrite( isServiceHandlerWrite );
        }
        if ( mBtService != null ) {
            log_debug( "set Handler" );
            mBtService.setHandler( mHandler );
        }		
    }

    /**
     * connect Bluetooth Device when touch button
     * @return boolean : 
     *      true : connect with saved address
     *      false : start Activity Bluetooth DeviceList
     */
    public boolean connectService( boolean secure ) {
        log_debug( "connectService()" );
        // no action if debug
        if ( isDebugEmulator ) {
            toast_short( mToastNoAciton ); 
            return true;
        }
        // connect the BT device at once
        // if there is a device address. 
        String addr = getPrefDeviceAddr();
       if ( isPrefUseAddr() && ( addr != null) && !addr.equals("") && ( mBtService != null )) {
            log_debug( "connect " + addr );
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice( addr );
            mBtService.connect( device, secure );
            return true;
        }
        // otherwise
        // start Activity Bluetooth DeviceList
        startActivityDeviceList( secure );
        return false;
    }
			
    /**
     * start Bluetooth Service when onResume
     */
    public void startService() {
        log_debug( "startService()" );
        int state = execStartService();
        switch( state ) {
            case STATE_CONNECTED:
                hideButtonConnect();
                break;
            default:
                notifyTitle( mTitleNotConnected );
                break;
        }
    }

    /**
     * execStartService
     * @return int state
     */
    private int execStartService() {
        // no action if debug
        if ( isDebugEmulator ) return STATE_NONE;
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_BT_ADAPTER_ENABLE activity returns.
        if ( mBtService == null ) return STATE_NONE;
        // Only if the state is STATE_NONE, do we know that we haven't started already
        int state = mBtService.getState();
        switch( state ) {
            case STATE_NONE:
                // Start the Bluetooth service
                log_debug( "BtService start" );
                mBtService.start();
                break;
        }
        return state;
    }

    /**
     * stop Bluetooth Service when onDestroy
     */
    public void stopService() {
        log_debug( "stopService()" );
        // no action if debug
        if ( isDebugEmulator ) return;
        // Stop the Bluetooth chat services
        if ( mBtService != null ) {
            log_debug( "BtService stop" );
            mBtService.stop();
        }
        showButtonConnect();
    }

    /**
     * clear Service
     */
    public void clearService() {
        mBtService = null;
    }
    
    /**
     * check the status of BT service
     * @return boolean : true connected, false not connected
     */
    public boolean isServiceConnected() {
        // true if debug
        if ( isDebugEmulator ) return true;
        // if service is null
        if ( mBtService == null ) {
            log_debug( "isServiceConnected() Service null" );
            return false;
        }
        // if connected
        if ( mBtService.getState() == STATE_CONNECTED ) {
            log_debug( "isServiceConnected() true" );
            return true;
        }
        // otherwise
        log_debug( "isServiceConnected() false" );
        return false;
    }
// --- Manager Control end ---

// --- Command ---    
    /**
     * Sends a message.
     * @param byte[]  A string of text to send.
     * @return boolean : true send, false error
     */
    public boolean write( byte[] bytes ) {
        log_debug( "write()" ); 
        if ( isDebugEmulator ) {
            toast_short( mToastNoAciton );
            return true;
        }
        // Check that we're actually connected before trying anything
        if ( !isServiceConnected() ) {
            toast_short( mToastNotConnected );
            return false;
        }
        // Check that there's actually something to send
        if ( bytes.length == 0 ) return false;
        // Get the message bytes and tell the BluetoothChatService to write
        if ( mBtService != null ) {
            if ( isTextViewDebugSend ) {
                mTextViewDebug.showMessage( "s", bytes );
            }
            if ( isDebugWriteBefore ) {
                log_bytes( "wb", bytes );
            }
            mBtService.write( bytes );
        }
        return true;
    }		               
// --- Command end ---

// --- menu ---
    /**
     * setMenuId
     */
    public void setMenuId(
        int id_secure, int id_insecure, int id_discoverable,
        int id_disconnect, int id_clear, int id_settings ) {
        mMenuConnectSecure = id_secure;
        mMenuConnectInsecure = id_insecure;
        mMenuDiscoverable = id_discoverable;
        mMenuDisconnect = id_disconnect;
        mMenuClear = id_clear;
        mMenuSettings = id_settings;
    } 

    /**
     * OptionsItemSelected full
     * @param MenuItem item
     * @return boolean : true : match;  false : unmatch
     */
    public boolean execOptionsItemSelectedFull( MenuItem item ) { 
        int id = item.getItemId(); 
        if ( id == mMenuConnectInsecure ) {
            execMenuConnectInsecure();
            return true;
        } else if ( id == mMenuDiscoverable ) {  
            execMenuDiscoverable();
            return true;
        }
        return execOptionsItemSelectedSecure( item ); 
    }

    /**
     * OptionsItemSelected secure
     * @param MenuItem item
     * @return boolean : true : match;  false : unmatch
     */
    public boolean execOptionsItemSelectedSecure( MenuItem item ) { 
        int id = item.getItemId();
        if ( id == mMenuConnectSecure ) {
            execMenuConnectSecure();
            return true;
        } else if ( id == mMenuDisconnect ) {  
            execMenuDisconnect();
            return true;
        } else if ( id == mMenuClear ) {   
            execMenuClearAddress();
            return true;
        } else if ( id == mMenuSettings ) {  
            execMenuSettings(); 
            return true;
        } 
        return false;
    }

    /**
     * execMenu ConnectSecure
     */
    public void execMenuConnectSecure() {
        connectService( MODE_SECURE );
    }

    /**
     * execMenu ConnectInsecure
     */
    public void execMenuConnectInsecure() {
        connectService( MODE_INSECURE );
    }

    /**
     * execMenu Discoverable
     */
    public void execMenuDiscoverable() {
        if ( mBluetoothAdapter.getScanMode() ==
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE ) {
            return;
        }
        startActivityAdapterDiscoverable();
    }

    /**
     * execMenu Settings
     */
    public void execMenuSettings() {
        startActivitySettings();
    }

    /**
     * execMenuDisconnect
     */  
    public void execMenuDisconnect() {
        stopService();
    }

    /**
     * execMenuClearAddress
     */  
    public void execMenuClearAddress() {
        clearPrefDeviceAddress();
        if ( !isServiceConnected() ) {
            showButtonConnect();
        }
    }

// --- startActivity ---
    /**
     * set RequestCode AdapterEnable
     * @patam int code
     */  
    public void setRequestCodeAdapterEnable( int code ) {
        mRequestEnable = code;
    }

    /**
     * set RequestCode AdapterDiscoverable
     * @patam int code
     */  
    public void setRequestCodeAdapterDiscoverable( int code ) {
        mRequestDiscoverable = code;
    }

    /**
     * initDeviceListClass
     * @patam Class<?> cls
     * @patam int secure
     * @patam int insecure
     */
    public void initDeviceListClass( Class<?> cls, int secure, int insecure ) {
        mDeviceListClass = cls;
        mRequestDeviceListSecure = secure;
        mRequestDeviceListInsecure = insecure;
    }

    /**
     * initSettingsClass
     * @patam Class<?> cls
     * @patam int code
     */
    public void initSettingsClass( Class<?> cls, int code ) {
        mSettingsClass = cls;
        mRequestSettings = code;
    }

    /**
     * startActivity AdapterEnable
     */
    private void startActivityAdapterEnable() {
        Intent intent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
        notifyStartActivity( intent, mRequestEnable );
    }

    /**
     * startActivity AdapterDiscoverable 
     */
    private void startActivityAdapterDiscoverable() {
        Intent intent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
        intent.putExtra( 
            BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
            DISCOVERABLE_DURATION );
        notifyStartActivity( intent, mRequestDiscoverable );
    }

    /**
     * startActivity DeviceList
     */
    private void startActivityDeviceList( boolean mode ) {
        if ( mDeviceListClass == null ) return;
        Intent intent = new Intent( mContext, mDeviceListClass );
        int request = mRequestDeviceListInsecure;
        if ( mode ) {
            request = mRequestDeviceListSecure;
        }       
        notifyStartActivity( intent, request );
    }

    /**
     * startActivity settings
     */
    private void startActivitySettings() {
        if ( mSettingsClass == null ) return;
        Intent intent = new Intent( mContext, mSettingsClass );
        notifyStartActivity( intent, mRequestSettings );
    }

// --- onActivityResult ---
    /**
     * callback from Activity
     * @param int request
     * @param int result
     * @param Intent data
     *      MATCH : request match
     *      UNMATCH : request unmatch
     *      FAIL : fail to enable Bluetooth
     */
    public int execActivityResult( int request, int result, Intent data ) {
        log_debug( "execActivityResult " + result );
        if ( request == mRequestEnable ) {
            // When the request to enable Bluetooth returns
            if ( result == Activity.RESULT_OK ) {
                // Bluetooth is now enabled, so set up a chat session
                execActivityResultAdapterEnable( data );
            } else {
                // User did not enable Bluetooth or an error occurred
                log_debug( "BT not enabled" );
                toast_short( mToastNotEnabled );
                return RET_RESULT_ENABEL_FAIL;
            }
            return RET_RESULT_MATCH;
        } else if ( request == mRequestDiscoverable ) {
            return RET_RESULT_MATCH;
        } else if ( request == mRequestDeviceListSecure) {
            // When DeviceListActivity returns with a device to connect
            if ( result == Activity.RESULT_OK ) {
                execActivityResultDeviceListSecure( data );
            }
            return RET_RESULT_MATCH;
        } else if ( request == mRequestDeviceListInsecure ) {
            // When DeviceListActivity returns with a device to connect
            if ( result == Activity.RESULT_OK ) {
                execActivityResultDeviceListInsecure( data );
            }
            return RET_RESULT_MATCH;
        } else if ( request == mRequestSettings ) {
            if ( result == Activity.RESULT_OK ) {
                execActivityResultSettings( data );
            }
            return RET_RESULT_MATCH;
        }
        return RET_RESULT_UNMATCH;
    }

    /**
     * callback from AdapterEnable
     * @param Intent data
     */
    public void execActivityResultAdapterEnable( Intent data ) {
        setupService();
    }

    /**
     * callback from  Bluetooth Device List Secure activity
     * @param Intent data
     */
    public void execActivityResultDeviceListSecure( Intent data ) {
        execActivityResultDeviceList( data, MODE_SECURE );
    }

    /**
     * callback from  Bluetooth Device List Insecure activity
     * @param Intent data
     */
    public void execActivityResultDeviceListInsecure( Intent data ) {
        execActivityResultDeviceList( data, MODE_INSECURE );
    }

    /**
     * callback from  Bluetooth Device List activity
     * @param Intent data
     * @param boolean secure
     */
    private void execActivityResultDeviceList( Intent data, boolean secure ) {
        log_debug( "execActivityResultDeviceList()" );
        // no action if debug
        if ( isDebugEmulator ) return;
        // When DeviceListActivity returns with a device to connect
        // Get the device MAC address
        String address = data.getExtras().getString( EXTRA_DEVICE_ADDRESS );
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice( address );
        if ( mBtService != null ) {
            log_debug( "connect " + address ) ;
            // Attempt to connect to the device
            mBtService.connect( device, secure );
        }
    }

    /**
     * callback from Settings activity
     * @param Intent data
    */
    public void execActivityResultSettings( Intent data ) {
        log_debug( "execActivityResultSettings()" );
        setTextViewDebugStatus();
    }

// --- onActivityResult end ---

// --- Message Handler ---
    /**
     * Message Handler ( handle message )
     * @param Message msg
     */
    public void execServiceHandler( Message msg ) {
        switch ( msg.what ) {
            case WHAT_READ:
                execHandlerRead( msg );
                break;
            case WHAT_WRITE:
                execHandlerWrite( msg );
                break;
            case WHAT_DEVICE_NAME:
                execHandlerDevice( msg );
                break;
            case WHAT_FAILED:
                execHandlerFailed( msg );
                break;
            case WHAT_LOST:
                execHandlerLost( msg );
                break;
            case WHAT_STATE_CHANGE:
                int state = msg.arg1;
                log_info( "MESSAGE_STATE_CHANGE: " + state );
                switch ( state ) {
                    case STATE_CONNECTED:
                        execHandlerConnected( msg );
                        break; 
                    case STATE_CONNECTING:
                        execHandlerConnecting( msg );
                        break;
                    case STATE_LISTEN:
                    case STATE_NONE:
                    default:
                        execHandlerNone( msg );        	
                        break;
                }
                break;
        }
    }

    /**
     * Message Handler ( read )
     * @param Message msg
     */
    public void execHandlerRead( Message msg ) {
        // create valid data bytes from double buffer
        byte[] buffer = (byte[]) msg.obj;
        int length = (int) msg.arg1;
        byte[] bytes = new byte[ length ];
        for ( int i=0; i<length; i++ ) {
            bytes[ i ] = buffer[ i ];
        }
        if ( isTextViewDebugRecv ) {
            mTextViewDebug.showMessage( "r", bytes );
        }
        if ( isDebugRead ) {
            log_bytes( "r", bytes );
        }
        notifyRead( bytes );
        if ( !isUseListString ) return;
        List<String> list = mStringUtility.getListString( bytes );
        if ( list.size() == 0 ) return;
        notifyRead( list );
    }

    /**
     * Message Handler ( write )
     * @param Message msg
     */
    public void execHandlerWrite( Message msg ) {
        byte[] bytes = (byte[]) msg.obj; 
        if ( isDebugWriteAfter ) {
            log_bytes( "wa", bytes );
        }
        notifyWrite( bytes );
    }

    /**
     * Message Handler ( connected )
     * @param Message msg
     */	
    public void execHandlerConnected( Message msg ) {
        log_debug( "Event Connected: " + mDeviceName );	
        // save Device Address
        if ( mBtService != null ) {
            setPrefDeviceName( mDeviceName );
            setPrefDeviceAddr( mBtService.getDeviceAddress() );
        }
        hideButtonConnect();
        String str = String.format( mTitleConnected, mDeviceName );
        notifyTitle( str );
    }

    /**
     * Message Handler ( Connecting )
     * @param Message msg
     */
    public void execHandlerConnecting( Message msg ) {
        notifyTitle( mTitleConnecting );
    }

    /**
     * Message Handler ( None )
     * @param Message msg
     */
    public void execHandlerNone( Message msg ) {
        notifyTitle( mTitleNotConnected );
    }

    /**
     * Message Handler ( device name )
     * @param Message msg
     */
    public void execHandlerDevice( Message msg ) {
        // get the connected device's name
        mDeviceName = msg.getData().getString( BUNDLE_DEVICE_NAME );
        log_debug( "Event Device: " + mDeviceName );
        toast_short( mToastConnected + mDeviceName );
        hideButtonConnect();
    }

    /**
     * Message Handler ( Failed )
     * @param Message msg
     */
    public void execHandlerFailed( Message msg ) {
        log_debug( "Event Faild" );
        toast_short( mToastFailed );
        showButtonConnect();
        // restart service
        if ( mBtService != null ) {
            mBtService.start();
        }
    }

    /**
     * Message Handler ( Lost )
     * @param Message msg
     */
    public void execHandlerLost( Message msg ) {
        log_debug( "Event Lost" );
        toast_short( mToastLost );           
        showButtonConnect();
        // restart service
        if ( mBtService != null ) {
            mBtService.start();
        }
    }

    /**
     * get DeviceName
     * @return String  
     */
    public String getDeviceName() {
        return mDeviceName;	
    }

    /**
     * setUseListString
     * @param boolean flag
     */
    public void setUseListString( boolean flag ) {
        isUseListString = flag;
    }
// --- Message Handler end ---

// --- notify ---  
    /**
     * notifyRead
     * @param byte[] bytes
     */	
    private void notifyRead( byte[] bytes ) {
        if ( mOnListener != null ) {
            mOnListener.onRead( bytes );
        }
    }

    /**
     * notifyRead
     * @param List<String> list
     * @param byte[] bytes
     */	
    private void notifyRead( List<String> list ) {
        if ( mOnListener != null ) {
            mOnListener.onRead( list );
        }
    }

    /**
     * notifyWrite
     * @param byte[] bytes
     */	
    private void notifyWrite( byte[] bytes ) {
        if ( mOnListener != null ) {
            mOnListener.onWrite( bytes );
        }
    }

     /**
     * notifyTitle
     * @param String title
     */	
    private void notifyTitle( String title ) {
        if ( !isTitleUse ) return;
        if ( mOnListener != null ) {
            mOnListener.onTitle( title );
        }
    }

     /**
     * notifyStartActivity
     * @param Intent intent
     * @param int request
     */	
    private void notifyStartActivity( Intent intent, int request ) {
        if ( mOnListener != null ) {
            mOnListener.onStartActivity( intent, request );
        }
    }

// --- ButtonConnect ---
    /**
     * setShowButton
     */
    public void setShowButton( boolean flag ) {
        isShowButton = flag;
    }

    /**
     * initLinearLayoutConnect
     */
    public void initLinearLayoutConnect( View view, int id ) {
        mLinearLayoutConnect = (LinearLayout) view.findViewById( id );
    }

    /**
     * initTextViewConnect
     */
    public void initTextViewtConnect( View view, int id ) {
        mTextViewConnect = (TextView) view.findViewById( id );
     }

    /**
     * initButtonConnectSecure
     */
    public void initButtonConnectSecure( View view, int id ) {
        mButtonConnectSecure = (Button) view.findViewById( id );
        mButtonConnectSecure.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                connectService( MODE_SECURE );
            }
        });
    }

    /**
     * initButtonConnectInsecure
     */
    public void initButtonConnectInsecure( View view, int id ) {
        mButtonConnectInsecure = (Button) view.findViewById( id );
        mButtonConnectInsecure.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                connectService( MODE_INSECURE );
            }
        });
    }

    /**
     * show ButtonConnect
     */	
    public void showButtonConnect() {
        if ( !isShowButton ) return;
        setLinearLayoutConnectVisibility( View.VISIBLE );
        setTextViewConnectVisibility( View.VISIBLE );
        setButtonConnectSecureVisibility( View.VISIBLE );
        setButtonConnectInsecureVisibility( View.VISIBLE );
    }

    /**
     * hide ButtonConnect
     */	
    public void hideButtonConnect() {
        setLinearLayoutConnectVisibility( View.GONE );
        setTextViewConnectVisibility( View.GONE );
        setButtonConnectSecureVisibility( View.GONE );
        setButtonConnectInsecureVisibility( View.GONE );
    }

    /**
     * setLinearLayoutConnectVisibility
     */	
    private void setLinearLayoutConnectVisibility( int visibility ) {
        if ( mLinearLayoutConnect == null ) return;
        mLinearLayoutConnect.setVisibility( visibility );		 
    }

    /**
     * setTextViewConnectVisibility
     */	
    private void setTextViewConnectVisibility( int visibility ) {
        if ( mTextViewConnect == null ) return;
        mTextViewConnect.setVisibility( visibility );
        if ( visibility != View.VISIBLE ) return;
        String addr = getPrefDeviceAddr();
        if ( "".equals(addr) ) {
            mTextViewConnect.setVisibility( View.GONE );
        } else {
            mTextViewConnect.setText( getPrefDeviceName() );
        }	 
    }

    /**
     * setButtonConnectVisibility
     */	
    private void setButtonConnectSecureVisibility( int visibility ) {
        if ( mButtonConnectSecure == null ) return;
        mButtonConnectSecure.setVisibility( visibility );		 
    }

    /**
     * setButtonConnectVisibility
     */	
    private void setButtonConnectInsecureVisibility( int visibility ) {
        if ( mButtonConnectInsecure == null ) return;
        mButtonConnectInsecure.setVisibility( visibility );		 
    }

// --- Shared Preferences ---
    /**
     * setUsePrefAddress
     */
    public void setUsePrefAddress( boolean flag ) {
        isUsePrefAddr = flag;
    }

    /**
     * setPrefName
     */
    public void setPrefName( String name, String addr, String use, String show ) {
        mPrefDeviceName = name;
        mPrefDeviceAddr = addr;
        mPrefUseAddr = use;
        mPrefShowDebug = show;
    }

    /**
     * get the device name
     * @return String
     */
    private String getPrefDeviceName() {
        if ( !isUsePrefAddr ) return "";
        return mPreferences.getString( mPrefDeviceName, DEFAULT_DEVICE_NAME );
    }

    /**
     * get the device address
     * @return String
     */
    private String getPrefDeviceAddr() {
        if ( !isUsePrefAddr ) return "";
        return mPreferences.getString( mPrefDeviceAddr, DEFAULT_DEVICE_ADDR );
    }

    /**
     * isPrefUseAddr
     * @return boolean
     */ 
    private boolean isPrefUseAddr() {
        if ( !isUsePrefAddr ) return false;
        return mPreferences.getBoolean( mPrefUseAddr, DEFAULT_USE_ADDR );
    }

    /**
     * save the device name
     * @param String name
     */
    private void setPrefDeviceName( String name ) {
        if ( !isUsePrefAddr ) return;
        mPreferences.edit().putString( mPrefDeviceName, name ).commit();
    }

    /**
     * save the device address
     * @param String addr
     */
    private void setPrefDeviceAddr( String addr ) {
        if ( !isUsePrefAddr ) return;
        mPreferences.edit().putString( mPrefDeviceAddr, addr ).commit();
    }

    /**
     * clear the device address
     */
    public void clearPrefDeviceAddress() {
        setPrefDeviceName( "" );
        setPrefDeviceAddr( "" );
    }

    /**
     * isPrefShowDebug
     * @return boolean
    */ 
    public boolean isPrefShowDebug() {
        return mPreferences.getBoolean( 
            mPrefShowDebug, DEFAULT_SHOW_DEBUG );
    }

// --- title --- 
    /*
     * setTitleUse
     * @param boolean flag
    */
    public void setTitleUse( boolean flag ) {
        isTitleUse = flag;
    }

    /*
     * setTitleMsg
     * @param id_connecting
     * @param id_connected
     * @param id_not_connected
    */
    public void setTitleMsg( int id_connecting, int id_connected, int id_not_connected ) {
        mTitleConnecting = getString( id_connecting );
        mTitleConnected = getString( id_connected );
        mTitleNotConnected = getString( id_not_connected );
    }

// --- toast --- 
    /*
     * setToastUse
     * @param boolean flag
    */
    public void setToastUse( boolean flag ) {
    	isToastUse = flag;
   }

    /*
     * setToastMsg
     */
    public void setToastMsg( 
        int id_not_available, int id_not_enabled, int id_failed, int id_lost, 
        int id_connected, int id_not_connected, int id_no_action ) {
        mToastNotAvailable = getString( id_not_available );
        mToastNotEnabled = getString( id_not_enabled );
        mToastFailed = getString( id_failed );
        mToastLost = getString( id_lost );
        mToastConnected = getString( id_connected );
        mToastNotConnected = getString( id_not_connected );
        mToastNoAciton = getString( id_no_action );
    }

    /**
     * show toast
     * @param String msg
     */				
    private void toast_short( String msg ) {
        if ( !isToastUse ) return;
    	BtToastMaster.showShort( mContext, msg );
    }

    /**
     * show toast
     * @param String msg
     */				
    private void toast_long( String msg ) {
        if ( !isToastUse ) return;
    	BtToastMaster.showLong( mContext, msg );
    }

    /**
     * getString
     */				
    private String getString( int id ) {
        return mContext.getResources().getString( id );
    }	 

// --- TextViewDebug ---
    /**
     * initScrollViewDebug
     * @patam View view
     * @patam int res_id
     */
    public void initScrollViewDebug( View view, int res_id ) {
        mTextViewDebug.initScrollViewDebug( view, res_id );
    }

    /**
     * initTextViewDebug
     * @patam View view
     * @patam int res_id
     */
    public void initTextViewDebug( View view, int res_id ) {
        mTextViewDebug.initTextViewDebug( view, res_id );
    }

    /**
     * setTextViewDebugStatus
     */
    public void setTextViewDebugStatus() {
        boolean flag = isPrefShowDebug();
        if ( flag ) {
            mTextViewDebug.setVisibility( View.VISIBLE );
        } else {
            mTextViewDebug.setVisibility( View.GONE );
        }
        setTextViewDebugSend( flag );
        setTextViewDebugRecv( flag );
    }

    /**
     * setTextViewDebugSend
     * @param boolean flag
     */	
    public void setTextViewDebugSend( boolean flag ) {
        isTextViewDebugSend = flag;
    }

    /**
     * setTextViewDebugRecv
     * @param boolean flag
     */	
    public void setTextViewDebugRecv( boolean flag ) {
        isTextViewDebugRecv = flag;
    }

// --- PackageInfo --- 
    /*
     * get PackageInfo
     */	
    public String getPackageInfo() {
        String msg = ""; 
        try {
            PackageManager manager = mContext.getPackageManager();
            String name = mContext.getPackageName();
            PackageInfo info = manager.getPackageInfo( name, 0) ;
            msg = "package name:" + info.packageName + LF;
            msg += "version code: " + info.versionCode + LF;
            msg += "version name: " + info.versionName ;
        } catch ( NameNotFoundException e ) {
            if (D) e.printStackTrace();
        }
        return msg;
    }

// --- Buletooth Service Param ---
    /**
     * setServiceProfileSerial
     */
    public void setServiceProfileSerial() {
        setServiceUuid( SERVICE_UUID_SPP,  SERVICE_UUID_SPP );
    }

    /**
     * setServiceProfileChat
     */
    public void setServiceProfileChat() {
        setServiceUuid( SERVICE_UUID_CHAT_SECURE,  SERVICE_UUID_CHAT_INSECURE );
    }

    /**
     * setServiceUuid
     * @param String secure UUID
     * @param String insecure UUID 
     */
    public void setServiceUuid( String secure, String insecure ) {
        mServiceUuidSecure = secure;
        mServiceUuidInsecure = insecure;
        if ( mBtService != null ) {
            mBtService.setUuid( secure, insecure );
        }
    }

    /**
     * setRevBuffer
     * @param int plane
     * @param int bytes
     */
    public void setServiceRevBuffer( int plane, int bytes ) {
        mServiceRecvBufferPlane = plane;
        mServiceRecvBufferByte = bytes;
        if ( mBtService != null ) {
            mBtService.setRevBuffer( plane, bytes );
        }
    }

    /**
     * setDebugService
     * @param boolean flag
     */	
    public void setDebugService( boolean flag ) {
        isDebugService = flag;
        if ( mBtService != null ) {
            mBtService.setDebugService( flag );
        }
    }

    /**
     * setDebugService
     * @param boolean flag
     */	
    public void setDebugServiceWrite( boolean flag ) {
        isDebugServiceWrite = flag;
        if ( mBtService != null ) {
            mBtService.setDebugWrite( flag );
        }
    }

    /**
     * setDebugServiceRead
     * @param boolean flag
     */	
    public void setDebugServiceRead( boolean flag ) {
        isDebugServiceRead = flag;
        if ( mBtService != null ) {
            mBtService.setDebugRead( flag );
        }
    }

    /**
     * setHandlerWrite
     * @param boolean flag
     */	
    public void setServiceHandlerWrite( boolean flag ) {
        isServiceHandlerWrite = flag;
        if ( mBtService != null ) {
            mBtService.setHandlerWrite( flag );
        }
    }
   		
// --- Debug ---  
    /**
     * setDebugManager
     * @param boolean flag
     */	
    public void setDebugManager( boolean flag ) {
        isDebugManager = flag; 
    }

    /**
     * setDebugManager
     * @param boolean flag
     */	
    public void setDebugManagerWriteBefore( boolean flag ) {
        isDebugWriteBefore = flag; 
    }

    /**
     * setDebugManager
     * @param boolean flag
     */	
    public void setDebugManagerWriteAfter( boolean flag ) {
        isDebugWriteAfter = flag; 
    }

    /**
     * setDebugManager
     * @param boolean flag
     */	
    public void setDebugManagerRead( boolean flag ) {
        isDebugRead = flag; 
    }

    /**
     * setDebugEmulator
     * @param boolean flag
     */	
    public void setDebugEmulator( boolean flag ) {
        isDebugEmulator = flag;
    }

    /**
     * log_bytes
     * @param String str
     * @param byte[] bytes
     */
    private void log_bytes( String str, byte[] bytes ) {
        String msg = str + " ";
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
        if (isDebugManager) log_d( msg ); 
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

    /**
     * write log
     * @param String msg
     */ 
    private void log_info( String msg ) {
        if (isDebugManager) {
            Log.i( 
                BtConstant.TAG, 
                TAG_SUB + BtConstant.LOG_COLON + msg );
        }
    }
// --- Debug end ---

}
