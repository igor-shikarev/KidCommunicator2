package ru.igorsh.kidcommunicator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

	private MediaPlayer mPlayer = null;
	private TextToSpeech mTts = null;
	private boolean mCanSpeak = false;
	private boolean mIsDonated = false;
	private LinearLayout mLayoutDonate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// инициализация конфига
		this.initConfig();

		mPlayer = new MediaPlayer();
    
		// показ панели благодарности
		mLayoutDonate = (LinearLayout) findViewById(R.id.layoutDonate);
		mLayoutDonate.setVisibility((mIsDonated ? View.INVISIBLE : View.VISIBLE));
		if (!mIsDonated) {
			Button btnYes = (Button) findViewById(R.id.btnYes);
			Button btnCancel = (Button) findViewById(R.id.btnCancel);

			btnYes.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showDonation();
				}
			});

			btnCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SharedPreferences lConfig = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					SharedPreferences.Editor lEditor = lConfig.edit();

					lEditor.putString(Constants.CFG_DONATED_IS_DONATED_KEY, "1");
					lEditor.commit();

					mLayoutDonate.animate()
						.translationY(mLayoutDonate.getHeight())
						.alpha(1.0f)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								super.onAnimationEnd(animation);
								mLayoutDonate.setVisibility(View.INVISIBLE);
							}
						});
				}
			});
		}
    //--------------------------------------------------------------------------

		// вывод информации о дисплее
		DisplayMetrics lMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(lMetrics);

		//TextView lInfo = (TextView) findViewById(R.id.tv_screen);
		//lInfo.setText("dpi:" + lMetrics.densityDpi + " (" + lMetrics.density + ") " + " w:" + lMetrics.widthPixels + " h:" + lMetrics.heightPixels);
		// инициализация кнопок
		int i;
		ImageView lImg;
		Bitmap lBtm, lBtm1;
		Resources lRes = getResources();

		// вычисление ширины рисунка
		int lWdp = (int) Math.floor(lMetrics.widthPixels / lMetrics.density) - 20;
		if (lRes.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			lWdp = (int) Math.floor(lWdp / 7) - 10;
		} else {
			lWdp = (int) Math.floor(lWdp / 4) - 10;
		}
		int lWpx = (int) Math.floor(lWdp * lMetrics.density);

		// инициализация кнопок
		String lLang = Locale.getDefault().toString();
		SharedPreferences lConfig = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		for (i = 0; i < Constants.BUTTON_NAMES.length; i++) {
			int lId = lRes.getIdentifier("img_view" + i, "id", getApplicationContext().getPackageName());
			lImg = (ImageView) findViewById(lId);
			lImg.setTag(i);

			// загрузка рисунка и изменение размера
			InputStream lTmp;
			try {
				lTmp = getAssets().open("btn_images/" + Constants.BUTTON_NAMES[i] + ".png");
				lBtm = BitmapFactory.decodeStream(lTmp);

				lBtm1 = Bitmap.createScaledBitmap(lBtm, lWpx, lWpx, false);
				lImg.setImageBitmap(lBtm1);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// зарузка строкового сообщения
			String lKey = lLang + "_cfg_phrases_" + Constants.BUTTON_NAMES[i];
			lImg.setContentDescription(lKey);
		}

		// инициализация голоса
		mTts = new TextToSpeech(this, this);

		//Toast toast = Toast.makeText(this, Locale.getDefault().toString(), Toast.LENGTH_LONG);
		//toast.show();
	}

	@Override
	public void onDestroy() {
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}

		mPlayer.release();

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem sender) {
		// Операции для выбранного пункта меню
		switch (sender.getItemId()) {
			case R.id.mi_about:
				showAbout();
				return true;
			case R.id.mi_settings:
				showSettings();
				return true;
			case R.id.mi_donation:
				showDonation();
				return true;        
			default:
				return super.onOptionsItemSelected(sender);
		}
	}

	public void doButtonClick(View sender) {
		SharedPreferences lConfig = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		TextView lTxt = (TextView) findViewById(R.id.tv_message);

		// пишем текст
		String lKey = sender.getContentDescription().toString();
		lTxt.setText(lConfig.getString(lKey, ""));

		// проговариваем текст
		if (lConfig.getString(Constants.CFG_SOUND_TYPE_KEY, "1").equals("0")) {
			// ничего не делаем
		} else if (lConfig.getString(Constants.CFG_SOUND_TYPE_KEY, "1").equals("1")) {
			mPlayer.reset();
			try {
				AssetFileDescriptor lFd = getResources().openRawResourceFd(R.raw.alert);
				mPlayer.setDataSource(lFd.getFileDescriptor(), lFd.getStartOffset(), lFd.getLength());
				lFd.close();
				mPlayer.prepare();
				mPlayer.start();
			} catch (IOException e) {
				Log.e("MediaPlayer", "Error", e);
			}
		} else if (lConfig.getString(Constants.CFG_SOUND_TYPE_KEY, "1").equals("2")) {
			if (mCanSpeak) {
				mTts.speak(lTxt.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
			} else {
				if (Locale.getDefault().toString().equalsIgnoreCase("ru_RU")) {
					mPlayer.reset();
					int lIdx = Integer.parseInt(sender.getTag().toString());
					try {
						AssetFileDescriptor lFd = getAssets().openFd("voice_ru/" + Constants.BUTTON_NAMES[lIdx] + ".ogg");
						mPlayer.setDataSource(lFd.getFileDescriptor(), lFd.getStartOffset(), lFd.getLength());
						lFd.close();
						mPlayer.prepare();
						mPlayer.start();
					} catch (IOException e) {
						Log.e("MediaPlayer", "Error", e);
					}

				} else {
					if (null != mPlayer) {
						mPlayer.reset();
					}

					mPlayer = MediaPlayer.create(this, R.raw.alert);
					mPlayer.start();
				}
			}
		}
	}

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int lResult = TextToSpeech.LANG_NOT_SUPPORTED;
			if (Locale.getDefault().toString().equalsIgnoreCase("ru_RU")) {
				// пытаемся поставить для русского, может заработает
				lResult = mTts.setLanguage(Locale.getDefault());
			} else {
				lResult = mTts.setLanguage(Locale.US);
			}

			if (lResult == TextToSpeech.LANG_MISSING_DATA || lResult == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Lanuage data is missing or the language is not supported.
				Log.e("TTS", "Language is not available.");
			} else {
				mCanSpeak = true;
			}
		} else {
			// Initialization failed.
			Log.e("TTS", "Could not initialize TextToSpeech.");
		}
	}

	private void initConfig() {
		SharedPreferences lConfig = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String lLang = Locale.getDefault().toString();

		SharedPreferences.Editor lEditor = lConfig.edit();

		// настройки звука
		if (lConfig.getString(Constants.CFG_SOUND_TYPE_KEY, "").isEmpty()) {
			lEditor.putString(Constants.CFG_SOUND_TYPE_KEY, "1");
		}

		// выводимые фразы
		int lDefMessId;
		String lDefMess, lKey;

		for (String lBtnName : Constants.BUTTON_NAMES) {
			lKey = lLang + "_cfg_phrases_" + lBtnName;
			lDefMessId = getResources().getIdentifier("mess_" + lBtnName, "string", getApplicationContext().getPackageName());
			lDefMess = getResources().getString(lDefMessId);

			if (lConfig.getString(lKey, "").isEmpty()) {
				lEditor.putString(lKey, lDefMess);
			}
		}

		lEditor.commit();

		// признак что пользователь поблагодарил
		mIsDonated = !lConfig.getString(Constants.CFG_DONATED_IS_DONATED_KEY, "").isEmpty();
	}

	private void showAbout() {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	private void showSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
  
  private void showDonation() {
    Intent intent = new Intent(MainActivity.this, DonateActivity.class);
    startActivity(intent);
	}
}
