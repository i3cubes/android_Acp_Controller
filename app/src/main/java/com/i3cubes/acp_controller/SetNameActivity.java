package com.i3cubes.acp_controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class SetNameActivity extends AppCompatActivity {

    public static int REQUEST_BLUETOOTH = 1;

    AppCompatButton but_ok,but_cancel;
    ImageView but_home,but_settings;
    EditText ip_name;
    TextView name;
    boolean admin=false;
    String admin_str="NO";
    String device_name="";
    private SetNameCommunicator setNameCommunicator;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_name);

        but_home=(ImageView) findViewById(R.id.but_img_set_name_search);
        but_settings=(ImageView) findViewById(R.id.but_img_set_name_settings);

        but_ok=(AppCompatButton) findViewById(R.id.but_name_ok);
        but_cancel=(AppCompatButton) findViewById(R.id.but_name_cancel);

        ip_name=(EditText) findViewById(R.id.ip_name);

        admin_str=getIntent().getStringExtra("ADMIN");
        device_name=getIntent().getStringExtra("NAME");

        if(getIntent().getStringExtra("MAC")!="") {

            final BluetoothManager bluetoothManager=(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter=bluetoothManager.getAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, REQUEST_BLUETOOTH);
            }
            this.setNameCommunicator = new SetNameCommunicator(this,mBluetoothAdapter);

            this.setNameCommunicator.getBTDevice(getIntent().getStringExtra("MAC"));
            this.setNameCommunicator.connectDevice();
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
                String name=(String) ip_name.getText().toString();
                setNameCommunicator.writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221106", name);
                //finish();
            }
        });
        but_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent login=new Intent(getApplicationContext(), MainActivity.class);
                //startActivity(login);
                setNameCommunicator.closeConnection();
                //setNameCommunicator=null;
                finish();
            }
        });
    }
    public void goBack(){
        Intent login=new Intent(getApplicationContext(), MainActivity.class);
        login.putExtra("AUTO_SCAN", "YES");
        login.putExtra("ADMIN", admin_str);
        startActivity(login);
    }
    public class SetNameCommunicator extends AcpCommunicator{
        public SetNameCommunicator(Context mContext, BluetoothAdapter BTa) {
            super(mContext, BTa);
        }

        @Override
        public void ProcessWriteCharacteristicValue(String uuid, String value) {
            super.ProcessWriteCharacteristicValue(uuid, value);
            System.out.println("WRITTEN:"+value);
            if(uuid.equalsIgnoreCase("ffeeddcc-bbaa-9988-7766-554433221106")){
                closeConnection();
                goBack();
            }
        }
    }
}