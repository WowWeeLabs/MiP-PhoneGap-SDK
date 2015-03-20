package com.wowwee.bluetoothrobotscontrol;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import com.wowwee.bluetoothrobotcontrollib.MipCommandValues;
import com.wowwee.bluetoothrobotcontrollib.MipRobotSound;
import com.wowwee.bluetoothrobotcontrollib.BluetoothRobot;
import com.wowwee.bluetoothrobotcontrollib.sdk.MipRobot;
import com.wowwee.bluetoothrobotcontrollib.sdk.MipRobot.MipRobotInterface;
import com.wowwee.bluetoothrobotcontrollib.sdk.MipRobotFinder;

public class RobotsControlPlugin extends CordovaPlugin implements MipRobotInterface {

	private static final int REQUEST_ENABLE_BT = 10000;

	private BluetoothAdapter mBluetoothAdapter;

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		Log.d("RobotsControlPlugin", "initialize: activity="+cordova.getActivity());

		final BluetoothManager bluetoothManager = (BluetoothManager) cordova.getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Set BluetoothAdapter to MipRobotFinder
		MipRobotFinder.getInstance().setBluetoothAdapter(mBluetoothAdapter);

		// Set Context to MipRobotFinder
		MipRobotFinder.getInstance().setApplicationContext(cordova.getActivity());

		disconnectAllMip();
	}

	@Override
	public void onResume(boolean multitasking) {
		Log.d("RobotsControlPlugin", "onResume");


	}

	@Override
	public void onPause(boolean multitasking) {
		Log.d("RobotsControlPlugin", "onPause");

		MipRobotFinder.getInstance().stopScanForMips();

		try{
			cordova.getActivity().unregisterReceiver(mMipFinderBroadcastReceiver);
		}catch (IllegalArgumentException e){
			e.printStackTrace();
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		Log.d("RobotsControlPlugin", "onNewIntent");
	}

	@Override
	public void onDestroy() {
		BluetoothRobot.unbindBluetoothLeService(cordova.getActivity());
		Log.d("RobotsControlPlugin", "onDestroy");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		Log.d("RobotsControlPlugin", "onActivityResult");
	}

	/*
	 * The following are private function for internal usage
	 */

	private void enforceToEnbleBT()
	{
		Log.d("RobotsControlPlugin", "enforceToEnbleBT");

		// Register MipRobotFinder broadcast receiver
		cordova.getActivity().registerReceiver(mMipFinderBroadcastReceiver, MipRobotFinder.getMipRobotFinderIntentFilter());

		if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				cordova.getActivity().startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		startScanMips();
	}

	private void stopScanMips()
	{
		Log.d("RobotsControlPlugin", "stopScanMips");

		MipRobotFinder.getInstance().clearFoundMipList();

		MipRobotFinder.getInstance().stopScanForMips();
	}

	private void startScanMips()
	{
		Log.d("RobotsControlPlugin", "startScanMips");

		stopScanMips();

		MipRobotFinder.getInstance().scanForMips();
	}

	private void disconnectAllMip()
	{
		Log.d("RobotsControlPlugin", "disconnectAllMip");

		// Disconnect mip if connected mip still exist
		for(MipRobot mip : MipRobotFinder.getInstance().getMipsConnected()) {
			mip.disconnect();

		}
	}

	private MipRobot getFoundMipById(String mipId)
	{
		Log.d("RobotsControlPlugin", "getFoundMipById: mipId="+mipId);

		for(MipRobot mip : MipRobotFinder.getInstance().getMipsFoundList()) {
			if (mip.getBluetoothDevice().getAddress().equals(mipId)) {
				return mip;
			}
		}

		return null;
	}

	private MipRobot getConnectedMipByProductId(String mipId)
	{
		Log.d("RobotsControlPlugin", "getConnectedMipByProductId: mipId="+mipId);

		for(MipRobot mip : MipRobotFinder.getInstance().getMipsConnected()) {
			if (mip.getBluetoothDevice().getAddress().equals(mipId)) {
				return mip;
			}
		}

		return null;
	}

	private void connectToMip(MipRobot mip, final CallbackContext callbackContext)
	{
		Log.d("RobotsControlPlugin", "connectToMip: " + mip.getName());

		mip.setCallbackInterface(new MipRobotInterface() {
			@Override public void mipRobotIsCurrentlyInBootloader(MipRobot arg0, boolean arg1) {}
			@Override public void mipRobotDidReceiveWeightReading(byte arg0, boolean arg1) {}
			@Override public void mipRobotDidReceiveVolumeLevel(int arg0) {}
			@Override public void mipRobotDidReceiveSoftwareVersion(Date arg0, int arg1) {}
			@Override public void mipRobotDidReceivePosition(MipRobot arg0, byte arg1) {}
			@Override public void mipRobotDidReceiveIRCommand(ArrayList<Byte> arg0, int arg1) {}
			@Override public void mipRobotDidReceiveHardwareVersion(int arg0, int arg1) {}
			@Override public void mipRobotDidReceiveBatteryLevelReading(MipRobot arg0, int arg1) {}

			@Override
			public void mipDeviceReady(MipRobot mip) {
				Log.d("RobotsControlPlugin", mip.getName()+" - mipDeviceReady");

				mip.setCallbackInterface(RobotsControlPlugin.this);

				if (callbackContext != null) {
					Log.d("RobotsControlPlugin", "sendPluginResult");

					PluginResult result = new PluginResult(PluginResult.Status.OK);
				    result.setKeepCallback(false);
					callbackContext.sendPluginResult(result);
				}
			}

			@Override
			public void mipDeviceDisconnected(MipRobot mip) {
				Log.d("RobotsControlPlugin", mip.getName()+" - mipDeviceDisconnected");

				if (callbackContext != null) {
					PluginResult result = new PluginResult(PluginResult.Status.ERROR, "MipRobot("+mip.getBluetoothDevice().getAddress()+") is disconnected.");
				    result.setKeepCallback(false);
					callbackContext.sendPluginResult(result);
				}
			}
		});
		mip.connect(cordova.getActivity());

		PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
	    result.setKeepCallback(true);
		callbackContext.sendPluginResult(result);

//		MipRobotFinder.getInstance().mipRobotDidConnect(mip);
	}

	private boolean moveMip(final String mipId, final float[] vector)
	{
		return moveMip(getConnectedMipByProductId(mipId), vector);
	}

	private boolean moveMip(final MipRobot mip, final float[] vector)
	{
		Log.d("RobotsControlPlugin", "moveMip: " + mip + ", " + vector[0]+", "+vector[1]);

		if (mip != null)
		{
			cordova.getThreadPool().execute(new Runnable() {

				@Override
				public void run() {
					mip.mipDrive(vector);
				}
			});

			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean playMipSound(final String mipId, final int soundId)
	{
		MipRobot mip = getConnectedMipByProductId(mipId);
		if (mip != null)
		{
			if (soundId >= MipCommandValues.kMipSoundFile_ONEKHZ_500MS_8K16BIT
					&& soundId <= MipCommandValues.kMipSoundFile_FREESTYLE_TRACKING_2)
			{
				MipRobotSound sound = new MipRobotSound((byte)soundId);
				mip.mipPlaySound(sound);

				return true;
			}
		}

		return false;
	}

	private boolean chestRGBLedWithColor(String mipId, int color)
	{
		MipRobot mip = getConnectedMipByProductId(mipId);
		if (mip != null)
		{
			mip.setMipChestRGBLedWithColor((byte)Color.red(color), (byte)Color.green(color), (byte)Color.blue(color), (byte)1);
		}

		return false;
	}

	private boolean mipFalloverWithStyle(String mipId, int styleId)
	{
		MipRobot mip = getConnectedMipByProductId(mipId);
		if (mip != null)
		{
			if (styleId >= MipCommandValues.kMipPositionOnBack
					&& styleId <= MipCommandValues.kMipPositionBackWithKickstand)
			{
				mip.mipFalloverWithStyle((byte)styleId);

				return true;
			}
		}

		return false;
	}

	/*
	 * BroadcastReceiver for Mip Finder
	 */

	private final BroadcastReceiver mMipFinderBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            Log.d("RobotsControlPlugin", "mMipFinderBroadcastReceiver.onReceive - action: "+action);

            if (MipRobotFinder.MipRobotFinder_MipFound.equals(action)) {
            	BluetoothDevice device = (BluetoothDevice)(intent.getExtras().get("BluetoothDevice"));
                Log.d("RobotsControlPlugin", "found MiP: " + device.getName());

//                connectToAnyoneMip();
            }
            else if (MipRobotFinder.MipRobotFinder_MipListCleared.equals(action)) {
            }
            else if (MipRobotFinder.MipRobotFinder_BluetoothError.equals(action)) {
            }

        }
	};

	/*
	 * The following are interface for MipRobotInterface
	 */

	@Override
	public void mipDeviceReady(MipRobot sender) {
		Log.d("RobotsControlPlugin", sender.getName()+" - mipDeviceReady");
	}

	@Override
	public void mipDeviceDisconnected(MipRobot sender) {
		Log.d("RobotsControlPlugin", sender.getName()+" - mipDeviceDisconnected");
	}

	@Override
	public void mipRobotDidReceiveBatteryLevelReading(MipRobot mip, int value) {
		Log.d("RobotsControlPlugin", mip.getName()+" - mipRobotDidReceiveBatteryLevelReading: "+value);
	}

	@Override
	public void mipRobotDidReceivePosition(MipRobot mip, byte position) {
		Log.d("RobotsControlPlugin", mip.getName()+" - mipRobotDidReceivePosition: "+position);
	}

	@Override
	public void mipRobotDidReceiveHardwareVersion(int mipHardwareVersion, int mipVoiceFirmwareVersion) {
		Log.d("RobotsControlPlugin", "mipRobotDidReceiveHardwareVersion: mipHardwareVersion="+mipHardwareVersion+", mipVoiceFirmwareVersion="+mipVoiceFirmwareVersion);
	}

	@Override
	public void mipRobotDidReceiveSoftwareVersion(Date mipFirmwareVersionDate, int mipFirmwareVersionId) {
	}

	@Override
	public void mipRobotDidReceiveVolumeLevel(int mipVolume) {
		Log.d("RobotsControlPlugin", "mipRobotDidReceiveVolumeLevel: "+mipVolume);
	}

	@Override
	public void mipRobotDidReceiveIRCommand(ArrayList<Byte> irDataArray, int length) {
		String output = "";
		for (Byte b : irDataArray)
		{
			output += ","+b;
		}

		Log.d("RobotsControlPlugin", "mipRobotDidReceiveIRCommand: "+output.substring(1));
	}

	@Override
	public void mipRobotDidReceiveWeightReading(byte value, boolean leaningForward) {
		Log.d("RobotsControlPlugin", "mipRobotDidReceiveWeightReading: value="+value+", leaningForward="+leaningForward);
	}

	@Override
	public void mipRobotIsCurrentlyInBootloader(MipRobot mip, boolean isBootloader) {
		Log.d("RobotsControlPlugin", mip.getName()+" - mipRobotIsCurrentlyInBootloader: "+isBootloader);
	}

	//Handle Cordova Coming Actions
	@Override
	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException
	{
		Log.d("RobotsControlPlugin", "execute: action="+action+", arg="+args.toString());

		try {
			Class[] _classes = new Class[args.length()+1];
			Object[] _args = new Object[args.length()+1];

			for (int i=0; i<args.length(); i++)
			{
				_classes[i] = args.get(i).getClass();

				Log.d("RobotsControlPlugin", "_classes: "+_classes[i].toString());

				if (String.class.equals(_classes[i]))
				{
					_args[i] = args.getString(i);
				}
				else if (Integer.class.equals(_classes[i]))
				{
					_args[i] = args.getInt(i);
				}
				else if (Double.class.equals(_classes[i]))
				{
					_args[i] = args.getDouble(i);
				}
				else if (Boolean.class.equals(_classes[i]))
				{
					_args[i] = args.getBoolean(i);
				}
			}
			_classes[_classes.length-1] = CallbackContext.class;
			_args[_args.length-1] = callbackContext;

			Method method = this.getClass().getMethod(action, _classes);
			method.invoke(this, _args);

			return true;
		} catch (Exception e) {
			Log.d("RobotsControlPlugin", e.toString());

			e.printStackTrace();
		}

		return false;
	}

	/*
	 * The Following function will be exposed to cordova
	 */

	public void cordovaTest(CallbackContext callbackContext)
	{
		Log.d("RobotsControlPlugin", "cordovaTest");

		callbackContext.success();
	}

	public void cordovaTestMultiArguments(String msg, Integer value, CallbackContext callbackContext)
	{
		Log.d("RobotsControlPlugin", "cordovaTestMultiArguments: msg="+msg+", value="+value);

		callbackContext.success();
	}

	public void cordovaStartScanMips(CallbackContext callbackContext)
	{
		Log.d("RobotsControlPlugin", "cordovaStartScanMips");

		enforceToEnbleBT();

		stopScanMips();

		startScanMips();
	}

	public void cordovaStopScanMips(CallbackContext callbackContext)
	{
		Log.d("RobotsControlPlugin", "cordovaStopScanMips");

		stopScanMips();
	}

	public void cordovaGetMips(CallbackContext callbackContext)
	{
		Log.d("RobotsControlPlugin", "cordovaGetMips");

		List<MipRobot> mipList = MipRobotFinder.getInstance().getMipsFoundList();

		Log.d("RobotsControlPlugin", "cordovaGetMips: mipList.size()="+mipList.size());

		if (mipList.size() > 0) {
			JSONArray mips = new JSONArray();
			for(MipRobot mipRobot : mipList) {
				JSONObject mip = new JSONObject();
				try {
					mip.put("name", mipRobot.getName());
					mip.put("id", mipRobot.getBluetoothDevice().getAddress());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				mips.put(mip);
			}

			callbackContext.success(mips);
		}
	}

	public void cordovaConnectMip(String mipId, final CallbackContext callbackContext)
	{
		Log.d("RobotsControlPlugin", "cordovaConnectMip: mipId="+mipId);

		final MipRobot mip = getFoundMipById(mipId);
		if (mip != null)
		{
			connectToMip(mip, callbackContext);
		}
		else
		{
			callbackContext.error("MipRobot("+mipId+") is not found.");
		}
	}

	private void cordovaMoveMip(String mipId, float[] vector, CallbackContext callbackContext)
	{
		if (moveMip(mipId, vector))
		{
			callbackContext.success();
		}
		else
		{
			callbackContext.error("MipRobot("+mipId+") is not found.");
		}
	}

	public void cordovaMoveMip(String mipId, Integer x, Integer y, CallbackContext callbackContext)
	{
		Log.d("RobotsControlPlugin", "cordovaMoveMip: "+mipId+", "+x+", "+y);

		cordovaMoveMip(mipId, new float[] {x.floatValue(), y.floatValue()}, callbackContext);
	}

	public void cordovaMoveMip(String mipId, Double x, Double y, CallbackContext callbackContext)
	{
		Log.d("RobotsControlPlugin", "cordovaMoveMip: "+mipId+", "+x+", "+y);

		cordovaMoveMip(mipId, new float[] {x.floatValue(), y.floatValue()}, callbackContext);
	}

	public void cordovaMoveMip(String mipId, Integer x, Double y, CallbackContext callbackContext)
	{
		Log.d("RobotsControlPlugin", "cordovaMoveMip: "+mipId+", "+x+", "+y);

		cordovaMoveMip(mipId, new float[] {x.floatValue(), y.floatValue()}, callbackContext);
	}

	public void cordovaMoveMip(String mipId, Double x, Integer y, CallbackContext callbackContext)
	{
		Log.d("RobotsControlPlugin", "cordovaMoveMip: "+mipId+", "+x+", "+y);

		cordovaMoveMip(mipId, new float[] {x.floatValue(), y.floatValue()}, callbackContext);
	}

	public void cordovaPlaySound(String mipId, Integer soundId, CallbackContext callbackContext)
	{
		Log.d("RobotsControlPlugin", "cordovaPlaySound: "+mipId+", "+soundId);

		if (playMipSound(mipId, soundId))
		{
			callbackContext.success();
		}
		else
		{
			callbackContext.error("MipRobot("+mipId+") is not found or sound("+soundId+") is invalid");
		}
	}

	public void cordovaChestRGBLedWithColor(String mipId, Integer redColor, Integer greenColor, Integer blueColor, CallbackContext callbackContext)
	{
		Log.d("RobotsControlPlugin", "cordovaChestRGBLedWithColor: "+mipId+", "+redColor+", "+greenColor+", "+blueColor);

		if (chestRGBLedWithColor(mipId, Color.rgb(redColor, greenColor, blueColor)))
		{
			callbackContext.success();
		}
		else
		{
			callbackContext.error("MipRobot("+mipId+") is not found");
		}
	}

	public void cordovaFalloverWithStyle(String mipId, Integer styleId, CallbackContext callbackContext)
	{
		Log.d("RobotsControlPlugin", "cordovaFalloverWithStyle: "+mipId+", "+styleId);

		if (mipFalloverWithStyle(mipId, styleId))
		{
			callbackContext.success();
		}
		else
		{
			callbackContext.error("MipRobot("+mipId+") is not found or styleId("+styleId+") is invalid");
		}
	}
}
