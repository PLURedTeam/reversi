package plu.red.reversi.android;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameListener} interface
 * to handle interaction events.
 */
public class SingleplayerFragment extends Fragment implements View.OnClickListener {

    private GameListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_singleplayer, container, false);

        v.findViewById(R.id.button_sp_with_easy_ai).setOnClickListener(this);
        v.findViewById(R.id.button_sp_with_medium_ai).setOnClickListener(this);
        v.findViewById(R.id.button_sp_with_hard_ai).setOnClickListener(this);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GameListener) {
            mListener = (GameListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_sp_with_easy_ai:

                break;
            case R.id.button_sp_with_medium_ai:

                break;

            case R.id.button_sp_with_hard_ai:

                break;
        }
    }
}
