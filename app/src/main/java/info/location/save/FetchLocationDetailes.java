package info.location.save;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;
import java.util.Locale;

/**
 * Created by ashrafiqubal on 06/01/17.
 */

public class FetchLocationDetailes extends AsyncTask<String, Void, Boolean> {
    private static final String TAG = "FetchLocationDetailes";
    @Override
    protected Boolean doInBackground(String... params) {
        // TODO Auto-generated method stub
        Boolean prepared;
        try {
            MainActivity.mInstance.getLocation();
            Geocoder gcd = new Geocoder(MainActivity.context, Locale.getDefault());
            try{
                List<Address> addresses = gcd.getFromLocation(MainActivity.mInstance.latitude, MainActivity.mInstance.longitude, 1);
                MainActivity.mInstance.addressline = addresses.get(0).getAddressLine(0)+", "+addresses.get(0).getAddressLine(1);
            }catch (Exception e){
                Log.d(TAG,"Error: "+e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error: Do in background "+e.getMessage());
            //Toast.makeText(context,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        prepared = true;
        return prepared;
    }
    @Override
    protected void onPostExecute(Boolean result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        Log.d(TAG, "onPostExecution");
        if(MainActivity.shouldShowDialogBoxAddress){
            MainActivity.mInstance.cancelProgressDialog();
            MainActivity.mInstance.displayDialogForAddress();
        }
    }
}
