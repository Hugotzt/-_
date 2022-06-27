package com.example.navigationview.ui.atlas;

import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Message;
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
import com.example.navigationview.ui.recyclerview.ChangeFragment;
import com.linchaolong.android.imagepicker.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CgatlasFragment extends Fragment {
    AutoCompleteTextView cname;
    EditText ename;
    EditText introduce;
    String[] cnames = ImageNetClasses.IMAGENET_CLASSES;
    private ImagePicker imagePicker = new ImagePicker();
    ImageView picture;
    byte[] photobytes;
    Handler handler;
    String id;


    public static CgatlasFragment newInstance() {
        return new CgatlasFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_cgatlas, container, false);

        id=getArguments().getString("id");
        cname = (AutoCompleteTextView) view.findViewById(R.id.cname);
        ArrayAdapter<String> adapterauto = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item, cnames);
        cname.setAdapter(adapterauto);
        cname.setThreshold(1);
        introduce = (EditText) view.findViewById(R.id.add);
        ename = (EditText) view.findViewById(R.id.ename);
        Button clearButton = (Button) view.findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Object[] values={id};
                        DBUtil.delete("atlas","uid=?",values);
                    }
                }).start();
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.nav_home);
                Toast.makeText(getActivity(), "删除数据成功！", Toast.LENGTH_LONG).show();
            }
        });

        picture = (ImageView) view.findViewById(R.id.photo);
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraOrGallery();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                try {
                    list=getData();
                    Message message=handler.obtainMessage();
                    message.obj=list;
                    handler.sendMessage(message);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        handler=new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                List<Map<String, Object>> mData = (List<Map<String, Object>>) msg.obj;
                cname.setText(mData.get(0).get("cname").toString());
                ename.setText(mData.get(0).get("ename").toString());
                introduce.setText(mData.get(0).get("introduce").toString());
                if(mData.get(0).get("picture") instanceof Bitmap)
                    picture.setImageBitmap((Bitmap) mData.get(0).get("picture"));
                else
                    picture.setBackgroundResource((Integer)mData.get(0).get("picture"));
            }

        };

        Button save= (Button) view.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ctxtname=cname.getText().toString().trim();
                String etxtname=ename.getText().toString().trim();
                String txtintroduce=introduce.getText().toString().trim();
                final HashMap map=new HashMap<String,Object>();
                map.put("cname",ctxtname);
                map.put("ename",etxtname);
                map.put("picture",photobytes);
                map.put("introduce",txtintroduce);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Object[] values={id};
                        DBUtil.update("atlas",map,"uid=?",values);
                    }
                }).start();
                NavController navController= Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
                navController.navigate(R.id.nav_home);
                Toast.makeText(getActivity(), "更改数据成功！", Toast.LENGTH_LONG).show();
            }
        });

        return view;

    }

    List<Map<String, Object>> getData() throws SQLException {
        Object[] values={id};
        ResultSet rs= DBUtil.rawQuery("select * from atlas where uid=?",values);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        while (rs.next())
        {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("cname", rs.getString("cname"));
            map.put("ename",rs.getString("ename"));
            map.put("introduce",rs.getString("introduce"));
            photobytes = rs.getBytes("picture");

            if (photobytes != null && photobytes.length > 0) {
                Bitmap image = BitmapFactory.decodeByteArray(photobytes, 0, photobytes.length);
                map.put("picture", image);
            } else {
                map.put("picture", R.mipmap.ic_launcher);
            }
            list.add(map);
        }
        return list;
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePicker.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private void startCameraOrGallery() {
        new AlertDialog.Builder(getActivity()).setTitle("设置图片")
                .setItems(new String[] { "从相册中选取图片", "拍照" }, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        ImagePicker.Callback callback = new ImagePicker.Callback() {
                            @Override public void onPickImage(Uri imageUri) {
                            }
                            @Override public void onCropImage(Uri imageUri) {
                                Glide.with(getActivity()).load(new File(imageUri.getPath())).into(picture);
                                Glide.with(getActivity()).load(new File(imageUri.getPath())).asBitmap().into(new SimpleTarget<Bitmap>(100, 100) {
                                    @Override
                                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                                        photobytes = stream.toByteArray();
                                    }
                                });
                            }
                        };
                        if (which == 0) {
                            // 从相册中选取图片
                            imagePicker.startGallery(CgatlasFragment.this, callback);
                        } else {
                            // 拍照
                            imagePicker.startCamera(CgatlasFragment.this, callback);
                        }
                    }
                })
                .show()
                .getWindow()
                .setGravity(Gravity.BOTTOM);
    }
}
