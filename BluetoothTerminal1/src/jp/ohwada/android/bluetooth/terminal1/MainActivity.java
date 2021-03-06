/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.terminal1;

import java.util.List;

import jp.ohwada.android.bluetooth.lib.BtActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Main Activity
 */
public class MainActivity extends BtActivity {

    // Debugging
    private static final boolean D = true;

    // UI
    private TerminalView mTerminalView;
    
    /**
     * === onCreate ===
     */
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        bt_init();
        bt_setDebug( D );
 
        /* bluetooth */
        bt_initManager();
        bt_initContentView( R.layout.activity_main );
        bt_initLinearLayoutConnect( R.id.LinearLayout_connect );
        bt_initTextViewConnect( R.id.TextView_connect );
        bt_initButtonConnectSecure( R.id.Button_connect_secure );
        bt_initScrollViewDebug( R.id.ScrollView_debug );
        bt_initTextViewDebug( R.id.TextView_debug );
        bt_initDeviceListClass( DeviceListActivity.class );
        bt_initSettingsClass( SettingsActivity.class );
        bt_initAdapterAndFinish();

        /* TerminalView */
        mTerminalView = new TerminalView( this );
        mTerminalView.initView( bt_getContentView() );
        mTerminalView.initAdapter( R.layout.report, R.id.TextView_report );
        mTerminalView.initEditTextSend( R.id.EditText_send );
        mTerminalView.initButtonSend( R.id.Button_send );
        mTerminalView.initListViewReport( R.id.ListView_report );
        mTerminalView.setOnChangedListener( new TerminalView.OnChangedListener() {
            @Override
            public void onSend( String str ) {
                bt_send( str );
            }
        });
    }

    /**
     * === onStart ===
     */
    @Override
    public void onStart() {
        super.onStart();
        bt_enableService();	
    }

    /**
     * === onResume ===
     */
    @Override
    public synchronized void onResume() {
        super.onResume();
        bt_startService();
        mTerminalView.refreshPref();
    }

    /**
     * === onPause ===
     */
    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    /**
     * === onDestroy ===
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        bt_stopService();
    }

    /**
     * === onCreateOptionsMenu ===
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }

    /**
     * === onOptionsItemSelected ===
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();
        if ( id == R.id.menu_clean ) {   
            mTerminalView.cleanTerminal();
            return true;
        }
        bt_execOptionsItemSelected( item ); 
        return true;
    }

    /**
     * === onActivityResult ===
     */
    @Override
    public void onActivityResult( int request, int result, Intent data ) {
        bt_execActivityResultAndFinish( request, result, data );
    }

// --- comand ---
    /**
     * execRecv
     * @param byte[] bytes
     */
    protected void bt_execRead( byte[] bytes ) {
        mTerminalView.execRead( bytes );
    }

    /**
     * execRecv
     * @param List<String> list
     */
    protected void bt_execRead( List<String> list ) {
        mTerminalView.execRead( list );
    }
}
