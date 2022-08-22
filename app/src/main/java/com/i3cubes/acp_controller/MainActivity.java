package com.i3cubes.acp_controller;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import androidx.appcompat.app.ActionBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements RecycleViewAdapter.ItemListener{
    //BluetoothAdapter BTAdapter;
    public static int REQUEST_BLUETOOTH = 1;
    private static final long SCAN_PERIOD=10000;
    private static final long WRITE_DELAY=1000;
    private static final long READ_DELAY=1000;
    List<device> devices = new ArrayList<device>();
    List<String> device_names=new ArrayList<String>();
    HashMap<String, BluetoothDevice> device_map=new HashMap<>();
    HashMap<String, device> d_map=new HashMap<>();
    BluetoothDevice selectedDevice;
    private ArrayList<String> arrayList = new ArrayList<>();
    private Set<BluetoothDevice>pairedDevices;

    private ListView listView;
    private RecyclerView recycleView;
    private AutoFitGridLayoutManager layoutManager;
    int col_width;
    private ImageButton buttonScan;
    private boolean enable_scan=true;
    private AnimationDrawable scanAnimation;
    private UsersAdapter adapter2;
    private boolean togle=true;
    private ArrayAdapter<String> adapter;
    //ImageView b_img_scan;

    private Handler handler = new Handler();
    private Handler mHandler=new Handler();
    private Handler keepAliveHandler=new Handler();
    private Runnable keepAliveRannable;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private boolean mGatt_dataSending=false;
    private BluetoothDevice btDevice=null;
    private device nearestDevice;
    private int rssiHigh=-125;
    private boolean retry_connecting=true;
    private boolean deviceOpen=false;
    private boolean communicating=false;
    private HashMap<String, Boolean> communicating_map=new HashMap<>();
    private int verify_cmd_status_count=0;
    private String last_sent_cmd="",last_status_cmd;
    private String extreme_position="";
    private boolean admin=false;
    private long last_communication_time;
    private int keep_alive_counter=0;

    private Dialog popupDialog;
    AlertDialog.Builder builder;

    private ProgressBar progressBar;
    private boolean onCommunication=false;
    private boolean forced_stop_scan=false;

    private SharedPreference sp;

    //Buttons
    ImageButton b_settings;
    ImageButton but_power;
    ImageButton but_left;
    ImageButton but_right;
    ImageButton but_swing;
    ImageButton but_45;
    ImageButton but_135;
    ImageView img_status;
    SwitchCompat but_onoff;
    ImageView img_last_status;
    ImageView img_connected;
    ImageView img_signal;
    ImageView but_img_settings,but_img_search;
    //ImageView but_top_img_open,but_top_img_swing,but_top_img_135,but_top_img_45;
    Spinner device_spinner;

    TextView tv_connecting,tv_connected_dev_name,tv_degree;

    boolean ivb_power_enable=false,ivb_up_enable=false,ivb_down_enable=false,ivb_swing_enable=false,b_settings_enable=false,ivb_low_enable=false,ivb_max_enable=false;

    String center_display_aquired_by;
    String admin_str="NO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v4);
        device_spinner=(Spinner) findViewById(R.id.spinner_devices);
        //List<String> devices=new ArrayList<String>();
        //devices.add("Living Room-1");
        //devices.add("Living Room-2");
        sp=new SharedPreference();

        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, device_names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        device_spinner.setAdapter(adapter);
        device_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?>arg0, View view, int arg2, long arg3) {

                String selected_val=device_spinner.getSelectedItem().toString();
                System.out.println("DRPOP DOWN:"+selected_val);
                device ddd=getDeviceFromName(selected_val);
                if(ddd!=null){
                    closeConnection();
                    connectToDevice(device_map.get(ddd.getMac()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        buttonScan = findViewById(R.id.img_but_scan);

        tv_connecting=(TextView) findViewById(R.id.tv_connecting);
        tv_connected_dev_name=(TextView) findViewById(R.id.tv_connected_device_name);
        tv_degree=(TextView) findViewById(R.id.tv_degree);

        img_connected=(ImageView) findViewById(R.id.img_connected_status);
        img_signal=(ImageView) findViewById(R.id.img_connected_device_signal);

        //but_top_img_open=(ImageView) findViewById(R.id.img_open);
        //but_top_img_swing=(ImageView) findViewById(R.id.img_swing);
        //but_top_img_135=(ImageView) findViewById(R.id.img_135);
        //but_top_img_45=(ImageView) findViewById(R.id.img_45);

        ///getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ///getSupportActionBar().setCustomView(R.layout.title_bar_layout);
        ///b_img_scan=(ImageView) findViewById(R.id.b_img_scan);
        //b_img_scan.setBackgroundResource(R.drawable.scan_animation);
        //scanAnimation=(AnimationDrawable) b_img_scan.getBackground();
        //listView = findViewById(R.id.bt_list);
        ///recycleView=(RecyclerView) findViewById(R.id.bt_tile_list);

        ///b_settings=(ImageButton) findViewById(R.id.b_settings);
        but_power=(ImageButton) findViewById(R.id.but_open);
        ////ivb_up=(ImageView) findViewById(R.id.iv_up);
        ///ivb_down=(ImageView) findViewById(R.id.iv_down);
        but_swing=(ImageButton) findViewById(R.id.but_swing);
        but_45=(ImageButton) findViewById(R.id.but_45);
        but_135=(ImageButton) findViewById(R.id.but_135);
        but_left=(ImageButton) findViewById(R.id.but_left);
        but_right=(ImageButton) findViewById(R.id.but_right);
        //but_onoff=(SwitchCompat) findViewById(R.id.but_onoff);
        img_status=(ImageView) findViewById(R.id.status_center);
        img_status.setVisibility(View.GONE);
        tv_degree.setVisibility(View.GONE);

        but_img_search=(ImageView) findViewById(R.id.but_img_search);
        but_img_settings=(ImageView) findViewById(R.id.but_img_settings);

        admin_str=getIntent().getStringExtra("ADMIN");
        /*
        but_onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                RearrangeControls();
                if(isChecked){
                    //OFF
                    readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221103");
                }
                else {
                    //ON
                    readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221107");
                }
            }
        });

         */
        ///img_last_status=(ImageView) findViewById(R.id.imv_last_status);

        popupDialog=new Dialog(this);

        progressBar=(ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        ///builder=new AlertDialog.Builder(this);
        last_communication_time=System.currentTimeMillis();

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this,R.string.ble_not_support,Toast.LENGTH_SHORT).show();
            finish();
        }
        checkPermission();

        final BluetoothManager bluetoothManager=(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter=bluetoothManager.getAdapter();
        if(mBluetoothAdapter==null){
            new AlertDialog.Builder(this)
                    .setTitle(R.string.ble_err_title)
                    .setMessage(R.string.ble_err_msg)
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);
        }
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //scanAnimation.start();
                makeTost(R.string.sys_msg1);
                closeConnection();
                scanBLE();
                //addDummyDevices();
                //adapter.notifyDataSetChanged();
            }
        });
        but_img_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeTost(R.string.sys_msg1);
                closeConnection();
                scanBLE();
            }
        });
        but_img_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mGatt!=null) {
                    Intent login = new Intent(getApplicationContext(), Login.class);
                    login.putExtra("ADMIN", admin_str);
                    login.putExtra("MAC", selectedDevice.getAddress());
                    login.putExtra("NAME", d_map.get(selectedDevice.getAddress()).getName());
                    System.out.println("NAME-SETTINGS:"+d_map.get(selectedDevice.getAddress()).getName()+"||"+selectedDevice.getAddress());
                    stopScanBLE();
                    //mHandler.
                    closeConnection();
                    startActivity(login);
                    finish();
                }
                else{
                    makeTost(R.string.sys_msg6);
                }
            }
        });

        //adapter=new RecycleViewAdapter(this,(ArrayList<device>) devices,this);
        //recycleView.setAdapter(adapter);
        //col_width= ((Resources.getSystem().getDisplayMetrics().widthPixels)-100)/2;
        //layoutManager=new AutoFitGridLayoutManager(this,col_width);
        //recycleView.setLayoutManager(layoutManager);
        //Keep Alive
        this.keepAliveRannable=new Runnable() {
            @Override
            public void run() {
                Log.w("KEEP ALIVE", "Called on main thread} Last communicated time::"+last_communication_time+" Current time::"+System.currentTimeMillis());
                //if(keep_alive_counter==2) { //15sec
                    //keep_alive_counter=0;
                    //Log.d("Handlers", "Called on main thread");
                    if (System.currentTimeMillis() - last_communication_time > 46000) {//46sec
                        closeConnection();
                        disableControls();
                        keepAliveHandler.removeCallbacks(this);//
                        System.out.println("Removed the call back");
                        if(retry_connecting){
                            retry_connecting=false;
                            System.out.println("Retry Connecting::"+selectedDevice.getName());
                            connectToDevice(selectedDevice);
                        }
                    }
                    else if (System.currentTimeMillis() - last_communication_time > 14000) {//14sec   ----14000
                        writeCharacteristic("ffeeddcc-bbaa-9988-7766-55443322110e", Long.toString(System.currentTimeMillis()), false);
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

        //ONcliks
        but_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("UP Pressed");
                setButtonPressed("ffeeddcc-bbaa-9988-7766-55443322110b");
                readCharacteristic("ffeeddcc-bbaa-9988-7766-55443322110b");
                last_sent_cmd="STEPUP";
                /*
                if(ivb_up_enable) {
                    setButtonPressed("ffeeddcc-bbaa-9988-7766-55443322110b");
                    readCharacteristic("ffeeddcc-bbaa-9988-7766-55443322110b");
                    ivb_up_enable=false;

                }

                 */
            }
        });
        but_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonPressed("ffeeddcc-bbaa-9988-7766-55443322110c");
                readCharacteristic("ffeeddcc-bbaa-9988-7766-55443322110c");
                last_sent_cmd="STEPDOWN";
                /*
                if(ivb_down_enable) {
                    setButtonPressed("ffeeddcc-bbaa-9988-7766-55443322110c");
                    readCharacteristic("ffeeddcc-bbaa-9988-7766-55443322110c");
                    ivb_down_enable=false;

                }

                 */
            }
        });
        but_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //System.out.println("BUT PRESSED");
                if (deviceOpen) {
                    //Close
                    setButtonPressed("ffeeddcc-bbaa-9988-7766-554433221103");
                    readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221103");
                    last_sent_cmd="CLOSE";
                } else {
                    //Open
                    setButtonPressed("ffeeddcc-bbaa-9988-7766-554433221107");
                    readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221107");
                    last_sent_cmd="OPEN";
                }
                //setButtonPressed("ffeeddcc-bbaa-9988-7766-554433221107");
                //readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221107");

                /*
                if(ivb_power_enable) {
                    if (deviceOpen) {
                        //Close
                        setButtonPressed("ffeeddcc-bbaa-9988-7766-554433221103");
                        readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221103");
                        last_sent_cmd="CLOSE";
                    } else {
                        //Open
                        setButtonPressed("ffeeddcc-bbaa-9988-7766-554433221107");
                        readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221107");
                        last_sent_cmd="OPEN";
                    }
                    //ivb_power_enable=false;
                }

                 */

            }
        });

        but_swing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonPressed("ffeeddcc-bbaa-9988-7766-554433221108");
                readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221108");
                last_sent_cmd="SWING";
                /*
                if(ivb_swing_enable) {
                    //ivb_swing_enable=false;
                    setButtonPressed("ffeeddcc-bbaa-9988-7766-554433221108");
                    readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221108");

                }

                 */
            }
        });

        but_45.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonPressed("ffeeddcc-bbaa-9988-7766-554433221105");
                readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221105");
                last_sent_cmd="LOW";
                /*
                if(ivb_low_enable) {
                    setButtonPressed("ffeeddcc-bbaa-9988-7766-554433221105");
                    readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221105");
                    ivb_low_enable=false;

                }

                 */
            }
        });
        but_135.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setButtonPressed("ffeeddcc-bbaa-9988-7766-554433221104");
                readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221104");
                last_sent_cmd="HIGH";
                /*
                if(ivb_max_enable) {
                    setButtonPressed("ffeeddcc-bbaa-9988-7766-554433221104");
                    readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221104");
                    ivb_max_enable=false;

                }

                 */
            }
        });
        /*
        b_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(admin) {
                    if (mGatt != null) {
                        if (b_settings_enable) {
                            PopupMenu popup = new PopupMenu(MainActivity.this, view);
                            popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.set_name:
                                            setName();
                                            return true;
                                        case R.id.set_presence:
                                            setPresence();
                                            return true;
                                        case R.id.set_wifi:
                                            setWiFi();
                                            return true;
                                        case R.id.set_logout:
                                            logOut();
                                            return true;
                                        default:
                                            return true;
                                    }
                                }
                            });
                            popup.show();
                        } else {
                            System.out.println("Button disabled");
                        }
                    } else {
                        makeTost(R.string.sys_msg2);
                    }
                }
                else{
                    adminLogin();
                }
            }
        });

         */
        //String auto_scan=getIntent().getStringExtra("AUTO_SCAN");
        //System.out.println("AUTO:"+auto_scan);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mBluetoothAdapter ==null || !mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,1);
        }
        else{
            if(Build.VERSION.SDK_INT>=21){
                System.out.println("Set BT Adapter");
                mLEScanner=mBluetoothAdapter.getBluetoothLeScanner();
                settings=new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
                filters=new ArrayList<ScanFilter>();
            }
            scanBLE();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled()){
            stopScanBLE();
        }
    }
    @Override
    public void onDestroy() {
        closeConnection();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        closeConnection();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void setDegree(String dg){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("ANGLE SET:"+dg);
                tv_degree.setText(dg);
                img_status.setVisibility(View.GONE);
                buttonScan.setVisibility(View.GONE);
                tv_degree.setVisibility(View.VISIBLE);

            }
        });

    }
    public void setCenterDisplay(String type,String val){
        switch (type){
            case "DEG":
                setDegree(val);
                break;
            case "TICK":
                img_status.setVisibility(View.VISIBLE);
                tv_degree.setVisibility(View.GONE);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 20000);
                break;
            default:
                break;
        }
    }
    private void setButtonPressed(String uuid){
        System.out.println("SET BUT PRESSED:"+uuid);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RearrangeControls();
                switch (uuid){
                    case "ffeeddcc-bbaa-9988-7766-554433221103":
                        //power but -close
                        but_power.setImageResource(R.drawable.but_4_inv_close);
                        //ivb_up.setImageResource(R.drawable.p_arrow_up_gray);
                        //ivb_swing.setImageResource(R.drawable.p_swing_gray);
                        //ivb_down.setImageResource(R.drawable.p_arrow_down_gray);
                        ///ivb_power.setImageResource(R.drawable.img_but_open_gray);
                        break;
                    case "ffeeddcc-bbaa-9988-7766-55443322110b":
                        //UP
                        but_left.setImageResource(R.drawable.left_white);
                        but_left.setBackgroundResource(R.drawable.shadow_background_meroon);
                        //but_left.setPadding(0, left_right_but_padding, 0, left_right_but_padding);
                        ///ivb_up.setImageResource(R.drawable.p_arrow_up_pressed);
                        //ivb_swing.setImageResource(R.drawable.p_swing_gray);
                        //ivb_down.setImageResource(R.drawable.p_arrow_down_gray);
                        //ivb_power.setImageResource(R.drawable.p_power_gray);
                        break;
                    case "ffeeddcc-bbaa-9988-7766-55443322110c":
                        //DOWN
                        but_right.setImageResource(R.drawable.right_white);
                        but_right.setBackgroundResource(R.drawable.shadow_background_meroon);
                        //but_right.setPadding(0, left_right_but_padding, 0, left_right_but_padding);
                        ///ivb_down.setImageResource(R.drawable.p_arrow_down_pressed);
                        //ivb_up.setImageResource(R.drawable.p_arrow_up_gray);
                        //ivb_swing.setImageResource(R.drawable.p_swing_gray);
                        //ivb_power.setImageResource(R.drawable.p_power_gray);
                        break;
                    case "ffeeddcc-bbaa-9988-7766-554433221107":
                        //power but -open
                        but_power.setImageResource(R.drawable.but_4_inv);
                        //but_top_img_open.setImageResource(R.drawable.open_white);
                        //ivb_up.setImageResource(R.drawable.p_arrow_up_gray);
                        //ivb_swing.setImageResource(R.drawable.p_swing_gray);
                        //ivb_down.setImageResource(R.drawable.p_arrow_down_gray);
                        ///ivb_power.setImageResource(R.drawable.img_but_open_gray);
                        break;
                    case "ffeeddcc-bbaa-9988-7766-554433221108":
                        //Swing
                        but_swing.setImageResource(R.drawable.but_1_inv);
                        //but_top_img_swing.setImageResource(R.drawable.swing_white);
                        ///ivb_swing.setImageResource(R.drawable.img_but_swing_gray);
                        //ivb_down.setImageResource(R.drawable.p_arrow_down_gray);
                        //ivb_up.setImageResource(R.drawable.p_arrow_up_gray);
                        //ivb_power.setImageResource(R.drawable.p_power_gray);
                        break;
                    case "ffeeddcc-bbaa-9988-7766-554433221104":
                        //MAX
                        ///ivb_max.setImageResource(R.drawable.img_but_135_gray);
                        but_135.setImageResource(R.drawable.but_3_inv);
                        //but_top_img_135.setImageResource(R.drawable.up_135_white);
                        break;
                    case "ffeeddcc-bbaa-9988-7766-554433221105":
                        //LOW
                        ///ivb_low.setImageResource(R.drawable.img_but_45_gray);
                        but_45.setImageResource(R.drawable.but_2_inv);
                       // but_top_img_45.setImageResource(R.drawable.down_45_white);
                        break;
                }
            }
        });

    }
    private void releaseButtonPressed(String uuid){
        //RearrangeControls();
        if(mGatt!=null){

        }
        else{
            closeConnection();
            //disableControls();
        }
    }
    private void disableButton(String uuid){
        Log.e("BUT", "Disable Button:"+uuid);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (uuid){
                    case "ffeeddcc-bbaa-9988-7766-55443322110b":
                        ///ivb_up.setImageResource(R.drawable.p_arrow_up_gray);
                        ivb_up_enable=false;
                        break;
                    case "ffeeddcc-bbaa-9988-7766-55443322110c":
                        ///ivb_down.setImageResource(R.drawable.p_arrow_down_gray);
                        ivb_down_enable=false;
                        break;
                    case "ffeeddcc-bbaa-9988-7766-554433221108":
                        ///ivb_swing.setImageResource(R.drawable.img_but_swing_gray);
                        ivb_swing_enable = false;
                        break;
                    case "ffeeddcc-bbaa-9988-7766-554433221103":
                        ///ivb_power.setImageResource(R.drawable.img_but_open_gray);
                        ivb_power_enable = false;
                        break;
                    case "ffeeddcc-bbaa-9988-7766-554433221107":
                        ///ivb_power.setImageResource(R.drawable.img_but_open_gray);
                        ivb_power_enable = false;
                        break;
                    case "ffeeddcc-bbaa-9988-7766-554433221104":
                        //MAX
                        ///ivb_max.setImageResource(R.drawable.img_but_135_gray);
                        ivb_max_enable = false;
                        break;
                    case "ffeeddcc-bbaa-9988-7766-554433221105":
                        //LOW
                        ///ivb_low.setImageResource(R.drawable.img_but_45_gray);
                        ivb_low_enable = false;
                        break;
                }
            }
        });

    }
    private void enableButton(String uuid){
        Log.w("BUT", "Enable Button:"+uuid);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (uuid){
                    case "ffeeddcc-bbaa-9988-7766-55443322110b":
                        if(!extreme_position.equalsIgnoreCase("MAX")) {
                            ///ivb_up.setImageResource(R.drawable.p_arrow_up);
                            ivb_up_enable = true;
                        }
                        break;
                    case "ffeeddcc-bbaa-9988-7766-55443322110c":
                        if(!extreme_position.equalsIgnoreCase("MIN")) {
                            ///ivb_down.setImageResource(R.drawable.p_arrow_down);
                            ivb_down_enable = true;
                        }
                        break;
                    case "ffeeddcc-bbaa-9988-7766-554433221108":
                        ///ivb_swing.setImageResource(R.drawable.img_but_swing);
                        ivb_swing_enable = true;
                        break;
                    case "ffeeddcc-bbaa-9988-7766-554433221103":
                        if(deviceOpen) {
                            ///ivb_power.setImageResource(R.drawable.img_but_close);
                        }
                        else{
                            ///ivb_power.setImageResource(R.drawable.img_but_open);
                        }
                        ivb_power_enable = true;
                        break;
                    case "ffeeddcc-bbaa-9988-7766-554433221107":
                        if(deviceOpen) {
                            ///ivb_power.setImageResource(R.drawable.img_but_close);
                        }
                        else{
                            ///ivb_power.setImageResource(R.drawable.img_but_open);
                        }
                        ivb_power_enable = true;
                        break;
                    case "ffeeddcc-bbaa-9988-7766-554433221104":
                        //HiGH
                        ///ivb_max.setImageResource(R.drawable.img_but_135);
                        ivb_max_enable = true;
                        break;
                    case "ffeeddcc-bbaa-9988-7766-554433221105":
                        //LOW
                        ///ivb_low.setImageResource(R.drawable.img_but_45);
                        ivb_low_enable = true;
                        break;
                }
            }
        });

    }
    private void setName(){
        builder.setTitle(R.string.pop_cn_title);
        builder.setMessage(R.string.pop_cn_msg);
        final EditText input=new EditText(getApplicationContext());
        builder.setView(input);

        builder.setPositiveButton(R.string.but_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name=input.getText().toString();
                //input.getText().clear();
                Log.i("SET NAME","name value:"+name);
                writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221106",name,true);
            }
        });
        builder.setNegativeButton(R.string.but_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }
    private void adminLogin(){
        builder.setTitle(R.string.pop_login_title);
        builder.setMessage(R.string.pop_login_msg);
        final EditText input=new EditText(getApplicationContext());
        builder.setView(input);

        builder.setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name=input.getText().toString();
                Log.i("Login Code","login value:"+name);
                //writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221106",name);
                if(name.equals("Admin")){
                    makeTost(R.string.pop_login_success);
                    admin=true;
                }
                else{
                    admin=false;
                    makeTost(R.string.pop_login_fail);
                }
                return;
            }
        });
        builder.setNegativeButton(R.string.but_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                admin=false;
                return;
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }
    private void logOut(){
        admin=false;
        makeTost(R.string.pop_login_logoff_success);
    }
    private void setPresence(){
        builder.setTitle(R.string.pop_cp_title);
        builder.setMessage(R.string.pop_cp_msg);
        final EditText input=new EditText(getApplicationContext());
        builder.setView(input);

        builder.setPositiveButton(R.string.but_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String val=input.getText().toString();
                Log.i("SET PRESENCE","presence value:"+val);
                writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221109",val,true);
            }
        });
        builder.setNegativeButton(R.string.but_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }
    private void setWiFi(){
        LayoutInflater layoutInflater=LayoutInflater.from(this);
        View mview=layoutInflater.inflate(R.layout.wifi_input_dialog,null);
        AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(this);
        dialogBuilder.setView(mview);
        final EditText te_ssid=(EditText) mview.findViewById(R.id.i_ssid);
        final EditText password=(EditText) mview.findViewById(R.id.i_password);
        dialogBuilder.setCancelable(true).setPositiveButton(R.string.but_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.out.println("Wifi SAVE");
                String val_ssid=te_ssid.getText().toString();
                String val_psw=password.getText().toString();
                Log.i("SET WIFI"," SSID=:"+val_ssid);
                writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221110", val_ssid,true);
                communicating_map.put("ffeeddcc-bbaa-9988-7766-554433221110",true);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!communicating_map.get("ffeeddcc-bbaa-9988-7766-554433221110")) {
                            Log.i("SET WIFI", " PSW=:" + val_psw);
                            writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221111", val_psw,true);
                        }
                        else{
                            makeTost(R.string.pop_swf_fail);
                        }
                    }
                }, 5000);
            }
        }).setNegativeButton(R.string.but_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog dialog=dialogBuilder.create();
        dialog.show();
    }
    private void checkPermission(){
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},100);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},100);
        }
        //if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)== PackageManager.PERMISSION_DENIED){
            //ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},100);
        //}
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.BLUETOOTH},100);
        }
    }
    private void scanBLE() {
        System.out.println("Scanning Blutooth");
        forced_stop_scan=false;
        arrayList.clear();
        devices.clear();
        device_map.clear();
        d_map.clear();
        device_names.clear();
        adapter.notifyDataSetChanged();
        showProgressDialog();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLEScanner.stopScan(mScanCallback);
                closeProgressDialog();
                device dev_memorized=d_map.get(sp.getDeviceAdrress(getApplicationContext()));
                if(dev_memorized!=null){
                    System.out.println("Connecting to Memorized device:"+dev_memorized.getName());
                    initialConnect(dev_memorized);
                }
                else {
                    if (nearestDevice != null) {
                        //closeConnection();
                        initialConnect(nearestDevice);
                    }
                }
            }
        },SCAN_PERIOD);
        mLEScanner.startScan(filters,settings,mScanCallback);
    }
    private void initialConnect(device d){
        if(!forced_stop_scan) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btDevice = mBluetoothAdapter.getRemoteDevice(d.getMac());
                    connectToDevice(btDevice);
                    selectedDevice = device_map.get(d.getMac());
                }
            });
        }
    }
    private void stopScanBLE(){
        mLEScanner.stopScan(mScanCallback);
        forced_stop_scan=true;
        closeProgressDialog();
    }
    private ScanCallback mScanCallback=new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("CallbackType::",String.valueOf(callbackType));
            Log.i("Result::",result.toString());
            BluetoothDevice btDevice=result.getDevice();
            String name;
            if(device_map.get(btDevice.getAddress())==null){
                device_map.put(btDevice.getAddress(),btDevice);
                device d=new device();
                d.setMac(btDevice.getAddress());
                if(btDevice.getName()==null){
                    name=btDevice.getAddress();
                    d.isBLE=false;
                }
                else{
                    name=btDevice.getName();
                    d.isBLE=true;
                }
                d.setName(name);
                d.signal_level=result.getRssi();

                if(d.isBLE) {
                    if(name.toUpperCase().contains("ACP")) {
                        if (result.getRssi() > rssiHigh) {
                            rssiHigh = result.getRssi();
                            nearestDevice = d;
                        }
                        System.out.println("DEVICE NAME:"+name);
                        d.name=(name.replace("ACP-","")).trim();
                        d.name=(d.name.replace("acp-","")).trim();
                        devices.add(d);
                        device_names.add(d.name);
                        //re-arrange layout

                        adapter.notifyDataSetChanged();
                        //reArrangeColumnView();
                        d_map.put(btDevice.getAddress(), d);
                    }
                }
            }
            ///////
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed::","Error Code::"+errorCode);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr:results){
                Log.i("Scan Result::",sr.toString());
            }
        }
    };

    private BluetoothAdapter.LeScanCallback mLEScanCallback=new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int i, byte[] bytes) {
            Log.i("onLEScan::",device.toString());
        }
    };

    private void makeTost(int str){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void showProgressDialog(){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }
    private void closeProgressDialog(){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });

    }
    private void showConnecting(String dev_name){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_connecting.setText("Connecting");
                tv_connected_dev_name.setText(dev_name);
            }
        });
    }
    private void showConnected(String dev_name){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_connecting.setText("Connected");
                tv_connected_dev_name.setText(dev_name);
                buttonScan.setVisibility(View.GONE);
                img_connected.setImageResource(R.drawable.img_tick_active);
                img_signal.setImageResource(R.drawable.img_wifi_active);
            }
        });
    }
    private void showDisconnected(String dev_name){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_connecting.setText("");
                tv_connected_dev_name.setText("");
                img_connected.setImageResource(R.drawable.img_tick_gray);
                img_signal.setImageResource(R.drawable.img_wifi_gray);
                tv_degree.setVisibility(View.GONE);
                buttonScan.setVisibility(View.VISIBLE);
            }
        });
    }
    private void showCMDExcutionStatus(boolean sts,String uuid){
        System.out.println("CMD STATUS UUID:"+uuid);
        if(uuid!="ffeeddcc-bbaa-9988-7766-554433221101") {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageView imv;
                    if (sts) {

                        //do not show for keep alive
                        if(!uuid.equals("ffeeddcc-bbaa-9988-7766-55443322110e")){
                            ///img_status.setImageResource(R.drawable.success_tick_2d);
                            //img_status.setVisibility(View.VISIBLE);
                            //tv_degree.setVisibility(View.GONE);
                            //buttonScan.setVisibility(View.GONE);
                            setCenterDisplay("TICK", "");
                        }
                    } else {
                        ///img_status.setImageResource(R.drawable.failed_cross_2d);
                        //img_status.setVisibility(View.VISIBLE);
                        //tv_degree.setVisibility(View.GONE);
                        //buttonScan.setVisibility(View.GONE);
                    }
                    /*
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            img_status.setVisibility(View.GONE);
                            tv_degree.setVisibility(View.GONE);
                            buttonScan.setVisibility(View.VISIBLE);
                        }
                    }, 6000);

                     */
                }
            });
        }
    }
    private void setCenterStatusImage(String sts){
        //last_status_cmd=sts;
        if(sts==null){
            sts="";
        }
        System.out.println("Setting Status Image:"+sts);
        String finalSts = sts;
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (finalSts){
                    case "OPEN":
                        ///img_last_status.setImageResource(R.drawable.img_sts_open);
                        //img_last_status.setVisibility(View.VISIBLE);
                        deviceOpen=true;
                        setDeviceOpenCloseControl(false);
                        //setDegree("90"+(char) 0x00B0);
                        setCenterDisplay("DEG","90"+(char) 0x00B0);
                        break;
                    case "CLOSE":
                        ///img_last_status.setImageResource(R.drawable.img_sts_close);
                        //img_last_status.setVisibility(View.VISIBLE);
                        deviceOpen=false;
                        setDeviceOpenCloseControl(true);
                        setCenterDisplay("DEG","0"+(char) 0x00B0);
                        break;
                    case "UP":
                        but_135.setImageResource(R.drawable.but_3_inv);
                        setCenterDisplay("DEG","135"+(char) 0x00B0);
                        /*
                        if(extreme_position.equalsIgnoreCase("MAX")){
                            ///img_last_status.setImageResource(R.drawable.img_sts_up);
                        }
                        else {
                            ///img_last_status.setImageResource(R.drawable.img_sts_up);
                        }

                         */
                        //img_last_status.setVisibility(View.VISIBLE);
                        break;
                    case "DOWN":
                        but_45.setImageResource(R.drawable.but_2_inv);
                        setCenterDisplay("DEG","45"+(char) 0x00B0);
                        /*
                        if(extreme_position.equalsIgnoreCase("MIN")){
                            ///img_last_status.setImageResource(R.drawable.img_sts_down);
                        }
                        else{
                            ///img_last_status.setImageResource(R.drawable.img_sts_down);
                        }

                         */
                        //img_last_status.setVisibility(View.VISIBLE);
                        break;
                    case "STEPUP":
                        if(extreme_position.equalsIgnoreCase("MAX")){
                            ///img_last_status.setImageResource(R.drawable.img_sts_up);
                        }
                        else {
                            ///img_last_status.setImageResource(R.drawable.img_sts_up);
                        }
                        //img_last_status.setVisibility(View.VISIBLE);
                        break;
                    case "STEPDOWN":
                        if(extreme_position.equalsIgnoreCase("MIN")){
                            ///img_last_status.setImageResource(R.drawable.img_sts_down);
                        }
                        else{
                            ///img_last_status.setImageResource(R.drawable.img_sts_down);
                        }
                        //img_last_status.setVisibility(View.VISIBLE);
                        break;
                    case "SWING":
                        but_swing.setImageResource(R.drawable.but_1_inv);
                        setCenterDisplay("TICK","");
                        ///img_last_status.setImageResource(R.drawable.img_sts_swing);
                        //img_last_status.setVisibility(View.VISIBLE);
                        break;
                    case "LOW":
                        setCenterDisplay("DEG","45"+(char) 0x00B0);
                        //but_45.setImageResource(R.drawable.but_2_inv);
                        ///img_last_status.setImageResource(R.drawable.img_sts_45);
                        //img_last_status.setVisibility(View.VISIBLE);
                        break;
                    case "HIGH":
                        setCenterDisplay("DEG","135"+(char) 0x00B0);
                        ///img_last_status.setImageResource(R.drawable.img_sts_135);
                        //img_last_status.setVisibility(View.VISIBLE);
                        break;
                    default:
                        //img_last_status.setVisibility(View.GONE);
                        break;
                }
            }
        });

    }
    public void enableControls(){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ///ivb_up.setImageResource(R.drawable.p_arrow_up);
                ///ivb_down.setImageResource(R.drawable.p_arrow_down);
                ///ivb_power.setImageResource(R.drawable.img_but_open);
                ///ivb_swing.setImageResource(R.drawable.img_but_swing);
                //b_settings.setImageResource(R.drawable.p_settings);
                ///ivb_low.setImageResource(R.drawable.img_but_45);
                ///ivb_max.setImageResource(R.drawable.img_but_135);
                ivb_power_enable = ivb_down_enable = ivb_up_enable = ivb_swing_enable = b_settings_enable = ivb_low_enable =ivb_max_enable= true;
            }
        });
    }
    public void disableControls(){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ///ivb_up.setImageResource(R.drawable.p_arrow_up_gray);
                ///ivb_down.setImageResource(R.drawable.p_arrow_down_gray);
                ///ivb_power.setImageResource(R.drawable.img_but_open_gray);
                ///ivb_swing.setImageResource(R.drawable.img_but_swing_gray);
                ///b_settings.setImageResource(R.drawable.p_settings_gray);
                ///ivb_low.setImageResource(R.drawable.img_but_45_gray);
                ///ivb_max.setImageResource(R.drawable.img_but_135_gray);
                ivb_power_enable = ivb_down_enable = ivb_up_enable = ivb_swing_enable = b_settings_enable = ivb_low_enable =ivb_max_enable =false;
                setCenterStatusImage("");
            }
        });
    }
    private void releaseMaxLow(){
        enableButton("ffeeddcc-bbaa-9988-7766-554433221104");
        enableButton("ffeeddcc-bbaa-9988-7766-554433221105");
    }
    private void releaseUpDown(){
        extreme_position="";
        enableButton("ffeeddcc-bbaa-9988-7766-55443322110b");
        enableButton("ffeeddcc-bbaa-9988-7766-55443322110c");
    }
    public void RearrangeControls(){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                but_power.setImageResource(R.drawable.but_4);
                but_swing.setImageResource(R.drawable.but_1);
                but_135.setImageResource(R.drawable.but_3);
                but_45.setImageResource(R.drawable.but_2);
                but_left.setImageResource(R.drawable.left_gray);
                but_left.setBackgroundResource(R.drawable.shadow_background);
                //but_left.setPadding(0, left_right_but_padding, 0, left_right_but_padding);
                but_right.setImageResource(R.drawable.right_gray);
                //but_right.setPadding(0, left_right_but_padding, 0, left_right_but_padding);
                but_right.setBackgroundResource(R.drawable.shadow_background);


                if(ivb_up_enable) {
                    ///ivb_up.setImageResource(R.drawable.p_arrow_up);
                }
                else{
                    ///ivb_up.setImageResource(R.drawable.p_arrow_up_gray);
                }
                if(ivb_down_enable) {
                    ///ivb_down.setImageResource(R.drawable.p_arrow_down);
                }
                else{
                    ///ivb_down.setImageResource(R.drawable.p_arrow_down_gray);
                }
                if(ivb_power_enable) {
                    if(deviceOpen) {
                        ///ivb_power.setImageResource(R.drawable.img_but_close);
                    }
                    else{
                        ///ivb_power.setImageResource(R.drawable.img_but_open);
                    }
                }
                else{
                    ///ivb_power.setImageResource(R.drawable.img_but_open_gray);
                }
                if(ivb_swing_enable) {
                    ///ivb_swing.setImageResource(R.drawable.img_but_swing);
                }
                else{
                    ///ivb_swing.setImageResource(R.drawable.img_but_swing_gray);
                }
                if(b_settings_enable) {
                    //b_settings.setImageResource(R.drawable.p_settings);
                }
                else{
                    //b_settings.setImageResource(R.drawable.p_settings_gray);
                }
                if(ivb_low_enable) {
                    ///ivb_low.setImageResource(R.drawable.img_but_45);
                }
                else{
                    ///ivb_low.setImageResource(R.drawable.img_but_45_gray);
                }
                if(ivb_max_enable) {
                    ///ivb_max.setImageResource(R.drawable.img_but_135);
                }
                else{
                    ///ivb_max.setImageResource(R.drawable.img_but_135_gray);
                }
                Log.w("BUT ENABLE", "Button rearrange rune");
            }
        });
    }
    public void setDeviceOpenCloseControl(boolean s){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(s){
                    System.out.println("setting but Open");
                    but_power.setImageResource(R.drawable.but_4);
                }
                else{
                    System.out.println("setting but Open");
                    but_power.setImageResource(R.drawable.but_4_close);
                }
            }
        });
    }

    public void setConnectedDevice(String name){
        MainActivity.this.runOnUiThread(new Runnable() {
            View child;
            TextView tv;
            ImageView c;
            @Override
            public void run() {
                if(name!="") {
                    int pos = adapter.getPosition(name);
                    device_spinner.setSelection(pos);
                }
            }
        });
    }
    private void reArrangeColumnView(){
        MainActivity.this.runOnUiThread(new Runnable() {
            View child;
            TextView tv;
            ImageView c;
            @Override
            public void run() {
                System.out.println("Called Rearrange");
                for (int i = 0; i < recycleView.getChildCount(); i++) {
                    child = recycleView.getChildAt(i);
                    //In case you need to access ViewHolder:
                    ///tv=child.findViewById(R.id.textView);
                    ///c=child.findViewById(R.id.imageView7);
                    int w=tv.getText().length();
                    System.out.println("TILE WIDTH:"+w);
                    if(w>13){
                        col_width=((Resources.getSystem().getDisplayMetrics().widthPixels)-100);
                    }
                }
                layoutManager=new AutoFitGridLayoutManager(getApplicationContext(),col_width);
                recycleView.setLayoutManager(layoutManager);
            }

        });
    }
    private void stopCommunicating(String uuid){
        System.out.println("run timeout callback for read of "+uuid);
        onCommunication=false;
        if(communicating_map.get(uuid)!=null){
            if (communicating_map.get(uuid)) {
                System.out.println("still communicating : " + uuid);
                communicating_map.put(uuid, false);
                closeProgressDialog();
                releaseButtonPressed(uuid);
                //showCMDExcutionStatus(false, uuid);
                //makeTost(R.string.sys_msg3);

            } else {
                System.out.println("communication ended");
                enableButton(uuid);
                releaseButtonPressed(uuid);
            }
        }
    }

    @Override

    public void onItemClick(device d, View v) {
        System.out.println("implemented method on ::"+d.name);
        if(btDevice!=null && btDevice.getAddress()==d.getMac()){
            //same device clicked
            System.out.println("same device clicked::"+d.name);
            ///TextView textView=(TextView) v.findViewById(R.id.textView);
            ///textView.setTextColor(v.getResources().getColor(R.color.black));
        }
        else{
            //other device clocked
            closeConnection();
            btDevice=mBluetoothAdapter.getRemoteDevice(d.getMac());
            connectToDevice(btDevice);
            selectedDevice=device_map.get(d.getMac());
        }
        stopScanBLE();
    }

    /// Device connectivity
    public void connectToDevice(BluetoothDevice device){
        if(mGatt==null){
            showProgressDialog();
            device dddd=d_map.get(device.getAddress());
            showConnecting(dddd.getName());
            mGatt=device.connectGatt(this,false,gattCallback);
            //mGatt.requestMtu(30);

        }
        else{
            Log.e("Connect Device:","device already connected");
        }
    }
    public void closeConnection(){
        if(mGatt!=null){
            mGatt.disconnect();
            mGatt=null;
            last_sent_cmd="";
            last_status_cmd="";
            communicating_map.clear();
            setConnectedDevice("");
            showDisconnected("");
            disableControls();
            btDevice=null;
            keepAliveHandler.removeCallbacks(keepAliveRannable);
            Log.e("DISCONNECT DEVICE:","Connection Status: CLOSED");
        }
        else{
            btDevice=null;
            mGatt=null;
            keepAliveHandler.removeCallbacks(keepAliveRannable);
            Log.e("DISCONNECT DEVICE:","Connection Status: already CLOSED");
        }
    }

    private final BluetoothGattCallback gattCallback=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange","Status:"+status);

            switch (newState){
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback","STATE_CONNECTED");
                    retry_connecting=true;
                    gatt.discoverServices();
                    enableControls();
                    closeProgressDialog();
                    device dd=d_map.get(gatt.getDevice().getAddress());
                    showConnected(dd.getName());
                    selectedDevice=gatt.getDevice();
                    setConnectedDevice(gatt.getDevice().getName());
                    sp.setDeviceData(getApplicationContext(), dd.getMac(), dd.getName());
                    last_communication_time=System.currentTimeMillis();
                    keepAliveHandler.post(keepAliveRannable);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i("gattCallback","STATE_DISCONNECTED");
                    closeProgressDialog();
                    showDisconnected("");
                    disableControls();
                    mGatt.close();
                    mGatt=null;
                    setConnectedDevice("");
                    if(retry_connecting){
                        retry_connecting=false;
                        //initialConnect(nearestDevice);
                        connectToDevice(selectedDevice);
                    }

                    keepAliveHandler.removeCallbacks(keepAliveRannable);
                    break;
                default:
                    Log.e("gattCallback","STATE_OTHER");
                    closeProgressDialog();
                    showDisconnected("");
                    disableControls();
                    mGatt.close();
                    mGatt=null;
                    setConnectedDevice("");
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
                    writeCharacteristic("ffeeddcc-bbaa-9988-7766-554433221101","Admin",true);
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
            closeProgressDialog();
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
            closeProgressDialog();
            //last_communication_time=System.currentTimeMillis(); // Set lat communicated time
            releaseButtonPressed(characteristic.getUuid().toString().toLowerCase());
            showCMDExcutionStatus(true,characteristic.getUuid().toString().toLowerCase());
            if(characteristic.getUuid().toString().equals("ffeeddcc-bbaa-9988-7766-554433221106")){
                Log.d("BLE","SCANNING AGAIN");
                makeTost(R.string.sys_msg1);
                closeConnection();
                scanBLE();
            }
            else if(characteristic.getUuid().toString().equals("ffeeddcc-bbaa-9988-7766-55443322110e")){
                Log.d("BLE","Keep Alive Written");
                last_communication_time=System.currentTimeMillis();
            }
        }
        @Override
        public void onMtuChanged(android.bluetooth.BluetoothGatt gatt, int mtu, int status){
            Log.d("BLE","onMtuChanged mtu="+mtu+",status="+status);
        }
    };
    private void ProcessReadCharacteristicValue(String uuid,String value){
        System.out.println("Processing Read Characteristic:"+uuid+"="+value);
        switch (uuid){
            case "ffeeddcc-bbaa-9988-7766-554433221107":
                //releaseMaxLow();
                //releaseUpDown();
                if(value.equalsIgnoreCase("OK")) {
                    deviceOpen = true;
                    System.out.println("OPEN");
                    setDeviceOpenCloseControl(false);
                    showCMDExcutionStatus(true,uuid);
                    procesConfirmationCMD(value);
                }
                else{
                    enableButton(uuid);
                }

                //releaseButtonPressed(uuid);

                break;

            case "ffeeddcc-bbaa-9988-7766-554433221103":
                releaseMaxLow();
                releaseUpDown();
                if(value.equalsIgnoreCase("OK")) {
                    deviceOpen = false;
                    System.out.println("CLOSE");

                    setDeviceOpenCloseControl(true);
                    showCMDExcutionStatus(true,uuid);
                    //readStatus(500);
                    procesConfirmationCMD(value);
                }
                else{
                    enableButton(uuid);
                }

                //releaseButtonPressed(uuid);
                break;
            case "ffeeddcc-bbaa-9988-7766-554433221104":
                System.out.println("HIGH");
                releaseUpDown();
                if(value.equalsIgnoreCase("OK")) {
                    extreme_position="";
                    showCMDExcutionStatus(true,uuid);
                    //readStatus(500);
                    disableButton(value);
                    procesConfirmationCMD(value);
                }
                else{
                    enableButton(uuid);
                }

                //releaseButtonPressed(uuid);
                break;
            case "ffeeddcc-bbaa-9988-7766-554433221105":
                System.out.println("LOW");
                releaseUpDown();
                if(value.equalsIgnoreCase("OK")) {
                    extreme_position="";
                    showCMDExcutionStatus(true,uuid);
                    //readStatus(500);
                    disableButton(value);
                    procesConfirmationCMD(value);
                }
                else{
                    enableButton(uuid);
                }

                //releaseButtonPressed(uuid);
                break;
            case "ffeeddcc-bbaa-9988-7766-554433221108":
                System.out.println("SWING");
                releaseUpDown();
                if(value.equalsIgnoreCase("OK")) {
                    extreme_position="";
                    showCMDExcutionStatus(true,uuid);
                    //readStatus(500);
                    releaseMaxLow();
                    procesConfirmationCMD(value);
                }
                else{
                    enableButton(uuid);
                }

                //releaseButtonPressed(uuid);
                break;
            case "ffeeddcc-bbaa-9988-7766-55443322110b":
                releaseMaxLow();
                if(value.equalsIgnoreCase("MAX")){
                    //releaseButtonPressed(uuid);
                    extreme_position="MAX";
                    //readStatus(500);
                    showCMDExcutionStatus(true,uuid);
                    disableButton(uuid);
                    if(!ivb_down_enable) {
                        enableButton("ffeeddcc-bbaa-9988-7766-55443322110c");
                    }
                    procesConfirmationCMD(value);
                }
                else if(value.equalsIgnoreCase("OK")){
                    extreme_position="";
                    releaseButtonPressed(uuid);
                    enableButton(uuid);
                    //readStatus(500);
                    procesConfirmationCMD(value);
                    showCMDExcutionStatus(true,uuid);
                    if(!ivb_down_enable) {
                        enableButton("ffeeddcc-bbaa-9988-7766-55443322110c");
                    }
                }
                else{
                    enableButton(uuid);
                    showCMDExcutionStatus(false,uuid);
                }
                readCharacteristic("ffeeddcc-bbaa-9988-7766-55443322110d");
                break;
            case "ffeeddcc-bbaa-9988-7766-55443322110c":
                releaseMaxLow();
                if(value.equalsIgnoreCase("MIN")){
                    //releaseButtonPressed(uuid);
                    extreme_position="MIN";
                    //readStatus(500);
                    procesConfirmationCMD(value);
                    showCMDExcutionStatus(true,uuid);
                    disableButton(uuid);
                    if(!ivb_up_enable) {
                        enableButton("ffeeddcc-bbaa-9988-7766-55443322110b");
                    }else{
                        System.out.println("UP enabled");
                    }
                }
                else if(value.equalsIgnoreCase("OK")){
                    //releaseButtonPressed(uuid);
                    extreme_position="";
                    //readStatus(500);
                    procesConfirmationCMD(value);
                    showCMDExcutionStatus(true,uuid);
                    if(!ivb_up_enable) {
                        enableButton("ffeeddcc-bbaa-9988-7766-55443322110b");
                    }else{
                        System.out.println("UP enabled");
                    }
                }
                else{
                    enableButton(uuid);
                    showCMDExcutionStatus(false,uuid);
                }
                readCharacteristic("ffeeddcc-bbaa-9988-7766-55443322110d");
                break;
            case "ffeeddcc-bbaa-9988-7766-554433221102":
                //verify action
                procesVerifyCMD(value.toUpperCase());
                break;
            case "ffeeddcc-bbaa-9988-7766-55443322110d":
                System.out.println("Processing Angle :"+value);
                String angle=value+(char) 0x00B0;
                setDegree(angle);
                break;
            default:
                //releaseButtonPressed(uuid);
                showCMDExcutionStatus(false,uuid);
                break;
        }
    }
    private void procesConfirmationCMD(String val){
        System.out.println("Processing verify CMD:"+val+"  -- last sent CMD="+last_sent_cmd);
        if(last_sent_cmd.equalsIgnoreCase("")){
            setCenterStatusImage(val);
            return;
        }
        if(val.equals("OK") ||(val.equals("MAX")) || (val.equals("MIN"))){
            switch (last_sent_cmd){
                case "CLOSE":
                    enableButton("ffeeddcc-bbaa-9988-7766-554433221107");
                    setCenterStatusImage(last_sent_cmd);
                    break;
                case "OPEN":
                    enableButton("ffeeddcc-bbaa-9988-7766-554433221103");
                    setCenterStatusImage(last_sent_cmd);
                    break;
                case "HIGH":
                    enableButton("ffeeddcc-bbaa-9988-7766-554433221105");
                    setCenterStatusImage(last_sent_cmd);
                    break;
                case "LOW":
                    enableButton("ffeeddcc-bbaa-9988-7766-554433221104");
                    setCenterStatusImage(last_sent_cmd);
                    break;
                case "SWING":
                    enableButton("ffeeddcc-bbaa-9988-7766-554433221108");
                    setCenterStatusImage(last_sent_cmd);
                    break;
                case "UP":
                    enableButton("ffeeddcc-bbaa-9988-7766-55443322110b");
                    setCenterStatusImage(last_sent_cmd);
                    break;
                case "DOWN":
                    enableButton("ffeeddcc-bbaa-9988-7766-55443322110c");
                    setCenterStatusImage(last_sent_cmd);
                    break;
                default:
                    setCenterStatusImage(last_sent_cmd);
                    break;
            }
        }
        else{
            setCenterStatusImage("");
        }
    }
    private void procesVerifyCMD(String val){
        System.out.println("Processing verify CMD:"+val+"  -- last sent CMD="+last_sent_cmd);
        if(last_sent_cmd.equalsIgnoreCase("")){
            setCenterStatusImage(val);
            return;
        }
    }
    private void verifyStatusAgain(){
        System.out.println("Reading status again for "+verify_cmd_status_count+" times");
        if(verify_cmd_status_count<3){
            readStatus(2000);
        }
        else{
            //failed CMD
            //set center with last status
            setCenterStatusImage(last_status_cmd);
            switch (last_sent_cmd){
                case "OPEN":
                    ivb_power_enable=true;
                    break;
                case "CLOSE":
                    ivb_power_enable=true;
                    break;
                case "UP":
                    //enableButton("ffeeddcc-bbaa-9988-7766-55443322110b");
                    ivb_up_enable=true;
                    break;
                case "DOWN":
                    //enableButton("ffeeddcc-bbaa-9988-7766-55443322110c");
                    ivb_down_enable=true;
                    break;
                case "SWING":
                    ivb_swing_enable=true;
                    break;
                case "MAX":
                    ivb_max_enable=true;
                    break;
                case "LOW":
                    ivb_low_enable=true;
                    break;
                default:
                    break;
            }
            RearrangeControls();
        }
    }
    public void writeCharacteristic(String uuid,String value,boolean show_progress){
        if(mGatt!=null) {

            BluetoothGattService service = mGatt.getService(UUID.fromString("ffeeddcc-bbaa-9988-7766-554433221100"));
            if (service != null) {

                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(uuid));
                if(show_progress) {
                    showProgressDialog();
                }
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
                    closeProgressDialog();
                    Log.e("WriteChar","characteristics is null");
                }
            }
            else{
                closeProgressDialog();
                Log.e("WriteChar","service is null");
                releaseButtonPressed(uuid);
                makeTost(R.string.sys_msg4);
            }
        }
        else{
            Log.e("WriteChar","mGat is null or still communicating:"+uuid);
            //releaseButtonPressed(uuid);
            makeTost(R.string.sys_msg5);
        }
    }
    public void readCharacteristic(String uuid){
        if(mGatt!=null && !onCommunication) {

            BluetoothGattService service = mGatt.getService(UUID.fromString("ffeeddcc-bbaa-9988-7766-554433221100"));
            if (service != null) {

                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(uuid));
                if (characteristic != null) {
                    showProgressDialog();
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
                    makeTost(R.string.sys_msg4);
                    releaseButtonPressed(uuid);
                    showCMDExcutionStatus(false,uuid);
                }
            }
            else{
                Log.e("ReadChar","service is null");
                makeTost(R.string.sys_msg4);
                releaseButtonPressed(uuid);
                showCMDExcutionStatus(false,uuid);
            }
        }
        else{
            Log.e("ReadChar","mGat is null or still communicating:"+uuid);
            //enableButton(uuid);
            releaseButtonPressed(uuid);
            makeTost(R.string.sys_msg5);
        }
    }
    private void readStatus(long delay){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("reading status in "+delay+" msec");
                readCharacteristic("ffeeddcc-bbaa-9988-7766-554433221102");
            }
        },delay);
    }

    public class UsersAdapter extends ArrayAdapter<device> {
        public UsersAdapter(Context context, ArrayList<device> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            device d = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                ///convertView = LayoutInflater.from(getContext()).inflate(R.layout.bt_list_item, parent, false);
            }
            // Lookup view for data population
            ///TextView tv1 = (TextView) convertView.findViewById(R.id.tv1);
            ///TextView tv2 = (TextView) convertView.findViewById(R.id.tv2);
            // Populate the data into the template view using the data object
           /// tv1.setText(d.name);
            ///tv2.setText("[ "+d.getMac()+"]");
            // Return the completed view to render on screen
            return convertView;
        }
    }

    private void addDummyDevices(){
        for(int i=1;i<7;i++){
            device d=new device();
            d.setName("Device#"+i);
            d.setMac("AA:BB:DD:"+i+i);
            devices.add(d);
            device_map.put("AA:BB:CC:"+i+i,null);
        }
    }
    private device getDeviceFromName(String n){
        device d=null;
        for(Map.Entry<String, device> entry : d_map.entrySet()){
            if(entry.getValue().getName().equals(n)){
                d=entry.getValue();
            }
        }
        return d;
    }

}