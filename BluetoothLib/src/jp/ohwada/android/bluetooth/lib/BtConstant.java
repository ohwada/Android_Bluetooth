/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

/**
 * Constant
 */
public class BtConstant {

// === Debug ===
    public static final String TAG = "BluetoothLib";
    public static final boolean DEBUG_LOG = true; 
    public static final boolean DEBUG_LOG_ACTIVITY = true;    
    public static final boolean DEBUG_LOG_MANAGER = true;    
    public static final boolean DEBUG_LOG_MANAGER_WRITE_BEFORE = false;
    public static final boolean DEBUG_LOG_MANAGER_WRITE_AFTER = false;
    public static final boolean DEBUG_LOG_MANAGER_READ = false;
    public static final boolean DEBUG_LOG_SERVICE = true;
    public static final boolean DEBUG_LOG_SERVICE_WRITE = false;
    public static final boolean DEBUG_LOG_SERVICE_READ = false; 
    public static final boolean DEBUG_LOG_LIST = true;
    public static final boolean DEBUG_LOG_STRING = true;
    public static final boolean DEBUG_LOG_STRING_READ = false; 
    public static final boolean DEBUG_LOG_EXCEPTION = true;

    // for emulator	
    public static final boolean DEBUG_EMULATOR = false;

    // debug text	
    public static final boolean DEBUG_TEXT_SEND = false;
    public static final boolean DEBUG_TEXT_RECV = false;

    // mode
    public static final boolean SERVICE_HANDLER_WRITE = false;

    /* Intent request codes */
    public static final int REQUEST_ADAPTER_ENABLE = 101;	        
    public static final int REQUEST_ADAPTER_DISCOVERABLE = 102;	 
    public static final int REQUEST_DEVICE_LIST_SECURE = 103;
    public static final int REQUEST_DEVICE_LIST_INSECURE = 104;
    public static final int REQUEST_SETTINGS = 105;           

    /* SharedPreferences */
    public static final String PREF_DEVICE_NAME = "bt_device_name";
    public static final String PREF_DEVICE_ADDR = "bt_device_addr";
    public static final String PREF_USE_ADDR = "bt_use_addr";
    public static final String PREF_SHOW_DEBUG = "bt_show_debug";

    // Recieve byffer of Bluetooth Service 
    public static final int SERVICE_RECV_BUFFER_PLANE = 32;
    public static final int SERVICE_RECV_BUFFER_BYTE = 1024;

    // Unique UUID of Bluetooth Service 
    public static final String SERVICE_UUID_CHAT_SECURE = "fa87c0d0-afac-11de-8a39-0800200c9a66";
    public static final String SERVICE_UUID_CHAT_INSECURE ="8ce255c0-200a-11e0-ac64-0800200c9a66";
    public static final String SERVICE_UUID_SPP = "00001101-0000-1000-8000-00805F9B34FB";

    /* log */
    public static final String LOG_COLON = ": ";
}
