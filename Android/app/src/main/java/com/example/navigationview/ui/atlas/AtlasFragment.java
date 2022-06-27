package com.example.navigationview.ui.atlas;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.example.navigationview.DBUtil;
import com.example.navigationview.R;
import com.example.navigationview.ui.pytorch.ImageNetClasses;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AtlasFragment extends Fragment {

    AutoCompleteTextView input = null;
    String ccname = null;
    String[] cnames = ImageNetClasses.IMAGENET_CLASSES;
    RecyclerView recyclerView;
    List<Map<String, Object>> mData;
    Handler handler;

    public static AtlasFragment newInstance() {
        return new AtlasFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_atlas, container, false);

        recyclerView=view.findViewById(R.id.recycler_view2);
        // 自动完成文本框
        input = (AutoCompleteTextView) view.findViewById(R.id.input);
        ArrayAdapter<String> adapterauto = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item, cnames);
        input.setAdapter(adapterauto);
        input.setThreshold(1);

        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mData=(List<Map<String, Object>>)msg.obj;
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));//垂直线性布局
                recyclerView.setAdapter(new MyRecycleViewAdapter());
            }
        };
        // 获取数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                    list=getData(ccname);
                    Message message=handler.obtainMessage();
                    message.obj=list;
                    handler.sendMessage(message);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        // 搜索
        TextView ser = (TextView) view.findViewById(R.id.textview);

        ser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ccname = input.getText().toString().trim();
                // 开启线程 存储内容
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                            list=getData(ccname);
                            Message message=handler.obtainMessage();
                            message.obj=list;
                            handler.sendMessage(message);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        return view;
    }

    private List<Map<String, Object>> getData(String ccname) throws SQLException {
        byte[] b = null;
        Bitmap image = null;
        ResultSet rs ;
        if (ccname == null){
            Object[] values={};
            rs= DBUtil.rawQuery("select * from atlas",values);
        }
        else{
            Object[] values={ccname};
            rs= DBUtil.rawQuery("select * from atlas where cname=?",values);
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        while (rs.next())
        {
            HashMap<String, Object> map = new HashMap<String, Object>();

            map.put("id",rs.getString("uid"));
            map.put("cname", rs.getString("cname"));
            map.put("ename",rs.getString("ename"));
            b = rs.getBytes("picture");

            if (b != null && b.length > 0) {
                image = BitmapFactory.decodeByteArray(b, 0, b.length);
                map.put("picture", image);
            } else {
                map.put("picture", R.mipmap.ic_launcher);
            }
            list.add(map);
        }
        return list;
    }

    class MyRecycleViewAdapter extends RecyclerView.Adapter<MyRecycleViewAdapter.ViewHolder>
    {
        public  class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView picture;
            public TextView cname;
            public TextView ename;
            public TextView publishertime;
            public ImageButton btn;
            // 每一个view的组建
            public ViewHolder(View convertView) {
                super(convertView);
                picture = (ImageView)convertView.findViewById(R.id.picture);
                cname = (TextView)convertView.findViewById(R.id.cname);
                ename = (TextView)convertView.findViewById(R.id.ename);
                btn = (ImageButton)convertView.findViewById(R.id.btn);
            }
        }

        @NonNull
        @Override
        public MyRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v= LayoutInflater.from(getActivity()).inflate(R.layout.item2,parent, false);
            return new MyRecycleViewAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyRecycleViewAdapter.ViewHolder holder, final int position) {
            if(mData.get(position).get("picture") instanceof Bitmap)
                holder.picture.setImageBitmap((Bitmap) mData.get(position).get("picture"));
            else
                holder.picture.setBackgroundResource((Integer)mData.get(position).get("picture"));
            holder.cname.setText((String)mData.get(position).get("cname"));
            holder.ename.setText((String)mData.get(position).get("ename"));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavController navController= Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
                    Bundle bundle=new Bundle();
                    bundle.putString("id",(String)mData.get(position).get("id"));
                    navController.navigate(R.id.nav_view,bundle);
                }
            });
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavController navController= Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
                    Bundle bundle=new Bundle();
                    bundle.putString("id",(String)mData.get(position).get("id"));
                    navController.navigate(R.id.nav_cgatlas,bundle);
                }
            });

        }
        @Override
        public int getItemCount() {
            return mData.size();
        }
    }
}
