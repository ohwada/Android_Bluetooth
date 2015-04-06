/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/**
 * SettingsActivity
 */
public class BtSettingsActivity extends PreferenceActivity {			
    /**
     * === onCreate ===
     * @param savedInstanceState Bundle
     */
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        execCreate();
    }

    /**
     * execCreate
     */
    private void execCreate() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        SettingsFragment fragment = new SettingsFragment();
        ft.replace( android.R.id.content, fragment );	
        ft.commit();
        setResult( Activity.RESULT_OK );
    }

    /**
     * SettingsFragment
     */
    private class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate( Bundle savedInstanceState ) {
            super.onCreate( savedInstanceState );
            addPreferencesFromResource( R.xml.bt_settings );
        }
    }	
}