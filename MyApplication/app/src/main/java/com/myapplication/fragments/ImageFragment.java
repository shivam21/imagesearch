/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.myapplication.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.myapplication.utils.ImageLoader;
import com.myapplication.R;
import com.myapplication.utils.ImageLoader2;


/**
 * A fragment for displaying an image.
 */
public class ImageFragment extends Fragment {

    private static final String KEY_IMAGE_RES = "com.google.samples.gridtopager.key.imageRes";

    public static ImageFragment newInstance(String url) {
        ImageFragment fragment = new ImageFragment();
        Bundle argument = new Bundle();
        argument.putString(KEY_IMAGE_RES, url);
        fragment.setArguments(argument);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_image, container, false);

        Bundle arguments = getArguments();
        String url = arguments.getString(KEY_IMAGE_RES);

        // Just like we do when binding views at the grid, we set the transition name to be the string
        // value of the image res.
        view.findViewById(R.id.image).setTransitionName(String.valueOf(url));
        ImageLoader2 imageLoader = new ImageLoader2(getActivity());
        Log.d("LETSSEE", "onCreateView: "+url);
        imageLoader.DisplayImage(url, (ImageView) view.findViewById(R.id.image));
        getParentFragment().startPostponedEnterTransition();
        return view;
    }
}
