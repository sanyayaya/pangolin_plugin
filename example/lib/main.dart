import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'dart:async';

import 'package:pangolin/pangolin.dart' as Pangolin;

void main() => runApp(MyApp());

typedef void NativeViewCreatedCallback();

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  bool showNativeAdView = false;
  NativeViewCreatedCallback nativeViewCreatedCallback;

  @override
  void initState() {
    Pangolin.pangolinResponseEventHandler.listen((value) {
      if (value is Pangolin.onRewardResponse) {
        print("激励视频回调：${value.rewardVerify}");
        print("激励视频回调：${value.rewardName}");
        print("激励视频回调：${value.rewardAmount}");
      } else {
        print("回调类型不符合");
      }
    });
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;

    Map<Permission, PermissionStatus> statuses = await [
      Permission.phone,
      Permission.location,
      Permission.storage,
    ].request();
    //校验权限
    if (statuses[Permission.location] != PermissionStatus.granted) {
      print("无位置权限");
    }
    _initPangolin();
    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  _initPangolin() async {
    await Pangolin.registerPangolin(
            appId: "5085233",
            useTextureView: true,
            appName: "wygx",
            allowShowNotify: true,
            allowShowPageWhenScreenLock: true,
            debug: false,
            supportMultiProcess: true)
        .then((v) {
      // _loadSplashAd();
      setState(() {
        showNativeAdView = true;
      });
    });
  }

  _loadSplashAd() async {
    Pangolin.loadSplashAd(mCodeId: "887344275", debug: false);
  }

  //945122969
  _loadRewardAd() async {
    await Pangolin.loadRewardAd(
        isHorizontal: false,
        debug: false,
        mCodeId: "945298709",
        supportDeepLink: true,
        rewardName: "书币",
        rewardAmount: 3,
        isExpress: true,
        expressViewAcceptedSizeH: 500,
        expressViewAcceptedSizeW: 500,
        userID: "user123",
        mediaExtra: "media_extra");
  }

  @override
  Widget build(BuildContext context) {
    Future<void> _onPlatformViewCreated(id) async {
      // if (widget.) {

      // }
    }
    return MaterialApp(
      showPerformanceOverlay: true,
      checkerboardOffscreenLayers: true, // 使用了saveLayer的图形会显示为棋盘格式并随着页面刷新而闪烁
      checkerboardRasterCacheImages: true,
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Center(
              child: Visibility(
            visible: showNativeAdView,
            child: ListView.builder(
                itemCount: 20,
                cacheExtent: 600,
                itemBuilder: (ctx, index) {
                  return Visibility(
                    visible: index % 3 == 0,
                    child: Pangolin.NativeAdFixedView(
                      posID: "945298711",
                    ),
                    replacement: Container(
                      color: Colors.green,
                      height: 300,
                    ),
                  );
                }),
            replacement: Container(),
          )
              //     Column(
              //   children: <Widget>[
              //     Visibility(
              //       visible: showNativeAdView,
              //       child: Pangolin.NativeAdWidgetView(
              //         posID: "945298711",
              //         decoration: BoxDecoration(
              //             border: Border(
              //                 bottom: BorderSide(width: 1, color: Colors.red))),
              //       ),
              //       replacement: Container(),
              //     ),
              //     Visibility(
              //       visible: showNativeAdView,
              //       child: Pangolin.NativeAdFixedView(posID: "945298711",),
              //       replacement: Container(),
              //     ),
              //     FlatButton(
              //       onPressed: () {
              //         _loadRewardAd();
              //       },
              //       child: Text("Pangolin"),
              //     ),
              //   ],
              // )
              ),
        ),
      ),
    );
  }
}
