
package be.smalltownheroes.adobe.analytics;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Callable;

import android.content.Context;
import android.util.Log;
import android.app.Activity;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.LifecycleEventListener;

import com.adobe.mobile.Config;
import com.adobe.mobile.Analytics;
import com.adobe.mobile.MediaSettings;
import com.adobe.mobile.Media;
import com.adobe.mobile.MediaState;
import com.adobe.mobile.Visitor;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import be.smalltownheroes.adobe.analytics.util.MapUtil;

public class RNAdobeAnalyticsModule extends ReactContextBaseJavaModule {

	private final ReactApplicationContext reactContext;
	private final Activity activity = getCurrentActivity();

	private final LifecycleEventListener mLifecycleEventListener = new LifecycleEventListener() {
		@Override
		public void onHostResume() {
			Log.d("RESUME", "LIFECYCLE");

			final Activity currentActivity = getCurrentActivity();

			final Callable<String> task = new Callable<String>(){
				@Override
				public String call() throws Exception {
					AdvertisingIdClient.Info idInfo;
					String adid = null;
					Context appContext = currentActivity.getApplicationContext();
					try {
						idInfo = AdvertisingIdClient.getAdvertisingIdInfo(appContext);
						if (idInfo != null) {
							adid = idInfo.getId();
						}
					} catch  (Exception ex) {
						Log.e("Error",  ex.getLocalizedMessage());
					}
					return  adid;
				}
			};
			Config.submitAdvertisingIdentifierTask(task);
			Config.collectLifecycleData(currentActivity);
		}

		@Override
		public void onHostPause() {
			Config.pauseCollectingLifecycleData();
		}

		@Override
		public void onHostDestroy() {

		}
	};

	public RNAdobeAnalyticsModule(ReactApplicationContext reactContext) {
		super(reactContext);
		this.reactContext = reactContext;
		Config.setContext(this.reactContext.getApplicationContext());
		reactContext.addLifecycleEventListener(mLifecycleEventListener);
	}

	@Override
	public String getName() {
		return "RNAdobeAnalytics";
	}

	@ReactMethod
	public void init(ReadableMap options) {
		Config.setDebugLogging(options.getBoolean("debug"));
	}

	@ReactMethod
	public void trackState(String state, ReadableMap contextData) {
		Map<String, Object> contextMap = convertReadableMapToHashMap(contextData);
		Log.i("RN-adobe-analytics", "####### trackState ####### " + state);
		Analytics.trackState(state, contextMap);
	}

	@ReactMethod
	public void trackAction(String action, ReadableMap contextData) {
		Map<String, Object> contextMap = convertReadableMapToHashMap(contextData);
		Log.i("RN-adobe-analytics", "####### trackAction ####### " + action);
		Analytics.trackAction(action, contextMap);
	}

	@ReactMethod
	public void trackVideo(String action, ReadableMap settings) {
		Log.i("RN-adobe-analytics", "####### trackVideo ####### " + action);
		switch (action) {
			case "open": {
				String name = settings.getString("name");
				Double length = settings.getDouble("length");
				String playerName = settings.getString("playerName");
				String playerId = settings.getString("playerId");
				final MediaSettings mediaSettings = Media.settingsWith(name, length, playerName, playerId);
				Media.open(mediaSettings, null);
				break;
			}
			case "close": {
				String name = settings.getString("name");
				Media.close(name);
				break;
			}
			case "play": {
				String name = settings.getString("name");
				Double offset = settings.getDouble("offset");
				Media.play(name, offset);
				break;
			}
			case "stop": {
				String name = settings.getString("name");
				Double offset = settings.getDouble("offset");
				Media.stop(name, offset);
				break;
			}
			case "complete": {
				String name = settings.getString("name");
				Double offset = settings.getDouble("offset");
				Media.complete(name, offset);
				break;
			}
			case "track": {
				String name = settings.getString("name");
				Map<String, Object> contextMap = convertReadableMapToHashMap(settings);
				Media.track(name, contextMap);
				break;
			}
			default: {
				Log.w("RN-adobe-analytics", "Unknown video track action:" + action);
				break;
			}
		}
	}
	
	@ReactMethod
	public void getVisitorID(Callback successCb) {
		WritableMap visitorMap = new WritableNativeMap();
		visitorMap.putString("MCID", Visitor.getMarketingCloudId());
		successCb.invoke(visitorMap);
	}

	private Map<String, Object> convertReadableMapToHashMap(ReadableMap readableMap) {
		return MapUtil.toMap(readableMap);
	}

}
