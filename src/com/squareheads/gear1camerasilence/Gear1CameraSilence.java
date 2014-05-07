package com.squareheads.gear1camerasilence;



import java.lang.reflect.Method;
import java.util.ArrayList;

import android.content.Context;
import android.media.SoundPool;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedHelpers;


public class Gear1CameraSilence implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {
	public static final String MY_PACKAGE_NAME = Gear1CameraSilence.class.getPackage().getName();
	
	private static XSharedPreferences pref;
	private boolean logEnabled = false;
	private ArrayList<Integer> m_BlockedSounds;
	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		if(!lpparam.packageName.equals("com.sec.android.app.camera")) {
			return;
		}
		
		//hook:
		//.class public Lcom/sec/android/app/camera/CommonEngine;
		//Lcom/sec/android/app/camera/CommonEngine;->initCameraSound()
		//after method, get mCameraSoundPool, hook playsound
		XposedHelpers.findAndHookMethod("com.sec.android.app.camera.CommonEngine", 
				lpparam.classLoader,
				"initCameraSound", 
				new XC_MethodHook() {

				
					@Override
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
							Log("initCameraSound beforeHookedMethod ...");
							//SoundPool cameraSoundPool = (SoundPool) XposedHelpers.getObjectField(param.thisObject, "mCameraSoundPool");
							
							hookLoadMethod();
							
					}
					
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						hookPlayMethod();
					}
			
		});
	}
	
	public void hookLoadMethod() {
		Log("Hook load method...");
		Method soundPoolLoadSoundMethod = XposedHelpers.findMethodExact(SoundPool.class, "load", 
				Context.class, //context
				int.class, //resId
				int.class //priority
				);
		
		 XposedBridge.hookMethod(soundPoolLoadSoundMethod, new  XC_MethodHook() {
			 @Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				 
				int resId = (Integer)param.args[1];
				int soundIdReturned = (Integer)param.getResult();
				Log("Camera soundpool loading sound from resId " + resId);

				 //Autofocus
				 if(optionEnabled(SettingsActivity.Pref_Disable_Sound_Autofocus, true)) {
					 if(resId == 0x7f050000) {
						 m_BlockedSounds.add(soundIdReturned);
						 Log("adding blocked sound from resId " + resId + " with soundid " + soundIdReturned);
					 }
					 else if(resId == 0x7f050001) {
						 m_BlockedSounds.add(soundIdReturned);
						 Log("adding blocked sound from resId " + resId + " with soundid " + soundIdReturned);
					 }
				 }
			}
		});
	}
	
	public void hookPlayMethod() {
		Log("Hook play sound...");
		Method soundPoolPlaySoundMethod = XposedHelpers.findMethodExact(SoundPool.class, "play", 
				int.class, //soundId
				float.class, //leftVolume
				float.class, //rightVolume
				int.class, //priority
				int.class, //loop
				float.class //rate
				);
		
		 XposedBridge.hookMethod(soundPoolPlaySoundMethod, new  XC_MethodHook() {
			 @Override
			protected void beforeHookedMethod(
					MethodHookParam param) throws Throwable {
				 Log("Play sound called");
				 int soundId = (Integer)param.args[0];
				 if (m_BlockedSounds.contains(soundId)) {
					 Log("Ignoring sound play for soundId " + soundId);
					 param.setResult(0);				 
				 }
				 else {
					 Log("Not blocking sound for soundId " + soundId);
				 }

			 }
		});
	}
	public void Log(String s) {
		if(logEnabled) {
			XposedBridge.log(s);
		}
	}
		
	
	public void Log(Throwable t) {
		if(logEnabled) {
			XposedBridge.log(t);
		
		}
	}
	private boolean optionEnabled(String option, boolean defVal) {
		//return defVal;
		//TODO: once settings page done
		return pref.getBoolean(option, defVal);
	}
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		//Initialise the prefs object
		pref = new XSharedPreferences(MY_PACKAGE_NAME);
		m_BlockedSounds = new ArrayList<Integer>();
		logEnabled = optionEnabled(SettingsActivity.Pref_Log_Enabled, true);
		
		//https://android.googlesource.com/platform/frameworks/base/+/android-4.4.2_r1/media/java/android/media/MediaActionSound.java
		/*
		 * looks like android uses soundpool for camera sounds
		 * hook all soundpools and log them with stack trace to determine where the camera sounds come from
		 */
		XposedHelpers.findAndHookMethod("android.media.SoundPool", 
				null, 
				"load", 
				String.class,
				int.class, 
				
				new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				Log("android.media.SoundPool.Load(S,I) called with path " + param.args[0]);
			}
		});
		
		
		XposedHelpers.findAndHookMethod("android.media.SoundPool", 
				null, 
				"play", 
				int.class, //soundId
				float.class, //leftVolume
				float.class, //rightVolume
				int.class, //priority
				int.class, //loop
				float.class, //rate
				new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				Log("android.media.SoundPool.Play called with soundid " + param.args[0]);
				Log(stackTraceToString(new Exception()));
			}
		});	
	}
	
	public static String stackTraceToString(Throwable e) {
	    StringBuilder sb = new StringBuilder();
	    for (StackTraceElement element : e.getStackTrace()) {
	        sb.append(element.toString());
	        sb.append("\n");
	    }
	    return sb.toString();
	}
	
	
	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
	    // replacements only for SystemUI
	    //if (!resparam.packageName.equals("com.sec.android.app.camera"))
	    //    return;
	    //0x7f050000
	    //0x7f050001
	    //resparam.res.getIdentifier(name, defType, defPackage)
	    // different ways to specify the resources to be replaced
	    //resparam.res.setReplacement(0x7f080083, "YEAH!"); // WLAN toggle text. You should not do this because the id is not fixed. Only for framework resources, you could use android.R.string.something
	    //resparam.res.setReplacement("com.android.systemui:string/quickpanel_bluetooth_text", "WOO!");
	    //resparam.res.setReplacement("com.android.systemui", "string", "quickpanel_gps_text", "HOO!");
	    //resparam.res.setReplacement("com.android.systemui", "integer", "config_maxLevelOfSignalStrengthIndicator", 6);
	}
}
		
		


