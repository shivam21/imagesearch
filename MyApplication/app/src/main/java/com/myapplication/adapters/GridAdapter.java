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

package com.myapplication.adapters;



import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.myapplication.utils.ImageLoader;
import com.myapplication.fragments.ImagePagerFragment;
import com.myapplication.ui.MainActivity;
import com.myapplication.models.Picture;
import com.myapplication.R;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A fragment for displaying a grid of images.
 */
public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ImageViewHolder> {
    ImageLoader imageLoader;
    List<Picture> data;

    /**
     * A listener that is attached to all ViewHolders to handle image loading events and clicks.
     */
    private interface ViewHolderListener {

        void onLoadCompleted(ImageView view, int adapterPosition);

        void onItemClicked(View view, int adapterPosition);
    }

    private final RequestManager requestManager;
    private final ViewHolderListener viewHolderListener;

    /**
     * Constructs a new grid adapter for the given {@link Fragment}.
     */
    public GridAdapter(Fragment fragment, List<Picture> list) {
        data = list;
        imageLoader = new ImageLoader(fragment.getContext());
        this.requestManager = Glide.with(fragment);
        this.viewHolderListener = new ViewHolderListenerImpl(fragment);
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_card, parent, false);
        return new ImageViewHolder(view, requestManager, viewHolderListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        holder.onBind();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    /**
     * Default {@link ViewHolderListener} implementation.
     */
    private static class ViewHolderListenerImpl implements ViewHolderListener {

        private Fragment fragment;
        private AtomicBoolean enterTransitionStarted;

        ViewHolderListenerImpl(Fragment fragment) {
            this.fragment = fragment;
            this.enterTransitionStarted = new AtomicBoolean();
        }

        @Override
        public void onLoadCompleted(ImageView view, int position) {
            // Call startPostponedEnterTransition only when the 'selected' image loading is completed.
            if (MainActivity.currentPosition != position) {
                return;
            }
            if (enterTransitionStarted.getAndSet(true)) {
                return;
            }
            fragment.startPostponedEnterTransition();
        }

        /**
         * Handles a view click by setting the current position to the given {@code position} and
         * starting a {@link  ImagePagerFragment} which displays the image at the position.
         *
         * @param view     the clicked {@link ImageView} (the shared element view will be re-mapped at the
         *                 GridFragment's SharedElementCallback)
         * @param position the selected view position
         */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onItemClicked(View view, int position) {
            // Update the position.
            MainActivity.currentPosition = position;

            // Exclude the clicked card from the exit transition (e.g. the card will disappear immediately
            // instead of fading out with the rest to prevent an overlapping animation of fade and move).
            ((TransitionSet) fragment.getExitTransition()).excludeTarget(view, true);

            ImageView transitioningView = view.findViewById(R.id.card_image);
            fragment.getFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true) // Optimize for shared element transition
                    .addSharedElement(transitioningView, transitioningView.getTransitionName())
                    .replace(R.id.fragment_container, new ImagePagerFragment(), ImagePagerFragment.class
                            .getSimpleName())
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * ViewHolder for the grid's images.
     */
    class ImageViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        private final ImageView image;
        private final RequestManager requestManager;
        private final ViewHolderListener viewHolderListener;

        ImageViewHolder(View itemView, RequestManager requestManager,
                        ViewHolderListener viewHolderListener) {
            super(itemView);
            this.image = itemView.findViewById(R.id.card_image);
            this.requestManager = requestManager;
            this.viewHolderListener = viewHolderListener;
            itemView.findViewById(R.id.card_view).setOnClickListener(this);
        }

        /**
         * Binds this view holder to the given adapter position.
         * <p>
         * The binding will load the image into the image view, as well as set its transition name for
         * later.
         */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        void onBind() {
            int adapterPosition = getAdapterPosition();
            setImage(adapterPosition);
            // Set the string value of the image resource as the unique transition name for the view.
            image.setTransitionName(data.get(adapterPosition).getUrl());
        }

        void setImage(final int adapterPosition) {
            imageLoader.DisplayImage(data.get(adapterPosition).getUrl(), image);
            viewHolderListener.onLoadCompleted(image, adapterPosition);
            }

        @Override
        public void onClick(View view) {
            // Let the listener start the ImagePagerFragment.
            viewHolderListener.onItemClicked(view, getAdapterPosition());
        }
    }

}