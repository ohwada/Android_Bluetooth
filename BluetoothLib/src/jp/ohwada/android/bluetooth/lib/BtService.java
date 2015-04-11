/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 * 
 * base on BluetoothChatService.java
 * https://android.googlesource.com/platform/development/+/master/samples/BluetoothChat/
 */
public class BtService {

    // Debugging
    private boolean isDebugService = BtConstant.DEBUG_LOG_SERVICE; 
    private boolean isDebugWrite = BtConstant.DEBUG_LOG_SERVICE_WRITE; 
    private boolean isDebugRead = BtConstant.DEBUG_LOG_SERVICE_READ; 
    private static final boolean D = BtConstant.DEBUG_LOG_EXCEPTION;
    private static final String TAG_SUB = "BtService";

    // mode
    private boolean isHandlerWrite = BtConstant.SERVICE_HANDLER_WRITE;

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    /* Message types sent from the BluetoothService Handler */
    public static final int WHAT_READ = 101;
    public static final int WHAT_WRITE = 102;
    public static final int WHAT_STATE_CHANGE = 103;
    public static final int WHAT_FAILED = 104;
    public static final int WHAT_LOST = 105;

    /* Constants that indicate the current connection state */
    public static final int STATE_NONE = 201;       // we're doing nothing
    public static final int STATE_LISTEN = 202;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 203; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 204;  // now connected to a remote device

    // connect mode
    public static final boolean MODE_SECURE = true;
    public static final boolean MODE_INSECURE = false;

    // socket type
    private static final String SOCKET_TYPE_SECURE = "secure";
    private static final String SOCKET_TYPE_INSECURE = "insecure";

    // handler
    private static final int ARG1_NONE = -1;
    private static final int ARG2_NONE = -1;

    // Unique UUID
    // default SPP, able to change chat profile  
    private UUID mUuidSecure = UUID.fromString( BtConstant.SERVICE_UUID_SPP );
    private UUID mUuidInsecure = UUID.fromString( BtConstant.SERVICE_UUID_SPP );

    // Number of receive buffer
    private int mRecvBufferPlane = BtConstant.SERVICE_RECV_BUFFER_PLANE;
    private int mRecvBufferByte = BtConstant.SERVICE_RECV_BUFFER_BYTE;

    // Member fields
    private final BluetoothAdapter mAdapter;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // note: remove final for multi activity
    private Handler mHandler;

    // note: add for save param
    private String mDeviceName = null;
    private String mDeviceAddress = null;

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BtService( Context context, Handler handler ) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     * Constructor
     * note: add for muilti activity
     * @param context  The UI Activity Context
     */
    public BtService( Context context ) {
        log_debug( "create BtService" );
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }

    /**
     * setHandler
     * note: add for muilti activity
     * @param handler A Handler to send messages back to the UI Activity
     */
    public void setHandler( Handler handler ) {
        log_debug( "handler " + handler.toString() );
        mHandler = handler;
    }

    /**
     * setUuid
     * note: add for change profile
     * @param String secure UUID
     * @param String insecure UUID 
     */
    public void setUuid( String secure, String insecure ) {
        mUuidSecure = UUID.fromString( secure );
        mUuidInsecure = UUID.fromString( insecure );
    }

    /**
     * setRecvBuffer
     * note: add for change plane of Recieve Buffer
     * @param int plane
     * @param int bytes
     */
    public void setRecvBuffer( int plane, int bytes ) {
        mRecvBufferPlane = plane;
        mRecvBufferByte = bytes;
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        log_debug( "setState() " + mState + " -> " + state );
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(
            WHAT_STATE_CHANGE, state, ARG2_NONE )
            .sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * get Device Name
     * note: add for save param
     * @return String
     */
    public String getDeviceName() {
        return mDeviceName;
    }

    /**
     * get Device Address
     * note: add for save param
     * @return String
     */
    public String getDeviceAddress() {
        return mDeviceAddress;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        log_debug( "start" );

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel(); 
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel(); 
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread( MODE_SECURE );
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread( MODE_INSECURE );
            mInsecureAcceptThread.start();
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        log_debug( "connect to: " + device );

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel(); 
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel(); 
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        log_debug( "connected, Socket Type: " + socketType );

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel(); 
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel(); 
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // note: remove this message, 
        // 　　　the device name can be got in the added method
        //
        // Send the name of the connected device back to the UI Activity
//        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE_NAME);
//        Bundle bundle = new Bundle();
//        bundle.putString(BluetoothChat.DEVICE_NAME, device.getName());
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);

        // save device param
        mDeviceName = device.getName();
        mDeviceAddress = device.getAddress();

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        log_debug( "stop" );

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        log_debug( "write" );
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        // note: change toast message to message type
        mHandler.sendMessage( 
            mHandler.obtainMessage( WHAT_FAILED ) );

        // note: move to manager
        // Start the service over to restart listening mode
//        BtService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        // note: change toast message to message type
        mHandler.sendMessage(
            mHandler.obtainMessage( WHAT_LOST ) );

        // note: move to manager
        // Start the service over to restart listening mode
//        BtService.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        /**
         * AcceptThread
         */
        public AcceptThread( boolean secure ) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? SOCKET_TYPE_SECURE : SOCKET_TYPE_INSECURE;

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(
                        NAME_SECURE, mUuidSecure );
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        NAME_INSECURE, mUuidInsecure );
                }
            } catch (IOException e) {
                log_error( "Socket Type: " + mSocketType + " listen() failed" );
                if (D) e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        /**
         * run
         */
        public void run() {
            log_debug( "Socket Type: " + mSocketType + " BEGIN AcceptThread " + this );
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                // note: add for sometimes ServerSocket is lost
                if ( mmServerSocket == null ) {
                    log_error( "ServerSocket is lost " + this );
                    connectionLost();
                    break;
                }
                // try to accept connection
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    log_error( "Socket Type: " + mSocketType + " accept() failed" );
                    if (D) e.printStackTrace();
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BtService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(
                                socket, socket.getRemoteDevice(), mSocketType );
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                log_error( "Could not close unwanted socket" );
                                if (D) e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
            log_info( "END AcceptThread, socket Type: " + mSocketType );

        }

        /**
         * cancel
         */
        public void cancel() {
            log_debug( "Socket Type: " + mSocketType + " cancel " + this );
            // note: add for sometimes ServerSocket is lost
            if ( mmServerSocket == null ) return;
            // try to close
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                log_error( "Socket Type: " + mSocketType + " close() of server failed" );
                if (D) e.printStackTrace();
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        /**
         * ConnectThread
         */
        public ConnectThread( BluetoothDevice device, boolean secure ) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? SOCKET_TYPE_SECURE : SOCKET_TYPE_INSECURE;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            mUuidSecure);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            mUuidInsecure);
                }
            } catch (IOException e) {
                log_error( "Socket Type: " + mSocketType + " create() failed" );
                if (D) e.printStackTrace();
            }
            mmSocket = tmp;
        }

        /**
         * run
         */
        public void run() {
            log_info( "BEGIN ConnectThread SocketType: " + mSocketType );
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    log_error( 
                        "unable to close() " + mSocketType + 
                        " socket during connection failure" );
                    if (D) e2.printStackTrace();
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BtService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        /**
         * cancel
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                log_error( "close() of connect " + mSocketType + " socket failed" );
                if (D) e.printStackTrace();
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        /**
         * ConnectedThread
         */
        public ConnectedThread( BluetoothSocket socket, String socketType ) {
            log_debug( "create ConnectedThread: " + socketType );
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                log_error( "temp sockets not created" );
                if (D) e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        /**
         * run
         */
        public void run() {
            log_info( "BEGIN ConnectedThread" );

             // note: add for multi buffers
            byte[][] buffers = new byte[ mRecvBufferPlane ][ mRecvBufferByte ];
            byte[] buf = new byte[ mRecvBufferByte ];
            int plane = 0;
            int length = 0;

            // Keep listening to the InputStream while connected
            while (true) {
                // note: add for multi buffers
                // set from multi buffer
                buf = buffers[ plane ];
                // change plane for next
                plane ++;
                if ( plane >= mRecvBufferPlane ) {
                    plane = 0;
                }
                // try to read
                length = 0;
                try {
                    // Read from the InputStream
                    length = mmInStream.read( buf );
                } catch (IOException e) {
                    log_error( "disconnected" );
                    if (D) e.printStackTrace();
                    connectionLost();
                    // note: move to manager
                    // Start the service over to restart listening mode
//                    BtService.this.start();
                    break;
                }
                if ( length == 0 ) continue;
                // note: add for debug
                if ( isDebugRead ) {
                    log_bytes( "r", buf, length );
                }
                // Send the obtained bytes to the UI Activity
                mHandler.obtainMessage(
                    WHAT_READ, length, ARG2_NONE, buf )
                    .sendToTarget();
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            log_debug( "ConnectedThread write" );            
            // note: add for debug
            if ( isDebugWrite ) {
                log_bytes( "w", buffer, buffer.length );
            }
            // try to write
            try {
                mmOutStream.write(buffer);
                if ( isHandlerWrite ) {
                    // Share the sent message back to the UI Activity
                    mHandler.obtainMessage(
                        WHAT_WRITE, ARG1_NONE, ARG2_NONE, buffer )
                        .sendToTarget();
                }
            } catch (IOException e) {
                log_error( "Exception during write" );
                if (D) e.printStackTrace();
            }
        }

        /**
         * cancel
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                log_error( "close() of connect socket failed" );
                if (D) e.printStackTrace();
            }
        }
    }

// --- debug ---
    /**
     * setHandlerWrite
     * @param boolean flag
     */	
    public void setHandlerWrite( boolean flag ) {
        isHandlerWrite = flag;
    }
 
   /**
     * setDebugService
     * @param boolean flag
     */	
    public void setDebugService( boolean flag ) {
        isDebugService = flag;
    }

   /**
     * setDebugWrite
     * @param boolean flag
     */	
    public void setDebugWrite( boolean flag ) {
        isDebugWrite = flag;
    }

   /**
     * setDebugRead
     * @param boolean flag
     */	
    public void setDebugRead( boolean flag ) {
        isDebugRead = flag;
    }

    /**
     * log_bytes for debug
     * @param String str
     * @param byte[] bytes
     * @param int length
     */
    private void log_bytes( String str, byte[] bytes, int length ) {
        String msg = str + " ";
        for ( int i=0; i<length ; i++ ) {
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
        if (isDebugService) log_d( msg );
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
        if (isDebugService) {
            Log.i( 
                BtConstant.TAG, 
                TAG_SUB + BtConstant.LOG_COLON + msg );
        }
    }

    /**
     * write log
     * @param String msg
     */ 
    private void log_error( String msg ) {
        if (D) {
            Log.e( 
                BtConstant.TAG, 
                TAG_SUB + BtConstant.LOG_COLON + msg );
        }
    }

}
