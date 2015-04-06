/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

/**
 * Bluetooth Manager for v4 Fragment 
 */
public class BtFragmentManager extends BtManager {

    // class
    private Activity mActivity;
    private Fragment mFragment;

    /*
     * === Constractor ===
     * @param Fragment fragment
     */  
    public BtFragmentManager( Fragment fragment ) {
        super( fragment.getActivity() );
        mActivity = fragment.getActivity();
        mFragment = fragment;
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
        mFragment.startActivityForResult( intent, request );
    }

}
