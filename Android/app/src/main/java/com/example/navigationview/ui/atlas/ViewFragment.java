package com.example.navigationview.ui.atlas;

import androidx.lifecycle.ViewModelProviders;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.navigationview.DBUtil;
import com.example.navigationview.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewFragment extends Fragment {

    ImageView picture;
    TextView cname;
    TextView ename;
    TextView introduce;

    Handler handler;
    private String id;
    byte[] photobytes;

    public static ViewFragment newInstance() {
        return new ViewFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_view, container, false);

        id=getArguments().getString("id");
        picture = (ImageView) view.findViewById(R.id.iv_avatar);
        cname = (TextView) view.findViewById(R.id.insect_cname);
        ename = (TextView) view.findViewById(R.id.insect_ename);
        introduce = (TextView) view.findViewById(R.id.insect_pr);
        // 修改
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 页面关系设置
                NavController navController= Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
                Bundle bundle=new Bundle();
                bundle.putString("id",id);
                // 跳转页面
                navController.navigate(R.id.nav_cgatlas,bundle);
            }
        });
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
                //getdata
                List<Map<String, Object>> mData = (List<Map<String, Object>>) msg.obj;
                //showdata
                cname.setText(mData.get(0).get("cname").toString());
                ename.setText(mData.get(0).get("ename").toString());
                introduce.setText(mData.get(0).get("introduce").toString());
                if(mData.get(0).get("picture") instanceof Bitmap)
                    picture.setImageBitmap((Bitmap) mData.get(0).get("picture"));
                else
                    picture.setBackgroundResource((Integer)mData.get(0).get("picture"));
            }

        };
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
}

