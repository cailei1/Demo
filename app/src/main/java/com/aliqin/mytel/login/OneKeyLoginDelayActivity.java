package com.aliqin.mytel.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliqin.mytel.BuildConfig;
import com.aliqin.mytel.MessageActivity;
import com.aliqin.mytel.R;
import com.aliqin.mytel.config.BaseUIConfig;
import com.aliqin.mytel.uitls.ExecutorManager;
import com.mobile.auth.gatewayauth.AuthRegisterViewConfig;
import com.mobile.auth.gatewayauth.AuthUIConfig;
import com.mobile.auth.gatewayauth.CustomInterface;
import com.mobile.auth.gatewayauth.PhoneNumberAuthHelper;
import com.mobile.auth.gatewayauth.PreLoginResultListener;
import com.mobile.auth.gatewayauth.ResultCode;
import com.mobile.auth.gatewayauth.TokenResultListener;
import com.mobile.auth.gatewayauth.model.TokenRet;

import static com.aliqin.mytel.AppUtils.dp2px;
import static com.aliqin.mytel.Constant.THEME_KEY;
import static com.aliqin.mytel.uitls.MockRequest.getPhoneNumber;

public class OneKeyLoginDelayActivity extends Activity {
    private static final String TAG = OneKeyLoginDelayActivity.class.getSimpleName();

    private TextView mTvResult;
    private Button mLoginBtn;
    private PhoneNumberAuthHelper mPhoneNumberAuthHelper;
    private String token;
    private ProgressDialog mProgressDialog;
    private TokenResultListener mCheckListener;
    private TokenResultListener mTokenResultListener;
    private boolean sdkAvailable = true;
    private int mUIType;
    private BaseUIConfig mUIConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_delay);
        mUIType = getIntent().getIntExtra(THEME_KEY, -1);

        mTvResult = findViewById(R.id.tv_result);
        mTvResult.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mTvResult.setTextIsSelectable(true);
                mTvResult.setSelectAllOnFocus(true);
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // ?????????????????????ClipData
                ClipData mClipData = ClipData.newPlainText("Label", mTvResult.getText());
                // ???ClipData?????????????????????????????????
                cm.setPrimaryClip(mClipData);
                Toast.makeText(OneKeyLoginDelayActivity.this,"??????token?????????",Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        mLoginBtn = findViewById(R.id.btn_login);
        sdkInit(BuildConfig.AUTH_SECRET);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //????????????????????????????????????checkEnvAvailable???accelerateLoginPage????????????????????????????????????????????????????????????
                if (sdkAvailable) {
                    configLoginTokenPort();
                    getLoginToken(5000);
                } else {
                    //???????????????????????? ????????????????????????
                    mPhoneNumberAuthHelper.setAuthListener(null);
                    Intent pIntent = new Intent(OneKeyLoginDelayActivity.this, MessageActivity.class);
                    startActivityForResult(pIntent, 1002);
                }
            }
        });
        mUIConfig = BaseUIConfig.init(mUIType, this, mPhoneNumberAuthHelper);
    }


    public void sdkInit(String secretInfo) {
        mCheckListener = new TokenResultListener() {
            @Override
            public void onTokenSuccess(String s) {
                try {
                    Log.i(TAG, "checkEnvAvailable???" + s);
                    TokenRet pTokenRet = TokenRet.fromJson(s);
                    if (ResultCode.CODE_ERROR_ENV_CHECK_SUCCESS.equals(pTokenRet.getCode())) {
                        accelerateLoginPage(5000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTokenFailed(String s) {
                sdkAvailable = false;
                Log.e(TAG, "checkEnvAvailable???" + s);
                //?????????????????????????????? ?????????????????????????????????
            }
        };
        mPhoneNumberAuthHelper = PhoneNumberAuthHelper.getInstance(this, mCheckListener);
        mPhoneNumberAuthHelper.getReporter().setLoggerEnable(true);
        mPhoneNumberAuthHelper.setAuthSDKInfo(secretInfo);
        mPhoneNumberAuthHelper.checkEnvAvailable(PhoneNumberAuthHelper.SERVICE_TYPE_LOGIN);
    }


    /**
     * ???????????????app???????????????????????? ????????????????????? ??????????????????????????????
     * ????????????????????????????????? ?????????????????????
     * ????????????????????????????????????????????????????????????????????????????????????????????????
     * @param timeout
     */
    public void accelerateLoginPage(int timeout) {
        mPhoneNumberAuthHelper.accelerateLoginPage(timeout, new PreLoginResultListener() {
            @Override
            public void onTokenSuccess(String s) {
                Log.e(TAG, "???????????????: " + s);
            }

            @Override
            public void onTokenFailed(String s, String s1) {
                Log.e(TAG, "??????????????????" + ", " + s1);
            }
        });
    }

    /**
     * ???????????????
     * @param timeout ????????????
     */
    public void getLoginToken(int timeout) {
        mUIConfig.configAuthPage();
        mTokenResultListener = new TokenResultListener() {
            @Override
            public void onTokenSuccess(String s) {
                hideLoadingDialog();
                TokenRet tokenRet = null;
                try {
                    tokenRet = TokenRet.fromJson(s);
                    if (ResultCode.CODE_START_AUTHPAGE_SUCCESS.equals(tokenRet.getCode())) {
                        Log.i(TAG, "????????????????????????" + s);
                    }

                    if (ResultCode.CODE_SUCCESS.equals(tokenRet.getCode())) {
                        Log.i(TAG, "??????token?????????" + s);
                        token = tokenRet.getToken();
                        getResultWithToken(token);
                        mPhoneNumberAuthHelper.setAuthListener(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onTokenFailed(String s) {
                Log.e(TAG, "??????token?????????" + s);
                hideLoadingDialog();
                mPhoneNumberAuthHelper.hideLoginLoading();
                //???????????????????????? ????????????????????????
                TokenRet tokenRet = null;
                try {
                    tokenRet = TokenRet.fromJson(s);
                    if (!ResultCode.CODE_ERROR_USER_CANCEL.equals(tokenRet.getCode())) {
                        Toast.makeText(getApplicationContext(), "?????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                        Intent pIntent = new Intent(OneKeyLoginDelayActivity.this, MessageActivity.class);
                        startActivityForResult(pIntent, 1002);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mPhoneNumberAuthHelper.quitLoginPage();
                mPhoneNumberAuthHelper.setAuthListener(null);
            }
        };
        mPhoneNumberAuthHelper.setAuthListener(mTokenResultListener);
        mPhoneNumberAuthHelper.getLoginToken(this, timeout);
        showLoadingDialog("?????????????????????");
    }



    /**
     * ??????????????????
     */
    private void configLoginTokenPort() {
        mPhoneNumberAuthHelper.removeAuthRegisterXmlConfig();
        mPhoneNumberAuthHelper.removeAuthRegisterViewConfig();
        mPhoneNumberAuthHelper.addAuthRegistViewConfig("switch_acc_tv", new AuthRegisterViewConfig.Builder()
                .setView(initDynamicView())
                .setRootViewId(AuthRegisterViewConfig.RootViewId.ROOT_VIEW_ID_BODY)
                .setCustomInterface(new CustomInterface() {
                    @Override
                    public void onClick(Context context) {
                        mPhoneNumberAuthHelper.quitLoginPage();
                    }
                }).build());
        int authPageOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
        if (Build.VERSION.SDK_INT == 26) {
            authPageOrientation = ActivityInfo.SCREEN_ORIENTATION_BEHIND;
        }
        mPhoneNumberAuthHelper.setAuthUIConfig(new AuthUIConfig.Builder()
                .setAppPrivacyOne("???????????????????????????", "https://www.baidu.com")
                .setAppPrivacyColor(Color.GRAY, Color.parseColor("#002E00"))
                .setPrivacyState(false)
                .setCheckboxHidden(true)
                .setStatusBarColor(Color.TRANSPARENT)
                .setStatusBarUIFlag(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                .setLightColor(true)
                .setAuthPageActIn("in_activity", "out_activity")
                .setAuthPageActOut("in_activity", "out_activity")
                .setProtocolShakePath("protocol_shake")
                .setVendorPrivacyPrefix("???")
                .setVendorPrivacySuffix("???")
                .setLogoImgPath("mytel_app_launcher")
                .setScreenOrientation(authPageOrientation)
                .create());
    }

    private View initDynamicView() {
        TextView switchTV = new TextView(this);
        RelativeLayout.LayoutParams mLayoutParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, dp2px(this, 50));
        mLayoutParams2.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        mLayoutParams2.setMargins(0, dp2px(this, 450), 0, 0);
        switchTV.setText("-----  ?????????view  -----");
        switchTV.setTextColor(0xff999999);
        switchTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.0F);
        switchTV.setLayoutParams(mLayoutParams2);
        return switchTV;
    }


    public void showLoadingDialog(String hint) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mProgressDialog.setMessage(hint);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
    }

    public void hideLoadingDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    public void getResultWithToken(final String token) {
        ExecutorManager.run(new Runnable() {
            @Override
            public void run() {
                final String result = getPhoneNumber(token);
                OneKeyLoginDelayActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvResult.setText("???????????????" + result);
                        mTvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
                        mLoginBtn.setVisibility(View.GONE);
                        mPhoneNumberAuthHelper.quitLoginPage();
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1002) {
            if (resultCode == 1) {
                mTvResult.setText("???????????????" + data.getStringExtra("result"));
                mTvResult.setMovementMethod(ScrollingMovementMethod.getInstance());
                mLoginBtn.setVisibility(View.GONE);
            }
        }

    }
}
