package com.example.nofarcohenzedek.dogo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.nofarcohenzedek.dogo.Model.DogOwner;
import com.example.nofarcohenzedek.dogo.Model.Model;
import com.example.nofarcohenzedek.dogo.Model.Trip;
import com.example.nofarcohenzedek.dogo.Model.User;

import java.nio.DoubleBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TripsReportActivity extends Activity {

    Boolean isOwner;
    Long userId;
    List<Trip> allTrips;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_report);

        setActionBar((Toolbar) findViewById(R.id.tripsReportToolBar));
        getActionBar().setDisplayShowTitleEnabled(false);
        progressBar = (ProgressBar) findViewById(R.id.tripsReportProgressBar);

        isOwner = getIntent().getBooleanExtra("isOwner", false);
        userId = getIntent().getLongExtra("userId",0);

        // Get all trips that connected to current user
        if(isOwner)
        {
            Model.getInstance().getTripsByDogOwnerId(userId, new Model.GetTripsListener() {
                @Override
                public void onResult(List<Trip> trips) {
                    allTrips = trips;

                    if (allTrips != null && !allTrips.isEmpty()) {
                        CustomAdapter adapter = new CustomAdapter();
                        ListView listView = (ListView) findViewById(R.id.tripsList);
                        listView.setAdapter(adapter);
                    }
                    else
                    {
                        ((TextView)findViewById(R.id.errorInTripsList)).setText("אין טיולים להצגה");
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
        else
        {
            Model.getInstance().getTripsByDogWalkerId(userId, new Model.GetTripsListener() {
                @Override
                public void onResult(List<Trip> trips) {
                    allTrips = trips;

                    if (allTrips != null && !allTrips.isEmpty()) {
                        CustomAdapter adapter = new CustomAdapter();
                        ListView listView = (ListView) findViewById(R.id.tripsList);
                        listView.setAdapter(adapter);
                    }
                    else
                    {
                        ((TextView)findViewById(R.id.errorInTripsList)).setText("אין טיולים להצגה");
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    /**
     * pay for trip - on select 'isPaid' checkBox
     * @param view
     */
    public void payTripBTN(View view)
    {
        CheckBox isPaid = ((CheckBox)view);

        // pay for this trip - only if the user is walker and the trip wasn't paid.
        if (isPaid.isChecked() && !isOwner)
        {
            Model.getInstance().payTrip(((Long) isPaid.getTag()));
            isPaid.setEnabled(false);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (isOwner)
        {
            getMenuInflater().inflate(R.menu.menu_prime_dog_owner, menu);
        }
        else
        {
            getMenuInflater().inflate(R.menu.menu_prime_dog_walker, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent intent = null;

        if (id == R.id.searchDW) {
            intent = new Intent(this, SearchActivity.class);
        } else if (id == R.id.map) {
            intent = new Intent(this, MapsActivity.class);
        } else if (id == R.id.dogsList) {
            intent = new Intent(this, DogsListActivity.class);
        } else if (id == R.id.messages) {
            intent = new Intent(this, MessagesActivity.class);
        } else if (id == R.id.myProfile) {
            intent = new Intent(this, MyProfileActivity.class);

        }

        intent.putExtra("isOwner", isOwner);
        intent.putExtra("userId", getIntent().getLongExtra("userId",0));
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return allTrips.size();
        }

        @Override
        public Object getItem(int position) {
            return allTrips.get(position);
        }

        @Override
        public long getItemId(int position) {
            return allTrips.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.raw_trips_layout, null);
            }

            TextView dogName = (TextView) convertView.findViewById(R.id.dogName);
            TextView ownerName = (TextView) convertView.findViewById(R.id.ownerName);
            TextView walkerName = (TextView) convertView.findViewById(R.id.walkerName);
            TextView date = (TextView) convertView.findViewById(R.id.date);
            TextView startTime = (TextView) convertView.findViewById(R.id.startTime);
            TextView endTime = (TextView) convertView.findViewById(R.id.endTime);
            TextView priceForTrip = (TextView) convertView.findViewById(R.id.priceForTrip);
            CheckBox isPaid = (CheckBox) convertView.findViewById(R.id.isPaid);

            // Get all properties from TRIP object
            Trip trip = allTrips.get(position);
            dogName.setText(trip.getDogOwner().getDog().getName());
            ownerName.setText(trip.getDogOwner().getFirstName());
            walkerName.setText(trip.getDogWalker().getFirstName());
            date.setText(new SimpleDateFormat("dd/MM/yyyy").format(trip.getStartOfWalking()));
            startTime.setText(new SimpleDateFormat("hh:mm a").format(trip.getStartOfWalking()));
            endTime.setText(new SimpleDateFormat("hh:mm a").format(trip.getEndOfWalking()));
            isPaid.setChecked(trip.getIsPaid());

            // Calculate the price for this trip
            double hoursOfTrip;
            double minutesOfTrip;

            if (trip.getEndOfWalking().getMinutes() < trip.getStartOfWalking().getMinutes()) {
                hoursOfTrip = trip.getEndOfWalking().getHours() - 1 - trip.getStartOfWalking().getHours();
                minutesOfTrip = 60 + trip.getEndOfWalking().getMinutes() - trip.getStartOfWalking().getMinutes();
            } else {
                hoursOfTrip = trip.getEndOfWalking().getHours() - trip.getStartOfWalking().getHours();
                minutesOfTrip = trip.getEndOfWalking().getMinutes() - trip.getStartOfWalking().getMinutes();
            }

            double price = trip.getDogWalker().getPriceForHour() * (hoursOfTrip + (minutesOfTrip / 60));

            // Show only 2-3 digits after point
            String stringPrice = String.valueOf(price);
            if(stringPrice.length() > 6) {
                stringPrice = stringPrice.substring(0, 6);
            }

            priceForTrip.setText(stringPrice);

            // Add to 'isPaid' checkBox tag - trip id
            isPaid.setTag(trip.getId());

            // if isPaid checked or the current user is owner, so the checkbox is disabled
            if(isPaid.isChecked() ||  isOwner)
            {
                isPaid.setEnabled(false);
            }

            return convertView;
        }
    }
}
