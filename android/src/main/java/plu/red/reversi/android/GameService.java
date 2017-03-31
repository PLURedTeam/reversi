package plu.red.reversi.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class GameService extends Service {
    public GameService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
