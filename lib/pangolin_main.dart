import 'dart:async';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:pangolin/pangolin.dart';

const int NETWORK_STATE_MOBILE = 1;
const int NETWORK_STATE_2G = 2;
const int NETWORK_STATE_3G = 3;
const int NETWORK_STATE_WIFI = 4;
const int NETWORK_STATE_4G = 5;

typedef NativeADEventCallback = Function(NativeADEvent event, Map arguments);

MethodChannel _channel = MethodChannel('com.tongyangsheng.pangolin')
  ..setMethodCallHandler(_methodHandler);

StreamController<BasePangolinResponse> _pangolinResponseEventHandlerController =
    new StreamController.broadcast();

Stream<BasePangolinResponse> get pangolinResponseEventHandler =>
    _pangolinResponseEventHandlerController.stream;

Future<bool> registerPangolin({
  @required String appId,
  @required bool useTextureView,
  @required String appName,
  @required bool allowShowNotify,
  @required bool allowShowPageWhenScreenLock,
  @required bool debug,
  @required bool supportMultiProcess,
  List<int> directDownloadNetworkType,
}) async {
  return await _channel.invokeMethod("register", {
    "appId": appId,
    "useTextureView": useTextureView,
    "appName": appName,
    "allowShowNotify": allowShowNotify,
    "allowShowPageWhenScreenLock": allowShowPageWhenScreenLock,
    "debug": debug,
    "supportMultiProcess": supportMultiProcess,
    "directDownloadNetworkType": directDownloadNetworkType ??
        [
          NETWORK_STATE_MOBILE,
          NETWORK_STATE_3G,
          NETWORK_STATE_4G,
          NETWORK_STATE_WIFI
        ]
  });
}

Future<bool> loadSplashAd(
    {@required String mCodeId, @required bool debug}) async {
  return await _channel
      .invokeMethod("loadSplashAd", {"mCodeId": mCodeId, "debug": debug});
}

Future loadRewardAd({
  @required String mCodeId,
  @required bool debug,
  @required bool supportDeepLink,
  @required String rewardName,
  @required int rewardAmount,
  @required bool isExpress,
  double expressViewAcceptedSizeH,
  double expressViewAcceptedSizeW,
  @required userID,
  String mediaExtra,
  @required bool isHorizontal,
}) async {
  return await _channel.invokeMethod("loadRewardAd", {
    "mCodeId": mCodeId,
    "debug": debug,
    "supportDeepLink": supportDeepLink,
    "rewardName": rewardName,
    "rewardAmount": rewardAmount,
    "isExpress": isExpress,
    "expressViewAcceptedSizeH": expressViewAcceptedSizeH,
    "expressViewAcceptedSizeW": expressViewAcceptedSizeW,
    "userID": userID,
    "mediaExtra": mediaExtra,
    "isHorizontal": isHorizontal,
  });
}

Future _methodHandler(MethodCall methodCall) {
  var response =
      BasePangolinResponse.create(methodCall.method, methodCall.arguments);
  _pangolinResponseEventHandlerController.add(response);
  return Future.value();
}

class NativeAdWidget extends StatelessWidget {
  NativeAdWidget({Key key, @required this.posID, this.adEventCallback})
      : super(key: key);
  final String posID;
  final NativeADEventCallback adEventCallback;
  @override
  Widget build(BuildContext context) {
    final viewType = "com.tongyangsheng.pangolin/nativeAdView";
    if (Platform.isAndroid) {
      return AndroidView(
        viewType: viewType,
        onPlatformViewCreated: _onPlatformViewCreated,
        // creationParams: {'posID': widget.posID},
        creationParamsCodec: StandardMessageCodec(),
      );
    } else if (Platform.isIOS) {
      return UiKitView(
        viewType: viewType,
        onPlatformViewCreated: _onPlatformViewCreated,
        creationParamsCodec: StandardMessageCodec(),
      );
    } else {
      return Container();
    }
  }

  void _onPlatformViewCreated(int id) {
    MethodChannel _methodChannel;
    final String methodChannelName = 'pangolin_native_$id';
    // print('------------------------------');
    // print(methodChannelName);
    // print('------------------------------');
    _methodChannel = MethodChannel(methodChannelName);
    _methodChannel.setMethodCallHandler(_handleMethodCall);
    if (_methodChannel != null) {
      _methodChannel.invokeMethod('refresh', posID);
    }
  }

  Future<void> _handleMethodCall(MethodCall call) async {
    if (adEventCallback != null) {
      NativeADEvent event;
      switch (call.method) {
        case 'onLayoutChange':
          event = NativeADEvent.onLayoutChange;
          break;
        case 'onNoAD':
          event = NativeADEvent.onNoAD;
          break;
        case 'onADLoaded':
          event = NativeADEvent.onADLoaded;
          break;
        case 'onRenderFail':
          event = NativeADEvent.onRenderFail;
          break;
        case 'onRenderSuccess':
          event = NativeADEvent.onRenderSuccess;
          break;
        case 'onADExposure':
          event = NativeADEvent.onADExposure;
          break;
        case 'onADClicked':
          event = NativeADEvent.onADClicked;
          break;
        case 'onADClosed':
          event = NativeADEvent.onADClosed;
          break;
        case 'onADLeftApplication':
          event = NativeADEvent.onADLeftApplication;
          break;
        case 'onADOpenOverlay':
          event = NativeADEvent.onADOpenOverlay;
          break;
        case 'onADCloseOverlay':
          event = NativeADEvent.onADCloseOverlay;
          break;
      }
      adEventCallback(event, call.arguments);
    }
  }
}

class NativeAdWidgetView extends StatefulWidget {
  const NativeAdWidgetView(
      {Key key,
      @required this.posID,
      this.decoration,
      this.ratio = 0.85,
      this.nativeAdheight})
      : super(key: key);
  final String posID;
  final Decoration decoration;
  final double nativeAdheight;
  final double ratio;
  @override
  _NativeAdWidgetViewState createState() => _NativeAdWidgetViewState();
}

class _NativeAdWidgetViewState extends State<NativeAdWidgetView> {
  double height = 2;
  @override
  Widget build(BuildContext context) {
    final successHeight = widget.nativeAdheight != null
        ? widget.nativeAdheight
        : MediaQuery.of(context).size.width * widget.ratio;
    return Container(
      decoration: widget.decoration,
      height: height,
      child: NativeAdWidget(
          posID: widget.posID,
          adEventCallback: (event, arguments) {
            if (event == NativeADEvent.onRenderSuccess) {
              setState(() {
                height = successHeight;
              });
            }
          }),
    );
  }
}

class NativeAdFixedView extends StatelessWidget {
  const NativeAdFixedView(
      {@required this.posID,
      this.decoration,
      this.ratio = 0.85,
      this.padding,
      this.margin});
  final String posID;
  final double ratio;
  final Decoration decoration;
  final EdgeInsetsGeometry padding;
  final EdgeInsetsGeometry margin;
  @override
  Widget build(BuildContext context) {
    final height = MediaQuery.of(context).size.width * ratio;
    return Container(
        padding: padding,
        margin: margin,
        decoration: decoration,
        height: height,
        child: NativeAdWidget(posID: posID));
  }
}

enum NativeADEvent {
  onLayoutChange,
  onNoAD,
  onADLoaded,
  onRenderFail,
  onRenderSuccess,
  onADExposure,
  onADClicked,
  onADClosed,
  onADLeftApplication,
  onADOpenOverlay,
  onADCloseOverlay,
}
