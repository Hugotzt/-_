package com.example.navigationview.ui.mybase;

import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.navigationview.R;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

public class MybaseFragment extends Fragment {


    public static MybaseFragment newInstance() {
        return new MybaseFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_mybase, container, false);

        ImageView avatar = (ImageView)view.findViewById(R.id.iv_avatar);
        TextView username = (TextView)view.findViewById(R.id.tv_user);
        TextView phone = (TextView)view.findViewById(R.id.tv_phone);
        TextView age = (TextView)view.findViewById(R.id.tv_age);
        Button exit = (Button)view.findViewById(R.id.btn_exit);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"退出登录",Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        });

        Properties properties = new Properties();

        try {
            FileInputStream fis = new FileInputStream(new File(getContext().getCacheDir(), "config"));
            properties.load(fis);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        username.setText((CharSequence) properties.get("name"));
        phone.setText((CharSequence) properties.get("phone"));
        age.setText((CharSequence) properties.get("age"));

        byte[] bitmapArray;
        bitmapArray = Base64.decode((String) properties.get("Image"), Base64.DEFAULT);
        avatar.setImageBitmap(BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length));
        return view;
    }
}
