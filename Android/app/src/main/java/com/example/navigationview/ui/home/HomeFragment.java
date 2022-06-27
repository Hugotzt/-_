package com.example.navigationview.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;


import androidx.navigation.NavController;
import androidx.navigation.Navigation;


import com.example.navigationview.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 底部导航栏
        BottomNavigationView navView2 =view.findViewById(R.id.nav_view2);
        navView2.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                NavController navController=Navigation.findNavController(getActivity(),R.id.nav_host_fragment2);
                if(id==R.id.navigation_home) {
                    navController.navigate(R.id.nav_recyclerview);
                }
                if(id==R.id.navigation_dashboard) {
                    navController.navigate(R.id.nav_pytorch);
                }
                if(id==R.id.navigation_notifications) {
                    navController.navigate(R.id.nav_mybase);
                }
                return true;
            }
        });

        return view;

    }
}
