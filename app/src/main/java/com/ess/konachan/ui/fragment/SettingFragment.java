package com.ess.konachan.ui.fragment;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.ess.konachan.R;
import com.ess.konachan.bean.MsgBean;
import com.ess.konachan.global.Constants;

import org.greenrobot.eventbus.EventBus;

public class SettingFragment extends PreferenceFragment {

    private ListPreference mListPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_setting);
        initView();
    }

    private void initView() {
        mListPreference = (ListPreference) findPreference(getString(R.string.setting_search_mode));
        final String key = getString(R.string.setting_search_mode);
        String defaultValue = getString(R.string.setting_search_mode_safe);
        mListPreference.setSummary(mListPreference.getSharedPreferences().getString(key, defaultValue));
        mListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String mode = (String) newValue;
                if (!mode.equals(preference.getSummary())) {
                    mListPreference.getEditor().putString(key, mode).apply();
                    EventBus.getDefault().post(new MsgBean(Constants.TOGGLE_SEARCH_MODE, null));
                }
                preference.setSummary(mode);
                return true;
            }
        });
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }
}
