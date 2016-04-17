package belote.beloteproject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class CreateRoomActivity extends AppCompatActivity {
    WifiP2pManager mManager;
    Channel mChannel;
    BroadcastReceiver mReceiver;
    ListView All, Choice;
    IntentFilter mIntentFilter;
    Handler progressHandler;
    private final IntentFilter intentFilter = new IntentFilter();


    ArrayList<String> choiceList = new ArrayList<String>();
    ArrayList<String> allList = new ArrayList<String>();
    HashMap<String, String> devices = new HashMap<String, String>();
    ArrayAdapter<String> allListAdapter, choiceLisAdapter;
    int connectedPeers = 0;
    private final int numberOfPlayers = 1;


    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            String[] lines = peerList.toString().split(System.getProperty("line.separator"));
            for (String line : lines) {
                if (line.contains("deviceAddress: ")){
                    String address = line.split("deviceAddress: ")[1];
                    String name = peerList.get(address).deviceName;
                    if(!allList.contains(name) && !choiceList.contains(name)){
                        allList.add(name);
                        devices.put(name, address);
                        allListAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_anctivity);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        //To register the BroadastReceiver
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel =  (Channel) mManager.initialize(this, getMainLooper(), null); //It was necessary to make a cast (Channel)
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);


        //To define the filter in the BroadcastReceiver
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        allListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, allList);
        choiceLisAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, choiceList);

        All = (ListView) findViewById(R.id.all);
        All.setAdapter(allListAdapter);
        All.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!choiceList.contains(allList.get(position)) && (choiceList.size()< numberOfPlayers)){
                    choiceList.add(allList.get(position));
                    allList.remove(position);
                    allListAdapter.notifyDataSetChanged();
                    choiceLisAdapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(getApplicationContext(), "too much players", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Choice = (ListView) findViewById(R.id.myList);
        Choice.setAdapter(choiceLisAdapter);
        Choice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!allList.contains(choiceList.get(position))){
                    allList.add(choiceList.get(position));
                    choiceList.remove(position);
                    choiceLisAdapter.notifyDataSetChanged();
                    allListAdapter.notifyDataSetChanged();
                }
            }
        });


    }
    public void SearchBotton(View v) {
            searchDevice();
        }



        void searchDevice() {
        mManager.discoverPeers((WifiP2pManager.Channel) mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "searching", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();

            }
        });
    }
    public void StartGame(View v) {
        if (!(choiceList.size() < numberOfPlayers)) {
//               Toast.makeText(getApplicationContext(), "click", Toast.LENGTH_LONG).show();
            final ProgressDialog connectingDialog = new ProgressDialog(this);
            connectingDialog.setCancelable(false);
            String message = getResources().getString(R.string.connecting_to_other);
            connectingDialog.setMessage(message);
            connectingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            connectingDialog.setProgress(0);
            connectingDialog.setMax(numberOfPlayers + 1);
            connectingDialog.show();
            mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    // TODO Auto-generated method stub
                    progressHandler.sendMessage(progressHandler.obtainMessage());
                }

                @Override
                public void onFailure(int reason) {
                    // TODO Auto-generated method stub
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void logOut(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public ArrayList<String> getDevicesAdress(){
        ArrayList<String> result = new ArrayList<String>();
        for(Map.Entry<String,String> map : devices.entrySet()){
            result.add(map.getValue());
        }
        return result;
    }
    public int getNumberOfPlayers(){return numberOfPlayers;}
    public int getConnectedPeers(){
        return connectedPeers;
    }
    public WifiP2pManager.PeerListListener getPeersListListener(){
        return peerListListener;
    }

}
