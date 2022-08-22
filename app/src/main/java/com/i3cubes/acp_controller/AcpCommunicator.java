package com.i3cubes.acp_controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AcpCommunicator {
    private static final long WRITE_DELAY=1000;
    private static final long READ_DELAY=1000;

    private Context mContext;

    private BluetoothDevice connected_BT_device;
    private BluetoothDevice btDevice;
    private BluetoothGatt mGatt;
    private BluetoothAdapter mBluetoothAdapter;

    private boolean onCommunication=false;
    private HashMap<String, Boolean> communicating_map=new HashMap<>();

    private Handler mHandler=new Handler();
    private Handler keepAliveHandler=new Handler();
    private Runnable keepAliveRannable;
    private long last_communication_time;
    private boolean retry_connecting=true;

    public AcpCommunicator(Context mContext,BluetoothAdapter BTa) {
        this.mContext = mContext;
        this.mBluetoothAdapter=BTa;


        this.keepAliveRannable=new Runnable() {
            @Override
            public void run() {
                Log.w("KEEP ALIVE", "{Called on AcpConnector} Last communicated time::"+last_communication_time+" Current time::"+System.currentTimeMillis());
                //if(keep_alive_counter==2) { //15sec
                //keep_alive_counter=0;
                //Log.d("Handlers", "Called on main thread");
                if (System.currentTimeMillis() - last_communication_time > 46000) {//46sec
                    closeConnection();
                    keepAliveHandler.removeCallbacks(this);//
                    System.out.println("Removed the call back");
                    if(retry_connecting){
                        retry_connecting=false;
                        System.out.println("Retry Connecting::");
                        connectDevice();
                    }
                }
                else if (System.currentTimeMillis() - last_communication_time > 14000) {//14sec   ----14000
                    writeCharacteristic("ffeeddcc-bbaa-9988-7766-55443322110e", Long.toString(System.currentTimeMillis()));
                } else {
                    Log.d("Handlers", "No need to send keep alive");
                }
                // }
                //else{
                //keep_alive_counter++;
                //}
                keepAliveHandler.postDelayed(this, 3000);//3sec
            }
        };

    }

    public void  getBTDevice(String mac){
        if(mac!=null || mac!="") {
            BluetoothDevice btDevice_t = mBluetoothAdapter.getRemoteDevice(mac);
            this.btDevice = btDevice_t;
        }
    }
    public void connectDevice(){
        if(mGatt==null && this.btDevice!=null){
            mGatt=this.btDevice.connectGatt(this.mContext,false,gattCallback);

        }
        else{
            Log.e("Connect Device:","device already connected");
        }
    }
    public void closeConnection(){
        if(mGatt!=null){
            mGatt.disconnect();
            mGatt=null;
            btDevice=null;
            keepAliveHandler.removeCallbacks(keepAliveRannable);

            Log.e("CLOSING CONNECTION:","Connection Status: CLOSED");
        }
        else{
            mGatt=null;
            btDevice=null;
            keepAliveHandler.removeCallbacks(keepAliveRannable);
            Log.e("CLOSING CONNECTION:","Connection Status: already CLOSED");
        }
    }
    private void stopCommunicating(String uuid){
        System.out.println("run timeout callback for read of "+uuid);
        onCommunication=false;
        if(communicating_map.get(uuid)!=null){
            if (communicating_map.get(uuid)) {
                System.out.println("still communicating : " + uuid);
                communicating_map.put(uuid, false);

            } else {
                System.out.println("communication ended");
            }
        }
    }

    private final BluetoothGattCallback gattCallback=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange","Status:"+status);

            switch (newState){
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback","STATE_CONNECTED");
                    gatt.discoverServices();
                    last_communication_time=System.currentTimeMillis();
                    keepAliveHandler.post(keepAliveRannable);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i("gattCallback","STATE_DISCONNECTED");
                    mGatt.close();
                    mGatt=null;

                    keepAliveHandler.removeCallbacks(keepAliveRannable);
                    break;
                default:
                    Log.e("gattCallback","STATE_OTHER");
                    mGatt.close();
                    mGatt=null;
                    keepAliveHandler.removeCallbacks(keepAliveRannable);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services =gatt.getServices();
            Log.i("onServiceDiscovered:",services.toString()+"::"+status);
            for(BluetoothGattService gattService:services){
                Log.i("SERVICE:",gattService.getUuid().toString());
                List<BluetoothGattCharacteristic> gattCharacteristics=gattService.getCharacteristics();
                for(BluetoothGattCharacteristic gattCharacteristic:gattCharacteristics){
                    Log.i("CHARACTERISTIC:",gattCharacteristic.getUuid().toString());
                }
            }
            //Write auth data after discovery
            //writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221101","admin");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221101","Admin");
                }
            },2000);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221102");
                }
            },4000);

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicRead",characteristic.getUuid().toString()+" VALUE:"+new String(characteristic.getValue()));
            //last_communication_time=System.currentTimeMillis(); // Set lat communicated time
            communicating_map.put(characteristic.getUuid().toString(),false);
            onCommunication=false;
            ProcessReadCharacteristicValue(characteristic.getUuid().toString(),new String(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicWrite",characteristic.getUuid().toString());
            communicating_map.put(characteristic.getUuid().toString(),false);
            onCommunication=false;
            //last_communication_time=System.currentTimeMillis(); // Set lat communicated time
            ProcessWriteCharacteristicValue(characteristic.getUuid().toString(),new String(characteristic.getValue()));
        }
        @Override
        public void onMtuChanged(android.bluetooth.BluetoothGatt gatt, int mtu, int status){
            Log.d("BLE","onMtuChanged mtu="+mtu+",status="+status);
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
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopCommunicating(uuid);
                        }
                    },WRITE_DELAY);
                    mGatt.writeCharacteristic(characteristic);
                    communicating_map.put(uuid,true);
                    onCommunication=true;
                }
                else{
                    Log.e("WriteChar","characteristics is null");
                }
            }
            else{
                Log.e("WriteChar","service is null");
                //makeTost(R.string.sys_msg4);
            }
        }
        else{
            Log.e("WriteChar","mGat is null or still communicating:"+uuid);
            //releaseButtonPressed(uuid);
            //makeTost(R.string.sys_msg5);
        }
    }
    public void readCharacteristic(String uuid){
        if(mGatt!=null && !onCommunication) {
            BluetoothGattService service = mGatt.getService(UUID.fromString("ffeeddcc-bbaa-9988-7766-554433221100"));
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(uuid));
                if (characteristic != null) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("set timeout for read");
                            stopCommunicating(uuid);
                        }
                    },READ_DELAY);
                    mGatt.readCharacteristic(characteristic);
                    communicating_map.put(uuid,true);
                    onCommunication=true;
                }
                else{

                }
            }
            else{
                Log.e("ReadChar","service is null");
            }
        }
        else{
            Log.e("ReadChar","mGat is null or still communicating:"+uuid);
        }
    }

    public void ProcessReadCharacteristicValue(String uuid,String value){
        System.out.println("Processing Read Characteristic:"+uuid+"="+value);
        //Need to overloading at implentation
    }
    public void ProcessWriteCharacteristicValue(String uuid,String value){
        System.out.println("Processing Write Characteristic:"+uuid+"="+value);
        //Need to overloading at implentation
    }
}
