package com.novus.navigo.uihelper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.novus.navigo.R;
import com.novus.navigo.ViewPlaceDetailsActivity;
import com.novus.navigo.model.Location;
import com.novus.navigo.model.Place;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sahajbedi on 11-Jan-16.
 */
public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> implements ItemTouchHelperAdapter {

    List<Place> places;
    private final OnStartDragListener mDragStartListener;
    private final static String TAG = "PlaceAdapter";
    private Context context;

    public PlaceAdapter(List<Place> places, Context context, OnStartDragListener dragStartListener) {
        this.places = new ArrayList<Place>();
        this.places = places;
        this.context = context;
        mDragStartListener = dragStartListener;
    }

    @Override
    public PlaceAdapter.PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        PlaceViewHolder pvh = new PlaceViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final PlaceAdapter.PlaceViewHolder holder, final int position) {
        Picasso.with(context).load(places.get(position).getIcon()).into(holder.icon);
        holder.placeName.setText(places.get(position).getName());
        holder.placeAddress.setText(places.get(position).getVicinity());
        Location destinationLocation = places.get(position).getGeometry().getLocation();
        holder.placeDistance.setText(getDistance(destinationLocation));
        holder.placeToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Star clicked at position "+position);
            }
        });
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked on item : " + places.get(position));
                Intent viewDetailsIntent = new Intent(context, ViewPlaceDetailsActivity.class);
                viewDetailsIntent.putExtra("id", places.get(position).getPlace_id());
                context.startActivity(viewDetailsIntent);
            }
        });
        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "Long click");
                return true;
            }
        });
        holder.layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    private String getDistance(Location destinationLocation) {
        android.location.Location origin = new android.location.Location("Origin");
        origin.setLatitude(42.953092);
        origin.setLongitude(-78.825522);
        android.location.Location destination = new android.location.Location("Destination");
        origin.setLatitude(destinationLocation.getLat());
        origin.setLongitude(destinationLocation.getLng());

        float distance = origin.distanceTo(destination);
        //Can access preference to show in miles/km/m
        String dist = distance + " M";

        if (distance > 1000.0f) {
            distance = distance / 1000.0f;
            dist = distance + " KM";
        }

        return dist;

    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public void refreshList(List<Place> places){
        this.places = places;
        notifyDataSetChanged();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(places, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        places.remove(position);
        notifyDataSetChanged();
        notifyItemRemoved(position);
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        CardView cv;
        ImageView icon;
        TextView placeName;
        TextView placeAddress;
        TextView placeDistance;
        ToggleButton placeToggleButton;
        RelativeLayout layout;

        PlaceViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.placesCardView);
            icon = (ImageView) itemView.findViewById(R.id.placeIcon);
            placeName = (TextView) itemView.findViewById(R.id.placeName);
            placeAddress = (TextView) itemView.findViewById(R.id.placeAddress);
            placeDistance = (TextView) itemView.findViewById(R.id.placeDistance);
            placeToggleButton = (ToggleButton)itemView.findViewById(R.id.placeToggle);
            layout = (RelativeLayout) itemView.findViewById(R.id.placeLayout);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}
