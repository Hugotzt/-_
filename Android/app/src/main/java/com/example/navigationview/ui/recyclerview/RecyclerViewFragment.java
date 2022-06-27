package com.example.navigationview.ui.recyclerview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.navigationview.DBUtil;
import com.example.navigationview.R;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecyclerViewFragment extends Fragment {

    RecyclerView recyclerView;
    List<Map<String, Object>> mData;
    Handler handler;

    public static RecyclerViewFragment newInstance() {
        return new RecyclerViewFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        recyclerView=view.findViewById(R.id.recycler_view);
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //getdata
                mData=(List<Map<String, Object>>)msg.obj;
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));//垂直线性布局
                recyclerView.setAdapter(new MyRecycleViewAdapter());
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                    list=getData();
                    Message message=handler.obtainMessage();
                    message.obj=list;
                    handler.sendMessage(message);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return view;
    }

    private List<Map<String, Object>> getData() throws SQLException {
        byte[] b = null;
        Bitmap image = null;
        Object[] values={};
        ResultSet rs= DBUtil.rawQuery("select * from insect",values);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        while (rs.next())
        {
            HashMap<String, Object> map = new HashMap<String, Object>();

            map.put("id",rs.getString("id"));
            map.put("place", rs.getString("place"));
            map.put("publishertime",rs.getString("publishertime"));
            map.put("label",rs.getString("label"));
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
            public TextView place;
            public TextView label;
            public TextView publishertime;

            // 每一个view的组建
            public ViewHolder(View convertView) {
                super(convertView);
                picture = (ImageView)convertView.findViewById(R.id.picture);
                place = (TextView)convertView.findViewById(R.id.place);
                label = (TextView)convertView.findViewById(R.id.label);
                publishertime = (TextView)convertView.findViewById(R.id.publishertime);
            }
        }


        @NonNull
        @Override
        public MyRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v= LayoutInflater.from(getActivity()).inflate(R.layout.item,parent, false);
            return new ViewHolder(v);
        }


        @Override
        public void onBindViewHolder(@NonNull MyRecycleViewAdapter.ViewHolder holder, final int position) {

            if(mData.get(position).get("picture") instanceof Bitmap)
                holder.picture.setImageBitmap((Bitmap) mData.get(position).get("picture"));
            else
                holder.picture.setBackgroundResource((Integer)mData.get(position).get("picture"));
            holder.place.setText((String)mData.get(position).get("place"));
            holder.label.setText((String)mData.get(position).get("label"));
            holder.publishertime.setText((String)mData.get(position).get("publishertime"));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 页面关系设置
                    NavController navController= Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
                    Bundle bundle=new Bundle();
                    bundle.putString("id",(String)mData.get(position).get("id"));
                    // 跳转页面
                    navController.navigate(R.id.nav_change,bundle);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }



}
