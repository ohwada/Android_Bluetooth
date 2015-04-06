/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Activity whitch controls Bluetooth device
 */
public class BtActivity extends Activity {

    /** Debug */
    private boolean bt_isDebug = BtConstant.DEBUG_LOG_ACTIVITY;
    private  static final String TAG_SUB = "Activity";

    // Intent request codes
    protected static final int REQUEST_ADAPTER_ENABLE = BtConstant.REQUEST_ADAPTER_ENABLE;
    protected static final int REQUEST_ADAPTER_DISCOVERABLE = BtConstant.REQUEST_ADAPTER_DISCOVERABLE;
    protected static final int REQUEST_DEVICE_LIST_SECURE = BtConstant.REQUEST_DEVICE_LIST_SECURE;
    protected static final int REQUEST_DEVICE_LIST_INSECURE = BtConstant.REQUEST_DEVICE_LIST_INSECURE;
    protected static final int REQUEST_SETTINGS = BtConstant.REQUEST_SETTINGS;

    // recieve
    private static final String BT_LF = "\n";

    /**
     * Bluetooth Manager
     */
    private BtActivityManager bt_mManager;

    // ContentView
    private View bt_mContentView = null;

// --- onCreate ---
    /**
     * init
     */
    protected void bt_init() { 
        bt_mManager = new BtActivityManager( this ); 
    }

    /**
     * getManager
     */
    protected BtManager bt_getManager() { 
        return bt_mManager;
    }

    /**
     * getDeviceName
     */
    protected String bt_getDeviceName() { 
        return bt_mManager.getDeviceName();
    }

    /**
     * initContentView
     * @param int res_id
     */
    protected void bt_initContentView( int res_id ) {  
        bt_mContentView = getLayoutInflater().inflate( res_id, null );
        setContentView( bt_mContentView ); 
    }

    /**
     * getContentView
     */
    protected View bt_getContentView() {
        return bt_mContentView; 
    }

// --- bluetooth service ---
    /**
     * initManager
     */
    protected void bt_initManager() {  
        bt_log_debug( bt_mManager.getPackageInfo() ); 
        bt_mManager.setTitleMsg( 
           R.string.bt_title_connecting, 
           R.string.bt_title_connected_to, 
           R.string.bt_title_not_connected );
        bt_mManager.setToastMsg(
            R.string.bt_toast_failed, 
            R.string.bt_toast_lost, 
            R.string.bt_toast_connected,
            R.string.bt_toast_not_connected,
            R.string.bt_toast_no_action );
        bt_mManager.setPrefName( 
            BtConstant.PREF_ADDR, 
            BtConstant.PREF_USE_ADDR,
            BtConstant.PREF_SHOW_DEBUG );
        bt_mManager.setDebugEmulator( 
            BtConstant.DEBUG_EMULATOR );
        bt_mManager.setRequestCodeAdapterEnable( 
            REQUEST_ADAPTER_ENABLE );
        bt_mManager.setRequestCodeAdapterDiscoverable( 
            REQUEST_ADAPTER_DISCOVERABLE ); 
        bt_mManager.setTextViewDebugStatus();
        bt_initListener();
    }

    /**
     * initAdapter
     */
    protected void bt_initAdapterAndFinish() {
        // Get local Bluetooth adapter 
       boolean ret = bt_initAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if ( !ret ) {
            bt_toast_long( R.string.bt_toast_not_available );
            finish();
        }
    }

    /**
     * initAdapter
     */
    protected boolean bt_initAdapter() {
        return bt_mManager.initAdapter();
    }

    /**
     * setServiceHandlerWrite
     */
    protected void bt_setServiceHandlerWrite( boolean flag ) {
    	bt_mManager.setServiceHandlerWrite( flag );
    }

    /**
     * setServiceProfileChat
     */
    protected void bt_setServiceProfileChat() {
        bt_mManager.setServiceProfileChat();
    }

    /**
     * setServiceProfileSerial
     */
    protected void bt_setServiceProfileSerial() {
        bt_mManager.setServiceProfileSerial();
    }

    /**
     * isServiceConnected
     */
    protected boolean bt_isServiceConnected() {
        return bt_mManager.isServiceConnected();
    }

    /**
     * setUsePrefAddress
     */
    protected void bt_setUsePrefAddress( boolean flag ) {
        bt_mManager.setUsePrefAddress( flag );
    }

    /**
     * initListener
     */
    protected void bt_initListener() {  	 
        /* Initialization of Bluetooth */
        bt_mManager.setOnChangedListener( new BtManager.OnChangedListener() { 
            @Override 
            public void onReadBytes( byte[] bytes ) {
                bt_execRead( bytes );
            }
            @Override 
            public void onReadStrings( List<String> list ) {
                bt_execRead( list );
            }
            @Override 
            public void onWrite( byte[] bytes ) {
                bt_execWrite( bytes );
            }
            @Override 
            public void onEvent( int code ) {
                bt_execEvent( code );
            }
        });	
    }

    /**
     * execRead
     * @param byte[] bytes
     */
    protected void bt_execRead( byte[] bytes ) {
        // dummy
    }

    /**
     * execRead
     * @param List<String> list
     */
    protected void bt_execRead( List<String> list ) {
        // dummy
    }

    /**
     * execWrite
     * @param byte[] bytes
     */
    protected void bt_execWrite( byte[] bytes ) {
        // dummy
    }

    /**
     * execEvent
     * @param int code
     */
    protected void bt_execEvent( int code ) {
        // dummy
    }

// --- Button Connect ---
    /**
     * initButtonConnectSecure
     * @patam int res_id
     */
    protected void bt_initButtonConnectSecure( int res_id ) {
        if ( bt_mContentView == null ) return; 
        bt_mManager.initButtonConnectSecure( bt_mContentView, res_id );
    }

    /**
     * initButtonConnectInsecure
     * @patam int res_id
     */
    protected void bt_initButtonConnectInsecure( int res_id ) {
        if ( bt_mContentView == null ) return; 
        bt_mManager.initButtonConnectInsecure( bt_mContentView, res_id );
    }

    /**
     * show ButtonConnect
     */	
    protected void bt_showButtonConnect() {
        bt_mManager.showButtonConnect();
    }

    /**
     * hide ButtonConnect
     */	
    protected void bt_hideButtonConnect() {
        bt_mManager.hideButtonConnect();
    }
    
// --- TextView Debug ---
    /**
     * nitScrollViewDebug
     * @patam int res_id
     */
    protected void bt_initScrollViewDebug( int res_id ) {
        if ( bt_mContentView == null ) return; 
        bt_mManager.initScrollViewDebug( bt_mContentView, res_id );
    }
  
    /**
     * initTextViewDebug
     * @patam int res_id
     */
    protected void bt_initTextViewDebug( int res_id ) {
        if ( bt_mContentView == null ) return; 
        bt_mManager.initTextViewDebug( bt_mContentView, res_id );
    }

    /**
     * setDebugTextSend
     * @param boolean flag
     */	
    protected void bt_setTextViewDebugSend( boolean flag ) {
        bt_mManager.setTextViewDebugSend( flag );
    }

    /**
     * setDebugTextRecv
     * @param boolean flag
     */	
    protected void bt_setTextViewDebugRecv( boolean flag ) {
        bt_mManager.setTextViewDebugRecv( flag );
    }

// --- Title bar ---
    /*
     * setTitleUse
    */
    protected void bt_setTitleUse( boolean flag ) {
        bt_mManager.setTitleUse( flag ); 
    }

// --- toast ---
    /*
     * setToastUse
     */
    protected void bt_setToastUse( boolean flag ) {
        bt_mManager.setToastUse( flag ); 
    }
// --- onCreate end ---

// --- onStart ---
    /**
     * enableService
     */
    protected boolean bt_enableService() {
    	return bt_mManager.enableService();
    }

// --- onResume --- 
    /**
     * startService
     */
    protected void bt_startService() {
        bt_mManager.startService();
    }

// --- onDestroy ---
    /**
     * stopService
     */
   protected void bt_stopService() {
        bt_mManager.stopService();
    }

// --- onCreateOptionsMenu ---
    /**
     * CreateOptionsMenu full
     * @param Menu menu
     */
    protected void bt_execCreateOptionsMenuFull( Menu menu ) {
        getMenuInflater().inflate( R.menu.bt_full, menu );
    }

    /**
     * CreateOptionsMenu secure
     * @param Menu menu
     */
    protected void bt_execCreateOptionsMenuSecure( Menu menu ) {
        getMenuInflater().inflate( R.menu.bt_secure, menu );
    }

// --- onOptionsItemSelected ---
    /**
     * OptionsItemSelected full
     * @param MenuItem item
     */
    protected boolean bt_execOptionsItemSelectedFull( MenuItem item ) { 
        int id = item.getItemId(); 
        if ( id == R.id.bt_menu_connect_insecure ) {
            bt_execMenuConnectInsecure();
            return true;
        } else if ( id == R.id.bt_menu_discoverable ) {  
            bt_execMenuDiscoverable();
            return true;
        }
        return bt_execOptionsItemSelectedSecure( item ); 
    }

    /**
     * OptionsItemSelected secure
     * @param MenuItem item
     */
    protected boolean bt_execOptionsItemSelectedSecure( MenuItem item ) { 
        int id = item.getItemId();
        if ( id == R.id.bt_menu_connect_secure ) {
            bt_execMenuConnectSecure();
            return true;
        } else if ( id == R.id.bt_menu_disconnect ) {  
            bt_execMenuDisconnect();
            return true;
        } else if ( id == R.id.bt_menu_clear ) {   
            bt_execMenuClearAddress();
            return true;
        } else if ( id == R.id.bt_menu_settings ) {  
            bt_execMenuSettings(); 
            return true;
        } 
        return false;
    }

    /**
     * execMenu ConnectSecure
     */
    protected void bt_execMenuConnectSecure() {
    	bt_mManager.execMenuConnectSecure();
    }

    /**
     * execMenu ConnectInsecure
     */
    protected void bt_execMenuConnectInsecure() {
    	bt_mManager.execMenuConnectInsecure();
    }

    /**
     * execMenu Discoverable
     */
    protected void bt_execMenuDiscoverable() {
    	bt_mManager.execMenuDiscoverable();
    }

    /**
     * execMenu Disconnect
     */
    protected void bt_execMenuDisconnect() {
        bt_mManager.execMenuDisconnect();
    }

    /**
     * execMenu ClearAddress
     */
    protected void bt_execMenuClearAddress() {
        bt_mManager.execMenuClearAddress();
    }

    /**
     * execMenu Settings
     */
    protected void bt_execMenuSettings() {
        bt_mManager.execMenuSettings();
    }

// --- startActivity ---
    /**
     * initDeviceListClass
     * @patam Class<?> cls
     */
    protected void bt_initDeviceListClass( Class<?> cls ) {
        bt_mManager.initDeviceListClass( 
            cls, REQUEST_DEVICE_LIST_SECURE, REQUEST_DEVICE_LIST_INSECURE );
    }

    /**
     * initSettingsClass
     * @patam Class<?> cls
     */
    protected void bt_initSettingsClass( Class<?> cls ) {
        bt_mManager.initSettingsClass( cls, REQUEST_SETTINGS );
    }

// --- onActivityResult ---
    /**
     * execActivityResult
     * @param int request
     * @param int result
     * @param Intent data
     */
    protected boolean bt_execActivityResult( int request, int result, Intent data ) {
        if ( request == BtConstant.REQUEST_ADAPTER_ENABLE ) {
            // When the request to enable Bluetooth returns
            if ( result == Activity.RESULT_OK ) {
                // Bluetooth is now enabled, so set up a chat session
                bt_execActivityResultAdapterEnable( data );
            } else {
                // User did not enable Bluetooth or an error occurred
                bt_toast_short( R.string.bt_toast_not_enabled );
                finish();
            }
            return true;
        } else if ( request == BtConstant.REQUEST_ADAPTER_DISCOVERABLE ) {
            return true;
        } else if ( request == BtConstant.REQUEST_DEVICE_LIST_SECURE ) {
            // When DeviceListActivity returns with a device to connect
            if ( result == Activity.RESULT_OK ) {
                bt_execActivityResultDeviceListSecure( data );
            }
            return true;
        } else if ( request == BtConstant.REQUEST_DEVICE_LIST_INSECURE ) {
            // When DeviceListActivity returns with a device to connect
            if ( result == Activity.RESULT_OK ) {
                bt_execActivityResultDeviceListInsecure( data );
            }
            return true;
        } else if ( request == BtConstant.REQUEST_SETTINGS ) {
            if ( result == Activity.RESULT_OK ) {
                bt_execActivityResultSettings( data );
            }
            return true;
        }
        return false;
    }

    /**
     * execActivityResult AdapterEnable
     */
    protected void bt_execActivityResultAdapterEnable( Intent data ) {
        bt_mManager.execActivityResultAdapterEnable( data );
    }

    /**
     * execActivityResult DeviceListSecure
     */
    protected void bt_execActivityResultDeviceListSecure( Intent data ) {
        bt_mManager.execActivityResultDeviceListSecure( data );
    }

    /**
     * execActivityResult DeviceListInsecure
     */
    protected void bt_execActivityResultDeviceListInsecure( Intent data ) {
        bt_mManager.execActivityResultDeviceListInsecure( data );
    }

    /**
     * execActivityResult Settings
     */
    protected void bt_execActivityResultSettings( Intent data ) {
        bt_mManager.execActivityResultSettings( data );
    }

// --- command ---
    /**
     * sendLF
     * @param String str
     */
    protected boolean bt_sendLF( String str ) {
        return bt_send( str + BT_LF ) ;
    }

    /**
     * send
     * @param String str
     */
    protected boolean bt_send( String str ) {
        // Check that there's actually something to send
        if ( str.length() == 0 )  return false;
        return bt_send( str.getBytes() );
    }

    /**
     * bt_send
     * @param byte[] bytes
     */
    protected boolean bt_send( byte[] bytes ) {
        return bt_mManager.write( bytes ); 
    }

// --- toast ---
    /**
     * show toast
     * @param int res_id
     */				
    protected void bt_toast_short( int res_id ) {
    	BtToastMaster.showShort( this, res_id );
    }

    /**
     * show toast
     * @param String msg
     */				
    protected void bt_toast_short( String msg ) {
    	BtToastMaster.showShort( this, msg );
    }

    /**
     * show toast
     * @param int res_id
     */				
    protected void bt_toast_long( int res_id ) {
    	BtToastMaster.showLong( this, res_id );
    }

    /**
     * show toast
     * @param String msg
     */				
    protected void bt_toast_long( String msg ) {
    	BtToastMaster.showLong( this, msg );
    }

// --- Debug ---
    /**
     * setDebug
     * @param boolean flag
     */	
    protected void bt_setDebug( boolean flag ) {
        bt_isDebug = flag;
    }


    /**
     * setDebug Manage Service
     * @param boolean flag
     */	
    protected void bt_setDebugManageService( boolean flag ) {
        bt_mManager.setDebugManager( flag );
        bt_mManager.setDebugService( flag );
    }

    /**
     * write log
     * @param String msg
     */ 
    protected void bt_log_debug( String msg ) {
        if (bt_isDebug) bt_log_d( msg );
    }

    /**
     * write log
     * @param String msg
     */ 
    protected void bt_log_d( String msg ) {
        Log.d( 
            BtConstant.TAG, 
            TAG_SUB + BtConstant.LOG_COLON + msg );
    }

}
