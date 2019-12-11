package cc.noharry.bleexample;

import android.Manifest.permission;
import android.annotation.SuppressLint;
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
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import cc.noharry.bleexample.ContentValue.BFrameConst;
import cc.noharry.bleexample.utils.ClsUtils;
import cc.noharry.bleexample.utils.L;
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
    /**
     * Handler 对象
     */
    private MyHandler mHandler;
    /**
     * 点击发送，开始预分包
     */
    private static final int START_SUBPACKAGE = 0X71;
    private static final int SHOW_LOADING = 0X13;
    private static final int HIDE_LOADING = 0X27;

    /**
     * 调起蓝牙询问的请求码
     */
    private static final int REQUEST_ENABLE_BT = 100;

    /**
     * 蓝牙设备的名称
     */
//    private static final String BLE_DEVICE_NAME = "LIF_BLE";
    private static final String BLE_DEVICE_NAME = "Corey_MI5S_S1";

    /**
     * 数据分包相关参数
     */
    // 是否准备就绪写入
    private boolean isWritingEntity;
    // 最后一包是否自动补零
    private final boolean lastPackComplete = false;
    // 每个包固定长度 20，包括头、尾、msgId
    private int packLength = 20;
    // 给每个数据分包一个消息 ID，递增
    int msgId;

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
        if (null != bluetoothManager) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
//            mBluetoothAdapter.setName("Corey_MIX3_C");
            Log.i("Ble_Client---", "local_name = " + mBluetoothAdapter.getName());
        }

        // 检测蓝牙在本机是否可用
        if (null == mBluetoothAdapter) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            return;
        }

        // 实例化 MyHandler
        mHandler = new MyHandler(this);

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

        // 确保蓝牙可用，若暂未可用，弹框提示用户开启蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // 设置 BLE 设备展示列表的适配器
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

                if (BLE_DEVICE_NAME.equals(result.getDevice().getName())) {
                    L.i("发现 " + BLE_DEVICE_NAME);
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
                if (BLE_DEVICE_NAME.equals(device.getName())) {
                    L.i("发现 " + BLE_DEVICE_NAME);
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

                    // 发现目标特征
                    gatt.discoverServices();
                    // 增加 MTU 容量，不建议使用，依然采用分包的方式
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

                        // TODO: 2019/12/10 马上将本机唯一标识码作为 token，传给 Server 保存
                        String contentStr = BFrameConst.TOKEN;
                        // 给 Handler 传参数，准备预分包，即字符串转 byte[]
                        Message msgSendContent = mHandler.obtainMessage();
                        msgSendContent.what = BFrameConst.START_MSG_ID_UNIQUE;
                        msgSendContent.obj = contentStr;
                        mHandler.sendMessage(msgSendContent);

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
                MainActivity.this.isWritingEntity = true;
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mTvWriteData.setText("statu:" + status + " hexValue:" + byte2HexStr(characteristic.getValue()) + " ,str:"
//                                + new String(characteristic.getValue()));
//                    }
//                });

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
                // TODO: 2019/12/2 这里需要加入判断，是否是当前 msgId 传过去的回调
                MainActivity.this.isWritingEntity = true;


//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mTvNotifyData.setText(" hexValue:" + byte2HexStr(characteristic.getValue()) + " ,str:"
//                                + new String(characteristic.getValue()));
//                    }
//                });

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

                // 获取输入框内容字符串
                String contentStr = mEtWrite.getText().toString().trim();
                if (TextUtils.isEmpty(contentStr)) {
                    Toast.makeText(MainActivity.this, "请输入发送内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 给 Handler 传参数，准备预分包，即字符串转 byte[]
                Message msgSendContent = mHandler.obtainMessage();
                // 数据包类型
                msgSendContent.what = BFrameConst.START_MSG_ID_CONTENT;
                msgSendContent.obj = contentStr;
                mHandler.sendMessage(msgSendContent);
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        preSubpackageByte(data);
//                    }
//                });
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
//            mBluetoothAdapter.startLeScan(uuids, mLeScanCallback);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
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
        L.i("device.name---server = " + device.getName());
        L.i("device.name---local = " + mBluetoothAdapter.getName());
//        try {
//            //创建createBond
//            ClsUtils.createBond(device.getClass(), device);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        mBluetoothGatt = device.connectGatt(this, false, mBluetoothGattCallback);
//        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
//            mBluetoothGatt.requestMtu(512);
//        }
    }

    public static class BluetoothConnectReceiver extends BroadcastReceiver {

        String strPsw = "111111";

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
                L.i("配对监听");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    L.i("未配对");
                    try {
                        /**
                         * cancelPairingUserInput（）取消用户输入密钥框，
                         * 个人觉得一般情况下不要和setPin（setPasskey、setPairingConfirmation、
                         * setRemoteOutOfBandData）一起用，
                         * 这几个方法都会remove掉map里面的key:value（<<<<<也就是互斥的>>>>>>）。
                         */
                        //1.确认配对
                        //ClsUtils.setPairingConfirmation(device.getClass(), device, true);
                        boolean connectResult = ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
//                        boolean connectResult = ClsUtils.pair(device.getAddress(), "1111");
                        //ClsUtils.setPasskey(device.getClass(), device, strPsw);
                        //ClsUtils.cancelPairingUserInput(device.getClass(), device); //一般调用不成功，前言里面讲解过了
                        L.i("配对信息===>>>>connectResult = " + connectResult);
                        abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
                    } catch (Exception e) {
                        L.e("反射异常：" + e);
                        // TODO Auto-generated catch block
                        L.i("请求连接错误");
                    }
                }
            }
        }
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

    /**
     * 获取将要发送的文本，并转换为 byte 数组
     * 即将分包
     * @param data
     * @param msgType 传输数据类型，token/实际数据/
     */
    private void preSubpackageByte(String data, int msgType) {

        // 字符串转换成 Byte 数组
        int dataLength = data.getBytes(StandardCharsets.UTF_8).length;
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        // 定义新的数据包存放数据（加包头包尾）
        byte[] dataFinalBytes = new byte[dataLength + 2 + 4];
        // 包头包尾加两个标识位
        dataFinalBytes[0] = (byte) 0xFF;
        dataFinalBytes[dataFinalBytes.length - 1] = (byte) 0x00;
        // 先放数据包类型
        byte[] msgStartIdByte = int2byte(msgType);
        System.arraycopy(msgStartIdByte, 0, dataFinalBytes, 1, BFrameConst.MESSAGE_ID_LENGTH);
        // 中间放传输内容
        System.arraycopy(dataBytes, 0, dataFinalBytes, 5, dataLength);

        // 分包操作
//        subpackageByte(dataBytes, msgType);
        subpackageByte(dataFinalBytes, msgType);

    }

    /**
     * 数据分包
     *
     * @param data 数据源
     */
    private void subpackageByte(byte[] data, int msgType) {

        // 连接间隔时间修改
        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
        }

        isWritingEntity = true;
        // 数据源数组的指针
        int index = 0;
        // 数据总长度
        int dataLength = data.length;
        // 待传数据有效长度，最后一个包是否需要补零
        int availableLength = dataLength;
        // 待发送数据包的个数
//        int packageCount = ((dataLength % 14 == 0) ? (dataLength / 14) : (dataLength / 14 + 1));
//        int packageCount = ((dataLength % 18 == 0) ? (dataLength / 18) : (dataLength / 18 + 1));
        int packageCount = ((dataLength % 20 == 0) ? (dataLength / 20) : (dataLength / 20 + 1));

        // 重试次数
        int retryCount = 0;
        // 是否消息开始第一个包，内含消息分包的个数
        boolean isMsgStart = true;

        while (index < dataLength) {

            // 未就绪，可能没收到返回，或未成功写入
            if (!isWritingEntity) {

                // 小于五次则等待，多于五次（250ms）则重发
                if (retryCount < 5) {

                    L.e("等待分包");
                    try {
                        Thread.sleep(20L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    retryCount++;
                    continue;

                } else {

                    // 重置次数，方便下次阻塞的时候计数
                    retryCount = 0;

                }

            }
            L.e("开始分包");
            // 开始分包，状态置为未就绪状态
            isWritingEntity = false;

            // 每包数据内容大小为 14
//            int onePackLength = packLength - 6;
//            int onePackLength = packLength - 4;
            int onePackLength = packLength;
            // 最后一包不足长度不会自动补零
            if (!lastPackComplete) {
//                onePackLength = (availableLength >= (packLength - 6) ? (packLength - 6) : availableLength);
//                onePackLength = (availableLength >= (packLength - 4) ? (packLength - 4) : availableLength);
                onePackLength = (availableLength >= packLength) ? packLength : availableLength;
            }

            // 实例化一个数据分包，长度为 20
//            byte[] txBuffer = new byte[onePackLength];
            byte[] txBuffer = new byte[packLength];

            // 数据包头 (byte)0xFF
//            txBuffer[0] = BFrameConst.FRAME_HEAD;
            // 数据包尾 (byte)0x00;
//            txBuffer[19] = BFrameConst.FRAME_END;

            byte[] msgIdByte;
            byte[] packageCountByte;
//            byte[] msgStartIdByte;
            if (isMsgStart) {

                // 数据包 [1]-[4] 为 msgId，起始位的 msgId 为 1，代表只发送头部信息，包含消息分包的个数
                msgIdByte = int2byte(BFrameConst.START_MSG_ID_START);
                packageCountByte = int2byte(packageCount);
//                msgStartIdByte = int2byte(msgType);


                /**
                 * 首包数组拷贝
                 * 原数组
                 * 元数据的起始位置
                 * 目标数组
                 * 目标数组的开始起始位置
                 * 要 copy 的数组的长度
                 */
//                System.arraycopy(msgIdByte, 0, txBuffer, 1, BFrameConst.MESSAGE_ID_LENGTH);
//                System.arraycopy(packageCountByte, 0, txBuffer, 5, BFrameConst.MESSAGE_ID_LENGTH);
//                System.arraycopy(msgStartIdByte, 0, txBuffer, 9, BFrameConst.MESSAGE_ID_LENGTH);
                System.arraycopy(msgIdByte, 0, txBuffer, 0, BFrameConst.MESSAGE_ID_LENGTH);
                System.arraycopy(packageCountByte, 0, txBuffer, 4, BFrameConst.MESSAGE_ID_LENGTH);
//                System.arraycopy(msgStartIdByte, 0, txBuffer, 8, BFrameConst.MESSAGE_ID_LENGTH);

                // 单个数据包发送
                boolean result = write(txBuffer);

//                if (!result) {
//                    isWritingEntity = false;
//                }

                // 将是否为首包置为false，后面的开始发正式数据
                isMsgStart = false;

            } else {

                // 数据包 [1]-[4] 为 msgId
//                msgIdByte = int2byte(msg_id);
//                L.i("msgId = " + msg_id);
//                // TODO: 2019/12/2 应该在收到 Server 回调的时候，做递增
//                msg_id++;


                /**
                 * 数组拷贝
                 * 原数组
                 * 元数据的起始位置
                 * 目标数组
                 * 目标数组的开始起始位置
                 * 要 copy 的数组的长度
                 */
//                System.arraycopy(msgIdByte, 0, txBuffer, 1, BFrameConst.MESSAGE_ID_LENGTH);
                // 2019/12/11 只有首包才传msgId，内容包不传 msg_id 了
//                System.arraycopy(msgIdByte, 0, txBuffer, 0, BFrameConst.MESSAGE_ID_LENGTH);

                // 数据包 [5]-[18] 为内容
//                for (int i = 4; i < onePackLength + 4; i++) {
//                    if (index < dataLength) {
//                        txBuffer[i] = data[index++];
//                    }
//                }
                for (int i = 0; i < onePackLength; i++) {
                    if (index < dataLength) {
                        txBuffer[i] = data[index++];
                    }
                }
//                L.i("index = " + index);
//                L.i("onePackLength = " + onePackLength);
//                L.i("dataLength = " + dataLength);

                // 更新剩余数据长度
                availableLength -= onePackLength;

                // 单个数据包发送
                boolean result = write(txBuffer);

//                if (!result) {
//                    isWritingEntity = false;
//                } else {
//                    double progress = new BigDecimal((float) index / dataLength).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
////                    mBleEntityLisenter.onWriteProgress(progress);
////                }
//                }

            }

            try {
                Thread.sleep(20L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }

        // 连接间隔时间修改
        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
        }
        L.e("写入完成");

    }

    public static int byteArrayToInt(byte[] b) {
        byte[] a = new byte[4];
        int i = a.length - 1, j = b.length - 1;
        for (; i >= 0; i--, j--) {//从b的尾部(即int值的低位)开始copy数据
            if (j >= 0)
                a[i] = b[j];
            else
                a[i] = 0;//如果b.length不足4,则将高位补0
        }
        int v0 = (a[0] & 0xff) << 24;//&0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
        int v1 = (a[1] & 0xff) << 16;
        int v2 = (a[2] & 0xff) << 8;
        int v3 = (a[3] & 0xff);
        return v0 + v1 + v2 + v3;
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

            mCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
//            mCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

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


    /**
     * 自定义一个静态类，防止内存泄漏
     */
    private static class MyHandler extends Handler {

        WeakReference<MainActivity> mainActivity;

        public MyHandler(MainActivity mainActivity) {
            this.mainActivity = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {

            final MainActivity act = mainActivity.get();

            switch (msg.what) {

                case BFrameConst.START_MSG_ID_UNIQUE:

                    /**
                     * 发送 TOKEN
                     * 第一个参数为发送内容，第二个参数为数据包类型：TOKEN/发送内容
                     */
                    act.preSubpackageByte((String) msg.obj, BFrameConst.START_MSG_ID_UNIQUE);
                    break;

                case BFrameConst.START_MSG_ID_CONTENT:

                    /**
                     * 发送实际内容
                     * 第一个参数为发送内容，第二个参数为数据包类型：TOKEN/发送内容
                     */
                    act.preSubpackageByte((String) msg.obj, BFrameConst.START_MSG_ID_CONTENT);
                    break;

//                case SHOW_LOADING: // 需要显示 Loading
//
//                    // 显示登录 Loading
//                    act.showLoading();
//
//                    break;
//
//                case HIDE_LOADING: //  需要隐藏 Loading
//
//                    // 隐藏 Loading
//                    act.hideLoading();
//
//                    break;

            }

        }

    }

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
