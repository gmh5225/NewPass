package com.gero.newpass.view.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;

import com.gero.newpass.R;
import com.gero.newpass.SharedPreferences.SharedPreferencesHelper;
import com.gero.newpass.databinding.FragmentSettingsBinding;
import com.gero.newpass.model.SettingData;
import com.gero.newpass.utilities.VibrationHelper;
import com.gero.newpass.view.activities.MainViewActivity;
import com.gero.newpass.view.adapters.SettingsAdapter;

import java.util.ArrayList;

public class SettingsFragment extends Fragment {
    private ImageButton buttonBack;
    private FragmentSettingsBinding binding;
    private ListView listView;
    private String url;
    private Intent intent;
    private Boolean isDarkModeSet;
    static final int DARK_THEME = 0;
    static final int CHANGE_LANGUAGE = 1;
    static final int CHANGE_PASSWORD = 2;
    static final int EXPORT = 3;
    static final int IMPORT = 4;
    static final int GITHUB = 5;
    static final int SHARE = 6;
    static final int CONTACT = 7;
    static final int APP_VERSION = 8;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(binding);
        Activity activity = this.getActivity();

        ArrayList<SettingData> arrayList = new ArrayList<>();

        buttonBack.setOnClickListener(v -> {
            if (activity instanceof MainViewActivity) {
                ((MainViewActivity) activity).onBackPressed();
            }
        });

        createSettingsList(arrayList);

        SettingsAdapter settingsAdapter = new SettingsAdapter(requireContext(), R.layout.list_row, arrayList, getActivity());

        listView.setAdapter(settingsAdapter);

        listView.setOnItemClickListener((parent, view1, position, id) -> {

            switch(position) {
                case DARK_THEME:
                    Log.i("ImageMenu", "DARK_THEME");

                    VibrationHelper.vibrate(requireContext(), getResources().getInteger(R.integer.vibration_duration1));

                    DialogFragment languageDialogFragment = new LanguageDialogFragment();
                    languageDialogFragment.setCancelable(false);
                    languageDialogFragment.show(requireActivity().getSupportFragmentManager(), "Language Dialog");
                    break;

                case CHANGE_LANGUAGE:
                    Log.i("ImageMenu", "CHANGE_LANGUAGE");
                    VibrationHelper.vibrate(requireContext(), getResources().getInteger(R.integer.vibration_duration1));


                    break;

                case CHANGE_PASSWORD:
                    Log.i("ImageMenu", "CHANGE_PASSWORD");
                    //TODO
                    break;

                case EXPORT:
                    Log.i("ImageMenu", "EXPORT");
                    //TODO
                    break;

                case IMPORT:
                    Log.i("ImageMenu", "IMPORT");
                    //TODO
                    break;

                case GITHUB:
                    Log.i("ImageMenu", "GITHUB");
                    VibrationHelper.vibrate(requireContext(), getResources().getInteger(R.integer.vibration_duration1));
                    url = "https://github.com/6eero/NewPass";
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    break;

                case SHARE:
                    Log.i("ImageMenu", "SHARE");
                    VibrationHelper.vibrate(requireContext(), getResources().getInteger(R.integer.vibration_duration1));
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.settings_share_text));
                    startActivity(Intent.createChooser(shareIntent, "Share with..."));
                    break;

                case CONTACT:
                    Log.i("ImageMenu", "CONTACT");
                    VibrationHelper.vibrate(requireContext(), getResources().getInteger(R.integer.vibration_duration1));
                    url = "https://t.me/geroED";
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    break;

                case APP_VERSION:
                    Log.i("ImageMenu", "APP_VERSION");
                    break;
            }
        });
    }

    private void createSettingsList(ArrayList<SettingData> arrayList) {
        arrayList.add(new SettingData(R.drawable.settings_icon_dark_theme, getString(R.string.settings_dark_theme), true, false, false));
        arrayList.add(new SettingData(R.drawable.settings_icon_language, getString(R.string.settings_change_language), false, false,false));
        arrayList.add(new SettingData(R.drawable.settings_icon_lock, getString(R.string.settings_change_password), false, false,false));
        arrayList.add(new SettingData(R.drawable.icon_export, getString(R.string.settings_export_db), false, false,false));
        arrayList.add(new SettingData(R.drawable.icon_import, getString(R.string.settings_import_db), false, false,false));
        arrayList.add(new SettingData(R.drawable.settings_icon_github, getString(R.string.settings_github), false, false,true));
        arrayList.add(new SettingData(R.drawable.settings_icon_share, getString(R.string.settings_share_newpass), false, false,true));
        arrayList.add(new SettingData(R.drawable.settings_icon_telegram, getString(R.string.settings_contact_me), false, false,true));
        arrayList.add(new SettingData(R.drawable.settings_icon_version, getString(R.string.app_version) + getAppVersion(), false, false,false));
    }

    private String getAppVersion() {
        String versionName = "";
        int versionCode = 0;

        try {
            PackageManager packageManager = requireActivity().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(requireActivity().getPackageName(), 0);
            versionName = packageInfo.versionName;
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    private void initViews(FragmentSettingsBinding binding) {
        buttonBack = binding.backButton;
        listView = binding.listView;
    }
}