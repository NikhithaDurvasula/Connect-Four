package game.nikhitha.connectfour.gameutils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.api.GoogleApiClient;

public abstract class GameActivity extends FragmentActivity implements
        GameHelper.GameHelperListener {

    // The game helper object. This class is mainly a wrapper around this object.
    protected GameHelper Helper;

    // We expose these constants here because we don't want users of this class
    // to have to know about GameHelper at all.
    public static final int CLIENT_GAMES = GameHelper.CLIENT_GAMES;
    public static final int CLIENT_PLUS = GameHelper.CLIENT_PLUS;
    public static final int CLIENT_ALL = GameHelper.CLIENT_ALL;

    // Requested clients. By default, that's just the games client.
    protected int RequestedClients = CLIENT_GAMES;

    private final static String TAG = "GameActivity";
    protected boolean DebugLog = false;

    /** Constructs a GameActivity with default client (GamesClient). */
    protected GameActivity() {
        super();
    }

    /**
     * Constructs a GameActivity with the requested clients.
     * @param requestedClients The requested clients (a combination of CLIENT_GAMES,
     *         CLIENT_PLUS).
     */
    protected GameActivity(int requestedClients) {
        super();
        setRequestedClients(requestedClients);
    }

    /**
     * Sets the requested clients. The preferred way to set the requested clients is
     * via the constructor, but this method is available if for some reason your code
     * cannot do this in the constructor. This must be called before onCreate or getGameHelper()
     * in order to have any effect. If called after onCreate()/getGameHelper(), this method
     * is a no-op.
     *
     * @param requestedClients A combination of the flags CLIENT_GAMES, CLIENT_PLUS
     *         or CLIENT_ALL to request all available clients.
     */
    protected void setRequestedClients(int requestedClients) {
        RequestedClients = requestedClients;
    }

    public GameHelper getGameHelper() {
        if (Helper == null) {
            Helper = new GameHelper(this, RequestedClients);
        }
        return Helper;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Helper == null) {
            getGameHelper();
        }
        Helper.setup(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Helper.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Helper.onStop();
    }

    @Override
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        Helper.onActivityResult(request, response, data);
    }

    protected GoogleApiClient getApiClient() {
        return Helper.getApiClient();
    }

    protected boolean isSignedIn() {
        return Helper.isSignedIn();
    }

    protected void beginUserInitiatedSignIn() {
        Helper.beginUserInitiatedSignIn();
    }

    protected void signOut() {
        Helper.signOut();
    }

    protected String getInvitationId() {
        return Helper.getInvitationId();
    }

    protected void reconnectClient() {
        Helper.reconnectClient();
    }

    protected boolean hasSignInError() {
        return Helper.hasSignInError();
    }

    protected GameHelper.SignInFailureReason getSignInError() {
        return Helper.getSignInError();
    }
}
