package com.example.navigationview.ui.recyclerview;

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
import android.widget.DatePicker;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeFragment extends Fragment {
    AutoCompleteTextView label;
    String[] labels = ImageNetClasses.IMAGENET_CLASSES;
    EditText publishtime;
    private int mYear;
    private int mMonth;
    private int mDay;
    private ImagePicker imagePicker = new ImagePicker();
    ImageView picture;
    byte[] photobytes;
    EditText place;
    Handler handler;
    String id;

    public static ChangeFragment newInstance() {
        return new ChangeFragment();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_change, container, false);
        id=getArguments().getString("id");
        // 自动完成文本框
        label = (AutoCompleteTextView) view.findViewById(R.id.label);
        ArrayAdapter<String> adapterauto = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item, labels);
        label.setAdapter(adapterauto);
        // 删除按钮
        Button clearButton = (Button) view.findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Object[] values={id};
                        DBUtil.delete("insect","id=?",values);
                    }
                }).start();
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.nav_home);
                Toast.makeText(getActivity(), "删除数据成功！", Toast.LENGTH_LONG).show();
            }
        });

        // 出版日期
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        publishtime = (EditText) view.findViewById(R.id.publishertime);

        publishtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onCreateDialog();
            }
        });
        picture = (ImageView) view.findViewById(R.id.photo);
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraOrGallery();
            }
        });
        place = (EditText) view.findViewById(R.id.place);
        //显示数据
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

        //显示数据
        handler=new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                List<Map<String, Object>> mData = (List<Map<String, Object>>) msg.obj;
                place.setText(mData.get(0).get("place").toString());
                label.setText(mData.get(0).get("label").toString());
                publishtime.setText(mData.get(0).get("publishertime").toString());
                if(mData.get(0).get("picture") instanceof Bitmap)
                    picture.setImageBitmap((Bitmap) mData.get(0).get("picture"));
                else
                    picture.setBackgroundResource((Integer)mData.get(0).get("picture"));
            }

        };

        // 更新
        Button save= (Button) view.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtIsbn=place.getText().toString().trim();
                String txtBookName=label.getText().toString().trim();
                String txtPublishTime=publishtime.getText().toString().trim();
                final HashMap map=new HashMap<String,Object>();
                map.put("place",txtIsbn);
                map.put("label",txtBookName);
                map.put("publishertime",txtPublishTime);
                map.put("picture",photobytes);
                // 开启线程 存储内容
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Object[] values={id};
                        DBUtil.update("insect",map,"id=?",values);
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
        ResultSet rs= DBUtil.rawQuery("select * from insect where id=?",values);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        while (rs.next())
        {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("place", rs.getString("place"));
            map.put("publishertime",rs.getString("publishertime"));
            map.put("label",rs.getString("label"));
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

    // 对话框创建
    protected Dialog onCreateDialog() {
        new DatePickerDialog(getActivity(), mDateSetListener, mYear, mMonth, mDay).show();
        return null;
    }

    // 出版日期
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDisplay();
        }
    };

    // 出版日期
    private void updateDisplay() {
        publishtime.setText(new StringBuilder()
                // Month is 0 based so add 1
                .append(mYear).append("-").append(mMonth + 1).append("-")
                .append(mDay));
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
                            imagePicker.startGallery(ChangeFragment.this, callback);
                        } else {
                            // 拍照
                            imagePicker.startCamera(ChangeFragment.this, callback);
                        }
                    }
                })
                .show()
                .getWindow()
                .setGravity(Gravity.BOTTOM);
    }
}
