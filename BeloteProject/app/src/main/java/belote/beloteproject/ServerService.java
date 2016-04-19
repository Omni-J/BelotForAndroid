package belote.beloteproject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerService extends Service {

    private ServerSocket socket;

    private boolean running = true;


    public void shutDown() {
        running = false;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        try {
            socket = new ServerSocket(7777);
        } catch (IOException e) {
            System.exit(-1);
        }
        while (running) {
            try {
                socket.accept();

            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
        return null;
    }
}
