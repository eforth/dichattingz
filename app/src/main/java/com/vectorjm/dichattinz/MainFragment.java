package com.vectorjm.dichattinz;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth auth;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    public MainFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        BaseActivity baseActivity = ((BaseActivity) getActivity());
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        navigationView = view.findViewById(R.id.navigation);
        drawerLayout = view.findViewById(R.id.drawer);


        drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.Open,
                R.string.Close);

        if (baseActivity == null) return;

        baseActivity.setSupportActionBar(toolbar);

        drawerLayout.addDrawerListener(drawerToggle);

        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        setupHeaderView(navigationView);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainer, new HomeFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.account:
                Toast.makeText(getActivity(), R.string.account, Toast.LENGTH_SHORT).show();
                break;
            case R.id.home:
                Toast.makeText(getActivity(), R.string.home, Toast.LENGTH_SHORT).show();
                break;
            case R.id.friends:
                Toast.makeText(getActivity(), R.string.friends, Toast.LENGTH_SHORT).show();
                break;
            case R.id.messaging:
                Toast.makeText(getActivity(), R.string.messaging, Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout:
                logoutAction();
                break;
            default:
                return true;
        }

        navigationView.getMenu().getItem(0).setChecked(false);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupHeaderView(NavigationView navigationView) {

        View header = navigationView.getHeaderView(0);
        ImageView imageView = header.findViewById(R.id.nav_header_imageView);
        TextView headerName = header.findViewById(R.id.nav_header_textView);

        if (auth.getCurrentUser() == null) return;

        FirebaseUser user = auth.getCurrentUser();

        headerName.setText(user.getDisplayName());
    }

    private void logoutAction() {
        if (getActivity() == null) return;

        auth.signOut();

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.baseContainer, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }
}
