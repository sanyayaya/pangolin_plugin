package com.tongyangsheng.pangolin;

import android.content.Context;

import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;
import io.flutter.plugin.platform.PlatformViewRegistry;

public class NativeAdViewFactory extends PlatformViewFactory {
    private final PlatformViewRegistry platformViewRegistry;
    private final BinaryMessenger platformViewMessenger;

    public NativeAdViewFactory(PlatformViewRegistry registrar, BinaryMessenger messenger) {
        super(StandardMessageCodec.INSTANCE);
        this.platformViewRegistry = registrar;
        this.platformViewMessenger = messenger;
    }

    @SuppressWarnings("unchecked")

    @Override
    public PlatformView create(Context context, int id, Object args) {
        Map<String, Object> params = (Map<String, Object>) args;
        return new NativeAdView(context, platformViewMessenger, id, params);
    }
}