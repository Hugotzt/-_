package com.example.navigationview.ui.atlas;

import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.navigationview.DBUtil;
import com.example.navigationview.R;
import com.example.navigationview.ui.pytorch.ImageNetClasses;
import com.linchaolong.android.imagepicker.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

public class InputFragment extends Fragment {
    // 自动完成文本框
    AutoCompleteTextView cname;
    String[] cnames = ImageNetClasses.IMAGENET_CLASSES;
    private ImagePicker imagePicker = new ImagePicker();
    byte[] photobytes={};
    ImageView picture;
    EditText ename;
    EditText introduce;

    static final int EXIT_DIALOG_ID = 0;

    public static InputFragment newInstance() {
        return new InputFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_input, container, false);
        // 中文名
        cname = (AutoCompleteTextView) view.findViewById(R.id.cname);
        ArrayAdapter<String> adapterauto = new ArrayAdapter<String>(getActivity(), R.layout.list_item, cnames);
        cname.setAdapter(adapterauto);
        cname.setThreshold(1);
        // 英文名
        ename = (EditText) view.findViewById(R.id.ename);
        // 选择图片
        picture = (ImageView) view.findViewById(R.id.photo);
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraOrGallery();
            }
        });
        // 介绍
        introduce = (EditText) view.findViewById(R.id.add);
        // 保存按键
        Button save= (Button) view.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // getdata
                updatedata();
            }
        });
        // 清除按钮
        Button clearButton = (Button) view.findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ename.setText("");
                cname.setText("");
                introduce.setText("");
            }
        });
        return view;
    }
    // 更新数据库（增加）
    private void updatedata(){
        String ctxtname = cname.getText().toString().trim();
        String etxtname = ename.getText().toString().trim();
        String txtintroduce = introduce.getText().toString().trim();
        final HashMap map=new HashMap<String,Object>();
        map.put("cname",ctxtname);
        map.put("ename",etxtname);
        map.put("picture",photobytes);
        map.put("introduce",txtintroduce);
        // 开启线程 增加内容
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBUtil.insert("atlas",map);
            }
        }).start();
        // 返回主页面
        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.nav_home);
        Toast.makeText(getActivity(), "添加数据成功！", Toast.LENGTH_LONG).show();
    }

    // 上传图片
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePicker.onActivityResult(this, requestCode, resultCode, data);
    }
    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
    private void startCameraOrGallery() {
        new AlertDialog.Builder(getActivity()).setTitle("设置图片")
                .setItems(new String[] { "从相册中选取图片", "拍照" }, new DialogInterface.OnClickListener() {

                    @Override public void onClick(DialogInterface dialog, int which) {
                        // 回调
                        ImagePicker.Callback callback = new ImagePicker.Callback() {
                            @Override public void onPickImage(Uri imageUri) {
                            }
                            @Override public void onCropImage(Uri imageUri) {
                                //picture.setImageURI(imageUri);
                                Glide.with(getActivity()).load(new File(imageUri.getPath())).into(picture);
                                Glide.with(getActivity()).load(new File(imageUri.getPath())).asBitmap().into(new SimpleTarget<Bitmap>(100, 100) {
                                    @Override
                                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                                        //savedb
                                        photobytes = stream.toByteArray();
                                    }
                                });
                            }
                        };

                        if (which == 0) {
                            // 从相册中选取图片
                            imagePicker.startGallery(InputFragment.this, callback);
                        } else {
                            // 拍照
                            imagePicker.startCamera(InputFragment.this, callback);
                        }
                    }

                })
                .show()
                .getWindow()
                .setGravity(Gravity.BOTTOM);
    }

}
