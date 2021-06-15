package com.happyplus.minuslauncher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.happyplus.minuslauncher.utils.Light;

import java.security.Policy;

public class MainActivity extends AppCompatActivity {

    private Light lightFlash;
    private static int PICK_CONTACT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 权限
        verifyReadContactsPermissions(this);
        // 打电话
        TextView call = findViewById(R.id.call);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);
            }
        });
        // 打开微信
        TextView wechat = findViewById(R.id.wechat);
        wechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "未安装微信", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 手电筒
        TextView light = findViewById(R.id.light);
        lightFlash = new Light(this);
        light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLight(light);
            }
        });
        // 刷抖音
        TextView tiktok = findViewById(R.id.tiktok);
        tiktok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.ss.android.ugc.aweme");
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "未安装抖音", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private static boolean hasClosed = false;

    public void toggleLight(TextView view) {
        if (hasClosed) {
            lightFlash.open();
            view.setText("关灯");
            hasClosed = false;
        } else {
            lightFlash.close();
            view.setText("开灯");
            hasClosed = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT) {
            if (data == null) {
                return;
            }
            Uri contactData = data.getData();
            Cursor c = managedQuery(contactData, null, null, null, null);
            if (c.moveToFirst()) {
                String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                String phoneNumber = null;
                if (hasPhone.equalsIgnoreCase("1")) {
                    hasPhone = "true";
                } else {
                    hasPhone = "false";
                }
                if (Boolean.parseBoolean(hasPhone)) {
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
                                    + contactId,
                            null,
                            null);
                    while (phones.moveToNext()) {
                        phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    callPhone(phoneNumber);
                    phones.close();
                }
            }
        }
    }

    /**
     * 拨打电话（直接拨打电话）
     *
     * @param phoneNum 电话号码
     */
    public void callPhone(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }

    /*
     * android 动态权限申请
     * */
    private static final int REQUEST_READ_CONTACTS = 1;
    private static String[] READ_CONTACTS = {
            "android.permission.READ_CONTACTS",
            "android.permission.CALL_PHONE"};

    public static void verifyReadContactsPermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, READ_CONTACTS, REQUEST_READ_CONTACTS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}