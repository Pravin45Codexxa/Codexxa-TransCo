package com.codexxatransco.user.retrofit;


import com.google.gson.JsonObject;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UserService {

	@Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "reg_user.php")
    Call<JsonObject> createUser(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "getzone.php")
    Call<JsonObject> getzone(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "vehiclelist.php")
    Call<JsonObject> vehiclelist(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "countrycode.php")
    Call<JsonObject> getCodelist(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "mobile_check.php")
    Call<JsonObject> mobileCheck(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "user_login.php")
    Call<JsonObject> userLogin(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "forget_password.php")
    Call<JsonObject> getForgot(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "couponlist.php")
    Call<JsonObject> getCouponList(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "check_coupon.php")
    Call<JsonObject> checkCoupon(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "order_now.php")
    Call<JsonObject> orderNow(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "map_info.php")
    Call<JsonObject> tripinfo(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "order_history.php")
    Call<JsonObject> orderHistory(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "logistic_history.php")
    Call<JsonObject> logisticHistory(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "parcel_history.php")
    Call<JsonObject> parcelHistory(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "logistic_information.php")
    Call<JsonObject> logisticInformation(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "parcel_information.php")
    Call<JsonObject> parcelInformation(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "cancle.php")
    Call<JsonObject> tripCancle(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "wallet_report.php")
    Call<JsonObject> walletReport(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "getdata.php")
    Call<JsonObject> getdata(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "paymentgateway.php")
    Call<JsonObject> getPaymentList(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "wallet_up.php")
    Call<JsonObject> walletUp(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "getproduct.php")
    Call<JsonObject> getproduct(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "logistic.php")
    Call<JsonObject> logistic(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "parcel.php")
    Call<JsonObject> parcel(@Body RequestBody requestBody);

    @Headers("X-API-KEY:cscodetech")
    @POST(APIClient.APPEND_URL + "getquote.php")
    Call<JsonObject> getquote(@Body RequestBody requestBody);




}
