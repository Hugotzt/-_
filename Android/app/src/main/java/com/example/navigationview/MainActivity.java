package com.example.navigationview;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 左上的菜单
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 侧滑菜单页面
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        // 侧滑菜单，分为上下两个页面
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        // 菜单组建构件 AppBar配置
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_input,
                R.id.nav_atlas,
                R.id.nav_feedback).setDrawerLayout(drawer).build();

        // NavController导航控制器，里面包含了导航规则，是导航的中枢
        // 从Fragment获取导航控制器，navGraph定义了导航规则
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        //.setupActionBarWithNavController.NavigationUI设置NavController导航改变的监听事件，修改页面标题栏
        // 为AppBar设置导航控制器，监听导航改变事件，修改标题
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        // NavigationUI.setupWithNavController，设置触发导航改变的事件，当底部导航栏被点击时触发导航
        // 为视图设置导航控制器，即监听视图的点击事件
        NavigationUI.setupWithNavController(navigationView, navController);

        // 左侧菜单设置
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);

        TextView name = (TextView) headerLayout.findViewById(R.id.textView);
        ImageView img = (ImageView) headerLayout.findViewById(R.id.imageView);

        Intent intent = getIntent();
        ArrayList<String> user =new ArrayList<>();
        user = intent.getStringArrayListExtra("User");

        name.setText(user.get(0));

        byte[] image = intent.getByteArrayExtra("Image");
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        img.setImageBitmap(bitmap);

        Properties properties = new Properties();
        try {
            FileOutputStream fos = new FileOutputStream(new File(getCacheDir(), "config"));
            properties.setProperty("name", user.get(0));
            properties.setProperty("age",user.get(1));
            properties.setProperty("phone",user.get(2));

            String string=null;
            ByteArrayOutputStream bStream=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,75,bStream);
            byte[]bytes=bStream.toByteArray();
            string=Base64.encodeToString(bytes,Base64.DEFAULT);
            properties.setProperty("Image", string);
            properties.store(fos, null);
            fos.flush();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 创建了主页顶部菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // 页面控制 顶部的选择菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController=Navigation.findNavController(this,R.id.nav_host_fragment);

        int id=item.getItemId();

        // 退出
        if(id==R.id.banben)
        {
            // 页面跳转
            navController.navigate(R.id.nav_banben);
        }

        // 昆虫图谱
        if(id==R.id.action_edit)
        {
            // 页面跳转
            navController.navigate(R.id.nav_atlas);
        }
        return super.onOptionsItemSelected(item);
    }

    // 页面控制 返回键会返回到主页面
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
        // onSupportNavigateUp 可以用toolbar的后退进行后退
    }



}
