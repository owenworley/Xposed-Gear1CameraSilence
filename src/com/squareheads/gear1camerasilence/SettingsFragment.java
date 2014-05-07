package com.squareheads.gear1camerasilence;

import android.os.Bundle;
import android.preference.PreferenceFragment;
/*
import android.content.SharedPreferences;
import android.util.Log;
*/
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_screen);
    }

	/*
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		Log.d("gearssilence", "Pref clicked " + preference.getKey());
		
		
		SharedPreferences prefs = this.getPreferenceManager().getSharedPreferences();
		
		Log.d("gearssilence", "SettingsActivity.Pref_Log_Enabled " + prefs.getBoolean(SettingsActivity.Pref_Log_Enabled, false));
		Log.d("gearssilence", "SettingsActivity.Pref_Disable_Sound_Autofocus " + prefs.getBoolean(SettingsActivity.Pref_Disable_Sound_Autofocus, true));
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	*/
}