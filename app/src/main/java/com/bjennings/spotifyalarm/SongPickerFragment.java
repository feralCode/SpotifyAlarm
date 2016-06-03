package com.bjennings.spotifyalarm;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SongPickerFragment extends Fragment {

    private SongPickerListener listener;
    private Fragment thisFragment = this;
    public SongPickerFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song_picker, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof SongPickerListener) {
            listener = (SongPickerListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SongPickerListener");
        }

        View me = getView();
        assert me != null;
        Button submitBtn = (Button)me.findViewById(R.id.save_btn);
        Button cancelBtn = (Button)me.findViewById(R.id.cancel_btn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                listener.onSelectSong(args);
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancel(thisFragment);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface SongPickerListener {
        void onSelectSong(Bundle args);
        void onCancel(Fragment frag);
    }
}
