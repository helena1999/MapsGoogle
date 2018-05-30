package com.example.opilane.mapsgoogle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by opilane on 5/30/2018.
 */

public class AsukohaInfoAknaAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private Context mContext;

    public AsukohaInfoAknaAdapter (Context context){
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.asukoha_info, null);
    }

    private void aknaText(Marker marker, View view){
        String title = marker.getTitle();
        TextView txtTitle = view.findViewById(R.id.title_view);
        if (!title.equals("")){
            txtTitle.setText(title);
        }
        String snippet = marker.getSnippet();
        TextView txtSnippet = view.findViewById(R.id.snippet);
        if (!snippet.equals("")){
            txtSnippet.setText(snippet);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        aknaText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        aknaText(marker, mWindow);
        return mWindow;
    }
}
