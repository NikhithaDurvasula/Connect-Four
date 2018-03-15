package game.nikhitha.connectfour;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;

import game.nikhitha.connectfour.interfaces.OnGoogleApiChange;
import game.nikhitha.connectfour.managers.MultiPlayer;
import game.nikhitha.connectfour.managers.RoomManager;
import io.fabric.sdk.android.Fabric;

public class App extends Application {

    private static MultiPlayer sMultiplayerManager;
    private static RoomManager sRoomManager;

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        App.sContext = getApplicationContext();
    }

    public static MultiPlayer getGoogleApiManager(Activity activity, OnGoogleApiChange listener) {
        if (sMultiplayerManager == null) {
            sMultiplayerManager = new MultiPlayer();
            sMultiplayerManager.init(activity, listener);
        }
        return sMultiplayerManager;
    }

    public static RoomManager getRoomManager(Activity activity) {
        if (sRoomManager == null) {
            sRoomManager = new RoomManager();
            sRoomManager.init(activity);
        }
        return sRoomManager;
    }

}
