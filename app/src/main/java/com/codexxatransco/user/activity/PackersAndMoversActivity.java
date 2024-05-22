package com.codexxatransco.user.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.codexxatransco.user.R;
import com.codexxatransco.user.adepter.AutoCompleteAdapter;
import com.codexxatransco.user.fregment.HomeSelectFragment;
import com.codexxatransco.user.polygon.Point;
import com.codexxatransco.user.utility.FromMover;
import com.codexxatransco.user.utility.ToMover;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PackersAndMoversActivity extends BaseActivity {

    @BindView(R.id.img_back)
    public ImageView imgBack;
    @BindView(R.id.pickup)
    public AutoCompleteTextView pickup;
    @BindView(R.id.chbox_from)
    public CheckBox chboxFrom;
    @BindView(R.id.ed_floorfrom)
    public EditText edFloorfrom;

    @BindView(R.id.ed_floorto)
    public EditText edFloorto;
    @BindView(R.id.drop)
    public AutoCompleteTextView drop;
    @BindView(R.id.chbox_to)
    public CheckBox chboxTo;
    @BindView(R.id.calendarTextView)
    public TextView calendarTextView;
    @BindView(R.id.txt_continue)
    public TextView txtContinue;


    AutoCompleteAdapter adapter;
    AutoCompleteAdapter adapterDrop;

    private PlacesClient placesClient;
    FromMover fromMover = new FromMover();
    ToMover toMover = new ToMover();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packers_and_movers);
        ButterKnife.bind(this);

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
                }, 1010);


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.API_KEY));
        }
        placesClient = Places.createClient(this);

        pickup.setThreshold(1);
        pickup.setOnItemClickListener(autocompleteClickListener);
        adapter = new AutoCompleteAdapter(this, placesClient);
        pickup.setAdapter(adapter);

        drop.setThreshold(1);
        drop.setOnItemClickListener(autocompleteClickListenerDrop);
        adapterDrop = new AutoCompleteAdapter(this, placesClient);
        drop.setAdapter(adapterDrop);

        calendarTextView.setOnClickListener(v -> {
            // Get current date
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            // Create a new DatePickerDialog and show it
            DatePickerDialog datePickerDialog = new DatePickerDialog(PackersAndMoversActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            // Update the text of the TextView with the selected date
                            calendarTextView.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        }
                    }, year, month, dayOfMonth);
            datePickerDialog.show();
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        });

    }

    private final AdapterView.OnItemClickListener autocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            try {
                final AutocompletePrediction item = adapter.getItem(i);
                String placeID = null;
                if (item != null) {
                    placeID = item.getPlaceId();
                }

                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS
                        , Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS);

                FetchPlaceRequest request = null;
                if (placeID != null) {
                    request = FetchPlaceRequest.builder(placeID, placeFields)
                            .build();
                }

                if (request != null) {
                    placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSuccess(FetchPlaceResponse task) {


                            Point point = new Point(task.getPlace().getLatLng().latitude,task.getPlace().getLatLng().longitude);
                            boolean contains = HomeSelectFragment.getInstance().getPolygon().contains(point);
                            if(contains){
                                fromMover.setAddress(task.getPlace().getName() + "," + task.getPlace().getAddress());
                                fromMover.setLats(task.getPlace().getLatLng().latitude);
                                fromMover.setLngs(task.getPlace().getLatLng().longitude);
                            }else {
                                pickup.setText("");
                                Toast.makeText(PackersAndMoversActivity.this,"Sorry! We do not allow pickup in the area you have selectd",Toast.LENGTH_SHORT).show();

                            }

                        }
                    }).addOnFailureListener(e -> e.printStackTrace());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };


    private final AdapterView.OnItemClickListener autocompleteClickListenerDrop = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            try {
                final AutocompletePrediction item = adapterDrop.getItem(i);
                String placeID = null;
                if (item != null) {
                    placeID = item.getPlaceId();
                }
                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS
                        , Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS);

                FetchPlaceRequest request = null;
                if (placeID != null) {
                    request = FetchPlaceRequest.builder(placeID, placeFields)
                            .build();
                }

                if (request != null) {
                    placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSuccess(FetchPlaceResponse task) {
                            Point point = new Point(task.getPlace().getLatLng().latitude,task.getPlace().getLatLng().longitude);
                            boolean contains = HomeSelectFragment.getInstance().getPolygon().contains(point);
                            if(contains){
                                toMover.setAddress(task.getPlace().getName() + "," + task.getPlace().getAddress());
                                toMover.setLats(task.getPlace().getLatLng().latitude);
                                toMover.setLngs(task.getPlace().getLatLng().longitude);
                            }else {
                                drop.setText("");
                                Toast.makeText(PackersAndMoversActivity.this,"Sorry! We do not allow pickup in the area you have selectd",Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).addOnFailureListener(e -> e.printStackTrace());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    @OnClick({R.id.img_back, R.id.txt_continue})
    public void onBindClick(View view) {
        int id = view.getId();
        if (id == R.id.img_back) {
            finish();
        } else if (id == R.id.txt_continue) {
            if (TextUtils.isEmpty(pickup.getText().toString())) {
                pickup.setError("");
                return;
            }
            if (TextUtils.isEmpty(drop.getText().toString())) {
                drop.setError("");
                return;
            }

            if (chboxFrom.isChecked()) {
                fromMover.setLift("1");
            } else {
                fromMover.setLift("0");
            }

            if (chboxTo.isChecked()) {
                toMover.setLift("1");
            } else {
                toMover.setLift("0");
            }
            String floorValue = edFloorfrom.getText().toString();
            if (floorValue == null || floorValue.equalsIgnoreCase("")) {
                fromMover.setFloor("0");
            } else {
                fromMover.setFloor(floorValue);
            }

            String flooryo = edFloorto.getText().toString();
            if (flooryo == null || flooryo.equalsIgnoreCase("")) {
                toMover.setFloor("0");
            } else {
                toMover.setFloor(flooryo);
            }


            startActivity(new Intent(PackersAndMoversActivity.this, PackersAndMover2Activity.class).putExtra("from", fromMover).putExtra("to", toMover));
        }
    }


}