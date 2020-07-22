package com.tongyangsheng.pangolin;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.platform.PlatformView;

public class NativeAdView implements PlatformView, MethodCallHandler {
    private String posID;
    private TTAdNative mTTAdNative;
    private TTNativeExpressAd ttNativeExpressAd;
    private FrameLayout nativeViewContainer;

    private MethodChannel methodChannel;
    private String methodChannelName;

    private Context context;

    NativeAdView(Context context, BinaryMessenger messenger, int id, Map<String, Object> o) {
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        mTTAdNative = ttAdManager.createAdNative(context);

        this.context = context;
        methodChannelName = "pangolin_native_" + id;
        methodChannel = new MethodChannel(messenger, methodChannelName);
        methodChannel.setMethodCallHandler(this);

        nativeViewContainer = new FrameLayout(context);
        nativeViewContainer.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public View getView() {
        return nativeViewContainer;
    }

    @Override
    public void dispose() {
//        methodChannel.setMethodCallHandler(null);
        if (ttNativeExpressAd != null) {
            //调用destroy()方法释放
            ttNativeExpressAd.destroy();
        }
    }

    public void loadNativeAd () {
        float expressViewWidth = UIUtils.getScreenWidthDp(this.context);
        float expressViewHeight = (float) (expressViewWidth * 0.86);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(posID) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth,expressViewHeight) //必填：期望个性化模板广告view的size,单位dp
                .setImageAcceptedSize(640,320) //这个参数设置即可，不影响个性化模板广告的size
                .build();
        mTTAdNative.loadNativeExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                nativeViewContainer.removeAllViews();
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0){
                    return;
                }
                ttNativeExpressAd = ads.get(0);
                bindAdListener(ttNativeExpressAd);
                ttNativeExpressAd.render();//调用render开始渲染广告
            }
        });
    }
    private void bindAdListener(TTNativeExpressAd ad){
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int i) {
//                methodChannel.invokeMethod("onAdClicked", null);
            }

            @Override
            public void onAdShow(View view, int i) {
//                methodChannel.invokeMethod("onAdShow", null);
            }

            @Override
            public void onRenderFail(View view, String s, int i) {
//                methodChannel.invokeMethod("onRenderFail", null);
            }

            @Override
            public void onRenderSuccess(View view, float v, float v1) {
                nativeViewContainer.removeAllViews();
                nativeViewContainer.addView(view);
//                methodChannel.invokeMethod("onRenderSuccess", null);
            }
        });
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        if (call.method.equals("refresh")) {
            posID = call.arguments.toString();
            loadNativeAd();
            result.success(true);
        } else {
            result.notImplemented();
        }
    }
}