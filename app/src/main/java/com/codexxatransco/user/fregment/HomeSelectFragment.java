package com.codexxatransco.user.fregment;

import static com.codexxatransco.user.utility.SessionManager.currency;
import static com.codexxatransco.user.utility.Utility.isSinglePoint;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.codexxatransco.user.R;
import com.codexxatransco.user.activity.CourierServiceActivity;
import com.codexxatransco.user.activity.DriverSearchActivity;
import com.codexxatransco.user.activity.PackersAndMoversActivity;
import com.codexxatransco.user.activity.TripDetailsActivity;
import com.codexxatransco.user.adepter.BannerAdp;
import com.codexxatransco.user.model.Zone;
import com.codexxatransco.user.polygon.Point;
import com.codexxatransco.user.polygon.Polygon;
import com.codexxatransco.user.retrofit.APIClient;
import com.codexxatransco.user.retrofit.GetResult;
import com.codexxatransco.user.utility.CustPrograssbar;
import com.codexxatransco.user.utility.CustomRecyclerView;
import com.codexxatransco.user.utility.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;


public class HomeSelectFragment extends Fragment implements GetResult.MyListener {
    @BindView(R.id.crd_1)
    public LinearLayout crd1;

    @BindView(R.id.crd_3)
    public LinearLayout crd3;
    @BindView(R.id.crd_4)
    public LinearLayout crd4;

    @BindView(R.id.recycler_banner)
    public CustomRecyclerView recyclerView;

    @BindView(R.id.txt_address)
    TextView txtAddress;


    @BindView(R.id.img_icon)
    ImageView imgIcon;
    @BindView(R.id.txt_date)
    TextView txtDate;
    @BindView(R.id.txt_type)
    TextView txtType;
    @BindView(R.id.txt_totle)
    TextView txtTotle;
    @BindView(R.id.txt_pickaddress)
    TextView txtPickaddress;
    @BindView(R.id.lvl_drop)
    LinearLayout lvlDrop;
    @BindView(R.id.txt_status)
    TextView txtStatus;
    @BindView(R.id.lvl_click)
    LinearLayout lvlClick;

    CustPrograssbar custPrograssbar;
    SessionManager sessionManager;
    Polygon polygon;
    public static HomeSelectFragment homeSelectFragment;

    public static HomeSelectFragment getInstance() {
        return homeSelectFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public Polygon getPolygon() {
        return polygon;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_select, container, false);
        ButterKnife.bind(this, view);
        homeSelectFragment = this;
        sessionManager = new SessionManager(getActivity());
        custPrograssbar = new CustPrograssbar();

        LinearLayoutManager mLayoutManager1 = new LinearLayoutManager(getActivity());
        mLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(mLayoutManager1);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        getLocation();

        getZone();


        return view;
    }
    private void getLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    10);
        } else {
            // Permission has already been granted
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            // Use the latitude and longitude values as needed
                            getAddressFromLocation(getActivity(),latitude,longitude);
                            // ...
                        }
                    });
        }
    }

    public void getAddressFromLocation(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String addressText = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append(", ");
                }

                sb.append(address.getLocality()).append(", ");
                sb.append(address.getPostalCode()).append(", ");
                sb.append(address.getCountryName());
                addressText = sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        txtAddress.setText(addressText);
    }
    private void getZone() {
        custPrograssbar.prograssCreate(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", sessionManager.getUserDetails().getId());

        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(jsonObject.toString(),MediaType.parse(getString(R.string.application_json)));
        Call<JsonObject> call = APIClient.getInterface().getzone(bodyRequest);
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
                Zone zone = gson.fromJson(result.toString(), Zone.class);
                sessionManager.setStringData(currency, zone.getMainData().getCurrency());
                Polygon.Builder poly2 = new Polygon.Builder();
                for (int i = 0; i < zone.getZones().size(); i++) {
                    poly2.addVertex(new Point(zone.getZones().get(i).getLat(), zone.getZones().get(i).getLng()));
                }
                polygon = poly2.build();
                BannerAdp bannerAdp =new BannerAdp(getActivity(), zone.getBannerList());
                recyclerView.setAdapter(bannerAdp);
                recyclerView.startAutoRotation();


                txtDate.setText("" + zone.getHistoryinfo().get(0).getDateTime());
                txtType.setText("" + zone.getHistoryinfo().get(0).getvType());
                txtTotle.setText(sessionManager.getStringData(SessionManager.currency) + zone.getHistoryinfo().get(0).getPTotal());
                txtPickaddress.setText("" + zone.getHistoryinfo().get(0).getPickAddress());
                lvlDrop.removeAllViews();

                for (int i = 0; i < zone.getHistoryinfo().get(0).getParceldata().size(); i++) {
                    LayoutInflater inflater = LayoutInflater.from(getActivity());

                    View view = inflater.inflate(R.layout.custome_droplocation, null);
                    TextView txtDropaddress = view.findViewById(R.id.txt_dropaddress);
                    txtDropaddress.setText("" + zone.getHistoryinfo().get(0).getParceldata().get(i).getDropPointAddress());

                    lvlDrop.addView(view);
                }

                txtStatus.setText("" + zone.getHistoryinfo().get(0).getOrderStatus());
                Glide.with(getActivity()).load(APIClient.baseUrl + "/" + zone.getHistoryinfo().get(0).getVImg()).thumbnail(Glide.with(getActivity()).load(R.drawable.emty)).into(imgIcon);
                lvlClick.setOnClickListener(v -> {
                    if (zone.getHistoryinfo().get(0).getOrderStatus().equalsIgnoreCase("Completed") || zone.getHistoryinfo().get(0).getOrderStatus().equalsIgnoreCase("Cancelled")) {
                        startActivity(new Intent(getActivity(), TripDetailsActivity.class).putExtra("myclass", zone.getHistoryinfo().get(0)).putExtra("list", (Serializable) zone.getHistoryinfo().get(0).getParceldata()));
                    } else {

                        startActivity(new Intent(getActivity(), DriverSearchActivity.class).putExtra("oid", zone.getHistoryinfo().get(0).getOrderid()));
                    }
                });

            }
        } catch (Exception e) {
            Log.e("Error", "--->" + e.getMessage());
        }
    }


    @OnClick({R.id.crd_1, R.id.crd_3, R.id.crd_4})
    public void onBindClick(View view) {
        int id = view.getId();
        if (id == R.id.crd_1) {
            bottonConfirm();
        } else if (id == R.id.crd_3) {
            startActivity(new Intent(getActivity(), PackersAndMoversActivity.class));
        } else if (id == R.id.crd_4) {
            startActivity(new Intent(getActivity(), CourierServiceActivity.class));
        }
    }


    public void bottonConfirm() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(getActivity());
        View sheetView = getLayoutInflater().inflate(R.layout.custome_selecttype, null);
        mBottomSheetDialog.setContentView(sheetView);
        LinearLayout lvlOne = sheetView.findViewById(R.id.lvl_one);
        LinearLayout lvlTwo = sheetView.findViewById(R.id.lvl_two);

        lvlOne.setOnClickListener(v -> {
            isSinglePoint = 0;
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            HomeFragment myFragment = new HomeFragment(); // replace with your own fragment class
            fragmentTransaction.add(R.id.container, myFragment, "home2"); // replace R.id.fragment_container with the ID of the container where you want to add the fragment
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            mBottomSheetDialog.cancel();

        });
        lvlTwo.setOnClickListener(v -> {
            isSinglePoint = 1;
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            HomeFragment myFragment = new HomeFragment(); // replace with your own fragment class
            fragmentTransaction.add(R.id.container, myFragment, "home2"); // replace R.id.fragment_container with the ID of the container where you want to add the fragment
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            mBottomSheetDialog.cancel();

        });
        mBottomSheetDialog.show();

    }


}