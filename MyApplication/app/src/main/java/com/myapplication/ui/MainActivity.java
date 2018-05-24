package com.myapplication.ui;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;


import com.myapplication.fragments.GridFragment;
import com.myapplication.R;
import com.myapplication.models.Picture;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class MainActivity extends AppCompatActivity {
    //062a6c0c49e4de1d78497d13a7dbb360

    public static List<Picture> list = new ArrayList<>();
    public static int currentPosition = -1;
    private static final String KEY_CURRENT_POSITION = "currentPosition";
    private String TAG = "MAINACTIVITY";

    private static final int MY_PERMISSIONS_REQUEST = 100;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0);
            // Return here to prevent adding additional GridFragments when changing orientation.
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            askpermission();
        } else {
            renderactivity();
        }
    }

    private void renderactivity() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, new GridFragment(), GridFragment.class.getSimpleName())
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_POSITION, currentPosition);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    renderactivity();
                } else {
                    askpermission();
                    return;
                }

                // other 'case' lines to check for other
                // permissions this app might request
            }
        }
    }

    private void askpermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int perm = checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (perm == PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    requestPermissions(
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST);
                } else {
                    requestPermissions(
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST);
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                }
            } else {
                renderactivity();
            }
        }
    }
}
