package ru.igorsh.kidcommunicator;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import ru.igorsh.kidcommunicator.util.IabHelper;
import ru.igorsh.kidcommunicator.util.IabHelper.IabAsyncInProgressException;
import ru.igorsh.kidcommunicator.util.IabResult;
import ru.igorsh.kidcommunicator.util.Purchase;

public class DonateActivity extends Activity {
	//important : put your own SKU's as defined here
	//static final String SKU_SMALL = "android.test.purchased";
  static final String SKU_SMALL = "kidcommunicator_donate_s";
	static final String SKU_MEDIUM = "kidcommunicator_donate_m";
	static final String SKU_LARGE = "kidcommunicator_donate_l";
	static final String SKU_XL = "kidcommunicator_donate_xl";
	static final String SKU_XXL = "kidcommunicator_donate_xxl";
	String TAG = "IgorSH";

	private Toast toast = null;

	// (arbitrary) request code for the purchase flow
	static final int RC_REQUEST = 10001;

	// the helper object
	IabHelper mHelper;

	// Button setups
	Button button_s, button_m, button_l, button_xl, button_xxl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,	WindowManager.LayoutParams.FLAG_FULLSCREEN);
    
		super.onCreate(savedInstanceState);
		setContentView(R.layout.donate);

		button_s = (Button) findViewById(R.id.donate_button_s);
		button_m = (Button) findViewById(R.id.donate_button_m);
		button_l = (Button) findViewById(R.id.donate_button_l);
		button_xl = (Button) findViewById(R.id.donate_button_xl);
		button_xxl = (Button) findViewById(R.id.donate_button_xxl);

		button_s.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				makeDonation(1);
			}
		});

		button_m.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				makeDonation(2);
			}
		});

		button_l.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				makeDonation(3);
			}
		});

		button_xl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				makeDonation(4);

			}
		});

		button_xxl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				makeDonation(5);
			}
		});

		// In-app purchase stuff
		//Remember to copy your application's specific license key from google play here
		//for security purposes, save it to an xml if it needs to be on github
		// app_lic312 + app_lic301 + app_lic413 + app_lic112
		String base64EncodedPublicKey = getString(R.string.app_lic312)
						+ getString(R.string.app_lic301)
						+ getString(R.string.app_lic413)
						+ getString(R.string.app_lic112);
		Log.d(TAG, "Creating IAB helper.");
		mHelper = new IabHelper(this, base64EncodedPublicKey);

		// enable debug logging (for a production application, you should set
		// this to false).
		// режим отладки
		mHelper.enableDebugLogging(true);

		// Start setup. This is asynchronous and the specified listener
		// will be called once setup completes.
		Log.d(TAG, "Starting setup.");
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				Log.d(TAG, "Setup finished.");

				if (!result.isSuccess()) {
					// Oh noes, there was a problem.
					toast(getString(R.string.in_app_bill_error) + result, false);
					Log.d(TAG, getString(R.string.in_app_bill_error) + result);
					return;
				}

				// Have we been disposed of in the meantime? If so, quit.
				if (mHelper == null)
					return;

				// IAB is fully set up. Now, let's get an inventory of stuff we own.
				//   --commented out here as we didn't need it for donation purposes.
				// Log.d(TAG, "Setup successful. Querying inventory.");
				// mHelper.queryInventoryAsync(mGotInventoryListener);
			}
		});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// very important:
		Log.d(TAG, "Destroying helper.");
		if (mHelper != null) {
			mHelper.disposeWhenFinished();
			mHelper = null;
		}
	}

	//DO NOT SKIP THIS METHOD
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + ","	+ data);
		
    if (mHelper == null)
			return;

		// Pass on the activity result to the helper for handling
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
			// not handled, so handle it ourselves (here's where you'd
			// perform any handling of activity results not related to in-app
			// billing...
			super.onActivityResult(requestCode, resultCode, data);
		} else {
			Log.d(TAG, "onActivityResult handled by IABUtil.");
		}
	}

	/** Verifies the developer payload of a purchase. */
	boolean verifyDeveloperPayload(Purchase p) {
		String payload = p.getDeveloperPayload();

		/**Follow google guidelines to create your own payload string here, in case it is needed.
		*Remember it is recommended to store the keys on your own server for added protection
		USE as necessary*/

		return true;
	}

	// Callback for when a purchase is finished
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

			// if we were disposed of in the meantime, quit.
			if (mHelper == null)
				return;

			if (result.isFailure()) {
				//toast(getString(R.string.purchase_error) + " " + result, false);
        toast(result.getMessage(), false);
        //Log.d(TAG, "result.isFailure" + " " + result);
				// setWaitScreen(false);
				return;
			}
			if (!verifyDeveloperPayload(purchase)) {
				toast(getString(R.string.error_verification), false);
        //Log.d(TAG, "verifyDeveloperPayload" + " " + result);
				// setWaitScreen(false);
				return;
			}

			Log.d(TAG, "Purchase successful.");

			if (purchase.getSku().equals(SKU_SMALL)
					|| purchase.getSku().equals(SKU_MEDIUM)
					|| purchase.getSku().equals(SKU_LARGE)
					|| purchase.getSku().equals(SKU_XL)
					|| purchase.getSku().equals(SKU_XXL)) {

				// Log.d(TAG, "small donation");
				try {
					mHelper.consumeAsync(purchase, mConsumeFinishedListener);
				} catch (IabAsyncInProgressException e) {
					Log.d(TAG, e.getMessage());
				}
			}

		}
	};

	// Called when consumption is complete
	IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
		public void onConsumeFinished(Purchase purchase, IabResult result) {
			Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

			// if we were disposed of in the meantime, quit.
			if (mHelper == null)
				return;

			//check which SKU is consumed here and then proceed.

			if (result.isSuccess()) {
        
        // записываем признак что сделали пожертвования
        SharedPreferences lConfig = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor lEditor = lConfig.edit();
        lEditor.putString(Constants.CFG_DONATED_IS_DONATED_KEY, "1");
        lEditor.commit();
        //----------------------------------------------------------------------

				Log.d(TAG, "Consumption successful. Provisioning.");

				toast(getString(R.string.thank_you), true);
			} else {
				toast(getString(R.string.error_consume) + result, false);
			}


			Log.d(TAG, "End consumption flow.");
		}
	};

	//the button clicks send an int value which would then call the specific SKU, depending on the
	//application
	public void makeDonation(int value) {
		//check your own payload string.
		String payload = "";

		try {
			switch (value) {
			case (1):
				mHelper.launchPurchaseFlow(this, SKU_SMALL, RC_REQUEST, mPurchaseFinishedListener, payload);
				//System.out.println("small purchase");
				break;
			case (2):
				mHelper.launchPurchaseFlow(this, SKU_MEDIUM, RC_REQUEST, mPurchaseFinishedListener, payload);
				//System.out.println("medium purchase");
				break;
			case (3):
				mHelper.launchPurchaseFlow(this, SKU_LARGE, RC_REQUEST,	mPurchaseFinishedListener, payload);
				//System.out.println("large purchase");
				break;
			case (4):
				//System.out.println("xl purchase");
				mHelper.launchPurchaseFlow(this, SKU_XL, RC_REQUEST, mPurchaseFinishedListener, payload);
				break;
			case (5):
				//System.out.println("xxl purchase");
				mHelper.launchPurchaseFlow(this, SKU_XXL, RC_REQUEST, mPurchaseFinishedListener, payload);
				break;

			default:
				break;
			}
		} catch (IabAsyncInProgressException e) {
			Log.d(TAG, e.getMessage());
		}
	}

	private void toast(final String msg, final Boolean is_long) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (toast == null) {
					toast = Toast.makeText(getApplicationContext(), "",	(is_long ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT));
				}
				toast.setText(msg);
				toast.show();
			}
		});
	}

}