package com.bjennings.spotifyalarm;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.JsonReader;
import android.util.JsonToken;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;


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

        SearchView searchBox = (SearchView)getView().findViewById(R.id.search_box);
        searchBox.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                String url = "https://api.spotify.com/v1/search?q=" + encode(query) + "&type=track&market=us&limit=5";

                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpget = new HttpGet(url);

                try {
                    CloseableHttpResponse response = httpclient.execute(httpget);



                    response.close();
                } catch(Exception e) {}

                return false;
            }

            private String encode(String query) {
                return query.replace(" ", "%20");
            }
        });

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

    public List readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readMessagesArray(reader);
        } finally {
                reader.close();
            }
        }

    public List readMessagesArray(JsonReader reader) throws IOException {
        List messages = new ArrayList();

        reader.beginArray();
        while (reader.hasNext()) {
            messages.add(readMessage(reader));
        }
        reader.endArray();
        return messages;
    }

    public String readMessage(JsonReader reader) throws IOException {
        long id = -1;
        String text = null;
        List geo = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id")) {

            } else if (name.equals("text")) {

            } else if (name.equals("geo") && reader.peek() != JsonToken.NULL) {

            }else if (name.equals("user")) {

            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return "";
    }
}
