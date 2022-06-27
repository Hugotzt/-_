package com.example.navigationview;

import androidx.appcompat.app.AppCompatActivity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.navigationview.ui.mybase.MybaseFragment;
import com.example.navigationview.ui.pytorch.pytorchFragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    EditText name;
    EditText password;
    private String cname;
    private String cpassword;
    User uu = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        name = findViewById(R.id.name);
        password = findViewById(R.id.password);
        Button reg = (Button) findViewById(R.id.btn_reg);
        Button login = (Button) findViewById(R.id.btn_log);
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reg(v);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cname = name.getText().toString();
                cpassword = password.getText().toString();

                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        DBUtil userDao = new DBUtil();
                        boolean aa = false;
                        try {
                            aa = userDao.login(cname,cpassword);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        int msg = 0;
                        if(aa){
                            msg = 1;
                            try {
                                uu = userDao.findUser(cname);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        hand1.sendEmptyMessage(msg);
                    }
                }).start();
            }
        });

    }
    public void reg(View view){
        startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
    }
    @Override
    public void onResume() {
        super.onResume();
        name.setText("");
        password.setText("");
    }

    final Handler hand1 = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1)
            {
                Toast.makeText(getApplicationContext(),"登录成功",Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),MainActivity.class);
                ArrayList<String> m=new ArrayList<>();
                m.add(uu.getUsername());
                m.add(Integer.toString(uu.getAge()));
                m.add(uu.getPhone());
                intent.putExtra("User",m);
                intent.putExtra("Image",uu.getImg());
                getApplicationContext().startActivity(intent);

            }
            else
            {
                Toast.makeText(getApplicationContext(),"登录失败",Toast.LENGTH_LONG).show();
            }
        }
    };
}
