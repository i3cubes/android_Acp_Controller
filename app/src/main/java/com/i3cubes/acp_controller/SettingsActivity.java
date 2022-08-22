package com.i3cubes.acp_controller;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    AppCompatButton but_ok,but_cancel;
    ImageView but_home,but_settings;
    ImageView but_set_name,but_set_presence,but_set_wifi_ssid,but_set_wifi_psw;
    TextView tv_logout;
    //EditText ip_logi_code;
    TextView name,pesence,ssid,password;
    boolean admin=false;
    String admin_str="NO";
    String device_name="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        but_home=(ImageView) findViewById(R.id.but_img_settings_search);
        but_settings=(ImageView) findViewById(R.id.but_img_ssettings_ettings);

        but_set_name=(ImageView) findViewById(R.id.but_setting_name);
        but_set_presence=(ImageView) findViewById(R.id.but_setting_presence);
        but_set_wifi_ssid=(ImageView) findViewById(R.id.but_setting_ssis);
        but_set_wifi_psw=(ImageView) findViewById(R.id.but_setting_psw);
        tv_logout=(TextView) findViewById(R.id.tv_logout);
        name=(TextView) findViewById(R.id.settings_name);

        admin_str=getIntent().getStringExtra("ADMIN");
        device_name=getIntent().getStringExtra("NAME");
        name.setText(device_name);

        but_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent login=new Intent(getApplicationContext(), MainActivity.class);
                login.putExtra("AUTO_SCAN", "YES");
                login.putExtra("ADMIN", admin_str);
                startActivity(login);
                finish();
            }
        });
        tv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent login=new Intent(getApplicationContext(), MainActivity.class);
                login.putExtra("AUTO_SCAN", "YES");
                login.putExtra("ADMIN", admin_str);
                startActivity(login);
                finish();
            }
        });
        but_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        but_set_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(getApplicationContext(), SetNameActivity.class);
                intent.putExtra("ADMIN", admin_str);
                intent.putExtra("MAC", getIntent().getStringExtra("MAC"));
                startActivity(intent);
            }
        });
        but_set_presence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(getApplicationContext(), SetPresenceActivity.class);
                intent.putExtra("ADMIN", admin_str);
                intent.putExtra("MAC", getIntent().getStringExtra("MAC"));
                startActivity(intent);
            }
        });
        but_set_wifi_ssid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(getApplicationContext(), SetWifiActivity.class);
                intent.putExtra("ADMIN", admin_str);
                intent.putExtra("MAC", getIntent().getStringExtra("MAC"));
                startActivity(intent);
            }
        });
        but_set_wifi_psw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(getApplicationContext(), SetWifiActivity.class);
                intent.putExtra("ADMIN", admin_str);
                intent.putExtra("MAC", getIntent().getStringExtra("MAC"));
                startActivity(intent);
            }
        });
    }
}