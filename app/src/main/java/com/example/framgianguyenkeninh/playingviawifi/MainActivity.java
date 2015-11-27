package com.example.framgianguyenkeninh.playingviawifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private boolean isWifiEnable;
    private RecyclerView mRecyclerView;
    private ListDeviceAdapter listDeviceAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<WifiP2pDevice> listDevice;
    public WifiP2pInfo info;
    private Button find;
    private Button btnSend;
    private EditText textSend;
    private boolean isServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        find = (Button) findViewById(R.id.find);
        btnSend = (Button) findViewById(R.id.send);
        textSend = (EditText) findViewById(R.id.text_chat);

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(textSend.getText())){
                    sendMessage(isServer);
                }
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.list_device);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        listDevice = new ArrayList<>();

        listDeviceAdapter = new ListDeviceAdapter(this, listDevice);
        listDeviceAdapter.setOnItemClickListener(onItemClickListener);
        mRecyclerView.setAdapter(listDeviceAdapter);

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mWifiP2pManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void sendMessage(boolean isServer) {

    }

    private OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void connect(int position) {
         Log.i("POSITION", ""+position);
            if(!isWifiEnable) return;

            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = listDevice.get(position).deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    //success logic
                    Toast.makeText(MainActivity.this, "Pair Success", Toast.LENGTH_LONG).show();
                    Log.d("LISTENER", "SUCCESS");
                    Resources resources = getResources();
                }


                @Override
                public void onFailure(int reason) {
                    //failure logic
                }
            });
        }

        @Override
        public void disconnect(int layoutPosition) {
            mWifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    listDevice.clear();
                    listDeviceAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(int reason) {

                }
            });
        }

        @Override
        public void send() {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 12);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void setIsWifiP2pEnabled(boolean enabled) {
        this.isWifiEnable = enabled;
    }

    public void updateThisDevice(WifiP2pDevice parcelableExtra) {
        Log.i("THIS_DEVICE_CHANGE", parcelableExtra.deviceAddress + ";" + parcelableExtra.deviceName + "; " + parcelableExtra.isGroupOwner());
    }

    public void showListDevice(WifiP2pDeviceList peerList) {
        listDevice.clear();
        listDevice.addAll(peerList.getDeviceList());
        listDeviceAdapter.notifyDataSetChanged();
    }

    public void resetData() {
        listDevice.clear();
        listDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        Uri uri = data.getData();
        Intent serviceIntent = new Intent(this, FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                info.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8888);
        startService(serviceIntent);
    }

    public void startServer() {
        isServer = true;
        FileServerAsyncTask asyncTask = new FileServerAsyncTask();
        asyncTask.execute();
    }

    public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8888);
                Log.d("LISTEN", "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d("LISTEN", "Server: connection done");
                final File f = new File(Environment.getExternalStorageDirectory() + "/"
                        + getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                        + ".jpg");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d("LISTEN", "server: copying files " + f.toString());
                InputStream inputstream = client.getInputStream();
                copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
                return f.getAbsolutePath();
            } catch (IOException e) {
                return null;
            }
        }

        /**
         * Start activity that can handle the JPEG image
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                startActivity(intent);
            }
        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
