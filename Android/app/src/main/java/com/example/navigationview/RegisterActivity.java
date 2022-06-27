package com.example.navigationview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.linchaolong.android.imagepicker.ImagePicker;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.SQLException;

public class RegisterActivity extends AppCompatActivity {
    byte[] imgbeys;

    private ImagePicker imagePicker = new ImagePicker();

    EditText name = null;
    EditText username = null;
    EditText password = null;
    EditText phone = null;
    EditText age = null;
    ImageView img = null;
    User uu = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        phone = findViewById(R.id.phone);
        age = findViewById(R.id.age);

        img = (ImageView)findViewById(R.id.img);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraOrGallery();
            }
        });

        Button register = (Button) findViewById(R.id.button2);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(v);
            }
        });
        Button clear = (Button) findViewById(R.id.button3);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name.setText("");
                username.setText("");
                password.setText("");
                phone.setText("");
                age.setText("");
            }
        });
    }

    // 注册
    public void register(View view){

        String cname = name.getText().toString();
        String cusername = username.getText().toString();
        String cpassword = password.getText().toString();
        String cphone = phone.getText().toString();
        int cgae = Integer.parseInt(age.getText().toString());


        if(cname.length() < 2 || cusername.length() < 2 || cpassword.length() < 2 ){
            Toast.makeText(getApplicationContext(),"输入信息不符合要求请重新输入",Toast.LENGTH_LONG).show();
            return;

        }
        // 创建数据库对象
        final User user = new User();
        user.setName(cname);
        user.setUsername(cusername);
        user.setPassword(cpassword);
        user.setAge(cgae);
        user.setPhone(cphone);
        user.setImg(imgbeys);


        new Thread(){
            @Override
            public void run() {

                int msg = 0;
                DBUtil userDao = new DBUtil();

                try {
                    uu = userDao.findUser(user.getName());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if(uu != null){
                    msg = 1;
                }

                boolean flag = false;

                try {
                    flag = userDao.register(user);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if(flag){
                    msg = 2;
                }
                hand.sendEmptyMessage(msg);

            }
        }.start();
    }

    final Handler hand = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0)
            {
                Toast.makeText(getApplicationContext(),"注册失败",Toast.LENGTH_SHORT).show();
            }
            if(msg.what == 1)
            {
                Toast.makeText(getApplicationContext(),"该账号已经存在，请换一个账号",Toast.LENGTH_SHORT).show();
            }
            if(msg.what == 2)
            {
                Intent intent = new Intent();
                //将想要传递的数据用putExtra封装在intent中
                intent.putExtra("a","注册");
                setResult(RESULT_CANCELED,intent);
                Toast.makeText(getApplicationContext(),"注册成功",Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    };
    //picture
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePicker.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
    // 打开文件或者相机
    private void startCameraOrGallery() {
        new AlertDialog.Builder(RegisterActivity.this).setTitle("设置图片")
                .setItems(new String[] { "从相册中选取图片", "拍照" }, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        // 回调
                        ImagePicker.Callback callback = new ImagePicker.Callback() {
                            @Override public void onPickImage(Uri imageUri) {
                            }

                            @Override public void onCropImage(final Uri imageUri) {
                                Glide.with(RegisterActivity.this).load(new File(imageUri.getPath())).into(img);
                                Glide.with(RegisterActivity.this).load(new File(imageUri.getPath())).asBitmap().into(new SimpleTarget<Bitmap>(100, 100) {
                                    @Override
                                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                                        //savedb
                                        imgbeys = stream.toByteArray();

                                    }
                                });
                            }
                        };
                        if (which == 0) {
                            // 从相册中选取图片
                            imagePicker.startGallery(RegisterActivity.this, callback);
                        } else {
                            // 拍照
                            imagePicker.startCamera(RegisterActivity.this, callback);
                        }
                    }
                })
                .show()
                .getWindow()
                .setGravity(Gravity.BOTTOM);
    }

}
