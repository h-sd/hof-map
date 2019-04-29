package com.example.myapplication;

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.app.SearchManager;
import android.content.Context;
import android.widget.Toast;


import com.example.myapplication.mod1.*;
import com.example.myapplication.mod3.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.InputStream;
import java.util.*;


public class drawer extends AppCompatActivity implements OnMapReadyCallback {

    private final int RESULT_CODE_1 = 1;
    private DrawerLayout drawerLayout;
    private NodeList data;
    GoogleMap mMap;
    private ArrayList<Marker> mMarker = new ArrayList<Marker>();
    private Polyline route;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        if (googleServiceAvailable())
        {
            Snackbar.make(findViewById(R.id.drawer_layout), "Good to Start", Snackbar.LENGTH_SHORT).show();
            initMap();
        }

        data = getData();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);


        /*
        *
        * * Run
        *
         */




        handleIntent(getIntent());
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        menuItem.setChecked(true);

                        drawerLayout.closeDrawers();
                        Intent startIntent;
                        switch (menuItem.getItemId())
                        {
                            case R.id.nav_setting:
                                startIntent = new Intent(getApplicationContext(), Setting.class);
                                startActivity(startIntent);
                                break;

                            case R.id.nav_place:
                                startIntent = new Intent(getApplicationContext(), Location.class);
                                startActivity(startIntent);
                                break;

                            case R.id.nav_help:
                                startIntent = new Intent(getApplicationContext(), tutorial.class);
                                startActivity(startIntent);
                        }

                        return true;
                    }
                }
        );

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.location_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.location_search);

        //Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.location_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    /*
      Search Bar Intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }
    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }
    }


    //Search view
    private void doSearch(String query)
    {
        if(query == null)
        {
            Snackbar.make(findViewById(R.id.drawer_layout),"Null Location, errors", Snackbar.LENGTH_SHORT);
            return;
        } else if (data.searchStruct(query) == null)
        {
            Snackbar.make(findViewById(R.id.drawer_layout),"No such Location", Snackbar.LENGTH_SHORT);
            return;
        }
        LatLng pos = new LatLng(data.searchStruct(query).getPos().getLat(),data.searchStruct(query).getPos().getLon());
        removeEverything();
        System.out.println(pos.latitude + " " + pos.longitude);
        setMarker(pos.latitude,pos.longitude);
}

    /*
     Init Data
     */
    private NodeList getData()
    {
        InputStream h = getResources().openRawResource(R.raw.node_hor);
        InputStream v = getResources().openRawResource(R.raw.node_ver);
        InputStream s = getResources().openRawResource(R.raw.struct);

        NodeList jsondata = new NodeList(h,v,s);
        return jsondata;
    }

    /*
    * Map Setup
     */
     private void initMap()
     {
         MapFragment mapFragment = (MapFragment)getFragmentManager()
                 .findFragmentById(R.id.map_view);
         mapFragment.getMapAsync(this);
     }

     private boolean googleServiceAvailable()
     {
         GoogleApiAvailability api = GoogleApiAvailability.getInstance();
         int isAvailable = api.isGooglePlayServicesAvailable(this);
         if(isAvailable == ConnectionResult.SUCCESS)
         {
             return true;
         } else if (api.isUserResolvableError(isAvailable))
         {
             Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
             dialog.show();
         } else
         {
             Toast.makeText(this, "Cannot Connect to Play Service", Toast.LENGTH_SHORT).show();
         }
         return false;
     }

     @Override
     public void onMapReady(GoogleMap googleMap) {
         this.mMap = googleMap;
         System.out.println(mMap == null);

         /* LatLng origin = new LatLng(data.find(R.string.hofstra_hall).getCoord().
         getLat(), data.find(R.string.hofstra_hall).getCoord().getLon()); */
         LatLng origin = new LatLng(40.71404307257277, -73.6003861264611);

         //mMap.addMarker(new MarkerOptions().position(origin));

         this.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 17.5f));
         drawPolyLine("Adams Hall", "Weed Hall");
     }

     //Internal function for drawPolyLine
     private ArrayList<LatLng> searchRoute(Struct start, Struct end)
     {
         //Search for the points
         AstarSearch astarSearch = new AstarSearch();
         astarSearch.search(start, end);
         LinkedList<Node> route = astarSearch.route_to_node();
         ArrayList<LatLng> result_path = new ArrayList<LatLng>();

         if(route == null || route.size() == 0 )
         {
             return null;
         }

         for(int index = 0; index <route.size(); index++)
         {
             LatLng pts = new LatLng(route.get(index).getCoord().getLat(),route.get(index).getCoord().getLon());
             result_path.add(pts);
         }

         return result_path;
     }

     //Display Route when two struct is found
     //Place Two Marker, one for start, and one for end
     public void drawPolyLine(String start, String end)
     {
         Struct startpt = data.searchStruct(start);
         Struct endpt = data.searchStruct(end);

         //Construct the Route
         if(searchRoute(startpt, endpt) == null)
         {
             Snackbar.make(findViewById(R.id.drawer_layout), "Couldn't Find Location. Try again.", Snackbar.LENGTH_SHORT);
             return;
         }
         ArrayList<LatLng> routeCoord = searchRoute(startpt, endpt);
         removeEverything();
         route = mMap.addPolyline(new PolylineOptions()
            .addAll(routeCoord));
     }

     //Move cam to new location and zoom
     public void goToLocationandZoom(double lat, double lng, float zoom)
     {
         LatLng mPosition = new LatLng(lat, lng);
         CameraUpdate updatePosition = CameraUpdateFactory.newLatLngZoom(mPosition, zoom);
         mMap.moveCamera(updatePosition);
     }

     //Set one marker at lat and lng
     public void setMarker(double lat, double lng)
     {
         LatLng thisLatLng = new LatLng(lat,lng);
         //remove all marker is there are 2 or more markers currently present

         MarkerOptions newOption = new MarkerOptions()
                 .position(thisLatLng);

         if (this.mMap == null)
         {
             System.out.println(this.mMap == null);
         }

         this.mMap.addMarker(newOption);
         //mMarker.add(newOption);
     }


     //Default Destructor for All Markers
     public void removeEverything()
     {
         for (Marker marker : mMarker)
         {
             marker.remove();
         }
         mMarker.clear();
         if (route != null)
         {
             route.remove();
         }
     }


     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent)
     {
         super.onActivityResult(requestCode, resultCode, dataIntent);
         switch (requestCode)
         {
             case RESULT_CODE_1:
                 String dataRe = dataIntent.getStringExtra("location"); //received string data, need to find the same data in Data to get lat and lng
                 LocationData latLng = data.searchStruct(dataRe).getPos();
                 setMarker(latLng.getLat(),latLng.getLon());
                 break;
         }
     }

     public List<String> divide_input(String in, char div){
         String temp = in;
         LinkedList<String> list = new LinkedList<String>();

         //Messy Divide
         while( !(temp.indexOf(div, 1) < 0) ){
             int iDiv = in.indexOf(div, 1);

             list.add(temp.substring(0, iDiv));
             temp = temp.substring(iDiv);
         }

         //Clean Entries
         if(true){
             int i = 0;
             for(String s : list){
                 if(s.charAt(0) == div && s.length() > 1){
                     s = s.substring(1);
                 }
                 s = s.trim();
                 list.set(i, s);
                 i++;
             }
         }

         return list;
     }
}
