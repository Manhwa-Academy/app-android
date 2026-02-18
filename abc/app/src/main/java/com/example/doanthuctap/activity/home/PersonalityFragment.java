package com.example.doanthuctap.activity.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanthuctap.R;
import com.example.doanthuctap.activity.personality.AccountInformationActivity;
import com.example.doanthuctap.helper.GlobalVariable;
import com.example.doanthuctap.model.Setting;
import com.example.doanthuctap.model.User;
import com.example.doanthuctap.recyclerviewadapter.SettingsRecyclerViewAdapter;
import com.example.doanthuctap.viewModel.home.PersonalityFragmentViewModel;

import java.util.ArrayList;
import java.util.List;

public class PersonalityFragment extends Fragment {

    private RecyclerView recyclerView;
    private SettingsRecyclerViewAdapter adapter;
    private final List<Setting> settings = new ArrayList<>();

    private TextView username;
    private TextView buttonChangeInformation;

    private User authUser;
    private PersonalityFragmentViewModel viewModel;
    private String token;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_personality, container, false);

        // Lấy token an toàn
        Bundle args = getArguments();
        if (args != null) {
            token = args.getString("accessToken");
        }

        setupComponent(view);
        setupViewModel();
        setupSettings();
        setupRecyclerView();
        setupEvent();

        return view;
    }

    private void setupComponent(View view) {
        recyclerView = view.findViewById(R.id.personalitySettings);
        username = view.findViewById(R.id.personalityName);
        buttonChangeInformation = view.findViewById(R.id.personalityButtonChangeInformation);

        GlobalVariable global =
                (GlobalVariable) requireActivity().getApplication();
        authUser = global.getAuthUser();
    }

    /**
     * 🔥 QUAN TRỌNG:
     * - Chỉ gọi API khi CHƯA có authUser
     * - Tránh ghi đè dữ liệu mới sau khi update
     */
    @SuppressLint("SetTextI18n")
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this)
                .get(PersonalityFragmentViewModel.class);

        if (authUser == null && token != null) {
            viewModel.getProfile(token);
        }

        viewModel.getResponse().observe(getViewLifecycleOwner(), response -> {

            if (response == null || response.getResult() != 1 || response.getData() == null) {
                username.setText(getString(R.string.unknown));
                return;
            }

            // Cập nhật GlobalVariable
            GlobalVariable global =
                    (GlobalVariable) requireActivity().getApplication();
            global.setAuthUser(response.getData());

            authUser = response.getData();
            username.setText(
                    authUser.getFirstName() + " " + authUser.getLastName()
            );
        });
    }

    /**
     * 🔥 FIX LỖI ĐỔI TÊN KHÔNG CẬP NHẬT
     * Mỗi lần quay lại fragment đều load lại authUser mới nhất
     */
    @Override
    public void onResume() {
        super.onResume();

        GlobalVariable global =
                (GlobalVariable) requireActivity().getApplication();
        authUser = global.getAuthUser();

        if (authUser != null) {
            username.setText(
                    authUser.getFirstName() + " " + authUser.getLastName()
            );
        } else {
            username.setText(getString(R.string.unknown));
        }
    }

    private void setupSettings() {
        settings.clear();

        settings.add(new Setting(
                "orders",
                getString(R.string.all_orders),
                R.drawable.ic_all_orders
        ));

        settings.add(new Setting(
                "darkMode",
                getString(R.string.dark_mode),
                R.drawable.ic_dark_mode
        ));

        settings.add(new Setting(
                "profile",
                getString(R.string.personal_information),
                R.drawable.ic_profile
        ));

        // ADMIN
        if (authUser != null && "admin".equals(authUser.getRole())) {
            settings.add(new Setting(
                    "adminOrders",
                    getString(R.string.all_orders_admin),
                    R.drawable.ic_all_orders_admin
            ));
            settings.add(new Setting(
                    "adminProducts",
                    getString(R.string.all_products_admin),
                    R.drawable.ic_all_products_admin
            ));
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        adapter = new SettingsRecyclerViewAdapter(requireActivity(), settings);
        recyclerView.setAdapter(adapter);
    }

    private void setupEvent() {
        buttonChangeInformation.setOnClickListener(view -> {
            Intent intent =
                    new Intent(requireContext(), AccountInformationActivity.class);
            startActivity(intent);
        });
    }
}
