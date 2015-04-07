package jp.ohwada.android.bluetooth.terminal1;

import java.io.UnsupportedEncodingException;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * adapter for Text
 */
public class TextAdapter extends ArrayAdapter<Text> {

   /** Debug */
    private static final boolean D = true;

    // mode
    public static final boolean SEND = true;
    public static final boolean RECV = false;

    // char	
    private static final String CHARSET_NAME = "UTF-8";

    // param
    private boolean isDisplayHex = false;

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    private int mResource;

    /**
     * If the inflated resource is not a TextView, {@link #mFieldId} is used to find
     * a TextView inside the inflated views hierarchy. This field must contain the
     * identifier that matches the one defined in the resource file.
     */
    private int mFieldId = 0;

    // Layout Inflater
    private LayoutInflater mInflater = null;
			
    /**
     * === constractor ===
     * @param Context context
     * @param int textViewResourceId
     * @param List<TextView> objects     
     * @return void	 
     */
    public TextAdapter( Context context, int resource, int textViewResourceId, List<Text> objects ) {
        super( context, 0, objects );
        mResource = resource;
        mFieldId = textViewResourceId;
        mInflater = (LayoutInflater) super.getContext().getSystemService( 
            Context.LAYOUT_INFLATER_SERVICE ) ;
    }

    /**
     * === get view ===
     * @param int position 
     * @param View convertView    
     * @param  ViewGroup parent      
     * @return View	 
     */
    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        View view = convertView;
        TextView textView;

        if (convertView == null) {
            view = mInflater.inflate( mResource, parent, false );
        } else {
            view = convertView;
        }

        try {
            //  Otherwise, find the TextView field within the layout
            textView = (TextView) view.findViewById(mFieldId);
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }
  
        // get item form Adapter
        Text item = (Text) getItem( position );
        // set value
        textView.setText( item.text ) ;
        textView.setTextColor( item.color ) ;
        return view;
    }

    /**
     * setDisplayHex
     */
    public void setDisplayHex( boolean flag ) {
        isDisplayHex = flag;
    }

    /**
     * add
     */
    public void add( boolean mode, byte[] bytes ) {
        setText( mode, bytesToString( bytes ) );
    }

    /**
     * add
     */
    public void add( boolean mode, String text ) {
        setText( mode, text );
    }

    /**
     * setText
     */
    private void setText( boolean mode, String text ) {
        if ( isDisplayHex ) {
            text = strToHex( text );
        }
        if ( mode ) {
            add( new Text( "> " + text, Color.BLUE ) );
        } else {
            add( new Text( "< " + text, Color.RED ) );
        }
    }

    /**
     * bytesToString
     */
    private String bytesToString( byte[] bytes ) {
        String str= "";
        try {
            str = new String( bytes, CHARSET_NAME );
        } catch ( UnsupportedEncodingException e) {
            if (D) e.printStackTrace();
        }
        return str;
    }

    /**
     * bytesToHex
     * @param String str
     */
    private String strToHex( String str ) {
        byte[] bytes = str.getBytes();
        String msg ="";
        for ( int i=0; i<bytes.length ; i++ ) {
            msg += String.format( "%02X", bytes[ i ] );
            msg += " ";
        }
        return msg;
    }

}
