package com.example.biu.bluelock;


import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by BIU on 2016/7/19.
 */
public class LockFragment extends Fragment implements View.OnClickListener {
    //控件命名
    private Dialog dialog;
    private ImageButton tab_ImgbtnLock;
    private Button tab_btnConnect;
    private TextView tvCurrent;
    private TextView tvDistance;
    //搜索设备part1
    private BluetoothAdapter blueadapter;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> adapter;
    private List<String> deviceList = new ArrayList<>();
    private ListView lviBluetooth;
    private Button btnsearch;
    private boolean isLocked = false;
    private boolean hasregister = false;
    private BluetoothReceiver mydevice = new BluetoothReceiver();

    //连接设备part2
    private BluetoothSocket socket;
    private BluetoothDevice device;
    private boolean isConnected = false;
    protected clientThread clientConnectThread;
    protected readThread mreadThread;
    private String address = null;
    private final String Lockmsg = "12345678open87654321";
    private final String unLockmsg = "12345678close87654321";
    private final String Lockstate = "12345678state87654321";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab01, container, false);
        tab_ImgbtnLock = (ImageButton) v.findViewById(R.id.tab01_ImgbtnLock);
        tab_btnConnect = (Button) v.findViewById(R.id.btnConnect);
        tvCurrent = (TextView) v.findViewById(R.id.txCurrent);
        tvDistance= (TextView) v.findViewById(R.id.tvDistance);
        tab_btnConnect.setOnClickListener(this);
        tab_ImgbtnLock.setOnClickListener(this);
        BluetoothManager bluetoothManager = (BluetoothManager)getContext().getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        return v;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             final byte[] scanRecord) {
            int startByte = 2;
            boolean patternFound = false;
            // 寻找ibeacon
            while (startByte <= 5) {
                if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && // Identifies
                        // an
                        // iBeacon
                        ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { // Identifies
                    // correct
                    // data
                    // length
                    patternFound = true;
                    break;
                }
                startByte++;
            }
            // 如果找到了的话
            if (patternFound) {
                // 转换为16进制
                byte[] uuidBytes = new byte[16];
                System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
                String hexString = bytesToHex(uuidBytes);

                // ibeacon的UUID值
                String uuid = hexString.substring(0, 8) + "-"
                        + hexString.substring(8, 12) + "-"
                        + hexString.substring(12, 16) + "-"
                        + hexString.substring(16, 20) + "-"
                        + hexString.substring(20, 32);

                // ibeacon的Major值
                int major = (scanRecord[startByte + 20] & 0xff) * 0x100
                        + (scanRecord[startByte + 21] & 0xff);

                // ibeacon的Minor值
                int minor = (scanRecord[startByte + 22] & 0xff) * 0x100
                        + (scanRecord[startByte + 23] & 0xff);

                String ibeaconName = device.getName();
                String mac = device.getAddress();
                int txPower = (scanRecord[startByte + 24]);
                Log.d("BLE",bytesToHex(scanRecord));
                Log.d("BLE", "Name：" + ibeaconName + "\nMac：" + mac
                        + " \nUUID：" + uuid + "\nMajor：" + major + "\nMinor："
                        + minor + "\nTxPower：" + txPower + "\nrssi：" + rssi);

               tvDistance.setText(calculateAccuracy(txPower,rssi)+"米");
            }
        }
    };
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }
    @Override
    public void onClick(View v) {
        //未连接时 建立连接
        if (v.getId() == R.id.btnConnect && isConnected == false) {
            dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.custom_dialog);
            search();
            //注册广播
            if (!hasregister) {
                hasregister = true;
                IntentFilter filterStart = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                IntentFilter filterEnd = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                getActivity().registerReceiver(mydevice, filterStart);
                getActivity().registerReceiver(mydevice, filterEnd);
            }
            dialog.setTitle("请选择一个设备连接");

            //setview
            lviBluetooth = (ListView) dialog.findViewById(R.id.dialog_listview);
            btnsearch = (Button) dialog.findViewById(R.id.dialog_btnsearch);
            adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, deviceList);
            lviBluetooth.setAdapter(adapter);
            //Listview点击事件
            lviBluetooth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final String msg = deviceList.get(position);
                    if (blueadapter.isDiscovering()) {
                        blueadapter.cancelDiscovery();
                        btnsearch.setText("repeat search");
                    }
                    final AlertDialog.Builder dialog1 = new AlertDialog.Builder(getContext());
                    dialog1.setTitle("请确定连接的设备");
                    dialog1.setMessage(msg);
                    dialog1.setPositiveButton("连接", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog1, int which) {
                            Bluetoothmsg.BlueToothAddress = msg.substring(msg.length() - 17);
                            if (Bluetoothmsg.lastblueToothAddress != Bluetoothmsg.BlueToothAddress) {
                                Bluetoothmsg.lastblueToothAddress = Bluetoothmsg.BlueToothAddress;
                            }
                            //这里进行连接操作
                            address = Bluetoothmsg.BlueToothAddress;
                            device = blueadapter.getRemoteDevice(address);
                            clientConnectThread = new clientThread();
                            clientConnectThread.start();
                            Toast.makeText(getContext(), "连接成功", Toast.LENGTH_LONG).show();
                            tab_btnConnect.setText("断开连接");
                            isConnected = true;
                            dialog.dismiss();

                        }
                    });
                    dialog1.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bluetoothmsg.BlueToothAddress = null;
                        }
                    });
                    dialog1.show();
                }
            });
            btnsearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (blueadapter.isDiscovering()) {
                        blueadapter.cancelDiscovery();
                        btnsearch.setText("repeat search");
                    } else {
                        findAvalibleDevice();
                        blueadapter.startDiscovery();
                        btnsearch.setText("stop search");
                    }
                }
            });
            dialog.show();
        }
        //断开连接
        if (v.getId() == R.id.btnConnect && isConnected == true) {
            shutdownClient();
            tab_btnConnect.setText("连接设备");
        }
        if (v.getId() == R.id.tab01_ImgbtnLock && isConnected == true) {
            //发送指令
            if (isLocked) {
                sendMessageHandle(unLockmsg);
                Toast.makeText(getContext(), "解锁成功", Toast.LENGTH_SHORT).show();
            } else {
                sendMessageHandle(Lockmsg);
                Toast.makeText(getContext(), "上锁成功", Toast.LENGTH_SHORT).show();
            }
        }
        if (v.getId() == R.id.tab01_ImgbtnLock && isConnected == false)
            Toast.makeText(getContext(), "请先连接设备", Toast.LENGTH_SHORT).show();

    }

    private void findAvalibleDevice() {
        //获取可配对蓝牙设备
        Set<BluetoothDevice> device = blueadapter.getBondedDevices();

        if (blueadapter != null && !blueadapter.isDiscovering()) {
            deviceList.clear();
            showDevices();
        }
        if (device.size() > 0) { //存在已经配对过的蓝牙设备
            for (Iterator<BluetoothDevice> it = device.iterator(); it.hasNext(); ) {
                BluetoothDevice btd = it.next();
                deviceList.add(btd.getName() + '\n' + btd.getAddress());
                showDevices();
            }
        }
    }

    private void showDevices() {
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, deviceList);
        lviBluetooth.setAdapter(adapter);
    }

    private void search() {
        blueadapter = BluetoothAdapter.getDefaultAdapter();
        if (blueadapter == null) {
            Toast.makeText(getContext(), "不支持蓝牙", Toast.LENGTH_LONG).show();
        }
        if (!blueadapter.isEnabled()) {
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, 1);
            blueadapter.enable();
        }
    }

    private class clientThread extends Thread {
        @Override
        public void run() {
            BluetoothSocket temp = null;
            try {
                Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                temp = (BluetoothSocket) m.invoke(device, 1);//这里端口为1
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            socket = temp;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        blueadapter.cancelDiscovery();
                        socket.connect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //开始接收数据
            mreadThread=new readThread();
            mreadThread.start();
        }
    }

    /* 停止客户端连接 */
    private void shutdownClient() {
        new Thread() {
            public void run() {
                isConnected = false;
//                if (clientConnectThread != null) {
//                    clientConnectThread.interrupt();
//                    clientConnectThread = null;
//                }
//                if (mreadThread != null) {
//                    mreadThread.interrupt();
//                    mreadThread = null;
//                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    socket = null;
                }
            }
        }.start();
    }

    //读取数据
    private class readThread extends Thread {
        public void run() {

            byte[] buffer = new byte[1024];
            int bytes;
            InputStream mmInStream = null;

            try {
                mmInStream = socket.getInputStream();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            while (isConnected) {
                try {
                    // Read from the InputStream
                    if ((bytes = mmInStream.read(buffer)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++) {
                            buf_data[i] = buffer[i];
                        }
                        String s = new String(buf_data);
                        if (s == "Lock") {
                            isLocked = true;
                            tvCurrent.setText("已上锁");
                        }
                        if (s == "unLock") {
                            isLocked = false;
                            tvCurrent.setText("未锁定");
                        }


                    }
                } catch (IOException e) {
                    try {
                        mmInStream.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    private void sendMessageHandle(String msg) {
        if (socket == null) {
            Toast.makeText(getContext(), "没有连接", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            OutputStream os = socket.getOutputStream();
            Toast.makeText(getContext(), "发送成功", Toast.LENGTH_SHORT).show();
            os.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //继承广播
    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {    //搜索到新设备
                BluetoothDevice btd = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //搜索没有配过对的蓝牙设备
                if (btd.getBondState() != BluetoothDevice.BOND_BONDED) {
                    deviceList.add(btd.getName() + '\n' + btd.getAddress());
                    showDevices();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {   //搜索结束

                if (lviBluetooth.getCount() == 0) {
                    deviceList.add("No can be matched to use bluetooth");
                    showDevices();
                }

                btnsearch.setText("repeat search");
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hasregister) {
            hasregister = false;
            getActivity().unregisterReceiver(mydevice);
        }
        shutdownClient();
        tab_btnConnect.setText("连接设备");
    }
}
