package com.codexxatransco.user.activity;

import static com.codexxatransco.user.utility.SessionManager.currency;
import static com.codexxatransco.user.utility.Utility.paymentsucsses;
import static com.codexxatransco.user.utility.Utility.tragectionID;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.codexxatransco.user.R;
import com.codexxatransco.user.model.Payment;
import com.codexxatransco.user.model.PaymentItem;
import com.codexxatransco.user.model.RestResponse;
import com.codexxatransco.user.model.User;
import com.codexxatransco.user.retrofit.APIClient;
import com.codexxatransco.user.retrofit.GetResult;
import com.codexxatransco.user.utility.CustPrograssbar;
import com.codexxatransco.user.utility.FromMover;
import com.codexxatransco.user.utility.MyDatabaseHelper;
import com.codexxatransco.user.utility.SessionManager;
import com.codexxatransco.user.utility.ToMover;
import com.codexxatransco.user.utility.Utility;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

public class CourierServiceActivity1 extends AppCompatActivity implements OnMapReadyCallback, GetResult.MyListener {

    @BindView(R.id.ima_back)
    public ImageView imaBack;
    @BindView(R.id.toolbar_title)
    public TextView toolbarTitle;
    @BindView(R.id.toolbar)
    public Toolbar toolbar;
    @BindView(R.id.appbar)
    public AppBarLayout appbar;
    @BindView(R.id.txt_price)
    public TextView txtPrice;
    @BindView(R.id.txt_continue)
    public TextView txtContinue;
    FromMover fromMover;
    ToMover toMovera;
    SessionManager sessionManager;
    User user;
    double total;
    CustPrograssbar custPrograssbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_service1);
        ButterKnife.bind(this);
        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(this);
        user = sessionManager.getUserDetails();
        fromMover = (FromMover) getIntent().getSerializableExtra("from");
        toMovera = (ToMover) getIntent().getSerializableExtra("to");
        total = getIntent().getDoubleExtra("totalCharge", 0.0);
        txtPrice.setText(sessionManager.getStringData(SessionManager.currency) + total);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getPayment();
    }

    private void getPayment() {


        JSONObject jsonObject = new JSONObject();
        RequestBody bodyRequest = RequestBody.create(jsonObject.toString(),MediaType.parse(getString(R.string.application_json)));
        Call<JsonObject> call = APIClient.getInterface().getPaymentList(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");

    }

    @OnClick({R.id.ima_back, R.id.txt_continue})
    public void onBindClick(View view) {
        int id = view.getId();
        if (id == R.id.ima_back) {
            finish();
        } else if (id == R.id.txt_continue) {
            bottonPaymentList();
        }
    }

    Polyline polyline;
    List<PaymentItem> paymentList = new ArrayList<>();


    public void bottonPaymentList() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.custome_payment, null);
        LinearLayout listView = sheetView.findViewById(R.id.lvl_list);
        TextView txtBooktrip = sheetView.findViewById(R.id.btn_booktrip);
        LinearLayout lvlWallat = sheetView.findViewById(R.id.lvl_wallat);
        lvlWallat.setVisibility(View.GONE);

        TextView txtTotal = sheetView.findViewById(R.id.txt_total);
        TextView txtWallet = sheetView.findViewById(R.id.txt_wallet);

        txtTotal.setText(getString(R.string.total)+" " + sessionManager.getStringData(currency) + total);
        txtWallet.setText(getString(R.string.procetcradit)+" " + sessionManager.getStringData(currency) + user.getWallet() + ")");


        for (int i = 0; i < paymentList.size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(this);
            PaymentItem paymentItem = paymentList.get(i);
            View view = inflater.inflate(R.layout.custome_paymentitem, null);
            ImageView imageView = view.findViewById(R.id.img_icon);
            TextView txtTitle = view.findViewById(R.id.txt_title);
            TextView txtSubtitel = view.findViewById(R.id.txt_subtitel);
            txtTitle.setText("" + paymentList.get(i).getmTitle());
            txtSubtitel.setText("" + paymentList.get(i).getSubtitle());
            Glide.with(this).load(APIClient.baseUrl + "/" + paymentList.get(i).getmImg()).thumbnail(Glide.with(this).load(R.drawable.emty)).into(imageView);
            int finalI = i;
            view.setOnClickListener(v -> {
                Utility.paymentId = paymentList.get(finalI).getmId();
                try {
                    switch (paymentList.get(finalI).getmTitle()) {
                        case "Razorpay":
                            int temtoal = (int) Math.round(total);
                            mBottomSheetDialog.cancel();
                            startActivity(new Intent(this, RazerpayActivity.class).putExtra(getString(R.string.amount), temtoal).putExtra(getString(R.string.detail), paymentItem));
                            break;
                        case "Cash On Delivery":
                            placeOrder();
                            mBottomSheetDialog.cancel();
                            break;
                        case "Paypal":
                            mBottomSheetDialog.cancel();
                            startActivity(new Intent(this, PaypalActivity.class).putExtra(getString(R.string.amount), total).putExtra(getString(R.string.detail), paymentItem));
                            break;
                        case "Stripe":
                            mBottomSheetDialog.cancel();
                            startActivity(new Intent(this, StripPaymentActivity.class).putExtra(getString(R.string.amount), total).putExtra(getString(R.string.detail), paymentItem));
                            break;
                        case "FlutterWave":
                            mBottomSheetDialog.cancel();
                            startActivity(new Intent(this, FlutterwaveActivity.class).putExtra(getString(R.string.amount), total));
                            break;
                        case "Paytm":
                            mBottomSheetDialog.cancel();
                            startActivity(new Intent(this, PaytmActivity.class).putExtra(getString(R.string.amount), total));
                            break;
                        case "SenangPay":
                            mBottomSheetDialog.cancel();
                            startActivity(new Intent(this, SenangpayActivity.class).putExtra(getString(R.string.amount), total).putExtra(getString(R.string.detail), paymentItem));
                            break;
                        case "PayStack":
                            int temtoal1 = (int) Math.round(total);
                            mBottomSheetDialog.cancel();
                            startActivity(new Intent(this, PaystackActivity.class).putExtra(getString(R.string.amount), temtoal1).putExtra(getString(R.string.detail), paymentItem));
                            break;
                        default:
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            listView.addView(view);
        }
        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
    }

    @Override
    public void callback(JsonObject result, String callNo) {

        try {
            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                Payment payment = gson.fromJson(result.toString(), Payment.class);
                paymentList = new ArrayList<>();

                for (int i = 0; i < payment.getData().size(); i++) {
                    if (payment.getData().get(i).getmStatus().equalsIgnoreCase("1")) {
                        paymentList.add(payment.getData().get(i));
                    }
                }
            } else if (callNo.equalsIgnoreCase("2")) {

                Gson gson = new Gson();
                RestResponse restResponse = gson.fromJson(result.toString(), RestResponse.class);

                if (restResponse.getResult().equalsIgnoreCase("true")) {
                    Toast.makeText(this, restResponse.getResponseMsg(), Toast.LENGTH_LONG).show();
                    MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(CourierServiceActivity1.this);
                    myDatabaseHelper.deleteAllData();
                    Intent intent = new Intent(this, HomeActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .putExtra("oid", restResponse.getOrderId());
                    startActivity(intent);
                    finish();

                } else {
                    finish();
                    Toast.makeText(this, restResponse.getResponseMsg(), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e("Error", "-->" + e.toString());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(fromMover.getLats(), fromMover.getLngs()), 12));
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(fromMover.getLats(), fromMover.getLngs()))
                .title("Pickup")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_long)));
        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(new LatLng(fromMover.getLats(), fromMover.getLngs()));
        Bitmap bitmap = drawTextToBitmap(CourierServiceActivity1.this, R.drawable.ic_destination_long, "");
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(toMovera.getLats(), toMovera.getLngs()))
                .title("Drop")
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
        latLngs.add(new LatLng(toMovera.getLats(), toMovera.getLngs()));

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

    @Override
    protected void onResume() {
        super.onResume();

        if (paymentsucsses == 1) {
            paymentsucsses = 0;
            placeOrder();
        }
    }

    private void placeOrder() {

        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());

            jsonObject.put("pickup_address", fromMover.getAddress());
            jsonObject.put("pick_lat", fromMover.getLats());
            jsonObject.put("pick_long", fromMover.getLngs());

            jsonObject.put("drop_address", toMovera.getAddress());
            jsonObject.put("drop_lat", toMovera.getLats());
            jsonObject.put("drop_long", toMovera.getLngs());
            jsonObject.put("pick_pincode", fromMover.getFloor());
            jsonObject.put("drop_pincode", toMovera.getFloor());
            jsonObject.put("parcel_weight", getIntent().getStringExtra("weight"));
            String pd = getIntent().getStringExtra("length") + "x" + getIntent().getStringExtra("width") + "x" + getIntent().getStringExtra("height");
            jsonObject.put("parcel_dimension", pd);


            jsonObject.put("total", total);
            jsonObject.put("transaction_id", tragectionID);
            jsonObject.put("p_method_id", Utility.paymentId);


        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(jsonObject.toString(),MediaType.parse(getString(R.string.application_json)));
        Call<JsonObject> call = APIClient.getInterface().parcel(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "2");

    }
}