#import <Cordova/CDV.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface RobotsControlPlugin : CDVPlugin
{
    NSString* connectMipCallback;
}

- (void)cordovaTest:(CDVInvokedUrlCommand *)command;
- (void)cordovaTestMultiArguments:(CDVInvokedUrlCommand *)command;
- (void)cordovaStartScanMips:(CDVInvokedUrlCommand *)command;
- (void)cordovaStopScanMips:(CDVInvokedUrlCommand *)command;
- (void)cordovaGetMips:(CDVInvokedUrlCommand *)command;
- (void)cordovaConnectMip:(CDVInvokedUrlCommand *)command;
- (void)cordovaMoveMip:(CDVInvokedUrlCommand *)command;
- (void)cordovaPlaySound:(CDVInvokedUrlCommand *)command;
- (void)cordovaChestRGBLedWithColor:(CDVInvokedUrlCommand *)command;
- (void)cordovaFalloverWithStyle:(CDVInvokedUrlCommand *)command;

@end