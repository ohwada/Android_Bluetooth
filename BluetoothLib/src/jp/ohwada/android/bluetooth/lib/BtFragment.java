/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.util.Log;

/**
 * Fragment whitch controls Bluetooth device 
 */
public class BtFragment extends Fragment {

    /** Debug */
    private boolean bt_isDebug = BtConstant.DEBUG_LOG_ACTIVITY;
    private  static final String TAG_SUB = "Fragment";

    // Intent request codes
    protected static final int REQUEST_ADAPTER_ENABLE = BtConstant.REQUEST_ADAPTER_ENABLE;
    protected static final int REQUEST_ADAPTER_DISCOVERABLE = BtConstant.REQUEST_ADAPTER_DISCOVERABLE;
    protected static final int REQUEST_DEVICE_LIST_SECURE = BtConstant.REQUEST_DEVICE_LIST_SECURE;
    protected static final int REQUEST_DEVICE_LIST_INSECURE = BtConstant.REQUEST_DEVICE_LIST_INSECURE;
    protected static final int REQUEST_SETTINGS = BtConstant.REQUEST_SETTINGS;

    /**
     * Bluetooth Manager
     */
    private BtFragmentManager bt_mManager;

// --- onCreate ---
    /**
     * init
     */
    protected void bt_init() { 
        bt_mManager = new BtFragmentManager( this ); 
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
            public void onEvent( int code ) {
                bt_execEvent( code );
            }
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

// --- onOptionsItemSelected ---
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
     * execActivityResult AdapterEnable
     */
    protected void bt_execActivityResultAdapterEnable( Intent data ) {
        bt_mManager.execActivityResultAdapterEnable( data );
    }

    /**
     * execActivityResult DeviceListInsecure
     */
    protected void bt_execActivityResultDeviceListInsecure( Intent data ) {
        bt_mManager.execActivityResultDeviceListInsecure( data );
    }

    /**
     * execActivityResult DeviceListSecure
     */
    protected void bt_execActivityResultDeviceListSecure( Intent data ) {
        bt_mManager.execActivityResultDeviceListSecure( data );
    }

    /**
     * execActivityResult Settings
     */
    protected void bt_execActivityResultSettings( Intent data ) {
        bt_mManager.execActivityResultSettings( data );
    }

// --- command ---
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
    	BtToastMaster.showShort( getActivity(), res_id );
    }

    /**
     * show toast
     * @param String msg
     */				
    protected void bt_toast_short( String msg ) {
    	BtToastMaster.showShort( getActivity(), msg );
    }

    /**
     * show toast
     * @param int res_id
     */				
    protected void bt_toast_long( int res_id ) {
    	BtToastMaster.showLong( getActivity(), res_id );
    }

    /**
     * show toast
     * @param String msg
     */				
    protected void bt_toast_long( String msg ) {
    	BtToastMaster.showLong( getActivity(), msg );
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
