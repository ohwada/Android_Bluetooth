/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.tester1;

import jp.ohwada.android.bluetooth.lib.BtSettingsActivity;
import android.os.Bundle;

/**
 * SettingsActivity
 */
public class SettingsActivity extends BtSettingsActivity {	

    /**
     * === onCreate ===
     * @param savedInstanceState Bundle
     */
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        execCreate();
    }

}
