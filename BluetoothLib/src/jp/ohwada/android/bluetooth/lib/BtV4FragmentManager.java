/**
 * 2015-03-01 K.OHWADA
 */ 

package jp.ohwada.android.bluetooth.lib;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Bluetooth Manager for v4 Fragment 
 */
public class BtV4FragmentManager extends BtManager {

    // class
    private FragmentActivity mActivity;
    private Fragment mFragment;

    /*
     * === Constractor ===
     * @param Fragment fragment
     */  
    public BtV4FragmentManager( Fragment fragment ) {
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
