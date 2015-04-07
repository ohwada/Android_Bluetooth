/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.arduino1;

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
    private ArduinoView mArduinoView;
	
    /**
     * === onCreate ===
     */
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        bt_init();
        bt_setDebug( D );

        /* Initialization of Bluetooth */
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

        /* ArduinoView */
        mArduinoView = new ArduinoView( this );
        mArduinoView.initLabel( R.string.label_on, R.string.label_off );
        mArduinoView.initView( bt_getContentView() );
        mArduinoView.initAdapter( R.layout.report, R.id.TextView_report );
        mArduinoView.initTextViewSwitch( R.id.TextView_switch );
        mArduinoView.initGraphView( R.id.GraphView  );
        mArduinoView.initButtonLed( R.id.Button_led );					
        mArduinoView.initSeekBarPwm( R.id.SeekBar_pwm );		
        mArduinoView.initListViewReport( R.id.ListView_report );	
        mArduinoView.setOnChangedListener( new ArduinoView.OnChangedListener() {
            @Override
            public void onSend( String str ) {
                bt_send( str );
            }
        });
	
        mArduinoView.showGraph();
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
    public void onResume() {
        super.onResume();
        bt_startService();
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
        switch ( item.getItemId() ) {
            case R.id.menu_graph:
                mArduinoView.showGraph();
                return true;
            case R.id.menu_log:
                mArduinoView.showLog();
                return true;
        }
        bt_execOptionsItemSelectedSecure( item );
        return true;
    }

    /**
     * === onActivityResult ===
     */
    @Override
    public void onActivityResult( int request, int result, Intent data ) {
        bt_execActivityResult( request, result, data );
    }

// --- comand ---
    /**
     * execRead
     * @param List<String> list
     */
    protected void bt_execRead( List<String> list ) {
        mArduinoView.execRead( list );
    }
    
}
