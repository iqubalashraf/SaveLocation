package info.location.save.sqldatabase;

/**
 * Created by ashrafiqubal on 05/01/17.
 */

public class LocationGetSet {
    //private variables
    int _id;
    String _latitude;
    String _longitude;
    String _address;

    // Empty constructor
    public LocationGetSet(){

    }

    // constructor
    public LocationGetSet(int id, String latitude, String longitude, String address){
        this._id = id;
        this._latitude = latitude;
        this._longitude = longitude;
        this._address = address;
    }

    // constructor
    public LocationGetSet( String latitude, String longitude, String address){
        this._latitude = latitude;
        this._longitude = longitude;
        this._address = address;
    }

    // getting ID
    public int getID(){
       return this._id;
    }

    // getting Latitude
    public String getLatitude(){
        return this._latitude;
    }

    // getting Longitude
    public String getLongitude(){
        return this._longitude;
    }

    // getting Address
    public String getAddress(){
        return this._address;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // setting Latitude
    public void setLatitude(String latitude){
        this._latitude = latitude;
    }

    // setting Longitude
    public void setLongitude(String longitude){
        this._longitude = longitude;
    }

    // setting Address
    public void setAddress(String address){
        this._address = address;
    }
}
