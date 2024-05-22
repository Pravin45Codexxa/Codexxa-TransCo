package com.codexxatransco.user.model;

import com.google.gson.annotations.SerializedName;

public class Login {

	@SerializedName("ResponseCode")
	private String responseCode;

	@SerializedName("ResponseMsg")
	private String responseMsg;

	@SerializedName("UserLogin")
	private User userLogin;

	@SerializedName("Result")
	private String result;

	public String getResponseCode(){
		return responseCode;
	}

	public String getResponseMsg(){
		return responseMsg;
	}

	public com.codexxatransco.user.model.User getUserLogin(){
		return userLogin;
	}

	public String getResult(){
		return result;
	}
}