package com.i3cubes.acp_controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class SetWifiActivity extends AppCompatActivity {

    public static int REQUEST_BLUETOOTH = 1;
    AppCompatButton but_ok,but_cancel;
    ImageView but_home,but_settings;
    EditText ip_ssid,ip_password;
    TextView name;
    boolean admin=false;
    String admin_str="NO";
    private SetWiFiCommunicator setWifiCommunicator;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_wifi);

        but_home=(ImageView) findViewById(R.id.but_img_wifi_search);
        but_settings=(ImageView) findViewById(R.id.but_img_wifi_settings);

        but_ok=(AppCompatButton) findViewById(R.id.but_wifi_ok);
        but_cancel=(AppCompatButton) findViewById(R.id.but_wifi_cancel);

        ip_ssid=(EditText) findViewById(R.id.ip_ssid);
        ip_password=(EditText) findViewById(R.id.id_psw);

        admin_str=getIntent().getStringExtra("ADMIN");
        if(getIntent().getStringExtra("MAC")!="") {

            final BluetoothManager bluetoothManager=(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter=bluetoothManager.getAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, REQUEST_BLUETOOTH);
            }
            this.setWifiCommunicator = new SetWiFiCommunicator(this,mBluetoothAdapter);

            this.setWifiCommunicator.getBTDevice(getIntent().getStringExtra("MAC"));
            this.setWifiCommunicator.connectDevice();
        }

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
        but_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login=new Intent(getApplicationContext(), SettingsActivity.class);
                login.putExtra("ADMIN", admin_str);
                startActivity(login);
                finish();
            }
        });

        but_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid=ip_ssid.getText().toString();
                String psw=ip_password.getText().toString();
                setWifiCommunicator.writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221110", ssid);
                finish();
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
    public void goBack(){
        finish();
    }
    public class SetWiFiCommunicator extends AcpCommunicator{
        public SetWiFiCommunicator(Context mContext, BluetoothAdapter BTa) {
            super(mContext, BTa);
        }

        @Override
        public void ProcessWriteCharacteristicValue(String uuid, String value) {
            super.ProcessWriteCharacteristicValue(uuid, value);
            System.out.println("WRITTEN:"+value);
            if(uuid.equalsIgnoreCase("ffeeddcc-bbaa-9988-7766-554433221110")){
                writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221111", ip_password.getText().toString());
            }
            if(uuid.equalsIgnoreCase("ffeeddcc-bbaa-9988-7766-554433221111")){
                closeConnection();
                goBack();
            }
        }
    }
}