/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;

/**
 * Bluetooth Manager for Activity 
 */
public class BtActivityManager extends BtManager {

    // class
    private Activity mActivity;
    
    /*
     * === Constractor ===
     * @param Activity activity
     */  
    public BtActivityManager( Activity activity ) {
        super( activity );
        mActivity = activity;
    }

    /*
     * setTitleStatus (overwrite)
     * @param CharSequence subTitle
    */
    protected void setTitleStatus( CharSequence subTitle ) {
        log_debug( "setTitleStatus " + subTitle );
        if ( !isTitleUse ) return;
        if ( mActivity == null ) return;
        final ActionBar actionBar = mActivity.getActionBar();
        if ( actionBar == null ) return;
        actionBar.setSubtitle( subTitle );
    }

    /**
     * startActivityForResult (overwrite)
     */
    protected void startActivityForResult( Intent intent, int request ) {
        mActivity.startActivityForResult( intent, request );
    }

}
