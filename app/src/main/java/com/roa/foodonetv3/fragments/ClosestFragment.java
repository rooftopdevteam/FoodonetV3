package com.roa.foodonetv3.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.roa.foodonetv3.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ClosestFragment extends Fragment {


    public ClosestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_closest, container, false);
    }

}
