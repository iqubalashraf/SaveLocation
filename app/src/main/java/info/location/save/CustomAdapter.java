package info.location.save;

import android.support.v7.widget.RecyclerView;

/**
 * Created by ashrafiqubal on 05/01/17.
 */

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import info.location.save.sqldatabase.DatabaseHandler;

import static info.location.save.MainActivity.context;

/**
 * Created by ashrafiqubal on 18/06/16.
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private static final String TAG = "CustomAdapter";

    private static String[] mTitleSet;
    private static String[] mAddressSet;
    private static String[] mLatitudeSet;
    private static String[] mLongitudeSet;
    private static int[] mIdSet;


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnCreateContextMenuListener {
        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick " + getPosition() + " " + mIdSet[getPosition()]);
            /*String[] locationLatLong = new String[2];
            DatabaseHandler db = new DatabaseHandler(context);
            locationLatLong = db.getLocation(mIdSet[getPosition()]);
            Log.d(TAG,locationLatLong[0]+"  "+locationLatLong[1]);
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f", Double.parseDouble(locationLatLong[0]), Double.parseDouble(locationLatLong[1]));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            context.startActivity(intent);*/
            Toast.makeText(MainActivity.context,"Long press for more option",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(mTitleSet[getPosition()]);
            menu.add(0, mIdSet[getAdapterPosition()], 1, "View Location");
            menu.add(0, mIdSet[getAdapterPosition()], 2, "Get Direction");
            menu.add(0, mIdSet[getAdapterPosition()], 3, "Share Location");
            menu.add(0, mIdSet[getAdapterPosition()], 4, "Delete Location");//groupId, itemId, order, title
        }
    }

    public class AddressViewHolder extends ViewHolder{
        TextView title,address,latitude,longitude;
        public AddressViewHolder(View view){
            super(view);
            this.title = (TextView)view.findViewById(R.id.title);
            this.address = (TextView)view.findViewById(R.id.full_address);
            this.latitude = (TextView)view.findViewById(R.id.latitude);
            this.longitude = (TextView)view.findViewById(R.id.longitude);
        }
    }
    public CustomAdapter(String[] titleSet, String[] addressSet, int[] idSet,String[] latitudeSet,String[] longitudeSet) {
        mTitleSet = titleSet;
        mAddressSet = addressSet;
        mIdSet = idSet;
        mLatitudeSet=latitudeSet;
        mLongitudeSet=longitudeSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        v = LayoutInflater.from((viewGroup.getContext())).inflate(R.layout.cardview_address,viewGroup,false);
        return new AddressViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            AddressViewHolder holder = (AddressViewHolder)viewHolder;
            holder.title.setText(mTitleSet[position]);
            holder.address.setText(mAddressSet[position]);
            holder.latitude.setText(mLatitudeSet[position]);
            holder.longitude.setText(mLongitudeSet[position]);
    }

    @Override
    public int getItemCount() {
        return mTitleSet.length;
    }

    @Override
    public int getItemViewType(int position) {
        return mIdSet[position];
    }
}
