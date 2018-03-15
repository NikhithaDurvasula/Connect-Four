package game.nikhitha.connectfour.managers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import game.nikhitha.connectfour.R;
import game.nikhitha.connectfour.gameutils.GameUtils;
import game.nikhitha.connectfour.interfaces.OnGoogleApiChange;

public class MultiPlayer implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MultiPlayer.class.getSimpleName();

    final static int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private Activity mContext;
    private OnGoogleApiChange onGoogleApiChange;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = false;
    private boolean mSignInClicked = false;

    public boolean ismSignInClicked() {
        return mSignInClicked;
    }

    public void setSignInClicked(boolean mSignInClicked) {
        this.mSignInClicked = mSignInClicked;
    }

    public boolean isResolvingConnectionFailure() {
        return mResolvingConnectionFailure;
    }

    public void setResolvingConnectionFailure(boolean mResolvingConnectionFailure) {
        this.mResolvingConnectionFailure = mResolvingConnectionFailure;
    }

    public boolean isAutoStartSignInFlow() {
        return mAutoStartSignInFlow;
    }

    public void setAutoStartSignInFlow(boolean mAutoStartSignInFlow) {
        this.mAutoStartSignInFlow = mAutoStartSignInFlow;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void init(Activity context, OnGoogleApiChange listener) {

        onGoogleApiChange = listener;

        mContext = context;

        // Create the Google Api Client with access to Games
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called. Sign in successful!");
        onGoogleApiChange.onConnectedApi(bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = GameUtils.resolveConnectionFailure(mContext, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, mContext.getString(R.string.signin_other_error));
        }
    }

    public void clear() {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.disconnect();
            }
            mGoogleApiClient = null;
        }
    }

}
