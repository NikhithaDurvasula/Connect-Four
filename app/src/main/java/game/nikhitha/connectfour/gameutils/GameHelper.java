package game.nikhitha.connectfour.gameutils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Games.GamesOptions;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.request.GameRequest;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.Plus.PlusOptions;

import java.util.ArrayList;

public class GameHelper implements com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener {

    static final String TAG = "GameHelper";

    /** Listener for sign-in success or failure events. */
    public interface GameHelperListener {
        void onSignInFailed();

        /** Called when sign-in succeeds. */
        void onSignInSucceeded();
    }

    // configuration done?
    private boolean SetupDone = false;

    // are we currently connecting?
    private boolean Connecting = false;

    // Are we expecting the result of a resolution flow?
    boolean ExpectingResolution = false;

    // was the sign-in flow cancelled when we tried it?
    // if true, we know not to try again automatically.
    boolean SignInCancelled = false;

    /**
     * The Activity we are bound to. We need to keep a reference to the Activity
     * because some games methods require an Activity (a Context won't do). We
     * are careful not to leak these references: we release them on onStop().
     */
    Activity Activity = null;

    // app context
    Context AppContext = null;

    // Request code we use when invoking other Activities to complete the
    // sign-in flow.
    final static int RC_RESOLVE = 9001;

    // Request code when invoking Activities whose result we don't care about.
    final static int RC_UNUSED = 9002;

    // the Google API client builder we will use to create GoogleApiClient
    com.google.android.gms.common.api.GoogleApiClient.Builder GoogleApiClientBuilder = null;

    // Api options to use when adding each API, null for none
    GamesOptions GamesApiOptions = GamesOptions.builder().build();
    PlusOptions PlusApiOptions = null;

    // Google API client object we manage.
    com.google.android.gms.common.api.GoogleApiClient GoogleApiClient = null;

    // Client request flags
    public final static int CLIENT_NONE = 0x00;
    public final static int CLIENT_GAMES = 0x01;
    public final static int CLIENT_PLUS = 0x02;
    public final static int CLIENT_SNAPSHOT = 0x08;
    public final static int CLIENT_ALL = CLIENT_GAMES | CLIENT_PLUS
            | CLIENT_SNAPSHOT;

    // What clients were requested? (bit flags)
    int RequestedClients = CLIENT_NONE;

    // Whether to automatically try to sign in on onStart(). We only set this
    // to true when the sign-in process fails or the user explicitly signs out.
    // We set it back to false when the user initiates the sign in process.
    boolean ConnectOnStart = true;

    /*
     * Whether user has specifically requested that the sign-in process begin.
     * If UserInitiatedSignIn is false, we're in the automatic sign-in attempt
     * that we try once the Activity is started -- if true, then the user has
     * already clicked a "Sign-In" button or something similar
     */
    boolean UserInitiatedSignIn = false;

    // The connection result we got from our last attempt to sign-in.
    com.google.android.gms.common.ConnectionResult ConnectionResult = null;

    // The error that happened during sign-in.
    SignInFailureReason SignInFailureReason = null;

    // Should we show error dialog boxes?
    boolean ShowErrorDialogs = true;

    // Print debug logs?
    boolean DebugLog = false;

    Handler Handler;

    /*
     * If we got an invitation when we connected to the games client, it's here.
     * Otherwise, it's null.
     */
    com.google.android.gms.games.multiplayer.Invitation Invitation;

    /*
     * If we got turn-based match when we connected to the games client, it's
     * here. Otherwise, it's null.
     */
    com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch TurnBasedMatch;

    /*
     * If we have incoming requests when we connected to the games client, they
     * are here. Otherwise, it's null.
     */
    ArrayList<GameRequest> Requests;

    // Listener
    GameHelperListener Listener = null;

    // Should we start the flow to sign the user in automatically on startup? If
    // so, up to
    // how many times in the life of the application?
    static final int DEFAULT_MAX_SIGN_IN_ATTEMPTS = 3;
    int MaxAutoSignInAttempts = DEFAULT_MAX_SIGN_IN_ATTEMPTS;

    /**
     * Construct a GameHelper object, initially tied to the given Activity.
     * After constructing this object, call @link{setup} from the onCreate()
     * method of your Activity.
     *
     * @param clientsToUse
     *            the API clients to use (a combination of the CLIENT_* flags,
     *            or CLIENT_ALL to mean all clients).
     */
    public GameHelper(Activity activity, int clientsToUse) {
        Activity = activity;
        AppContext = activity.getApplicationContext();
        RequestedClients = clientsToUse;
        Handler = new Handler();
    }

    /**
     * Sets the maximum number of automatic sign-in attempts to be made on
     * application startup. This maximum is over the lifetime of the application
     * (it is stored in a SharedPreferences file). So, for example, if you
     * specify 2, then it means that the user will be prompted to sign in on app
     * startup the first time and, if they cancel, a second time the next time
     * the app starts, and, if they cancel that one, never again. Set to 0 if
     * you do not want the user to be prompted to sign in on application
     * startup.
     */
    public void setMaxAutoSignInAttempts(int max) {
        MaxAutoSignInAttempts = max;
    }

    void assertConfigured(String operation) {
        if (!SetupDone) {
            String error = "GameHelper error: Operation attempted without setup: "
                    + operation
                    + ". The setup() method must be called before attempting any other operation.";
            throw new IllegalStateException(error);
        }
    }

    private void doApiOptionsPreCheck() {
        if (GoogleApiClientBuilder != null) {
            String error = "GameHelper: you cannot call set*ApiOptions after the client "
                    + "builder has been created. Call it before calling createApiClientBuilder() "
                    + "or setup().";
            throw new IllegalStateException(error);
        }
    }

    /**
     * Sets the options to pass when setting up the Games API. Call before
     * setup().
     */
    public void setGamesApiOptions(GamesOptions options) {
        doApiOptionsPreCheck();
        GamesApiOptions = options;
    }

    /**
     * Sets the options to pass when setting up the Plus API. Call before
     * setup().
     */
    public void setPlusApiOptions(PlusOptions options) {
        doApiOptionsPreCheck();
        PlusApiOptions = options;
    }

    /**
     * Creates a GoogleApiClient.Builder for use with @link{#setup}. Normally,
     * you do not have to do this; use this method only if you need to make
     * nonstandard setup (e.g. adding extra scopes for other APIs) on the
     * GoogleApiClient.Builder before calling @link{#setup}.
     */
    public com.google.android.gms.common.api.GoogleApiClient.Builder createApiClientBuilder() {
        if (SetupDone) {
            String error = "GameHelper: you called GameHelper.createApiClientBuilder() after "
                    + "calling setup. You can only get a client builder BEFORE performing setup.";
            throw new IllegalStateException(error);
        }

        com.google.android.gms.common.api.GoogleApiClient.Builder builder = new GoogleApiClient.Builder(
                Activity, this, this);

        if (0 != (RequestedClients & CLIENT_GAMES)) {
            builder.addApi(Games.API, GamesApiOptions);
            builder.addScope(Games.SCOPE_GAMES);
        }

        if (0 != (RequestedClients & CLIENT_PLUS)) {
            builder.addApi(Plus.API);
            builder.addScope(Plus.SCOPE_PLUS_LOGIN);
        }

        if (0 != (RequestedClients & CLIENT_SNAPSHOT)) {
          builder.addScope(Drive.SCOPE_APPFOLDER);
          builder.addApi(Drive.API);
        }

        GoogleApiClientBuilder = builder;
        return builder;
    }

    /**
     * Performs setup on this GameHelper object. Call this from the onCreate()
     * method of your Activity. This will create the clients and do a few other
     * initialization tasks. Next, call @link{#onStart} from the onStart()
     * method of your Activity.
     *
     * @param listener
     *            The listener to be notified of sign-in events.
     */
    public void setup(GameHelperListener listener) {
        if (SetupDone) {
            String error = "GameHelper: you cannot call GameHelper.setup() more than once!";
            throw new IllegalStateException(error);
        }
        Listener = listener;
        if (GoogleApiClientBuilder == null) {
            // we don't have a builder yet, so create one
            createApiClientBuilder();
        }

        GoogleApiClient = GoogleApiClientBuilder.build();
        GoogleApiClientBuilder = null;
        SetupDone = true;
    }

    /**
     * Returns the GoogleApiClient object. In order to call this method, you
     * must have called @link{setup}.
     */
    public com.google.android.gms.common.api.GoogleApiClient getApiClient() {
        if (GoogleApiClient == null) {
            throw new IllegalStateException(
                    "No GoogleApiClient. Did you call setup()?");
        }
        return GoogleApiClient;
    }

    /** Returns whether or not the user is signed in. */
    public boolean isSignedIn() {
        return GoogleApiClient != null && GoogleApiClient.isConnected();
    }

    /** Returns whether or not we are currently connecting */
    public boolean isConnecting() {
        return Connecting;
    }

    /**
     * Returns whether or not there was a (non-recoverable) error during the
     * sign-in process.
     */
    public boolean hasSignInError() {
        return SignInFailureReason != null;
    }

    /**
     * Returns the error that happened during the sign-in process, null if no
     * error occurred.
     */
    public SignInFailureReason getSignInError() {
        return SignInFailureReason;
    }

    // Set whether to show error dialogs or not.
    public void setShowErrorDialogs(boolean show) {
        ShowErrorDialogs = show;
    }

    /** Call this method from your Activity's onStart(). */
    public void onStart(Activity act) {
        Activity = act;
        AppContext = act.getApplicationContext();

        assertConfigured("onStart");

        if (ConnectOnStart) {
            if (GoogleApiClient.isConnected()) {
                Log.w(TAG,
                        "GameHelper: client was already connected on onStart()");
            } else {
                Connecting = true;
                GoogleApiClient.connect();
            }
        } else {
            Handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyListener(false);
                }
            }, 1000);
        }
    }

    /** Call this method from your Activity's onStop(). */
    public void onStop() {
        assertConfigured("onStop");
        if (GoogleApiClient.isConnected()) {
            GoogleApiClient.disconnect();
        }
        Connecting = false;
        ExpectingResolution = false;

        // let go of the Activity reference
        Activity = null;
    }

    /**
     * Returns the invitation ID received through an invitation notification.
     * This should be called from your GameHelperListener's
     *
     * @link{GameHelperListener#onSignInSucceeded method, to check if there's an
     *                                            invitation available. In that
     *                                            case, accept the invitation.
     * @return The id of the invitation, or null if none was received.
     */
    public String getInvitationId() {
        if (!GoogleApiClient.isConnected()) {
            Log.w(TAG,
                    "Warning: getInvitationId() should only be called when signed in, "
                            + "that is, after getting onSignInSuceeded()");
        }
        return Invitation == null ? null : Invitation.getInvitationId();
    }

    /**
     * Returns the invitation received through an invitation notification. This
     * should be called from your GameHelperListener's
     *
     * @link{GameHelperListener#onSignInSucceeded method, to check if there's an
     *                                            invitation available. In that
     *                                            case, accept the invitation.
     * @return The invitation, or null if none was received.
     */
    public com.google.android.gms.games.multiplayer.Invitation getInvitation() {
        if (!GoogleApiClient.isConnected()) {
            Log.w(TAG,
                    "Warning: getInvitation() should only be called when signed in, "
                            + "that is, after getting onSignInSuceeded()");
        }
        return Invitation;
    }

    public boolean hasInvitation() {
        return Invitation != null;
    }

    public boolean hasTurnBasedMatch() {
        return TurnBasedMatch != null;
    }

    public boolean hasRequests() {
        return Requests != null;
    }

    public void clearInvitation() {
        Invitation = null;
    }

    public void clearTurnBasedMatch() {
        TurnBasedMatch = null;
    }

    public void clearRequests() {
        Requests = null;
    }

    /**
     * Returns the tbmp match received through an invitation notification. This
     * should be called from your GameHelperListener's
     *
     * @link{GameHelperListener#onSignInSucceeded method, to check if there's a
     *                                            match available.
     * @return The match, or null if none was received.
     */
    public com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch getTurnBasedMatch() {
        if (!GoogleApiClient.isConnected()) {
            Log.w(TAG,
                    "Warning: getTurnBasedMatch() should only be called when signed in, "
                            + "that is, after getting onSignInSuceeded()");
        }
        return TurnBasedMatch;
    }

    /**
     * Returns the requests received through the onConnected bundle. This should
     * be called from your GameHelperListener's
     *
     * @link{GameHelperListener#onSignInSucceeded method, to check if there are
     *                                            incoming requests that must be
     *                                            handled.
     * @return The requests, or null if none were received.
     */
    public ArrayList<GameRequest> getRequests() {
        if (!GoogleApiClient.isConnected()) {
        }
        return Requests;
    }

    /** Sign out and disconnect from the APIs. */
    public void signOut() {
        if (!GoogleApiClient.isConnected()) {
            // nothing to do
            return;
        }

        // for Plus, "signing out" means clearing the default account and
        // then disconnecting
        if (0 != (RequestedClients & CLIENT_PLUS)) {
            Plus.AccountApi.clearDefaultAccount(GoogleApiClient);
        }

        // For the games client, signing out means calling signOut and
        // disconnecting
        if (0 != (RequestedClients & CLIENT_GAMES)) {
            Games.signOut(GoogleApiClient);
        }

        // Ready to disconnect
        ConnectOnStart = false;
        Connecting = false;
        GoogleApiClient.disconnect();
    }

    /**
     * Handle activity result. Call this method from your Activity's
     * onActivityResult callback. If the activity result pertains to the sign-in
     * process, processes it appropriately.
     */
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {
        if (requestCode != RC_RESOLVE) {
            return;
        }

        // no longer expecting a resolution
        ExpectingResolution = false;

        if (!Connecting) {
            return;
        }

        // We're coming back from an activity that was launched to resolve a
        // connection problem. For example, the sign-in UI.
        if (responseCode == Activity.RESULT_OK) {
            // Ready to try to connect again.
            connect();
        } else if (responseCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
            connect();
        } else if (responseCode == Activity.RESULT_CANCELED) {
            // User cancelled.
            SignInCancelled = true;
            ConnectOnStart = false;
            UserInitiatedSignIn = false;
            SignInFailureReason = null; // cancelling is not a failure!
            Connecting = false;
            GoogleApiClient.disconnect();

            // increment # of cancellations
            int prevCancellations = getSignInCancellations();
            int newCancellations = incrementSignInCancellations();

            notifyListener(false);
        } else {
            // Whatever the problem we were trying to solve, it was not
            // solved. So give up and show an error message.
            giveUp(new SignInFailureReason(ConnectionResult.getErrorCode(),
                    responseCode));
        }
    }

    void notifyListener(boolean success) {
        if (Listener != null) {
            if (success) {
                Listener.onSignInSucceeded();
            } else {
                Listener.onSignInFailed();
            }
        }
    }

    /**
     * Starts a user-initiated sign-in flow. This should be called when the user
     * clicks on a "Sign In" button. As a result, authentication/consent dialogs
     * may show up. At the end of the process, the GameHelperListener's
     * onSignInSucceeded() or onSignInFailed() methods will be called.
     */
    public void beginUserInitiatedSignIn() {
        resetSignInCancellations();
        SignInCancelled = false;
        ConnectOnStart = true;

        if (GoogleApiClient.isConnected()) {
            // nothing to do
            notifyListener(true);
            return;
        }

        // indicate that user is actively trying to sign in (so we know to
        // resolve
        // connection problems by showing dialogs)
        UserInitiatedSignIn = true;

        if (ConnectionResult != null) {
            // We have a pending connection result from a previous failure, so
            // start with that.
            Connecting = true;
            resolveConnectionResult();
        } else {
            // We don't have a pending connection result, so start anew.
            Connecting = true;
            connect();
        }
    }

    void connect() {
        if (GoogleApiClient.isConnected()) {
            return;
        }
        Connecting = true;
        Invitation = null;
        TurnBasedMatch = null;
        GoogleApiClient.connect();
    }

    /**
     * Disconnects the API client, then connects again.
     */
    public void reconnectClient() {
        if (!GoogleApiClient.isConnected()) {
            Log.w(TAG, "reconnectClient() called when client is not connected.");
            // interpret it as a request to connect
            connect();
        } else {
            GoogleApiClient.reconnect();
        }
    }

    /** Called when we successfully obtain a connection to a client. */
    @Override
    public void onConnected(Bundle connectionHint) {

        if (connectionHint != null) {
            com.google.android.gms.games.multiplayer.Invitation inv = connectionHint
                    .getParcelable(Multiplayer.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                Invitation = inv;
            }

            // Do we have any requests pending?
            Requests = Games.Requests
                    .getGameRequestsFromBundle(connectionHint);
            if (!Requests.isEmpty()) {
                // We have requests in onConnected's connectionHint.
            }

            TurnBasedMatch = connectionHint
                    .getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);
        }

        // we're good to go
        succeedSignIn();
    }

    void succeedSignIn() {
        SignInFailureReason = null;
        ConnectOnStart = true;
        UserInitiatedSignIn = false;
        Connecting = false;
        notifyListener(true);
    }

    private final String GAMEHELPER_SHARED_PREFS = "GAMEHELPER_SHARED_PREFS";
    private final String KEY_SIGN_IN_CANCELLATIONS = "KEY_SIGN_IN_CANCELLATIONS";

    // Return the number of times the user has cancelled the sign-in flow in the
    // life of the app
    int getSignInCancellations() {
        SharedPreferences sp = AppContext.getSharedPreferences(
                GAMEHELPER_SHARED_PREFS, Context.MODE_PRIVATE);
        return sp.getInt(KEY_SIGN_IN_CANCELLATIONS, 0);
    }

    // Increments the counter that indicates how many times the user has
    // cancelled the sign in
    // flow in the life of the application
    int incrementSignInCancellations() {
        int cancellations = getSignInCancellations();
        SharedPreferences.Editor editor = AppContext.getSharedPreferences(
                GAMEHELPER_SHARED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(KEY_SIGN_IN_CANCELLATIONS, cancellations + 1);
        editor.commit();
        return cancellations + 1;
    }

    // Reset the counter of how many times the user has cancelled the sign-in
    // flow.
    void resetSignInCancellations() {
        SharedPreferences.Editor editor = AppContext.getSharedPreferences(
                GAMEHELPER_SHARED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(KEY_SIGN_IN_CANCELLATIONS, 0);
        editor.commit();
    }

    /** Handles a connection failure. */
    @Override
    public void onConnectionFailed(com.google.android.gms.common.ConnectionResult result) {
        // save connection result for later reference

        ConnectionResult = result;

        int cancellations = getSignInCancellations();
        boolean shouldResolve = false;

        if (UserInitiatedSignIn) {
            shouldResolve = true;
        } else if (SignInCancelled) {
            shouldResolve = false;
        } else if (cancellations < MaxAutoSignInAttempts) {
            shouldResolve = true;
        } else {
            shouldResolve = false;
        }

        if (!shouldResolve) {
            // Fail and wait for the user to want to sign in.
            ConnectionResult = result;
            Connecting = false;
            notifyListener(false);
            return;
        }

        // Resolve the connection result. This usually means showing a dialog or
        // starting an Activity that will allow the user to give the appropriate
        // consents so that sign-in can be successful.
        resolveConnectionResult();
    }

    /**
     * Attempts to resolve a connection failure. This will usually involve
     * starting a UI flow that lets the user give the appropriate consents
     * necessary for sign-in to work.
     */
    void resolveConnectionResult() {
        // Try to resolve the problem

        if (ConnectionResult.hasResolution()) {
            // This problem can be fixed. So let's try to fix it.
            try {
                // launch appropriate UI flow (which might, for example, be the
                // sign-in flow)
                ExpectingResolution = true;
                ConnectionResult.startResolutionForResult(Activity,
                        RC_RESOLVE);
            } catch (SendIntentException e) {
                // Try connecting again
                connect();
            }
        } else {
            // It's not a problem what we can solve, so give up and show an
            // error.
            giveUp(new SignInFailureReason(ConnectionResult.getErrorCode()));
            
            ConnectionResult = null;
        }
    }

    public void disconnect() {
        if (GoogleApiClient.isConnected()) {
            GoogleApiClient.disconnect();
        } else {
            Log.w(TAG,
                    "disconnect() called when client was already disconnected.");
        }
    }

    /**
     * Give up on signing in due to an error. Shows the appropriate error
     * message to the user, using a standard error dialog as appropriate to the
     * cause of the error. That dialog will indicate to the user how the problem
     * can be solved (for example, re-enable Google Play Services, upgrade to a
     * new version, etc).
     */
    void giveUp(SignInFailureReason reason) {
        ConnectOnStart = false;
        disconnect();
        SignInFailureReason = reason;

        showFailureDialog();
        Connecting = false;
        notifyListener(false);
    }

    /** Called when we are disconnected from the Google API client. */
    @Override
    public void onConnectionSuspended(int cause) {
        disconnect();
        SignInFailureReason = null;
        Connecting = false;
        notifyListener(false);
    }

    public void showFailureDialog() {
        if (SignInFailureReason != null) {
            int errorCode = SignInFailureReason.getServiceErrorCode();
            int actResp = SignInFailureReason.getActivityResultCode();

            if (ShowErrorDialogs) {
                showFailureDialog(Activity, actResp, errorCode);
            }
        }
    }

    /** Shows an error dialog that's appropriate for the failure reason. */
    public static void showFailureDialog(Activity activity, int actResp,
                                         int errorCode) {
        if (activity == null) {
            Log.e("GameHelper", "*** No Activity. Can't show failure dialog!");
            return;
        }
        Dialog errorDialog = null;

        errorDialog.show();
    }

    static Dialog makeSimpleDialog(Activity activity, String text) {
        return (new AlertDialog.Builder(activity)).setMessage(text)
                .setNeutralButton(android.R.string.ok, null).create();
    }

    static Dialog
    makeSimpleDialog(Activity activity, String title, String text) {
        return (new AlertDialog.Builder(activity)).setMessage(text)
                .setTitle(title).setNeutralButton(android.R.string.ok, null)
                .create();
    }

    // Represents the reason for a sign-in failure
    public static class SignInFailureReason {
        public static final int NO_ACTIVITY_RESULT_CODE = -100;
        int mServiceErrorCode = 0;
        int mActivityResultCode = NO_ACTIVITY_RESULT_CODE;

        public int getServiceErrorCode() {
            return mServiceErrorCode;
        }

        public int getActivityResultCode() {
            return mActivityResultCode;
        }

        public SignInFailureReason(int serviceErrorCode, int activityResultCode) {
            mServiceErrorCode = serviceErrorCode;
            mActivityResultCode = activityResultCode;
        }

        public SignInFailureReason(int serviceErrorCode) {
            this(serviceErrorCode, NO_ACTIVITY_RESULT_CODE);
        }
    }

    // Not recommended for general use. This method forces the
    // "connect on start" flag
    // to a given state. This may be useful when using GameHelper in a
    // non-standard
    // sign-in flow.
    public void setConnectOnStart(boolean connectOnStart) {
        ConnectOnStart = connectOnStart;
    }
}
