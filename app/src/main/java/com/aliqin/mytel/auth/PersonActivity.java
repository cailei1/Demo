package com.aliqin.mytel.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aliqin.mytel.BuildConfig;
import com.aliqin.mytel.MessageActivity;
import com.aliqin.mytel.R;
import com.mobile.auth.gatewayauth.PhoneNumberAuthHelper;
import com.mobile.auth.gatewayauth.TokenResultListener;

public class PersonActivity extends Activity {
    private static final String TAG = PersonActivity.class.getSimpleName();

    private TextView mTvPhone;
    private Button mVerifyBtn;
    private PhoneNumberAuthHelper mPhoneNumberAuthHelper;
    private boolean sdkAvailable = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        mTvPhone = findViewById(R.id.number_tv);
        mVerifyBtn = findViewById(R.id.verify_btn);
        mVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sdkAvailable) {
                    Intent intent = new Intent(PersonActivity.this, NumberAuthActivity.class);
                    startActivityForResult(intent, 1001);
                } else {
                    Intent intent = new Intent(PersonActivity.this, MessageActivity.class);
                    startActivityForResult(intent, 1001);
                }
                mPhoneNumberAuthHelper.setAuthListener(null);
            }
        });
        sdkInit(BuildConfig.AUTH_SECRET);
    }

    public void sdkInit(String secretInfo) {
        TokenResultListener pCheckListener = new TokenResultListener() {
            @Override
            public void onTokenSuccess(String s) {
                Log.i(TAG, "checkEnvAvailableļ¼" + s);
            }

            @Override
            public void onTokenFailed(String s) {
                sdkAvailable = false;
                Log.e(TAG, "checkEnvAvailableļ¼" + s);
            }
        };
        mPhoneNumberAuthHelper = PhoneNumberAuthHelper.getInstance(this, pCheckListener);
        mPhoneNumberAuthHelper.getReporter().setLoggerEnable(true);
        mPhoneNumberAuthHelper.setAuthSDKInfo(secretInfo);
        mPhoneNumberAuthHelper.checkEnvAvailable(PhoneNumberAuthHelper.SERVICE_TYPE_AUTH);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 2) {
            showRetDialog("ę¬ęŗå·ē ę ”éŖå¤±č“„ļ¼ä½æēØå¶ä»ę ”éŖę¹å¼");
        } else if (resultCode == 1) {
            showRetDialog("ē»å®ęå");
            mVerifyBtn.setVisibility(View.INVISIBLE);
            String phoneNumber = data.getStringExtra("result");
            mTvPhone.setText("ęęŗå·: " + phoneNumber);
        }
        mPhoneNumberAuthHelper.setAuthListener(null);
    }

    private void showRetDialog(final String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("ē”®å®", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
