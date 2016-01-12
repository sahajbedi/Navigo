package com.novus.navigo;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.yasevich.endlessrecyclerview.EndlessRecyclerView;
import com.novus.navigo.interfaces.PlacesAPI;
import com.novus.navigo.model.Place;
import com.novus.navigo.model.PlacesList;
import com.novus.navigo.services.LocationService;
import com.novus.navigo.uihelper.OnStartDragListener;
import com.novus.navigo.uihelper.PlaceAdapter;
import com.novus.navigo.uihelper.SimpleItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class PlaceFragment extends android.support.v4.app.Fragment implements OnStartDragListener, Callback<PlacesList>, EndlessRecyclerView.Pager {

    private List<Place> places;
    private ItemTouchHelper mItemTouchHelper;
    private final String TAG = "PlaceFragment";
    PlaceAdapter adapter;
    SwipeRefreshLayout swipeLayout;

    private CompositeSubscription mCompositeSubscription;
    LocationService locationService;
    Observable<android.location.Location> fetchDataObservable;
    Subscriber<android.location.Location> subscriber;
    private boolean activityStarted = false;
    private static final long LOCATION_TIMEOUT_SECONDS = 50;

    private EndlessRecyclerView recyclerView;
    private boolean loading = false;
    private static final int ITEMS_ON_PAGE = 9;
    private static final int TOTAL_PAGES = 10;
    private static final long DELAY = 1000L;
    private final Handler handler = new Handler();

    private android.location.Location userLocation;
    private String nextPageToken;
    private boolean gettingNextPage = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        final LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationService = new LocationService(locationManager);
        subscriber = getLocationSubscriber();
        userLocation = new android.location.Location("User Location");
        nextPageToken = new String();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_place, container, false);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.placeSwipeRefreshLayout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "Refreshing");
                swipeLayout.setRefreshing(false);
            }
        });

        recyclerView = (EndlessRecyclerView) rootView.findViewById(android.R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setProgressView(R.layout.item_progress);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!activityStarted) {
            Log.d(TAG, "Onresume subscribing again");
            mCompositeSubscription = new CompositeSubscription();
            fetchDataObservable = locationService.getLocation()
                    .timeout(LOCATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            mCompositeSubscription.add(fetchDataObservable
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber));
            activityStarted = true;
        }
    }

    @Override
        public void onResponse(Response<PlacesList> response, Retrofit retrofit) {
        int statusCode = response.code();
        String message = response.message();
        boolean isSuccess = response.isSuccess();
        Log.d(TAG, "Response received");
        Log.d(TAG, "Status Code : " + statusCode);
        Log.d(TAG, "Message : " + message);
        Log.d(TAG, "Success : " + isSuccess);

        if (statusCode != 200)   //Handle error
            return;
        if (response == null)    //Response is null
            return;
        String status = response.body().getStatus();
        if (status.equals("OVER_QUERY_LIMIT")) {
            Log.d(TAG, "Query Limit exceeded");
            return;
        } else if (status.equals("REQUEST_DENIED")) {       //Invalid key parameter
            Log.d(TAG, "Request Denied");
            return;
        } else if (status.equals("INVALID_REQUEST")) {      //Parameter like location or radius was missing
            if (gettingNextPage) {
                Log.d(TAG, "Invalid Request. Try after a bit");
                return;
            } else {
                Log.d(TAG, "Invalid Request");
                return;
            }
        }
        if (status.equals("ZERO_RESULTS")) {
            Log.d(TAG, "No results returned");
            return;
        }
        String nextPageToken = response.body().getNext_page_token();

        this.nextPageToken = nextPageToken;
        Log.d(TAG, "Status : " + status);
        Log.d(TAG, "Next page token : " + nextPageToken);
        ArrayList<Place> list = (ArrayList) response.body().getResults();
        if (list == null) {
            Log.d(TAG, "Null list");
            return;
        } else if (list.size() == 0) {
            Log.d(TAG, "No results returned");
            return;
        }
        if (!gettingNextPage) {       //First time results
            this.places = list;
            adapter = new PlaceAdapter(list, getActivity(), this);
            recyclerView.setAdapter(adapter);
            recyclerView.setPager(this);
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
            mItemTouchHelper = new ItemTouchHelper(callback);
            mItemTouchHelper.attachToRecyclerView(recyclerView);
            for (int i = 0; i < list.size(); i++)
                Log.d(TAG, list.get(i).toString());

        } else {
            for (int i = 0; i < list.size(); i++)
                places.add(list.get(i));
            adapter.refreshList(places);
        }
    }

    @Override
    public void onFailure(Throwable t) {
        Log.d(TAG, "Failure");
        Log.d(TAG, "Message : " + t.getMessage());
        Log.d(TAG, "toString() : " + t.getMessage());
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public boolean shouldLoad() {
        return !loading && adapter.getItemCount() / ITEMS_ON_PAGE < TOTAL_PAGES;
    }

    @Override
    public void loadNextPage() {
        if (nextPageToken == null || nextPageToken.equals("")) {
            Log.d(TAG, "nextPageToken is null. End of results.");
            return;
        }

        loading = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.setRefreshing(false);
                loading = false;
                addItems();
            }
        }, DELAY);
    }

    private void addItems() {
        Log.d(TAG, "Adding more items");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        PlacesAPI stackOverflowAPI = retrofit.create(PlacesAPI.class);
        gettingNextPage = true;
        Call<PlacesList> call = stackOverflowAPI.getNextPage(userLocation.getLatitude() + "," + userLocation.getLongitude(), nextPageToken);
        //asynchronous call
        call.enqueue(this);
    }

    private void initializeData(android.location.Location location) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        PlacesAPI stackOverflowAPI = retrofit.create(PlacesAPI.class);

        Call<PlacesList> call = stackOverflowAPI.getPlacesWithLocation(location.getLatitude() + "," + location.getLongitude());
        //asynchronous call
        call.enqueue(this);
    }

    public Subscriber<android.location.Location> getLocationSubscriber() {
        return new Subscriber<android.location.Location>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "Finished getting locations");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError Message : " + e.getMessage());
            }

            @Override
            public void onNext(android.location.Location location) {
                Log.d(TAG, "Location : " + location.getLatitude() + ", " + location.getLongitude());
                userLocation = location;
                initializeData(location);
            }
        };
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mCompositeSubscription.unsubscribe();
        super.onDestroy();

    }
}