package com.codexxatransco.user.activity;

import android.Manifest;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.codexxatransco.user.R;
import com.codexxatransco.user.adepter.AutoCompleteAdapter;
import com.codexxatransco.user.fregment.HomeSelectFragment;
import com.codexxatransco.user.model.ResponcePrice;
import com.codexxatransco.user.polygon.Point;
import com.codexxatransco.user.retrofit.APIClient;
import com.codexxatransco.user.retrofit.GetResult;
import com.codexxatransco.user.utility.CustPrograssbar;
import com.codexxatransco.user.utility.FromMover;
import com.codexxatransco.user.utility.ToMover;
import com.codexxatransco.user.utility.Utility;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class CourierServiceActivity extends AppCompatActivity implements GetResult.MyListener {

    @BindView(R.id.img_back)
    public ImageView imgBack;
    @BindView(R.id.pickup)
    public AutoCompleteTextView pickup;
    @BindView(R.id.txt_pin_pick)
    public TextView txtPinPick;
    @BindView(R.id.drop)
    public AutoCompleteTextView drop;
    @BindView(R.id.txt_pin_drop)
    public TextView txtPinDrop;
    @BindView(R.id.ed_wight)
    public EditText edWight;
    @BindView(R.id.ed_lenth)
    public EditText edLenth;
    @BindView(R.id.ed_breath)
    public EditText edBreath;
    @BindView(R.id.ed_height)
    public EditText edHeight;
    @BindView(R.id.txt_continue)
    public TextView txtContinue;
    AutoCompleteAdapter adapter;
    AutoCompleteAdapter adapterDrop;

    private PlacesClient placesClient;
    CustPrograssbar custPrograssbar;
    ResponcePrice responcePrice;

    FromMover fromMover = new FromMover();
    ToMover toMover = new ToMover();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_service);
        ButterKnife.bind(this);
        custPrograssbar = new CustPrograssbar();
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
        getQuote();

    }

    private void getQuote() {
        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uidaa", "101");

        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(jsonObject.toString(), MediaType.parse(getString(R.string.application_json)));
        Call<JsonObject> call = APIClient.getInterface().getquote(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");

    }


    @Override
    public void callback(JsonObject result, String callNo) {
        try {
            custPrograssbar.closePrograssBar();
            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                responcePrice = gson.fromJson(result.toString(), ResponcePrice.class);


            }
        } catch (Exception e) {
            Log.e("Error", "-->" + e.toString());
        }
    }

    @OnClick({R.id.img_back, R.id.txt_continue})
    public void onBindClick(View view) {
        int id = view.getId();
        if (id == R.id.img_back) {
            finish();
        } else if (id == R.id.txt_continue && isValid()) {

            ExampleAsyncTask asyncTask = new ExampleAsyncTask();
            asyncTask.execute();

        }
    }

    private boolean isValid() {

        if (TextUtils.isEmpty(edWight.getText().toString())) {
            edWight.setError("");
            return false;
        }
        if (TextUtils.isEmpty(edLenth.getText().toString())) {
            edLenth.setError("");
            return false;
        }
        if (TextUtils.isEmpty(edBreath.getText().toString())) {
            edBreath.setError("");
            return false;
        }
        if (TextUtils.isEmpty(edHeight.getText().toString())) {
            edHeight.setError("");
            return false;
        }


        return true;
    }

    public void priceGet(Double distance) {
        // Insert the length of the package in centimeters
        double length = Double.parseDouble(edLenth.getText().toString());
        double width = Double.parseDouble(edBreath.getText().toString()); // Insert the width of the package in centimeters
        double height = Double.parseDouble(edHeight.getText().toString()); // Insert the height of the package in centimeters
        double weight = Double.parseDouble(edWight.getText().toString()); // Insert the weight of the package in kilograms

        double actualWeight = weight;
        double volumetricWeight = (length * width * height) / 5000;
        if (volumetricWeight > actualWeight) {
            actualWeight = volumetricWeight;
        }

        double shippingCost;
        if (distance <= responcePrice.getVehicleData().getUkms()) {
            shippingCost = responcePrice.getVehicleData().getUprice() * distance;
        } else {
            shippingCost = responcePrice.getVehicleData().getUprice() * responcePrice.getVehicleData().getUkms() + responcePrice.getVehicleData().getAprice() * (distance - responcePrice.getVehicleData().getUkms());
        }

        double weightFee = actualWeight * responcePrice.getKGPRICE();
        double totalCharge = shippingCost + weightFee;
        Log.e("totalCharge", "" + totalCharge);
        Log.e("volumetricWeight", "" + volumetricWeight);
        if (pickAddress && dropAddress && txtPinPick.getText() != null && txtPinDrop.getText() != null) {
            startActivity(new Intent(this, CourierServiceActivity1.class)
                    .putExtra("weight", String.valueOf(weight))
                    .putExtra("length", String.valueOf(length))
                    .putExtra("width", String.valueOf(width))
                    .putExtra("height", String.valueOf(height))
                    .putExtra("totalCharge", totalCharge)
                    .putExtra("from", fromMover).putExtra("to", toMover));
        } else {
            txtPinPick.setError("");
            txtPinDrop.setError("");
        }

    }


    private String getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String postalCode = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                postalCode = address.getPostalCode();
                // Do something with the postal code
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return postalCode;
    }

    boolean pickAddress = false;
    boolean dropAddress = false;
    private final AdapterView.OnItemClickListener autocompleteClickListener = (adapterView, view, i, l) -> {
        try {
            final AutocompletePrediction item = adapter.getItem(i);
            String placeID = null;
            if (item != null) {
                placeID = item.getPlaceId();
            }
            List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS);

            FetchPlaceRequest request = null;

            if (placeID != null) {
                request = FetchPlaceRequest.builder(placeID, placeFields).build();
            }

            if (request != null) {
                placesClient.fetchPlace(request)
                        .addOnSuccessListener(task -> {
                            Log.e("Tast", "--> " + task.getPlace().getAddressComponents());
                            Point point = new Point(task.getPlace().getLatLng().latitude, task.getPlace().getLatLng().longitude);
                            boolean contains = HomeSelectFragment.getInstance().getPolygon().contains(point);
                            if (contains) {
                                pickAddress = true;
                                fromMover.setAddress(task.getPlace().getName() + "," + task.getPlace().getAddress());
                                fromMover.setLats(task.getPlace().getLatLng().latitude);
                                fromMover.setLngs(task.getPlace().getLatLng().longitude);
                                txtPinPick.setText("" + getAddressFromLocation(task.getPlace().getLatLng().latitude, task.getPlace().getLatLng().longitude));
                                fromMover.setFloor(txtPinPick.getText().toString());
                            } else {
                                pickup.setText("");
                                Toast.makeText(CourierServiceActivity.this, "Sorry! We do not allow pickup in the area you have selected", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> e.printStackTrace());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    };


    private final AdapterView.OnItemClickListener autocompleteClickListenerDrop = (adapterView, view, i, l) -> {


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
                placesClient.fetchPlace(request).addOnSuccessListener(task -> {
                    Point point = new Point(task.getPlace().getLatLng().latitude, task.getPlace().getLatLng().longitude);
                    boolean contains = HomeSelectFragment.getInstance().getPolygon().contains(point);
                    if (contains) {
                        dropAddress = true;
                        toMover.setAddress(task.getPlace().getName() + "," + task.getPlace().getAddress());
                        toMover.setLats(task.getPlace().getLatLng().latitude);
                        toMover.setLngs(task.getPlace().getLatLng().longitude);
                        txtPinDrop.setText("" + getAddressFromLocation(task.getPlace().getLatLng().latitude, task.getPlace().getLatLng().longitude));
                        toMover.setFloor(txtPinDrop.getText().toString());

                    } else {
                        drop.setText("");
                        Toast.makeText(CourierServiceActivity.this, "Sorry! We do not allow pickup in the area you have selectd", Toast.LENGTH_SHORT).show();
                    }


                }).addOnFailureListener(e -> e.printStackTrace());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    };

    public class ExampleAsyncTask extends AsyncTask<Void, Void, Double> {

        @Override
        protected Double doInBackground(Void... voids) {
            double v = 0;
            try {
                v = Utility.getDirections(fromMover.getLats(), fromMover.getLngs(), toMover.getLats(), toMover.getLngs());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return v;
        }

        @Override
        protected void onPostExecute(Double roadDistance) {
            super.onPostExecute(roadDistance);

            priceGet(roadDistance);

        }
    }

}