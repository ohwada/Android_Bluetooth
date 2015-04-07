/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * SettingsActivity
 */

/**
 * SettingsFragment
 */
public class BtSettingsFragment extends PreferenceFragment {
 
   private int mResId = 0;

    /*
     * === Constractor ===
     * @param int res_id
     */
    public BtSettingsFragment( int res_id ) {
        super();
        mResId = res_id;
    }

    /**
     * === onCreate ===
     */
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        addPreferencesFromResource( mResId );
    }	
}