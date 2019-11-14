package com.gatech.update.Controller;

import com.github.omadahealth.lollipin.lib.managers.AppLockActivity;

public class CustomPinActivity extends AppLockActivity {
    @Override
    public void showForgotDialog() {
        //Launch your popup or anything you want here
    }

    @Override
    public void onPinFailure(int attempts) {

    }

    @Override
    public void onPinSuccess(int attempts) {

    }
}