package com.myapplication.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.myapplication.utils.GridRecyclerView;
import com.myapplication.database.PrevRequestdb;
import com.myapplication.R;
import com.myapplication.utils.SharedPref;
import com.myapplication.adapters.GridAdapter;
import com.myapplication.models.Picture;
import com.myapplication.ui.MainActivity;
import com.myapplication.utils.EndlessScrollListener;
import com.myapplication.utils.Url;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.myapplication.ui.MainActivity.list;

/**
 * A fragment for displaying a grid of images.
 */
public class GridFragment extends Fragment {
    private String TAG = "GRADFRAG";
    private GridRecyclerView recyclerView;
    private EditText etSearch;
    private Button btnSearch;
    private SharedPref pref;
    private GridAdapter adapter;
    private int currentpage = 0;
    private List<Picture> mylist;
    private PrevRequestdb db;
    private View v;
    private boolean isConnected;
    private EndlessScrollListener scrolllistner;
    private String query;
    private ProgressDialog dialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.gridfrag, container, false);
        setHasOptionsMenu(true);
        Toolbar toolbar = v.findViewById(R.id.toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        recyclerView = v.findViewById(R.id.recyclerview);
        etSearch = v.findViewById(R.id.etSearch);
        btnSearch = v.findViewById(R.id.btnSearch);
        pref = new SharedPref(getActivity());
        final int numcolumns = pref.getNumcolums();
        db = new PrevRequestdb(getActivity());
        if (((MainActivity) getActivity()).list != null) {
            List<Picture> list = ((MainActivity) getActivity()).list;
            adapter = new GridAdapter(GridFragment.this, list);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), numcolumns));
            prepareTransitions();
            postponeEnterTransition();
        }

        btnSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                query = etSearch.getText().toString().trim().replace(" ", "%20");
                if (query.length() > 0) {
                    if (getisconnected()) {
                        StringRequest request = new StringRequest(Request.Method.GET, String.format(Url.filckrapi, query, (currentpage + 1)), new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                db.addData((currentpage + 1), query, response);
                                dialog.dismiss();
                                renderrecyclerview(response, numcolumns, query);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                searchoffline(numcolumns, query);
                                dialog.dismiss();
                                error.printStackTrace();
                            }
                        });
                        request.setRetryPolicy(new DefaultRetryPolicy(
                                5000,
                                0,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        Volley.newRequestQueue(getActivity()).add(request);
                        dialog = new ProgressDialog(getActivity());
                        dialog.setMessage("Connecting...");
                        dialog.show();
                    } else {
                        searchoffline(numcolumns, query);
                    }
                }
            }
        });


        return v;
    }

    private void searchoffline(int numcolumns, String query) {
        Cursor c = db.getData(1, query);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            String response = c.getString(0);
            renderrecyclerview(response, numcolumns, query);
        } else {
            Snackbar.make(v, "No Internet Connection", Snackbar.LENGTH_LONG).setAction("Try Again", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onRefresh();
                }
            }).show();
        }
    }

    public boolean getisconnected() {
        isConnected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] arr = connectivityManager.getAllNetworkInfo();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].getState() == NetworkInfo.State.CONNECTED) {
                isConnected = true;
                break;
            }
        }
        return isConnected;
    }

    private void onRefresh() {
        if (!getisconnected()) {
            Snackbar.make(v, "No Internet Connection", Snackbar.LENGTH_LONG).setAction("Try Again", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onRefresh();
                }
            }).show();

        }
    }

    private void renderrecyclerview(String response, final int numcolumns, final String query) {
        mylist = parsedata(response);
        ((MainActivity) getActivity()).list = mylist;
        adapter = new GridAdapter(GridFragment.this, mylist);
        recyclerView.setAdapter(adapter);
        GridLayoutManager llm = new GridLayoutManager(getActivity(), numcolumns);
        recyclerView.setLayoutManager(llm);
        scrolllistner = new EndlessScrollListener(llm) {

            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (currentpage != page) {
                    Log.d(TAG, "onLoadMore: " + page + " " + totalItemsCount);
                    currentpage++;
                    if (getisconnected()) {
                        setRequest2(query);
                    } else {
                        searchoffline2(query);
                    }

                }
            }

        };
        recyclerView.addOnScrollListener(scrolllistner);

        prepareTransitions();
        postponeEnterTransition();
    }

    private void searchoffline2(String query) {
        Cursor c = db.getData((currentpage + 1), query);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            String response = c.getString(0);
            List<Picture> list2 = addata(response);
            ((MainActivity) getActivity()).list = list2;
            adapter.notifyDataSetChanged();

        }
    }

    private void setRequest2(final String query) {
        StringRequest request = new StringRequest(Request.Method.GET, String.format(Url.filckrapi, query, (currentpage + 1)), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                db.addData((currentpage + 1), query, response);
                List<Picture> list2 = addata(response);
                ((MainActivity) getActivity()).list = list2;
                adapter.notifyDataSetChanged();
                //Log.d("DATA", "onResponse: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                searchoffline2(query);
                error.printStackTrace();
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(getActivity()).add(request);
    }

    private List<Picture> addata(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            JSONObject obj1 = obj.getJSONObject("photos");
            JSONArray arr = obj1.getJSONArray("photo");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj2 = arr.getJSONObject(i);
                String farmId = obj2.getString("farm");
                String serverId = obj2.getString("server");
                String photoId = obj2.getString("id");
                String secret = obj2.getString("secret");
                //Log.d(TAG, "parsedata: "+"http://farm" + farmId + ".staticflickr.com/" + serverId + "/" + photoId + "_" + secret + ".jpg");
                Picture picture = new Picture("http://farm" + farmId + ".staticflickr.com/" + serverId + "/" + photoId + "_" + secret + ".jpg");
                mylist.add(picture);
            }
            return mylist;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (MainActivity.currentPosition != -1) {

            scrollToPosition();

        }
    }

    /**
     * Scrolls the recycler view to show the last viewed item in the grid. This is important when
     * navigating back from the grid.
     */
    private void scrollToPosition() {
        recyclerView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left,
                                       int top,
                                       int right,
                                       int bottom,
                                       int oldLeft,
                                       int oldTop,
                                       int oldRight,
                                       int oldBottom) {
                recyclerView.removeOnLayoutChangeListener(this);
                final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                View viewAtPosition = layoutManager.findViewByPosition(MainActivity.currentPosition);
                // Scroll to position if the view for the current position is null (not currently part of
                // layout manager children), or it's not completely visible.
                if (viewAtPosition == null || layoutManager
                        .isViewPartiallyVisible(viewAtPosition, false, true)) {
                    // recyclerView.post(() ->);
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            layoutManager.scrollToPosition(MainActivity.currentPosition);
                        }
                    });
                }
            }
        });
    }

    /**
     * Prepares the shared element transition to the pager fragment, as well as the other transitions
     * that affect the flow.
     */
    private void prepareTransitions() {
        setExitTransition(TransitionInflater.from(getContext())
                .inflateTransition(R.transition.grid_exit_transition));

        // A similar mapping is set at the ImagePagerFragment with a setEnterSharedElementCallback.
        setExitSharedElementCallback(
                new SharedElementCallback() {
                    @Override
                    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                        // Locate the ViewHolder for the clicked position.
                        RecyclerView.ViewHolder selectedViewHolder = recyclerView
                                .findViewHolderForAdapterPosition(MainActivity.currentPosition);
                        if (selectedViewHolder == null || selectedViewHolder.itemView == null) {
                            return;
                        }

                        // Map the first shared element name to the child ImageView.
                        sharedElements
                                .put(names.get(0), selectedViewHolder.itemView.findViewById(R.id.card_image));
                    }
                });
    }

    private List<Picture> parsedata(String response) {
        List<Picture> list = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(response);
            JSONObject obj1 = obj.getJSONObject("photos");
            JSONArray arr = obj1.getJSONArray("photo");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj2 = arr.getJSONObject(i);
                // Log.d(TAG, "parsedata: " + obj2.toString());
                String farmId = obj2.getString("farm");
                String serverId = obj2.getString("server");
                String photoId = obj2.getString("id");
                String secret = obj2.getString("secret");
                //Log.d(TAG, "parsedata: "+"http://farm" + farmId + ".staticflickr.com/" + serverId + "/" + photoId + "_" + secret + ".jpg");
                Picture picture = new Picture("http://farm" + farmId + ".staticflickr.com/" + serverId + "/" + photoId + "_" + secret + ".jpg");
                list.add(picture);
            }
            return list;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.col2:
                pref.setNumcolums(2);
                if (scrolllistner != null) {
                    GridLayoutManager llm = new GridLayoutManager(getActivity(), 2);
                    recyclerView.setLayoutManager(llm);
                    serscrolllistner(llm);
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.col3:
                pref.setNumcolums(3);
                if (scrolllistner != null) {
                    GridLayoutManager llm = new GridLayoutManager(getActivity(), 3);
                    recyclerView.setLayoutManager(llm);
                    serscrolllistner(llm);
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.col4:
                pref.setNumcolums(4);
                if (scrolllistner != null) {
                    GridLayoutManager llm = new GridLayoutManager(getActivity(), 4);
                    recyclerView.setLayoutManager(llm);
                    serscrolllistner(llm);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void serscrolllistner(GridLayoutManager llm) {
        scrolllistner = new EndlessScrollListener(llm) {

            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (currentpage != page) {
                    Log.d(TAG, "onLoadMore: " + page + " " + totalItemsCount);
                    currentpage++;
                    if (getisconnected()) {
                        setRequest2(query);
                    } else {
                        searchoffline2(query);
                    }

                }
            }

        };
        recyclerView.addOnScrollListener(scrolllistner);
    }

}
