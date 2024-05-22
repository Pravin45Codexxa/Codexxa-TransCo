package com.codexxatransco.user.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.codexxatransco.user.R;
import com.codexxatransco.user.fregment.CategoryFragment;
import com.codexxatransco.user.model.PackersMovers;
import com.codexxatransco.user.retrofit.APIClient;
import com.codexxatransco.user.retrofit.GetResult;
import com.codexxatransco.user.utility.CustPrograssbar;
import com.codexxatransco.user.utility.FromMover;
import com.codexxatransco.user.utility.MyData;
import com.codexxatransco.user.utility.MyDatabaseHelper;
import com.codexxatransco.user.utility.ToMover;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class PackersAndMover2Activity extends BaseActivity implements GetResult.MyListener, ViewPager.OnPageChangeListener {

    @BindView(R.id.img_back)
    public ImageView imgBack;
    @BindView(R.id.appbar)
    public AppBarLayout appbar;
    @BindView(R.id.viewpager)
    public ViewPager viewpager;
    @BindView(R.id.txt_continue)
    public TextView txtContinue;
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    CustPrograssbar custPrograssbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packers_and_mover2);
        ButterKnife.bind(this);
        custPrograssbar = new CustPrograssbar();
        viewPager = findViewById(R.id.viewpager);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.addOnPageChangeListener(this);

        getCategory();

    }

    private void getCategory() {
        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", "Nodata");

        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(jsonObject.toString(),MediaType.parse(getString(R.string.application_json)));
        Call<JsonObject> call = APIClient.getInterface().getproduct(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");

    }

    PackersMovers packersMovers;

    @Override
    public void callback(JsonObject result, String callNo) {
        try {
            custPrograssbar.closePrograssBar();
            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                packersMovers = gson.fromJson(result.toString(), PackersMovers.class);
                if (packersMovers.getResult().equalsIgnoreCase("true")) {
                    for (int i = 0; i < packersMovers.getCategoryData().size(); i++) {

                        viewPagerAdapter.add(packersMovers.getCategoryData().get(i).getMainCatTitle() + "(" + packersMovers.getCategoryData().get(i).getSubcatData().size() + ")");

                    }
                    viewPager.setAdapter(viewPagerAdapter);
                    tabLayout = findViewById(R.id.tab_layout);
                    tabLayout.setupWithViewPager(viewPager);
                }

            }
        } catch (Exception e) {
        Log.e("Error","--"+e.getMessage());
        }

    }

    @OnClick({R.id.img_back, R.id.txt_continue})
    public void onBindClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.txt_continue:
                MyDatabaseHelper databaseHelper = new MyDatabaseHelper(this);

                List<MyData> myData = databaseHelper.getAllData();
                if(!myData.isEmpty()){
                    FromMover fromMover = (FromMover) getIntent().getSerializableExtra("from");
                    ToMover toMovera = (ToMover) getIntent().getSerializableExtra("to");
                    startActivity(new Intent(this, PackersAndMover3Activity.class)
                            .putExtra("vehicle",packersMovers.getVehicledata())
                            .putExtra("from", fromMover).putExtra("to", toMovera));
                }else {
                    Toast.makeText(this,"select item and category",Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Log.e("position", "6RR" + position);

    }

    @Override
    public void onPageSelected(int position) {
        Log.e("position", "6" + position);


    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {


        private final List<String> fragmentTitle = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        public void add(String title) {

            fragmentTitle.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            CategoryFragment fragment = new CategoryFragment().newInstance(packersMovers, position);
            return fragment;
        }

        @Override
        public int getCount() {
            return packersMovers.getCategoryData().size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitle.get(position);
        }
    }


}