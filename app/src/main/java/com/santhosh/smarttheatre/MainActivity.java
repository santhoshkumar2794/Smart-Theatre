package com.santhosh.smarttheatre;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenu;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.santhosh.smarttheatre.database.FavouriteDataSet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static String API_KEY = BuildConfig.API_KEY;
    public static float deviceDensity;
    public static int screenWidth,screenHeight;

    List<MovieData> movieHolderList = new ArrayList<>();
    MovieRecycleAdapter movieRecycleAdapter;
    boolean isLoading = false;
    int currentPage = 1;
    String type = "popular";
    private FavouriteDataSet favouriteDataSet;

    @BindView(R.id.default_favourite_page)
    TextView statusTextView;
    @BindView(R.id.movie_list)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        BottomNavigationView bottomNavigationMenu = (BottomNavigationView) findViewById(R.id.bottom_bar);
        bottomNavigationMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        deviceDensity = getResources().getDisplayMetrics().density;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        int noOfCols = calculateNoOfColumns(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.movie_list);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, noOfCols);
        recyclerView.setLayoutManager(gridLayoutManager);
        movieRecycleAdapter = new MovieRecycleAdapter(20);
        recyclerView.setAdapter(movieRecycleAdapter);

        favouriteDataSet = new FavouriteDataSet(this);
        favouriteDataSet.openDB();
        favouriteDataSet.getFavList();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int count = gridLayoutManager.getItemCount();
                int lastItemIndex = gridLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && count <=(lastItemIndex+5)){
                    isLoading = true;
                    requestData();
                }
            }
        });
        if (checkConnection()) {
            statusTextView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            getPopularRequest();
        }
    }

    private boolean checkConnection(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float holderWidth = context.getResources().getDimension(R.dimen.default_holder_width)/displayMetrics.density;
        int noOfColumns = (int) (dpWidth / holderWidth);
        return noOfColumns;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int noOfCols = calculateNoOfColumns(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.movie_list);
        GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        gridLayoutManager.setSpanCount(noOfCols);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int ID= item.getItemId();
        if (ID==R.id.top_rated){
            if (checkConnection()) {
                statusTextView.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                movieHolderList = new ArrayList<>();
                currentPage=1;
                type = "top_rated";
                getTopRated();
            }else {
                recyclerView.setVisibility(View.INVISIBLE);
                statusTextView.setVisibility(View.VISIBLE);
                statusTextView.setText(R.string.network_unavailable);
            }
            return true;
        }else if (ID==R.id.popular){
            if (checkConnection()) {
                statusTextView.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                movieHolderList = new ArrayList<>();
                currentPage = 1;
                type = "popular";
                getPopularRequest();
            }else {
                recyclerView.setVisibility(View.INVISIBLE);
                statusTextView.setVisibility(View.VISIBLE);
                statusTextView.setText(R.string.network_unavailable);
            }
            return true;
        }else if (ID==R.id.favourites){
            type = "favourites";
            movieHolderList = favouriteDataSet.getFavList();
            if (movieHolderList.size()>0){
                movieRecycleAdapter = new MovieRecycleAdapter(movieHolderList);
                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.movie_list);
                recyclerView.setAdapter(movieRecycleAdapter);
                recyclerView.setVisibility(View.VISIBLE);
                statusTextView.setVisibility(View.INVISIBLE);
            }else {
                statusTextView.setVisibility(View.VISIBLE);
                statusTextView.setText(R.string.no_fav);
                recyclerView.setVisibility(View.INVISIBLE);
            }
            return true;
        }
        return false;
    }

    private void processJson(JSONObject jsonObject) throws Exception {
        int pageNumber = jsonObject.getInt("page");
        JSONArray jsonArray = jsonObject.optJSONArray("results");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject movieObject = jsonArray.getJSONObject(i);
            MovieData movieData = new Gson().fromJson(movieObject.toString(),MovieData.class);
            movieHolderList.add(movieData);
        }
        if (pageNumber == 1) {
            movieRecycleAdapter = new MovieRecycleAdapter(movieHolderList);
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.movie_list);
            recyclerView.setAdapter(movieRecycleAdapter);
        } else {
            movieRecycleAdapter.setMovieDataList(movieHolderList);
            movieRecycleAdapter.notifyDataSetChanged();
        }
        isLoading = false;
    }

    private void requestData(){
        ++currentPage;
        if (type.equals("popular")){
            getPopularRequest();
        }else if (type.equals("top_rated")){
            getTopRated();
        }
    }

    private void getTopRated(){
        String url = "http://api.themoviedb.org/3/movie/top_rated?api_key=" + API_KEY + "&page="+currentPage;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    processJson(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("error_response","here "+error.toString());
            }
        });

        queue.add(jsonObjectRequest);
    }

    private void getPopularRequest() {
        String url = "http://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY + "&page="+currentPage;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    processJson(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        favouriteDataSet.openDB();
        if (type.equals("favourites")){
            movieHolderList = favouriteDataSet.getFavList();
            movieRecycleAdapter.setMovieDataList(movieHolderList);
            movieRecycleAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        favouriteDataSet.closeDB();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        favouriteDataSet.closeDB();
        super.onDestroy();
    }
}
