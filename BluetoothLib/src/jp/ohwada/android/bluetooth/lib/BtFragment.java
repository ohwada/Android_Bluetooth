/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

import java.util.List;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Fragment whitch controls Bluetooth device 
 */
public class BtFragment extends Fragment {

    /** Debug */
    private boolean bt_isDebug = BtConstant.DEBUG_LOG_ACTIVITY;
    private  static final String TAG_SUB = "Fragment";

    // Intent request codes
    protected static final int BT_REQUEST_ADAPTER_ENABLE = BtConstant.REQUEST_ADAPTER_ENABLE;
    protected static final int BT_REQUEST_ADAPTER_DISCOVERABLE = BtConstant.REQUEST_ADAPTER_DISCOVERABLE;
    protected static final int BT_REQUEST_DEVICE_LIST_SECURE = BtConstant.REQUEST_DEVICE_LIST_SECURE;
    protected static final int BT_REQUEST_DEVICE_LIST_INSECURE = BtConstant.REQUEST_DEVICE_LIST_INSECURE;
    protected static final int BT_REQUEST_SETTINGS = BtConstant.REQUEST_SETTINGS;

    // return code of enableAdapter
    private static final int BT_RET_ENABLE_SETUP = BtManager.RET_ENABLE_SETUP;

    /**
     * Bluetooth Manager
     */
    private BtManager bt_mManager;

// --- onCreate ---
    /**
     * init
     */
    protected void bt_init() { 
        bt_mManager = new BtManager( getActivity() );
        bt_mManager.setHandler( bt_serviceHandler ); 
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
        bt_mManager.setMenuId(
            R.id.bt_menu_connect_secure,
            R.id.bt_menu_connect_insecure,
            R.id.bt_menu_discoverable,
            R.id.bt_menu_disconnect,
            R.id.bt_menu_clear,  
            R.id.bt_menu_settings );
        bt_mManager.setTitleMsg( 
           R.string.bt_title_connecting, 
           R.string.bt_title_connected_to, 
           R.string.bt_title_not_connected );
        bt_mManager.setToastMsg(
            R.string.bt_toast_not_available,
            R.string.bt_toast_not_enabled, 
            R.string.bt_toast_failed, 
            R.string.bt_toast_lost, 
            R.string.bt_toast_connected,
            R.string.bt_toast_not_connected,
            R.string.bt_toast_no_action );
        bt_mManager.setPrefName( 
            BtConstant.PREF_DEVICE_NAME, 
            BtConstant.PREF_DEVICE_ADDR,  
            BtConstant.PREF_USE_ADDR,
            BtConstant.PREF_SHOW_DEBUG );
        bt_mManager.setDebugEmulator( 
            BtConstant.DEBUG_EMULATOR ); 
        bt_initRequestCode(); 
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
     * setUseListString
     * @param boolean flag
     */
    protected void bt_setUseListString( boolean flag ) {
        bt_mManager.setUseListString( flag );
    }

// --- Listener ---
    /**
     * initListener
     */
    protected void bt_initListener() {  	 
        /* Initialization of Bluetooth */
        bt_mManager.setOnChangedListener( new BtManager.OnChangedListener() { 
            @Override 
            public void onRead( byte[] bytes ) {
                bt_execRead( bytes );
            }
            @Override 
            public void onRead( List<String> list ) {
                bt_execRead( list );
            }
            @Override 
            public void onWrite( byte[] bytes ) {
                bt_execWrite( bytes );
            }
            @Override 
            public void onTitle( String str ) {
                bt_execTitle( str );
            }
            @Override 
            public void onStartActivity( Intent intent, int request ) {
                bt_execStartActivity( intent, request  );
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
     * execTitle
     * @patam String title
     */
    protected void bt_execTitle( String title ) {
    	bt_setSubTitle( title );
    }

    /**
     * execStartActivity
     * @patam Intent intent
     * @patam int request
     */
    protected void bt_execStartActivity( Intent intent, int request ) {
        startActivityForResult( intent, request );
    }
// --- onCreate end ---

// --- onStart ---
    /**
     * enableService
     */
    protected boolean bt_enableService() {
        bt_mManager.setTextViewDebugStatus();
        bt_mManager.showButtonConnect();
        int ret = bt_mManager.enableService();
        if ( ret == BT_RET_ENABLE_SETUP ) {
            return true;
        }
        return false;
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
            cls, 
            BT_REQUEST_DEVICE_LIST_SECURE, 
            BT_REQUEST_DEVICE_LIST_INSECURE );
    }

    /**
     * initSettingsClass
     * @patam Class<?> cls
     */
    protected void bt_initSettingsClass( Class<?> cls ) {
        bt_mManager.initSettingsClass( cls, BT_REQUEST_SETTINGS );
    }

    /**
     * initRequestCode
     */
    protected void bt_initRequestCode() {
        bt_mManager.setRequestCodeAdapterEnable( 
            BT_REQUEST_ADAPTER_ENABLE );
        bt_mManager.setRequestCodeAdapterDiscoverable( 
            BT_REQUEST_ADAPTER_DISCOVERABLE ); 
    }

// --- onActivityResult ---
    /**
     * callback from AdapterEnable
     * @param Intent data
     */
    protected void bt_execActivityResultAdapterEnable( Intent data ) {
        bt_mManager.execActivityResultAdapterEnable( data );
    }

    /**
     * callback from  Bluetooth Device List Secure activity
     * @param Intent data
     */
    protected void bt_execActivityResultDeviceListSecure( Intent data ) {
        bt_mManager.execActivityResultDeviceListSecure( data );
    }

    /**
     * callback from  Bluetooth Device List Insecure activity
     * @param Intent data
     */
    protected void bt_execActivityResultDeviceListInsecure( Intent data ) {
        bt_mManager.execActivityResultDeviceListInsecure( data );
    }

    /**
     * callback from Settings activity
     * @param Intent data
    */
    protected void bt_execActivityResultSettings( Intent data ) {
        bt_mManager.execActivityResultSettings( data );
    }

// --- Handler ---
    /**
     * The Handler that gets information back from the BtService
     */
    private final Handler bt_serviceHandler = new Handler() {
        @Override
        public void handleMessage( Message msg ) {
        	bt_execServiceHandler( msg );
        }
    };

    /**
     * Message Handler ( handle message )
     * @param Message msg
     */
    protected void bt_execServiceHandler( Message msg ) {
        bt_mManager.execServiceHandler( msg );
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

// --- Title bar ---
    /*
     * setTitleUse
     * @param boolean flag
     */
    protected void bt_setTitleUse( boolean flag ) {
    	bt_mManager.setTitleUse( flag ); 
    }

    /*
     * setSubTitle
     * @param CharSequence title
    */
    private final void bt_setSubTitle( CharSequence title ) {
        final ActionBar actionBar = getActivity().getActionBar();
        if ( actionBar == null ) return;
        actionBar.setSubtitle( title );
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
