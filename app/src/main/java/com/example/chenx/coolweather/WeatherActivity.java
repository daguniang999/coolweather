package com.example.chenx.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chenx.coolweather.gson.Forecast;
import com.example.chenx.coolweather.gson.LifestyleType;
import com.example.chenx.coolweather.gson.Weather;
import com.example.chenx.coolweather.util.HttpUtil;
import com.example.chenx.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView comfText;
    private TextView drsgText;
    private TextView fluText;
    private TextView sportText;
    private TextView travText;
    private TextView cwText;
    private TextView airText;

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefreshLayout;
    private String mWeatherId;

    public DrawerLayout drawerLayout;
    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        setContentView(R.layout.activity_weather);

        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
        comfText=(TextView)findViewById(R.id.comf_text);
        drsgText=(TextView)findViewById(R.id.drsg_text);
        fluText=(TextView)findViewById(R.id.flu_text);
        sportText=(TextView)findViewById(R.id.sport_text);
        travText=(TextView)findViewById(R.id.trav_text);
        cwText=(TextView)findViewById(R.id.cw_text);
        airText=(TextView)findViewById(R.id.air_text);
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        navButton=(Button)findViewById(R.id.nav_button);

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);

        String bingPic=prefs.getString("bing.pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

        if(weatherString!=null){
            Weather weather=Utility.handleWeatherResponse(weatherString);
            mWeatherId=weather.basic.cid;
            showWeatherInfo(weather);
        }else {
            String weatherId=getIntent().getStringExtra("weather_id");
           // Log.d("cccc", "onCreate: "+weatherId);
            mWeatherId=weatherId;
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });


    }

    private void loadBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }
        });


    }

    public void requestWeather(String weatherId){
        String weatherUrl="http://106.15.190.83:4545/weather.aspx?location="+weatherId+"&key=149f26e9e37843f8a2e09169075edf9b";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
              //  Log.d("cccc", "onResponse: "+responseText);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();

                            mWeatherId=weather.basic.cid;//??????
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });


            }
        });

        loadBingPic();
    }

    private void showWeatherInfo(Weather weather){
        String cityName=weather.basic.location;
        String updateTime=weather.update.loc.split(" ")[1];
        String degree=weather.now.tmp+"℃";
        String weatherInfo=weather.now.cond_txt;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        forecastLayout.removeAllViews();

        for(Forecast forecast:weather.daily_forecast){
            View view=LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=(TextView)view.findViewById(R.id.date_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_text);
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
            TextView minText=(TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.cond_txt_d);
            maxText.setText(forecast.tmp_max);
            minText.setText(forecast.tmp_min);
            forecastLayout.addView(view);
        }

        for(LifestyleType lifestyleType:weather.lifestyle){
                switch (lifestyleType.type){
                    case "comf":
                        comfText.setText("舒适度:"+lifestyleType.txt);
                        break;
                    case "drsg":
                        drsgText.setText("衣着:"+lifestyleType.txt);
                        break;
                    case "flu":
                        fluText.setText("头发:"+lifestyleType.txt);
                        break;
                    case "sport":
                        sportText.setText("运动:"+lifestyleType.txt);
                        break;
                    case "trav":
                        travText.setText("旅游:"+lifestyleType.txt);
                        break;
                    case "cw":
                        cwText.setText("洗车:"+lifestyleType.txt);
                        break;
                    case "air":
                        airText.setText("空气:"+lifestyleType.txt);
                        break;
                    default :break;

                }


        }



        weatherLayout.setVisibility(View.VISIBLE);
    }
}
