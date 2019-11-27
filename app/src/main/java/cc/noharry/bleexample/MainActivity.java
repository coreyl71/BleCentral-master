package cc.noharry.bleexample;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private Button mBtnScan;
    private static final int REQUSET_CODE = 100;
    private ScanCallback mScanCallback;
    private LeScanCallback mLeScanCallback;
    private BluetoothDevice mBluetoothDevice;
    private Button mBtnStopScan;
    private Button mBtnConnect;
    private Button mBtnDisconnect;
    private Button mBtnRead;
    private Button mBtnWrite;
    private Button mBtnNotify;
    private BluetoothGattCallback mBluetoothGattCallback;
    private BluetoothGatt mBluetoothGatt;
    private final static UUID UUID_SERVER = UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb");
    private final static UUID UUID_CHARREAD = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb");
    private final static UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private final static UUID UUID_CHARWRITE = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private final static UUID UUID_ADV_SERVER = UUID.fromString("0000ffe3-0000-1000-8000-00805f9b34fb");
    private BluetoothGattCharacteristic mCharacteristic;
    private Button mBtnDisableNotify;
    private TextView mTvScanState;
    private TextView mTvConnState;
    private TextView mTvReadData;
    private TextView mTvWriteData;
    private TextView mTvNotifyData;
    private EditText mEtWrite;
    private AtomicBoolean isScanning = new AtomicBoolean(false);
    private Handler mHandler = new Handler();
    private static final int REQUEST_ENABLE_BT = 100;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothAdapter.setName("Corey_MIX3_C");
        Log.i("Ble_Client---", "local_name = " + mBluetoothAdapter.getName());

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 找控件
        initView();
        // 设置回调
        initCallback();
        // 设置点击事件监听
        initEvent();
        // 权限校验
        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Initializes list view adapter.
//    mLeDeviceListAdapter = new LeDeviceListAdapter();
//    setListAdapter(mLeDeviceListAdapter);
//    scanLeDevice(true);
    }

    @SuppressLint("NewApi")
    private void initCallback() {
        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                L.i("onScanResult:" + " callbackType:" + callbackType + " result:" + result);
                if (isScanning.get()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvScanState.setText("扫描中");
                        }
                    });
                }

                if ("Corey_MI5S_S".equals(result.getDevice().getName())) {
                    L.i("发现 Corey_MI5S_S");
                    mBluetoothDevice = result.getDevice();
                    stopNewScan();
                }
            }
        };
        mLeScanCallback = new LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                L.i("onLeScan:" + " name:" + device.getName() + " mac:" + device.getAddress() + " rssi:" + rssi);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvScanState.setText("扫描中");
                    }
                });
                if ("Corey_MI5S_S".equals(device.getName())) {
                    L.i("发现 Corey_MI5S_S");
                    mBluetoothDevice = device;
                    stopScan();
                }
            }
        };
        mBluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                L.i("onConnectionStateChange status:" + status + " newState:" + newState);
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    L.i("STATE_DISCONNECTED");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvConnState.setText("断开连接");
                        }
                    });
                    gatt.close();
                } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                    L.i("STATE_CONNECTED");
                    L.i("start discoverServices");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvConnState.setText("已连接");
                        }
                    });

                    gatt.discoverServices();
//                    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
//                        gatt.requestMtu(5120);
//                    }

                } else {
                    mTvConnState.setText(newState);
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                L.i("onServicesDiscovered status:" + status);
                BluetoothGattService service = gatt.getService(UUID_SERVER);
                if (service != null) {
                    mCharacteristic = service.getCharacteristic(UUID_CHARWRITE);
                    if (mCharacteristic != null) {
                        L.i("获取到目标特征");
                    }
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             final BluetoothGattCharacteristic characteristic, final int status) {
                L.i("onCharacteristicRead status:" + status + " value:"
                        + byte2HexStr(characteristic.getValue()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvReadData.setText("statu:" + status + " hexValue:" + byte2HexStr(characteristic.getValue()) + " ,str:"
                                + new String(characteristic.getValue()));
                    }
                });

            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              final BluetoothGattCharacteristic characteristic, final int status) {
                L.i("onCharacteristicWrite status:" + status + " value:"
                        + byte2HexStr(characteristic.getValue()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvWriteData.setText("statu:" + status + " hexValue:" + byte2HexStr(characteristic.getValue()) + " ,str:"
                                + new String(characteristic.getValue()));
                    }
                });

            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                final BluetoothGattCharacteristic characteristic) {

                //开启notify之后，我们就可以在这里接收数据了。
                //分包处理数据
//                btBuffer.appendBuffer(characteristic.getValue());
//                while (true){
//                    boolean ret = subPackageOnce(btBuffer);
//                    if (false == ret) break;
//                }

                L.i("onCharacteristicChanged value:" + byte2HexStr(characteristic.getValue()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvNotifyData.setText(" hexValue:" + byte2HexStr(characteristic.getValue()) + " ,str:"
                                + new String(characteristic.getValue()));
                    }
                });

            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                         int status) {
                L.i("onDescriptorRead status:" + status + " value:" + byte2HexStr(descriptor.getValue()));
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                          int status) {
                L.i("onDescriptorWrite status:" + status + " value:" + byte2HexStr(descriptor.getValue()));
            }
        };
    }

//    private boolean subPackageOnce(BluetoothBuffer buffer) {
//        if (null == buffer) return false;
//        if (buffer.getBufferSize() >= 14) {
//            byte[] rawBuffer =  buffer.getBuffer();
//            //求包长
//            if (isHead(rawBuffer)){
//                pkgSize = byteToInt(rawBuffer[2], rawBuffer[3]);
//            }else {
//                pkgSize = -1;
//                for (int i = 0; i < rawBuffer.length-1; ++i){
//                    if (rawBuffer[i] == -2 && rawBuffer[i+1] == 1){
//                        buffer.releaseFrontBuffer(i);
//                        return true;
//                    }
//                }
//                return false;
//            }
//            //剥离数据
//            if (pkgSize > 0 && pkgSize <= buffer.getBufferSize()) {
//                byte[] bufferData = buffer.getFrontBuffer(pkgSize);
//                long time = System.currentTimeMillis();
//                buffer.releaseFrontBuffer(pkgSize);
//                //在这处理数据
////                deal something。。。。。
//                return true;
//            }
//        }
//        return false;
//    }

    private void initEvent() {
        mBtnScan.setOnClickListener(this);
        mBtnStopScan.setOnClickListener(this);
        mBtnConnect.setOnClickListener(this);
        mBtnDisconnect.setOnClickListener(this);
        mBtnRead.setOnClickListener(this);
        mBtnWrite.setOnClickListener(this);
        mBtnNotify.setOnClickListener(this);
        mBtnDisableNotify.setOnClickListener(this);
    }

    private void initView() {
        mBtnScan = findViewById(R.id.btn_scan);
        mBtnStopScan = findViewById(R.id.btn_stop_scan);
        mBtnConnect = findViewById(R.id.btn_connect);
        mBtnDisconnect = findViewById(R.id.btn_disconnect);
        mBtnRead = findViewById(R.id.btn_read);
        mBtnWrite = findViewById(R.id.btn_write);
        mBtnNotify = findViewById(R.id.btn_notify);
        mBtnDisableNotify = findViewById(R.id.btn_disable_notify);
        mTvScanState = findViewById(R.id.tv_scan_state);
        mTvConnState = findViewById(R.id.tv_connect_state);
        mTvReadData = findViewById(R.id.tv_read_data);
        mTvWriteData = findViewById(R.id.tv_write_data);
        mTvNotifyData = findViewById(R.id.tv_notify_data);
        mEtWrite = findViewById(R.id.et_write);
    }

    private ByteBuf buffer = Unpooled.buffer(1024 * 1000);

    @RequiresApi(api = VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_scan:
                scan();
                break;

            case R.id.btn_stop_scan:
                stopScan();
                break;

            case R.id.btn_connect:
                if (mBluetoothDevice != null) {
                    connect(mBluetoothDevice);
                }
                break;

            case R.id.btn_disconnect:
                disConnect();
                break;

            case R.id.btn_read:
                read();
                break;

            case R.id.btn_write:

                String data = mEtWrite.getText().toString().trim();
                if (TextUtils.isEmpty(data)) {
                    Toast.makeText(MainActivity.this, "请输入发送内容", Toast.LENGTH_SHORT).show();
                    break;
                }

                // 字符串转换成 Byte 数组
                byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
                // 数据分包
                subpackageByte(dataBytes);
//                write(dataBytes);
                break;

            case R.id.btn_notify:
                enableNotify();
                break;

            case R.id.btn_disable_notify:
                disableNotify();
                break;

            default:
        }
    }



    /************************************蓝牙操作相关 开始*********************************************/

    /**
     * 新的扫描方法
     */
    @RequiresApi(api = VERSION_CODES.M)
    private void scanNew() {
        mTvScanState.setText("开始扫描");
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        ScanSettings settings = new ScanSettings
                .Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        List<ScanFilter> scanFilters = new ArrayList<>();
        bluetoothManager
                .getAdapter()
                .getBluetoothLeScanner()
                .startScan(scanFilters, settings, mScanCallback);
    }

    /**
     * 扫描(可适配低版本)
     */
    private void scan() {

        L.i("start scan");
        mTvScanState.setText("开始扫描");

        if (mBluetoothAdapter == null) {
            L.i("mBluetoothAdapter == null");
        } else {
            mBluetoothAdapter.startLeScan(/*uuids,*/mLeScanCallback);
        }

    }

    private void stopScan() {
        L.e("stopScan");
        if (mBluetoothAdapter == null) {
            L.i("mBluetoothAdapter == null");
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        //扫描真正停止很多时候有点延迟
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mTvScanState.setText("停止扫描");
            }
        }, 500);

    }

    @RequiresApi(api = VERSION_CODES.M)
    private void stopNewScan() {
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        // 扫描真正停止很多时候有点延迟
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mTvScanState.setText("停止扫描");
            }
        }, 500);
    }


    /**
     * 连接设备
     *
     * @param device 需要连接的蓝牙设备，即外围设备、从设备、作为 Server 端的设备
     */
    private void connect(BluetoothDevice device) {
        L.i("device.name = " + device.getName());
        mBluetoothGatt = device.connectGatt(this, false, mBluetoothGattCallback);
//        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
//            mBluetoothGatt.requestMtu(512);
//        }
    }

//  private final static String TAG = MainActivity.class.getSimpleName();
//  public boolean connect(final String address) {
//    if (mBluetoothAdapter == null || address == null) {
//      Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
//      return false;
//    }
//
//    // Previously connected device.  Try to reconnect.
//    if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
//            && mBluetoothGatt != null) {
//      Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//      if (mBluetoothGatt.connect()) {
//        mConnectionState = STATE_CONNECTING;
//        return true;
//      } else {
//        return false;
//      }
//    }
//
//    final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//    if (device == null) {
//      Log.w(TAG, "Device not found.  Unable to connect.");
//      return false;
//    }
//    // We want to directly connect to the device, so we are setting the autoConnect
//    // parameter to false.
//    mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
//    Log.d(TAG, "Trying to create a new connection.");
//    mBluetoothDeviceAddress = address;
//    mConnectionState = STATE_CONNECTING;
//    return true;
//  }

    /**
     * 断开连接
     */
    private void disConnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    /**
     * 读特征
     */
    private void read() {
        if (mBluetoothGatt != null && mCharacteristic != null) {
            L.i("开始读 uuid：" + mCharacteristic.getUuid().toString());
            mBluetoothGatt.readCharacteristic(mCharacteristic);
        } else {
            L.e("读失败！");
        }
    }

    private boolean isWritingEntity;
    // 当前是否为自动写入模式
    private boolean isAutoWriteMode = false;
    // 最后一包是否自动补零
    private final boolean lastPackComplete = false;
    // 每个包固定长度 20，包括头、尾、msgId
    private int packLength = 20;
    private final Object lock = new Object();

    private HashSet<Integer> resIdSets = new HashSet<>();

    /**
     * 数据分包
     * @param data 数据源
     */
    private void subpackageByte(byte[] data) {

        isWritingEntity = true;
        // 数据源数组的指针
        int index = 0;
        // 数据总长度
        int dataLength = data.length;
        // 待传数据有效长度，最后一个包是否需要补零
        int availableLength = dataLength;
        // 给每个数据分包一个消息 ID，递增
        int msgId = 1000;

        while (index < dataLength) {

            if (!isWritingEntity){
                L.e("写入取消");
            }

            // 每包数据内容大小为 14
            int onePackLength = packLength - 6;
            // 最后一包不足长度不会自动补零
            if (!lastPackComplete) {
                onePackLength = (availableLength >= (packLength - 6) ? (packLength - 6) : availableLength);
            }

            // 实例化一个数据分包，长度为 20
//            byte[] txBuffer = new byte[onePackLength];
            byte[] txBuffer = new byte[packLength];

            // 数据包头 (byte)0xFF
            txBuffer[0] = BFrameConst.FRAME_HEAD;
            // 数据包尾 (byte)0x00;
            txBuffer[19] = BFrameConst.FRAME_END;

            // 数据包 [1]-[4] 为 msgId
            byte[] msgIdByte = int2byte(msgId);
            msgId++;
            /**
             * 数组拷贝
             * 原数组
             * 元数据的起始位置
             * 目标数组
             * 目标数组的开始起始位置
             * 要 copy 的数组的长度
             */
            System.arraycopy(msgIdByte, 0, txBuffer, 1, BFrameConst.MESSAGE_ID_LENGTH);

            // 数据包 [5]-[18] 为内容
            for (int i = 5; i < onePackLength + 5; i++){
                if(index < dataLength){
                    txBuffer[i] = data[index++];
                }
            }
            L.i("index = " + index);
            L.i("onePackLength = " + onePackLength);
            L.i("dataLength = " + dataLength);
//            for (int i = 0; i < onePackLength; i++){
//                if(index < dataLength){
//                    txBuffer[i] = data[index++];
//                }
//            }

            // 更新剩余数据长度
            availableLength -= onePackLength;

            // 单个数据包发送
            boolean result = write(txBuffer);

            if(!result) {
//                if(mBleEntityLisenter != null) {
//                    mBleEntityLisenter.onWriteFailed();
                    isWritingEntity = false;
                    isAutoWriteMode = false;
//                    return false;
//                }
            } else {
//                if (mBleEntityLisenter != null) {
                    double progress = new BigDecimal((float)index / dataLength).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//                    mBleEntityLisenter.onWriteProgress(progress);
//                }
            }

//            if (autoWriteMode) {
//                synchronized (lock) {
//                    try {
////                        lock.wait(500);
//                        lock.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            } else {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//            }
        }

        // 这里写入完成
//        if(mBleEntityLisenter != null){
//            mBleEntityLisenter.onWriteSuccess();
//            isWritingEntity = false;
//            isAutoWriteMode = false;
//        }
//        return true;
        L.e("写入完成");

    }

    public static byte[] prepareOutFrame(int msgid, byte frametype, String msg) {
        //512  1024  2048  4096  12288
        int buffer_size = msg.getBytes().length + 10;
        byte[] utf8StringContent;
        byte[] frame = new byte[buffer_size];
        frame[0] = BFrameConst.FRAME_HEAD; //(byte)0xFF
        frame[4] = frametype;
        //5-8
        //消息id
        byte[] msgIdByte = int2byte(msgid);
        //MESSAGE_ID_LENGTH=4

        //content
        L.d("prepareOutFrame  msgid : " + msgid);
        L.d("prepareOutFrame  frametype : " + frametype);
        L.d("prepareOutFrame  msg : " + msg);
        try {
            utf8StringContent = msg.getBytes("utf-8");

            /**
             * 原数组
             * 元数据的起始位置
             * 目标数组
             * 目标数组的开始起始位置
             * 要 copy 的数组的长度
             */
            System.arraycopy(msgIdByte, 0, frame, 5, BFrameConst.MESSAGE_ID_LENGTH);
            System.arraycopy(utf8StringContent, 0, frame, 9, utf8StringContent.length);
        } catch (UnsupportedEncodingException e) {
            L.d("UnsupportedEncodingException  " + e.toString());
            e.printStackTrace();
            // return;
        }
        frame[buffer_size - 1] = BFrameConst.FRAME_END;//(byte)0x00;
        return frame;

    }

    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];

        targets[3] = (byte) (res & 0xff);// 最低位
        targets[2] = (byte) ((res >> 8) & 0xff);// 次低位
        targets[1] = (byte) ((res >> 16) & 0xff);// 次高位
        targets[0] = (byte) (res >>> 24);// 最高位,无符号右移。
        return targets;
    }

    /**
     * 写特征
     *
     * @param data 最大20byte
     */
    private boolean write(byte[] data) {
        boolean result = false;
        if (mBluetoothGatt != null && mCharacteristic != null) {
            L.i("开始写 uuid：" + mCharacteristic.getUuid().toString() + " hex:" + byte2HexStr(data) + " str:" + new String(data));

//      mCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

            mCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            mCharacteristic.setValue(data);
            result = mBluetoothGatt.writeCharacteristic(mCharacteristic);

        } else {
            L.e("写失败");
        }
        return result;
    }

    /**
     * 开启通知
     */
    private void enableNotify() {
        L.e("mBluetoothGatt = " + mBluetoothGatt);
        L.e("mCharacteristic = " + mCharacteristic);
        if (mBluetoothGatt != null && mCharacteristic != null) {
            BluetoothGattDescriptor descriptor = mCharacteristic
                    .getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
            boolean local = mBluetoothGatt.setCharacteristicNotification(mCharacteristic, true);
            L.i("中央设备开启通知 结果:" + local);
            if (descriptor != null) {
                int parentWriteType = mCharacteristic.getWriteType();
                mCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                boolean remote = mBluetoothGatt.writeDescriptor(descriptor);
                mCharacteristic.setWriteType(parentWriteType);
                L.i("外围设备开启通知 结果:" + remote);
            }
        } else {
            L.e("开启通知失败");
        }
    }

    /**
     * 关闭通知
     */
    private void disableNotify() {
        if (mBluetoothGatt != null && mCharacteristic != null) {
            BluetoothGattDescriptor descriptor = mCharacteristic
                    .getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
            boolean local = mBluetoothGatt.setCharacteristicNotification(mCharacteristic, false);
            L.i("中央设备关闭通知 结果:" + local);
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                boolean remote = mBluetoothGatt.writeDescriptor(descriptor);
                L.i("外围设备关闭通知 结果:" + remote);
            }
        } else {
            L.e("关闭通知失败");
        }
    }

    /************************************蓝牙操作相关 结束*********************************************/


    /*************************************开启蓝牙相关 开始**************************************************/

    /**
     * 判断蓝牙是否开启
     *
     * @return
     */
    public boolean isEnable() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    public void openBtByUser() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (!mBluetoothAdapter.isEnabled()) {

            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    L.i("打开蓝牙成功");
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    L.i("打开蓝牙失败");
                }
                break;
        }
    }

    /**
     * 打开蓝牙
     *
     * @param listener
     * @return
     */
    public boolean openBt(OnBTOpenStateListener listener) {
        btOpenStateListener = listener;
        BTStateReceiver receiver = new BTStateReceiver();
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        registerBtStateReceiver(this, receiver);
        if (mBluetoothAdapter.isEnabled()) {
            btOpenStateListener.onBTOpen();
            return true;
        }
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            return mBluetoothAdapter.enable();
        }
        return false;
    }

    private void registerBtStateReceiver(Context context, BTStateReceiver btStateReceiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(btStateReceiver, filter);
    }

    private void unRegisterBtStateReceiver(Context context, BTStateReceiver btStateReceiver) {
        try {
            context.unregisterReceiver(btStateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private OnBTOpenStateListener btOpenStateListener = null;

    public interface OnBTOpenStateListener {
        void onBTOpen();
    }

    /**
     * 用于监听蓝牙开启状态广播
     */
    private class BTStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent intent) {

            String action = intent.getAction();
            L.i("action=" + action);

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

                int state = intent
                        .getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                L.i("state=" + state);

                switch (state) {

                    case BluetoothAdapter.STATE_TURNING_ON:
                        L.i("ACTION_STATE_CHANGED:  STATE_TURNING_ON");
                        break;

                    case BluetoothAdapter.STATE_ON:
                        L.i("ACTION_STATE_CHANGED:  STATE_ON");
                        if (null != btOpenStateListener) {
                            btOpenStateListener.onBTOpen();
                        }
                        unRegisterBtStateReceiver(MainActivity.this, this);
                        break;

                    default:
                        break;

                }

            }

        }

    }

    /*************************************开启蓝牙相关 结束**************************************************/


    // 执行权限检查的方法
    private void checkPermission() {
        if ((ContextCompat.checkSelfPermission(this,
                permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{permission.ACCESS_COARSE_LOCATION},
                    REQUSET_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUSET_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    L.i("权限请求成功");
                } else {
                    L.i("权限请求失败");
                }
                return;
            }
        }
    }


    public String byte2HexStr(byte[] value) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        int bit;

        for (int i = 0; i < value.length; i++) {
            bit = (value[i] & 0x0F0) >> 4;
            sb.append(chars[bit]);
            bit = value[i] & 0x0F;
            sb.append(chars[bit]);
            if (i != value.length - 1) {
                sb.append('-');
            }

        }
        return "(0x) " + sb.toString().trim();
    }

}
