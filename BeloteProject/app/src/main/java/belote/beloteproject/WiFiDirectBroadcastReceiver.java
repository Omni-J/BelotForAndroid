package belote.beloteproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;

import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private  CreateRoomActivity mActivity;
    private String deviceName;
    private  String deviceAddress;


    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       CreateRoomActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                mManager.requestPeers(mChannel, mActivity.getPeersListListener());
            }
            // Call WifiP2pManager.requestPeers() to get a list of current peers
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        // TODO Auto-generated method stub
                        if (info.groupFormed && info.isGroupOwner && mActivity.getConnectedPeers() == mActivity.getNumberOfPlayers()) {
                            Intent serverIntent = new Intent(mActivity, ServerService.class);
                            serverIntent.putExtra("name", deviceName);
                            serverIntent.putExtra("address", deviceAddress);
                            serverIntent.putStringArrayListExtra("addresses", mActivity.getDevicesAdress());
                            mActivity.startService(serverIntent);

                        }
                        if (info.groupFormed) {
                            Intent clientIntent = new Intent(mActivity, InGameAct.class);
                            clientIntent.putExtra("host", info.groupOwnerAddress.toString());
                            clientIntent.putExtra("name", deviceName);
                            clientIntent.putExtra("address", deviceAddress);
                            mActivity.startActivity(clientIntent);
                        }
                    }
                });
            }        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

}
