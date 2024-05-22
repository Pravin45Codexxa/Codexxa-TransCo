package com.codexxatransco.user.activity;

import static com.codexxatransco.user.utility.SessionManager.currency;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.codexxatransco.user.R;
import com.codexxatransco.user.model.LogisticDetails;
import com.codexxatransco.user.model.LogisticInfo;
import com.codexxatransco.user.model.User;
import com.codexxatransco.user.retrofit.APIClient;
import com.codexxatransco.user.retrofit.GetResult;
import com.codexxatransco.user.utility.CustPrograssbar;
import com.codexxatransco.user.utility.SessionManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class LogisticDetailsActivity extends BaseActivity implements OnMapReadyCallback, GetResult.MyListener {


    @BindView(R.id.ima_back)
    public ImageView imaBack;
    @BindView(R.id.txt_date)
    public TextView txtDate;
    @BindView(R.id.txt_packid)
    public TextView txtPackid;
    @BindView(R.id.txtttotle)
    public TextView txtttotle;
    @BindView(R.id.img_icon)
    public ImageView imgIcon;
    @BindView(R.id.txt_ridername)
    public TextView txtRidername;
    @BindView(R.id.txt_vtype)
    public TextView txtVtype;
    @BindView(R.id.lvl_rider)
    public LinearLayout lvlRider;
    @BindView(R.id.txt_status)
    public TextView txtStatus;
    @BindView(R.id.txt_pickaddress)
    public TextView txtPickaddress;
    @BindView(R.id.txt_dropaddress)
    public TextView txtDropaddress;
    @BindView(R.id.toolbar_title)
    public TextView toolbarTitle;

    @BindView(R.id.txt_wight)
    public TextView txtWight;

    @BindView(R.id.txt_lenth)
    public TextView txtLenth;
    @BindView(R.id.txt_breath)
    public TextView txtBreath;

    @BindView(R.id.ed_height)
    public TextView edHeight;

    @BindView(R.id.lvl_logistic)
    public LinearLayout lvlLogistic;

    @BindView(R.id.lvl_parcel)
    public LinearLayout lvlParcel;


    @BindView(R.id.lvl_product)
    public LinearLayout lvlProduct;

    String orderID;


    User user;
    CustPrograssbar custPrograssbar;
    String type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logistic_detail);
        ButterKnife.bind(this);
        sessionManager = new SessionManager(this);
        user = sessionManager.getUserDetails();
        custPrograssbar = new CustPrograssbar();
        orderID = getIntent().getStringExtra("order_id");
        type=getIntent().getStringExtra("type");

        getLogisticDetails();
    }

    private void getLogisticDetails() {
        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());
            jsonObject.put("order_id", orderID);

        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(jsonObject.toString(),MediaType.parse(getString(R.string.application_json)));
        Call<JsonObject> call;
        if(type.equals("logistic")){
           call = APIClient.getInterface().logisticInformation(bodyRequest);
        }else {
            call = APIClient.getInterface().parcelInformation(bodyRequest);
        }

        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");
    }


    @OnClick({R.id.ima_back})
    public void onBindClick(View view) {
        if (view.getId() == R.id.ima_back) {
            finish();
        }
    }

    Polyline polyline;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(item.getPickLat(), item.getPickLng()), 10));

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(item.getPickLat(), item.getPickLng()))
                .title("Pickup")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_long)));


        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(new LatLng(item.getPickLat(), item.getPickLng()));


        Bitmap bitmap = drawTextToBitmap(LogisticDetailsActivity.this, R.drawable.ic_destination_long, "1");

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(item.getDropLat(), item.getDropLong()))
                .title("Drop")
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
        latLngs.add(new LatLng(item.getDropLat(), item.getDropLong()));

        polyline = googleMap
                .addPolyline((new PolylineOptions())
                        .addAll(latLngs).width(5).color(Color.BLUE)
                        .geodesic(true));
    }

    public Bitmap drawTextToBitmap(Context gContext, int gResId, String gText) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap =
                BitmapFactory.decodeResource(resources, gResId);

        Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize((int) (16 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 3;

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }

    LogisticInfo item;

    @Override
    public void callback(JsonObject result, String callNo) {
        try {
            custPrograssbar.closePrograssBar();
            Gson gson = new Gson();
            LogisticDetails details = gson.fromJson(result.toString(), LogisticDetails.class);
            if (details.getResult().equalsIgnoreCase("true")) {
                item = details.getLogisticInfo();
                Glide.with(this).load(APIClient.baseUrl + "/" + item.getRiderImg()).thumbnail(Glide.with(this).load(R.drawable.emty)).into(imgIcon);

                if (item.getStatus().equalsIgnoreCase("cancelled") || item.getStatus().equalsIgnoreCase("Pending")) {
                    lvlRider.setVisibility(View.GONE);
                }
                toolbarTitle.setText(""+item.getPackageNumber());
                txtttotle.setText(sessionManager.getStringData(currency) + item.getTotal());
                txtDate.setText("" + item.getLogisticDate());
                txtPackid.setText("" + item.getPackageNumber());
                txtRidername.setText("" + item.getRiderName());
                txtVtype.setText("" + item.getPMethodName());
                txtStatus.setText("" + item.getStatus());
                txtPickaddress.setText("" + item.getPickAddress());
                txtDropaddress.setText("" + item.getDropAddress());

                if(type.equalsIgnoreCase("logistic")){
                    lvlLogistic.setVisibility(View.VISIBLE);
                    lvlParcel.setVisibility(View.GONE);
                    for (int i = 0; i < item.getProductdata().size(); i++) {
                        LayoutInflater inflater = LayoutInflater.from(this);
                        View view = inflater.inflate(R.layout.product_item_logistic, null);
                        TextView productTitle = view.findViewById(R.id.product_title);
                        TextView productPrice = view.findViewById(R.id.product_price);
                        TextView count = view.findViewById(R.id.count);
                        TextView total = view.findViewById(R.id.total);

                        productTitle.setText("" + item.getProductdata().get(i).getProductName());
                        productPrice.setText(sessionManager.getStringData(currency) + item.getProductdata().get(i).getPrice());
                        count.setText("" + item.getProductdata().get(i).getQuantity());
                        total.setText(sessionManager.getStringData(currency) + item.getProductdata().get(i).getTotal());
                        lvlProduct.addView(view);
                    }
                }else {

                    lvlLogistic.setVisibility(View.GONE);
                    lvlParcel.setVisibility(View.VISIBLE);
                    txtWight.setText(""+item.getParcelWeight());
                    txtLenth.setText(""+item.getParcelDimension().get(0));
                    txtBreath.setText(""+item.getParcelDimension().get(1));
                    edHeight.setText(""+item.getParcelDimension().get(2));

                }



                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            }

        } catch (Exception e) {
        Log.e("Error",""+e.getMessage());
        }
    }
}