package com.i3cubes.acp_controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    AppCompatButton but_ok,but_cancel;
    ImageView but_home,but_settings;
    EditText ip_logi_code;
    boolean admin=false;
    String admin_str="NO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        but_home= (ImageView) findViewById(R.id.but_img_login_home);
        but_settings=(ImageView) findViewById(R.id.but_img_lgin_settings);
        but_ok=(AppCompatButton) findViewById(R.id.but_login_ok);
        but_cancel=(AppCompatButton) findViewById(R.id.but_login_cancel);
        ip_logi_code=(EditText) findViewById(R.id.ip_login_code);

        admin_str=getIntent().getStringExtra("ADMIN");
        String device_name=getIntent().getStringExtra("NAME");
        if(admin_str!=null && admin_str.equalsIgnoreCase("YES")){
            admin=true;
            Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
            settings.putExtra("ADMIN", "YES");
            settings.putExtra("MAC", getIntent().getStringExtra("MAC"));
            settings.putExtra("NAME", device_name);
            startActivity(settings);
            finish();
        }
        else{
            admin=false;
        }

        but_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent login=new Intent(getApplicationContext(), MainActivity.class);
                login.putExtra("AUTO_SCAN", "YES");
                startActivity(login);
                finish();
            }
        });
        but_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        but_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=ip_logi_code.getText().toString();
                Log.i("Login Code","login value:"+name);
                //writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221106",name);
                if(name.equals("Admin")){
                    makeTost(R.string.pop_login_success);
                    admin=true;
                    Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                    settings.putExtra("ADMIN", "YES");
                    settings.putExtra("MAC", getIntent().getStringExtra("MAC"));
                    settings.putExtra("NAME", device_name);
                    startActivity(settings);
                    finish();
                }
                else{
                    admin=false;
                    makeTost(R.string.pop_login_fail);
                }
            }
        });
        but_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent login=new Intent(getApplicationContext(), MainActivity.class);
                //startActivity(login);
                finish();
            }
        });
    }

    private void makeTost(int str){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
            }
        });

    }
}