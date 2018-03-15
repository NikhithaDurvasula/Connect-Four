package game.nikhitha.connectfour;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import game.nikhitha.connectfour.interfaces.OnFragmentInteraction;

public class NewGameBoard extends Fragment {

    private static final String TAG = NewGameBoard.class.getSimpleName();

    private OnFragmentInteraction mListener;

    @BindView(R.id.button_continue) Button mButtonContinue;

    @OnClick({ R.id.newGameScreen,
            R.id.vsPlayer,
            R.id.vsAdvanced,
            R.id.vsHard,
            R.id.button_multiplayer,
            R.id.button_continue })
    public void startGame(View view) {
        mListener.onFragmentClick(view.getId());
    }

    private Unbinder mUnbinder;

    public NewGameBoard() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment NewGameBoard.
     */
    public static NewGameBoard newInstance() {
        return new NewGameBoard();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.newgameboard, container, false);

        mUnbinder = ButterKnife.bind(this, rootView);

        mButtonContinue.setVisibility(MainActivity.isContinueVisible ? View.VISIBLE : View.GONE);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteraction) {
            mListener = (OnFragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNewGameFragmentInteraction");
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

}
