package com.ihealthlabs.simpleble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ihealth.communication.control.HsProfile;
import com.ihealth.communication.manager.DiscoveryTypeEnum;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private int callbackId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Step 1, init iHealth Device SDK, If you have multi activity, I recommend move init function to application class.
        iHealthDevicesManager.getInstance().init(getApplication(),  Log.VERBOSE, Log.VERBOSE);
        callbackId = iHealthDevicesManager.getInstance().registerClientCallback(miHealthDevicesCallback);
        iHealthDevicesManager.getInstance().addCallbackFilterForDeviceType(callbackId, iHealthDevicesManager.TYPE_PT3SBT, iHealthDevicesManager.TYPE_PO3);

        findViewById(R.id.btn_permission).setOnClickListener(v -> {
            checkAndroidPermission();
        });

        findViewById(R.id.btn_auth).setOnClickListener(v -> {
            auth();
        });

        findViewById(R.id.btn_scan).setOnClickListener(v -> {
            scanDevice();
        });

        findViewById(R.id.btn_connect).setOnClickListener(v -> {
            connectDevice("");
        });
    }

    // Step 2, Request location permission. It maybe also need scan and connect permission above android 11.
    private void checkAndroidPermission() {
        PermissionX.init(this).permissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
        ).request((allGranted, grantedList, deniedList) -> {
            if (allGranted) {
                Toast.makeText(MainActivity.this, "All permissions are granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Step 3 add your license file into project
    private void auth() {
        try {
            InputStream is = getAssets().open("xxx.pem");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            boolean isPass = iHealthDevicesManager.getInstance().sdkAuthWithLicense(buffer);
            if (isPass) {
                Toast.makeText(MainActivity.this, "license is pass", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "license is expire", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Step 4, Scan device, if it find device, the callback onScanDevice will return device message.
    private void scanDevice() {
        iHealthDevicesManager.getInstance().startDiscovery(DiscoveryTypeEnum.PT3SBT);
        // iHealthDevicesManager.getInstance().startDiscovery(DiscoveryTypeEnum.PO3);
    }

    // Step 5, Pass mac that you get from scanning to this function. The callback will return connection status.
    private void connectDevice(String mac) {
        iHealthDevicesManager.getInstance().connectDevice("", mac, iHealthDevicesManager.TYPE_PT3SBT);
    }

    private iHealthDevicesCallback miHealthDevicesCallback = new iHealthDevicesCallback() {

        @Override
        public void onScanDevice(String mac, String deviceType, int rssi, Map manufactorData) {
            Log.i("", "onScanDevice - mac:" + mac + " - deviceType:" + deviceType + " - rssi:" + rssi + " - manufactorData:" + manufactorData);
        }

        @Override
        public void onDeviceConnectionStateChange(String mac, String deviceType, int status, int errorID, Map manufactorData) {
            Log.i("", "mac:" + mac + " deviceType:" + deviceType + " status:" + status + " errorid:" + errorID + " -manufactorData:" + manufactorData);
        }

        @Override
        public void onScanError(String reason, long latency) {
            Log.e("", reason);
        }

        @Override
        public void onScanFinish() {
            Log.i("", "onScanFinish");
        }

        @Override
        public void onDeviceNotify(String mac, String deviceType, String action, String message) {
            Log.i("", "onDeviceNotify: " + message);
        }
    };
}