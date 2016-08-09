package ru.igorsh.kidcommunicator;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, Constants {

	private ListPreference mSoundCfg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setPreferenceScreen(createPreferences());
		
		//addPreferencesFromResource(R.xml.settings);
	}

	private PreferenceScreen createPreferences() {
		PreferenceScreen lRoot = getPreferenceManager().createPreferenceScreen(this);
		
		SharedPreferences lConfig = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		//PreferenceCategory lSoundCategory = new PreferenceCategory(this);
		//lSoundCategory.setTitle(R.string.cfg_button_sound_title);
		//lRoot.addPreference(lSoundCategory);

		mSoundCfg = new ListPreference(this);
		mSoundCfg.setTitle(R.string.title_sound_settings);
		mSoundCfg.setEntries(R.array.cfg_button_sound_names);
		mSoundCfg.setEntryValues(R.array.cfg_button_sound_values);
		mSoundCfg.setDialogTitle(R.string.cfg_button_sound_title);
		mSoundCfg.setKey(CFG_SOUND_TYPE_KEY);
		mSoundCfg.setDefaultValue("1");
		lRoot.addPreference(mSoundCfg);
		
		// настройка фраз
		String lLang = Locale.getDefault().toString();
		PreferenceScreen lPhrasesScreen = getPreferenceManager().createPreferenceScreen(this);
		lPhrasesScreen.setKey(lLang + ".cfg_phrases");
		lPhrasesScreen.setTitle(R.string.title_phrases_settings);
			
		for (String lBtnName : Constants.BUTTON_NAMES) {
			String lKey = lLang + "_cfg_phrases_" + lBtnName;
			
			// создание полей для ввода текст
			EditTextPreference lEdit = new EditTextPreference(this);
			lEdit.setKey(lKey);
			lEdit.setTitle(lConfig.getString(lKey, ""));

			try {
				InputStream lTmp = getAssets().open("btn_images/" + lBtnName + ".png");
				Bitmap lImg = BitmapFactory.decodeStream(lTmp);
				Bitmap lImgSmall = Bitmap.createScaledBitmap(lImg, 48, 48, false);

				lEdit.setDialogTitle(R.string.str_input_text);
				lEdit.setDialogIcon(new BitmapDrawable(getResources(), lImgSmall));

				lEdit.setIcon(new BitmapDrawable(getResources(), lImgSmall));

			} catch (IOException e) {
				lEdit.setTitle(lBtnName);
				lEdit.setDialogTitle(R.string.str_input_text);

				e.printStackTrace();
			}
				
			lPhrasesScreen.addPreference(lEdit);
		}
		
		lRoot.addPreference(lPhrasesScreen);
		return lRoot;
	}
	
	@Override
	protected void onStart() {
		super.onStart();		
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();		
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences aPreferences, String aKey) {		
		if (!aKey.equals(Constants.CFG_SOUND_TYPE_KEY)) {
			Preference lPref = getPreferenceScreen().findPreference(aKey);
			if (lPref != null) {
				lPref.setTitle(aPreferences.getString(aKey, ""));
			}
		}
	}

}
