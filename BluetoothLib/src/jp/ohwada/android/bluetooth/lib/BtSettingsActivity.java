/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.preference.PreferenceActivity;

/**
 * SettingsActivity
 */
public class BtSettingsActivity extends PreferenceActivity {			

    /**
     * execCreate
     */
    protected void execCreate() {
        execCreate( R.xml.bt_settings );
    }

    /**
     * execCreate
     */
    protected void execCreate( int res_id ) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        BtSettingsFragment fragment = new BtSettingsFragment( res_id );
        ft.replace( android.R.id.content, fragment );	
        ft.commit();
        setResult( Activity.RESULT_OK );
    }

}