package com.aliqin.mytel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class MessageActivity extends Activity {
    private Button mAuthButton;
    private EditText mNumberEt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        mAuthButton = findViewById(R.id.auth_btn);
        mNumberEt = findViewById(R.id.et_number);
        mAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = mNumberEt.getText().toString();
                //判断手机号是否合法
                if (!TextUtils.isEmpty(phoneNumber)) {
                    JSONObject pJSONObject = new JSONObject();
                    try {
                        pJSONObject.put("account", UUID.randomUUID().toString());
                        pJSONObject.put("phoneNumber", "***********");
                    }catch (JSONException e){

                    }
                    Intent pIntent = new Intent();
                    pIntent.putExtra("result", pJSONObject.toString());
                    setResult(1, pIntent);
                    finish();
                }
            }
        });
    }
}
