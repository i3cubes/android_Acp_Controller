package com.i3cubes.acp_controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class AcpConnect extends AppCompatActivity {
    TextView log_view;
    TextView dev_name;
    private Handler handler;
    BluetoothSocket btSocket=null;
    private boolean isBTConnected=false;
    BluetoothAdapter btAdoptor=null;
    String devAddress;
    String devName;


    BluetoothDevice btDevice=null;
    private BluetoothGatt mGatt;
    private boolean mGatt_dataSending=false;

    AlertDialog.Builder builder;

    final Context c = this;

    @Override
    protected void onStop() {
        closeConnection();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        closeConnection();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        closeConnection();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extra=getIntent().getExtras();
        devName=extra.getString("NAME");
        devAddress=extra.getString("ADDRESS");
        dev_name=(TextView) findViewById(R.id.device_name);

        setContentView(R.layout.activity_acp_connect);
        log_view=(TextView) findViewById(R.id.acpc_log_view);
        Button connect=(Button) findViewById(R.id.b_connect);
        Button disconnect=(Button) findViewById(R.id.b_disconnect);
        Button up=(Button) findViewById(R.id.b_stepup);
        Button down=(Button) findViewById(R.id.b_stepdown);
        Button close=(Button) findViewById(R.id.b_close);
        Button login=(Button) findViewById(R.id.b_login);
        Button status=(Button) findViewById(R.id.b_status) ;
        Button open=(Button) findViewById(R.id.b_open);
        Button swing=(Button) findViewById(R.id.b_swing);
        Button set_name=(Button) findViewById(R.id.b_set_name);
        Button presence=(Button) findViewById(R.id.b_presence);
        Button wifi=(Button) findViewById(R.id.b_wifi);

        builder=new AlertDialog.Builder(this);
        //dev_name.setText(devName);
        defineHandler();
        initDevice();

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //log_view.setText("Conncting to Device....");
                //log_view.append("Connecting to Device.....");
                connectToDevice(btDevice);

            }
        });


        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeConnection();
            }
        });
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221104");
            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221105");
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221103");
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221101","Admin");
            }
        });
        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221102");
            }
        });
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221107");
            }
        });
        swing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221108");
            }
        });
        set_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setTitle("Change Device Name");
                builder.setMessage("Enter Device Name");
                final EditText input=new EditText(getApplicationContext());
                builder.setView(input);

                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name=input.getText().toString();
                        Log.i("SET NAME","name value:"+name);
                        writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221106",name);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
            }
        });
        presence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setTitle("Change Presence Value");
                builder.setMessage("Enter Presence Value(0-60)");
                final EditText input=new EditText(getApplicationContext());
                builder.setView(input);

                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String val=input.getText().toString();
                        Log.i("SET PRESENCE","presence value:"+val);
                        writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221109",val);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
            }
        });
        wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater=LayoutInflater.from(c);
                View mview=layoutInflater.inflate(R.layout.wifi_input_dialog,null);
                AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(c);
                dialogBuilder.setView(mview);
                final EditText te_ssid=(EditText) mview.findViewById(R.id.i_ssid);
                final EditText password=(EditText) mview.findViewById(R.id.i_password);
                dialogBuilder.setCancelable(true).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("Wifi SAVE");
                        String val_ssid=te_ssid.getText().toString();
                        String val_psw=password.getText().toString();
                        Log.i("SET WIFI"," SSID=:"+val_ssid);
                        writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221110",val_ssid);
                        mGatt_dataSending=true;
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!mGatt_dataSending) {
                                    Log.i("SET WIFI", " PSW=:" + val_psw);
                                    writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221111", val_ssid);
                                }
                                else{
                                    ShowMessage("WIFI : setting SSID not finished");
                                }
                            }
                        }, 5000);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                AlertDialog dialog=dialogBuilder.create();
                dialog.show();
            }
        });
    }

    private void initDevice(){
        final BluetoothManager bluetoothManager=(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdoptor=bluetoothManager.getAdapter();
        btDevice=btAdoptor.getRemoteDevice(devAddress);

    }
    private void defineHandler(){
        if(handler==null){
            handler=new Handler(){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    Bundle bun=msg.getData();
                    switch (msg.what){
                        case 100:
                            log_view.append("\n "+bun.getString("message"));
                        break;
                    }
                }
            };
        }
    }
    public void closeConnection(){
        if(mGatt!=null){
            mGatt.close();
            mGatt=null;
            ShowMessage("Connection Status: CLOSED");
        }
        else{
            ShowMessage("Already CLOSED");
        }
    }
    public void connectToDevice(BluetoothDevice device){
        if(mGatt==null){
            mGatt=device.connectGatt(this,false,gattCallback);
            mGatt.requestMtu(30);
        }
        else{
            Log.e("Connect Device:","device already connected");
        }
    }
    private final BluetoothGattCallback gattCallback=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange","Status:"+status);

            switch (newState){
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback","STATE_CONNECTED");
                    ShowMessage("Connection Status: CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i("gattCallback","STATE_DISCONNECTED");
                    ShowMessage("Connection Status: DISCONNECTED");
                    mGatt.close();
                    mGatt=null;
                    break;
                default:
                    Log.e("gattCallback","STATE_OTHER");
                    ShowMessage("Connection Status: OTHER");
                    mGatt.close();
                    mGatt=null;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services =gatt.getServices();
            Log.i("onServiceDiscovered:",services.toString());
            for(BluetoothGattService gattService:services){
                Log.i("SERVICE:",gattService.getUuid().toString());
                ShowMessage("SERVICE :"+gattService.getUuid().toString());
                List<BluetoothGattCharacteristic> gattCharacteristics=gattService.getCharacteristics();
                for(BluetoothGattCharacteristic gattCharacteristic:gattCharacteristics){
                    Log.i("CHARACTERISTIC:",gattCharacteristic.getUuid().toString());
                    ShowMessage("CHARACTERISTIC :"+gattCharacteristic.getUuid().toString());
                }
            }
            //Write auth data after discovery
            //writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221101","admin");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicRead",characteristic.toString());
            ShowMessage("READ :"+characteristic.getUuid().toString()+"|VAL="+characteristic.getValue());
            //gatt.disconnect();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicWrite",characteristic.toString());
            mGatt_dataSending=false;
            ShowMessage("WRITTEN :"+characteristic.getUuid().toString());
        }
        @Override
        public void onMtuChanged(android.bluetooth.BluetoothGatt gatt, int mtu, int status){
            Log.d("BLE","onMtuChanged mtu="+mtu+",status="+status);
            ShowMessage("MTU Changed to "+mtu);
        }
    };

    public void writeCharacteristic(String uuid,String value){
        if(mGatt!=null) {
            BluetoothGattService service = mGatt.getService(UUID.fromString("ffeeddcc-bbaa-9988-7766-554433221100"));
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(uuid));
                if (characteristic != null) {
                    characteristic.setValue(value.getBytes());
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    mGatt.writeCharacteristic(characteristic);
                }
            }
            else{
                Log.e("WriteChar","service is null");
            }
        }
        else{
            Log.e("WriteChar","mGat is null");
        }
    }
    public void readCharacteristic(String uuid){
        if(mGatt!=null) {
            BluetoothGattService service = mGatt.getService(UUID.fromString("ffeeddcc-bbaa-9988-7766-554433221100"));
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(uuid));
                if (characteristic != null) {
                    mGatt.readCharacteristic(characteristic);
                }
            }
            else{
                Log.e("ReadChar","service is null");
            }
        }
        else{
            Log.e("ReadChar","mGat is null");
        }
    }
    public void ShowMessage(String message){
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        msg.what = 100;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
    private class ConnectBT extends AsyncTask<Integer,Void,Void>{
        private boolean connected=true;
        String ser_UUID;
        @Override
        protected void onPreExecute() {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("message", "Connecting to Device ["+devAddress+"]");
            msg.what = 100;
            msg.setData(bundle);
            handler.sendMessage(msg);

            //pairDevice(btDevice);
        }

        @Override
        protected Void doInBackground(Integer... ints) {
            if(btSocket!=null || isBTConnected) {
                try {
                    btSocket.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
            ser_UUID=getUUID(ints[0]);

            try (BluetoothSocket bluetoothSocket = btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(ser_UUID))) {
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();
            }
            catch (IOException e){
                e.printStackTrace();
                connected=false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            msg.what = 100;
            if(connected){
                bundle.putString("message", "Connected to Device on UUID:"+ser_UUID);
                isBTConnected=true;
            }
            else{
                bundle.putString("message", "Failed to Connect to Device on UUID:"+ser_UUID);
            }
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    public void pairDevice(BluetoothDevice d){
        try {
            Method method=d.getClass().getMethod("createBond",(Class[]) null);
            method.invoke(d,(Object[]) null);
            log_view.append("\n Paring Done");
            System.out.println("Paring Done");
        } catch (Exception e) {
            e.printStackTrace();
            log_view.append("\n Paring Failed");
            System.out.println("Paring Failed");
        }
    }
    private String getUUID(Integer i){
        switch (i){
            case 101:
                return "FFEEDDCCBBAA99887766554433221101";
            case 104:
                return "FFEEDDCCBBAA99887766554433221104";
            case 105:
                return "FFEEDDCCBBAA99887766554433221105";
            case 103:
                return "FFEEDDCCBBAA99887766554433221103";
            default:
                return "FFEEDDCCBBAA99887766554433221103";
        }
    }
}