package com.example.moneyu.Activity.Helpers;

import android.view.View;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.moneyu.R;
import com.google.android.material.snackbar.Snackbar;
public class SnackbarHelper {
    public static void showSnackbar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        // Customize the Snackbar
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(view.getContext(), com.google.android.material.R.color.design_default_color_primary)); // Set background color to yellow3

        // Get the TextView of the Snackbar and set text color
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(view.getContext(), com.google.android.material.R.color.design_default_color_on_primary)); // Set text color to green1

        // Set animation mode to fade
        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);

        snackbar.show();
    }
}
