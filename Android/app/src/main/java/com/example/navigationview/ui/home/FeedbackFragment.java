package com.example.navigationview.ui.home;


import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


import com.example.navigationview.ui.home.MqttManager;

import com.example.navigationview.R;
import com.lichfaker.log.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FeedbackFragment extends Fragment {

    public static final String URL = "tcp://192.168.44.1:1883";
    private String userName = "000";
    private String password = "000";
    private String clientId = "android_tzt";

    EditText theme;
    EditText sug;
    EditText phone;
    Button send;
    public static FeedbackFragment newInstance() {
        return new FeedbackFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        theme = (EditText) view.findViewById(R.id.host);
        sug = (EditText) view.findViewById(R.id.sug);
        phone = (EditText) view.findViewById(R.id.phone);
        send = (Button) view.findViewById(R.id.button);

        boolean b = MqttManager.getInstance().creatConnect(URL, userName, password, clientId);
        Logger.d("isConnected: " + b);

        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date curDate =  new Date(System.currentTimeMillis());
                final String  txtpublishertime  =  formatter.format(curDate);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String message = "{\"theme\":\"" +
                                theme.getText().toString() + "\",\"sug\":\"" +
                                sug.getText().toString() + "\",\"phone\":\""+
                                phone.getText().toString() + "\",\"time\":\"" +
                                txtpublishertime + "\"}";
                        MqttManager.getInstance().publish("/mqtt", 0, message.getBytes());
                    }
                }).start();
            }
        });


        return view;


    }



}
