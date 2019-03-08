package com.example.chenx.coolweather.util;

import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;

import com.example.chenx.coolweather.db.City;
import com.example.chenx.coolweather.db.County;
import com.example.chenx.coolweather.db.Province;
import com.example.chenx.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {

    public static boolean handleProvinceRespones(String respones){
        if(!TextUtils.isEmpty(respones)){
            try {

                JSONArray allProvinces = new JSONArray(respones);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province=new Province();//
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.setProvinceName(provinceObject.getString("name"));
                    province.save();
                }

                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }

        }

        return false;

    }
    public static boolean handleCityRespones(String respones,int provinceId){
        if(!TextUtils.isEmpty(respones)){
            try {

                JSONArray allCities = new JSONArray(respones);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city=new City();//
                    city.setCityCode(cityObject.getInt("id"));
                    city.setCityName(cityObject.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }

                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }

        }

        return false;

    }

    public static boolean handleCountyRespones(String respones,int cityId){
        if(!TextUtils.isEmpty(respones)){
            try {

                JSONArray allCounties = new JSONArray(respones);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county=new County();//
                    county.setCityId(cityId);
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.save();
                }

                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }

        }

        return false;

    }


    public static Weather handleWeatherResponse(String response){

        try{
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather6");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            Log.d("cccc", "handleWeatherResponse: "+weatherContent);

            return new Gson().fromJson(weatherContent,Weather.class);

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }



}
