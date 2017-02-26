package com.santhosh.smarttheatre;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static String API_KEY = "MOVIESDB_API_KEY";
    List<MovieData> movieHolderList = new ArrayList<>();
    MovieRecycleAdapter movieRecycleAdapter;
    boolean isLoading = false;
    int currentPage = 1;
    String type = "popular";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.movie_list);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);

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
        getPopularRequest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int ID= item.getItemId();
        if (ID==R.id.top_rated){
            movieHolderList = new ArrayList<>();
            currentPage=1;
            type = "top_rated";
            getTopRated();
        }else if (ID==R.id.popular){
            movieHolderList = new ArrayList<>();
            currentPage=1;
            type = "popular";
            getPopularRequest();
        }
        return super.onOptionsItemSelected(item);
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
                Log.v("error_reposnse","here "+error.toString());
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
}
