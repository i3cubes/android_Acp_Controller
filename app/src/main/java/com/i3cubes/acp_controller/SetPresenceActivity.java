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

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class SetPresenceActivity extends AppCompatActivity {

    public static int REQUEST_BLUETOOTH = 1;
    AppCompatButton but_ok,but_cancel;
    ImageView but_home,but_settings;
    EditText ip_presence;
    TextView name;
    boolean admin=false;
    String admin_str="NO";
    private SetPresenceCommunicator setPresenceCommunicator;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_presence);

        but_home=(ImageView) findViewById(R.id.but_img_presence_search);
        but_settings=(ImageView) findViewById(R.id.but_img_presence_settings);

        but_ok=(AppCompatButton) findViewById(R.id.but_presence_ok);
        but_cancel=(AppCompatButton) findViewById(R.id.but_presence_cancel);
        ip_presence=(EditText) findViewById(R.id.id_presence);

        admin_str=getIntent().getStringExtra("ADMIN");
        if(getIntent().getStringExtra("MAC")!="") {

            final BluetoothManager bluetoothManager=(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter=bluetoothManager.getAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, REQUEST_BLUETOOTH);
            }
            this.setPresenceCommunicator = new SetPresenceCommunicator(this,mBluetoothAdapter);

            this.setPresenceCommunicator.getBTDevice(getIntent().getStringExtra("MAC"));
            this.setPresenceCommunicator.connectDevice();
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
                String pres=(String) ip_presence.getText().toString();
                setPresenceCommunicator.writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221109", pres);
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
    public class SetPresenceCommunicator extends AcpCommunicator{
        public SetPresenceCommunicator(Context mContext, BluetoothAdapter BTa) {
            super(mContext, BTa);
        }

        @Override
        public void ProcessWriteCharacteristicValue(String uuid, String value) {
            super.ProcessWriteCharacteristicValue(uuid, value);
            System.out.println("WRITTEN:"+value);
            if(uuid.equalsIgnoreCase("ffeeddcc-bbaa-9988-7766-554433221109")){
                closeConnection();
                goBack();
            }
        }
    }
}