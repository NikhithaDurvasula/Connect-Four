package game.nikhitha.connectfour;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.SignInButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import game.nikhitha.connectfour.interfaces.OnFragmentInteraction;

public class MultiplayerBoard extends Fragment {

    private static final String TAG = MultiplayerBoard.class.getSimpleName();

    @BindView(R.id.sign_in_button)
    SignInButton mSignInButton;
    @BindView(R.id.sign_out_button) Button mSignOutButton;
    @BindView(R.id.button_invitation) Button mInviteButton;

    @OnClick({R.id.sign_in_button,
            R.id.sign_out_button,
            R.id.button_invitation })
    public void onClick(View view) {
        mListener.onFragmentClick(view.getId());
    }

    private Unbinder mUnbinder;

    private OnFragmentInteraction mListener;

    public MultiplayerBoard() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment MultiplayerBoard.
     */
    public static MultiplayerBoard newInstance() {
        return new MultiplayerBoard();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.multiplayerboard, container, false);

        mUnbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (((MainActivity) getActivity()).mGoogleApiClient.isConnected() ||
                ((MainActivity) getActivity()).mGoogleApiClient.isConnecting()) {
            showUi(true);
        } else {
            showUi(false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteraction) {
            mListener = (OnFragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onMultiplayerFragmentInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    public void showUi(boolean connected) {
        if (this.isVisible()) {
            if (connected) {
                mInviteButton.setVisibility(View.VISIBLE);
                mSignInButton.setVisibility(View.GONE);
                mSignOutButton.setVisibility(View.VISIBLE);
            } else {
                mInviteButton.setVisibility(View.GONE);
                mSignOutButton.setVisibility(View.GONE);
                mSignInButton.setVisibility(View.VISIBLE);
            }
        }
    }
}
