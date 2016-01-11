package com.novus.navigo.interfaces;

import com.novus.navigo.model.PlacesList;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by sahajbedi on 11-Jan-16.
 */
public interface PlacesAPI {
    @GET("/maps/api/place/search/json?location=42.953092,-78.825522&radius=50000&&types=museum&sensor=false&key=AIzaSyA3f1UU1RNDKEdIVoMAP9K9E23hCh9dfhY")
    Call<PlacesList> getPlaces();

    @GET("/maps/api/place/search/json?radius=50000&&types=museum&sensor=false&key=AIzaSyA3f1UU1RNDKEdIVoMAP9K9E23hCh9dfhY")
    Call<PlacesList> getPlacesWithLocation(@Query("location") String location);
}
