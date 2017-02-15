package com.badjoras.safetynettest;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.badjoras.safetynettest.base.BaseActivity;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends BaseActivity {

    @BindView(R.id.coordinatorLayout) CoordinatorLayout coordinatorLayout;

    @BindString(R.string.double_back_message) String doubleBackMessage;
    @BindString(R.string.exit) String exitButtonString;

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        addFragment(R.id.fragment_container, HomeFragment.newInstance(), HomeFragment.TAG);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        showUserWarning(doubleBackMessage, exitButtonString, exitClickListener);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    public void showUserWarning(String message, String actionString, View.OnClickListener clickListener){
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

        if(clickListener!=null){
            snackbar.setAction(actionString, clickListener);
        }

        snackbar.show();
    }

}
