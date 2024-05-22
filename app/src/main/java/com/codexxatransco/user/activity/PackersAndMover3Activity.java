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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codexxatransco.user.R;
import com.codexxatransco.user.model.Payment;
import com.codexxatransco.user.model.PaymentItem;
import com.codexxatransco.user.model.RestResponse;
import com.codexxatransco.user.model.User;
import com.codexxatransco.user.model.VehicleData;
import com.codexxatransco.user.retrofit.APIClient;
import com.codexxatransco.user.retrofit.GetResult;
import com.codexxatransco.user.utility.CustPrograssbar;
import com.codexxatransco.user.utility.FromMover;
import com.codexxatransco.user.utility.MyData;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class PackersAndMover3Activity extends BaseActivity implements GetResult.MyListener, OnMapReadyCallback {

    @BindView(R.id.recyclerView)
    public RecyclerView recyclerView;
    @BindView(R.id.txt_total)
    public TextView txtTotal;
    @BindView(R.id.img_back)
    public ImageView imgBack;
    @BindView(R.id.txt_continue)
    public TextView txtContinue;
    MyDatabaseHelper myDatabaseHelper;
    double total;
    FromMover fromMover;
    ToMover toMover;
    VehicleData vehicleData;
    CustPrograssbar custPrograssbar;
    User user;
    String logisticDate = "";
    List<PaymentItem> paymentList = new ArrayList<>();
    List<String> futureDatesListPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packers_and_mover3);
        ButterKnife.bind(this);
        custPrograssbar = new CustPrograssbar();
        user = sessionManager.getUserDetails();
        myDatabaseHelper = new MyDatabaseHelper(this);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        fromMover = (FromMover) getIntent().getSerializableExtra("from");
        toMover = (ToMover) getIntent().getSerializableExtra("to");
        vehicleData = (VehicleData) getIntent().getSerializableExtra("vehicle");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d");
        SimpleDateFormat dateFormatpass = new SimpleDateFormat("yyyy-MM-dd");

        long currentTimeMillis = System.currentTimeMillis();
        total = myDatabaseHelper.getTotalPrice();

        Log.e("total", "-- " + total);
        List<String> futureDatesList = new ArrayList<>();
        futureDatesListPass = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Date date = calendar.getTime();
            String dateStr = dateFormat.format(date);
            String dateStrpass = dateFormatpass.format(date);
            if (date.getTime() >= currentTimeMillis) {
                futureDatesList.add(dateStr);
                futureDatesListPass.add(dateStrpass);
            }
            // Move to the next day
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }


        DateAdapter adapter = new DateAdapter(futureDatesList);


        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float density = getResources().getDisplayMetrics().density;
        int noOfColumns = (int) (screenWidth / (70 * density));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, noOfColumns);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        ExampleAsyncTask asyncTask = new ExampleAsyncTask();
        asyncTask.execute();
        getPayment();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getPayment() {
        custPrograssbar.prograssCreate(this);

        JSONObject jsonObject = new JSONObject();
        RequestBody bodyRequest = RequestBody.create(jsonObject.toString(),MediaType.parse(getString(R.string.application_json)));
        Call<JsonObject> call = APIClient.getInterface().getPaymentList(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");

    }

    private void placeOrder() {

        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());

            jsonObject.put("pickup_address", fromMover.getAddress());
            jsonObject.put("pick_lat", fromMover.getLats());
            jsonObject.put("pick_long", fromMover.getLngs());
            jsonObject.put("pick_has_lift", fromMover.getLift());
            jsonObject.put("pick_floor_no", fromMover.getFloor());

            jsonObject.put("drop_address", toMover.getAddress());
            jsonObject.put("drop_lat", toMover.getLats());
            jsonObject.put("drop_long", toMover.getLngs());
            JSONArray jsonArray = new JSONArray();

            MyDatabaseHelper databaseHelper = new MyDatabaseHelper(this);
            List<MyData> myData = databaseHelper.getAllData();

            for (int i = 0; i < myData.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("product_name", myData.get(i).getName());
                object.put("quantity", myData.get(i).getCount());
                object.put("price", myData.get(i).getPrice());
                double tt = Double.parseDouble(myData.get(i).getPrice()) * myData.get(i).getCount();
                object.put("total", tt);
                jsonArray.put(object);
            }

            jsonObject.put("drop_has_lift", toMover.getLift());
            jsonObject.put("drop_floor_no", toMover.getFloor());
            jsonObject.put("logistic_date", logisticDate);
            jsonObject.put("total", total);
            jsonObject.put("transaction_id", tragectionID);
            jsonObject.put("p_method_id", Utility.paymentId);
            jsonObject.put("ProductData", jsonArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(jsonObject.toString(),MediaType.parse(getString(R.string.application_json)));
        Call<JsonObject> call = APIClient.getInterface().logistic(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "2");

    }

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
        txtBooktrip.setOnClickListener(v -> {
            Utility.paymentId = "5";
            placeOrder();
        });

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
            custPrograssbar.closePrograssBar();
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
        Log.e("Error",""+e.getMessage());
        }
    }

    @OnClick({R.id.img_back, R.id.txt_continue})
    public void onBindClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.txt_continue:
                if(!logisticDate.equalsIgnoreCase("")){
                    bottonPaymentList();
                }else {
                    Toast.makeText(this,"Please select date",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    Polyline polyline;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(fromMover.getLats(), fromMover.getLngs()), 12));
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(fromMover.getLats(), fromMover.getLngs()))
                .title("Pickup")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_long)));
        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(new LatLng(fromMover.getLats(), fromMover.getLngs()));
        Bitmap bitmap = drawTextToBitmap(PackersAndMover3Activity.this, R.drawable.ic_destination_long, "1");
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(toMover.getLats(), toMover.getLngs()))
                .title("Drop")
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
        latLngs.add(new LatLng(toMover.getLats(), toMover.getLngs()));

        polyline = googleMap
                .addPolyline((new PolylineOptions())
                        .addAll(latLngs).width(5).color(Color.BLUE)
                        .geodesic(true));

    }


    public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

        private List<String> mDates;
        private int selectedItem = -1;

        public DateAdapter(List<String> dates) {
            mDates = dates;
        }

        @Override
        public int getItemCount() {
            return mDates.size();
        }

        @Override
        public DateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.list_item_date, parent, false);
            return new DateViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(DateViewHolder holder, int position) {
            String date = mDates.get(position);
            holder.dateTextView.setText(date.split(",")[0]);
            holder.dateTextView1.setText("" + date.split(",")[1]);


            if (selectedItem == position) {
                holder.pillTextView.setBackground(ContextCompat.getDrawable(PackersAndMover3Activity.this,R.drawable.boxfill));
            } else {
                holder.pillTextView.setBackground(ContextCompat.getDrawable(PackersAndMover3Activity.this,R.drawable.box));

            }


            holder.pillTextView.setOnClickListener(v -> {

                int previousSelectedItem = selectedItem;
                selectedItem = position;
                logisticDate = futureDatesListPass.get(position);
                if (previousSelectedItem != -1) {
                    notifyItemChanged(previousSelectedItem);
                }
                notifyItemChanged(selectedItem);

            });
        }

        public class DateViewHolder extends RecyclerView.ViewHolder {

            TextView dateTextView;
            TextView dateTextView1;

            LinearLayout pillTextView;

            public DateViewHolder(View itemView) {
                super(itemView);
                dateTextView = itemView.findViewById(R.id.date_text_view);
                dateTextView1 = itemView.findViewById(R.id.date_text_view1);

                pillTextView = itemView.findViewById(R.id.pillTextView);
            }
        }
    }

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

            double kgTotal;
            if (roadDistance <= vehicleData.getUkms()) {
                kgTotal = vehicleData.getUkms();
                // Handle case where total distance is less than or equal to 2 km
            } else {
                double remainingDistance = roadDistance - vehicleData.getUkms();
                double additionalCharge = remainingDistance * vehicleData.getAprice();
                kgTotal = vehicleData.getUprice() + additionalCharge;
                // Handle case where total distance is greater than 2 km
            }
            total = total + kgTotal;

            Log.e("Distance", "roadDistance" + new DecimalFormat("#.00").format(total));

            txtTotal.setText(sessionManager.getStringData(SessionManager.currency) + new DecimalFormat("#.00").format(total));

        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (paymentsucsses == 1) {
            paymentsucsses = 0;
            placeOrder();
        }
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

}