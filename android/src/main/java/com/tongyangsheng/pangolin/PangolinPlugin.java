package com.tongyangsheng.pangolin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.platform.PlatformViewRegistry;


/** PangolinPlugin */
public class PangolinPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

    private MethodChannel methodChannel;
    private Context applicationContext;
    private Activity activity;


  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
      onAttachedToEngine(flutterPluginBinding.getApplicationContext(), flutterPluginBinding.getBinaryMessenger(), flutterPluginBinding.getPlatformViewRegistry());
  }

    private void onAttachedToEngine(Context applicationContext , BinaryMessenger messenger, PlatformViewRegistry platformViewRegistry) {
        this.applicationContext = applicationContext;
        methodChannel = new MethodChannel(messenger, "com.tongyangsheng.pangolin");
        methodChannel.setMethodCallHandler(this);
        platformViewRegistry.registerViewFactory("com.tongyangsheng.pangolin/nativeAdView", new NativeAdViewFactory(platformViewRegistry, messenger));
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }
  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "com.tongyangsheng.pangolin");
    channel.setMethodCallHandler(new PangolinPlugin());
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("register")) {
      String appId = call.argument("appId");
      Boolean useTextureView = call.argument("useTextureView");
      String appName = call.argument("appName");
      Boolean allowShowNotify = call.argument("allowShowNotify");
      Boolean allowShowPageWhenScreenLock = call.argument("allowShowPageWhenScreenLock");
      Boolean debug = call.argument("debug");
      Boolean supportMultiProcess = call.argument("supportMultiProcess");
      List<Integer> directDownloadNetworkType = call.argument("directDownloadNetworkType");
      if(useTextureView == null)
      {
        useTextureView = false;
      }
      if (allowShowNotify == null)
      {
        allowShowNotify = true;
      }
      if (allowShowPageWhenScreenLock == null)
      {
        allowShowPageWhenScreenLock = true;
      }
      if (debug == null)
      {
        debug = true;
      }
      if (supportMultiProcess == null)
      {
        supportMultiProcess = false;
      }
      if (appId == null || appId.trim().isEmpty())
      {
        result.error("500","appId can't be null",null);
      }
      else {
        if (appName == null || appName.trim().isEmpty()) {
          result.error("600", "appName can't be null", null);
        } else {
          TTAdManagerHolder.init(applicationContext, appId, useTextureView, appName, allowShowNotify, allowShowPageWhenScreenLock, debug, supportMultiProcess,directDownloadNetworkType);
          result.success(true);
        }
      }
    }
    else if(call.method.equals("loadSplashAd"))
    {
      String mCodeId = call.argument("mCodeId");
      Boolean deBug = call.argument("debug");
      Intent intent = new Intent();
      intent.setClass(activity,SplashActivity.class);
      intent.putExtra("mCodeId",mCodeId);
      intent.putExtra("debug",deBug);
      activity.startActivity(intent);
    }
    else if (call.method.equals("loadRewardAd"))
    {
      Boolean isHorizontal = call.argument("isHorizontal");
      String mCodeId = call.argument("mCodeId");
      Boolean debug = call.argument("debug");
      Boolean supportDeepLink = call.argument("supportDeepLink");
      String rewardName = call.argument("rewardName");
      int rewardAmount = (int)call.argument("rewardAmount");
      Boolean isExpress = call.argument("isExpress");

      double expressViewAcceptedSizeH;
      if (call.argument("expressViewAcceptedSizeH") == null)
      {
        expressViewAcceptedSizeH = 500;
      }
      else
      {
        expressViewAcceptedSizeH = call.argument("expressViewAcceptedSizeH");
      }
      double expressViewAcceptedSizeW;
      if (call.argument("expressViewAcceptedSizeW") == null)
      {
        expressViewAcceptedSizeW = 500;
      }
      else
      {
        expressViewAcceptedSizeW = call.argument("expressViewAcceptedSizeW");
      }

      String userID = call.argument("userID");
      String mediaExtra;
      if (call.argument("mediaExtra") == null)
      {
        mediaExtra = "media_extra";
      }
      else
      {
        mediaExtra = call.argument("mediaExtra");
      }

      RewardVideo rewardVideo = new RewardVideo();
      RewardVideo._channel = methodChannel;
      rewardVideo.activity = activity;
      rewardVideo.context = applicationContext;
      if (isHorizontal)
      {
        rewardVideo.mHorizontalCodeId = mCodeId;
      }
      else
      {
        rewardVideo.mVerticalCodeId = mCodeId;
      }

      if (debug != null)
      {
        rewardVideo.debug = debug;
      }
      else
      {
        rewardVideo.debug = false;
      }

      rewardVideo.supportDeepLink = supportDeepLink;
      rewardVideo.expressViewAcceptedSizeH = expressViewAcceptedSizeH;
      rewardVideo.expressViewAcceptedSizeW = expressViewAcceptedSizeW;
      rewardVideo.rewardName = rewardName;
      rewardVideo.rewardAmount = rewardAmount;
      rewardVideo.userID = userID;
      rewardVideo.mediaExtra = mediaExtra;
      rewardVideo.init();

//      if (isHorizontal == null)
//      {
//        result.error("600","isHorizonal can not be null",null);
//      }
//      else
//      {
//        RewardVideoActivity._channel = methodChannel;
//        if(isHorizontal)
//        {
//          Intent intent = new Intent();
//          intent.setClass(activity, RewardVideoActivity.class);
//          intent.putExtra("horizontal_rit",mCodeId);
//          intent.putExtra("debug",debug);
//          intent.putExtra("supportDeepLink", supportDeepLink);
//          intent.putExtra("rewardName",rewardName);
//          intent.putExtra("rewardAmount",rewardAmount);
//          intent.putExtra("isExpress",isExpress);
//          intent.putExtra("expressViewAcceptedSizeH",expressViewAcceptedSizeH);
//          intent.putExtra("expressViewAcceptedSizeW",expressViewAcceptedSizeW);
//          intent.putExtra("userID",userID);
//          intent.putExtra("mediaExtra",mediaExtra);
//          activity.startActivity(intent);
//        }
//        else
//        {
//          Intent intent = new Intent();
//          intent.setClass(activity, RewardVideoActivity.class);
//          intent.putExtra("vertical_rit",mCodeId);
//          intent.putExtra("debug",debug);
//          intent.putExtra("supportDeepLink", supportDeepLink);
//          intent.putExtra("rewardName",rewardName);
//          intent.putExtra("rewardAmount",rewardAmount);
//          intent.putExtra("expressViewAcceptedSizeH",expressViewAcceptedSizeH);
//          intent.putExtra("expressViewAcceptedSizeW",expressViewAcceptedSizeW);
//          intent.putExtra("userID",userID);
//          intent.putExtra("mediaExtra",mediaExtra);
//          activity.startActivity(intent);
//        }
//      }
    }
    else
      {
      result.notImplemented();
    }
  }
}
