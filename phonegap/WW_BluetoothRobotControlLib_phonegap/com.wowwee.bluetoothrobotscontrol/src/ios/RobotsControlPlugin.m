#import "RobotsControlPlugin.h"

@import WowWeeMiPSDK;

@interface RobotsControlPlugin () <MipRobotDelegate>
@end

@implementation RobotsControlPlugin

//Code for RobotsControlPlugin
- (void)cordovaTest:(CDVInvokedUrlCommand *)command
{
    NSLog(@"cordovaTest");
    
    [self justSendOkPluginResult: command.callbackId];
}

- (void)cordovaTestMultiArguments:(CDVInvokedUrlCommand *)command
{
    NSString* msg = [command argumentAtIndex:0];
    NSNumber* val = [command argumentAtIndex:1];
    
    NSLog(@"cordovaTestMultiArguments: msg=%@, value=%@", msg, val);
    
    [self justSendOkPluginResult: command.callbackId];
}

- (void)cordovaStartScanMips:(CDVInvokedUrlCommand *)command
{
    NSLog(@"startScanMips");
    
    [self removeMipNotification];
    
    [[MipRobotFinder sharedInstance] clearFoundMipList];
    
    [self addMipNotifcation];
    
    [[MipRobotFinder sharedInstance] scanForMips];
    
    [self justSendOkPluginResult: command.callbackId];
}

- (void)cordovaStopScanMips:(CDVInvokedUrlCommand *)command
{
    NSLog(@"stopScanMips");
    
    [self removeMipNotification];
    
    [self justSendOkPluginResult: command.callbackId];
}

- (void)cordovaGetMips:(CDVInvokedUrlCommand *)command
{
    int mipsFoundCount = [[MipRobotFinder sharedInstance].mipsFound count];
    
    NSLog(@"getMips: mipsFound.count=%d", mipsFoundCount);
    
    if (mipsFoundCount > 0)
    {
        NSMutableArray* mips = [NSMutableArray arrayWithCapacity: mipsFoundCount];
        
        for (MipRobot* mip in [MipRobotFinder sharedInstance].mipsFound)
        {
            NSString* mipName = mip.name;
            NSString* mipId = [NSString stringWithFormat:@"%d", [self getMipId:mip]];
            
            NSLog(@"> mipId: %@", mipId);
            
            [mips addObject:[NSMutableDictionary dictionaryWithObjects:[NSArray arrayWithObjects:mipName, mipId, nil] forKeys:[NSArray arrayWithObjects:@"name", @"id", nil]]];
        }
        
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:mips];
        [pluginResult setKeepCallbackAsBool:false];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void)cordovaConnectMip:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* mipId = [command argumentAtIndex:0];
    
    NSLog(@"connectMip: mipId=%@", mipId);
    
    MipRobot* foundMip = [self getMipByFoundList: mipId];
    if (foundMip != nil)
    {
        connectMipCallback = command.callbackId;
        
        foundMip.delegate = self;
        
        [foundMip connect];
        
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
        [pluginResult setKeepCallbackAsBool:true];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:connectMipCallback];
    }
}

- (void)cordovaMoveMip:(CDVInvokedUrlCommand *)command
{
    NSString* mipId = [command argumentAtIndex:0];
    NSNumber* x = [command argumentAtIndex:1];
    NSNumber* y = [command argumentAtIndex:2];
    
    NSLog(@"moveMip: mipId=%@, x=%@, y=%@", mipId, x, y);
    
    MipRobot* mip = [self getMipByFoundList:mipId];
    
    if (mip != nil)
    {
        [mip mipDrive:CGVectorMake(x.floatValue, y.floatValue)];
        
        [self justSendOkPluginResult:command.callbackId];
    }
    else
    {
        [self justSendErrorPluginResult:command.callbackId WithMsg:[NSString stringWithFormat:@"MipRobot(%@) is not found", mipId]];
    }
}

- (void)cordovaPlaySound:(CDVInvokedUrlCommand *)command
{
    NSString* mipId = [command argumentAtIndex:0];
    NSNumber* soundId = [command argumentAtIndex:1];
    
    NSLog(@"playSound: mipId=%@, soundId=%@", mipId, soundId);
    
    MipRobot* mip = [self getMipByFoundList:mipId];
    MipRobotSound* mipSound = [self getMipRobotSound: soundId.intValue];
    
    if (mip != nil && mipSound != nil)
    {
        [mip mipPlaySound:mipSound];
        
        [self justSendOkPluginResult:command.callbackId];
    }
    else
    {
        [self justSendErrorPluginResult:command.callbackId WithMsg:[NSString stringWithFormat:@"MipRobot(%@) is not found or sound(%@) is invalid", mipId, soundId]];
    }
}

- (void)cordovaChestRGBLedWithColor:(CDVInvokedUrlCommand *)command
{
    NSString* mipId = [command argumentAtIndex:0];
    NSNumber* red = [command argumentAtIndex:1];
    NSNumber* green = [command argumentAtIndex:2];
    NSNumber* blue = [command argumentAtIndex:3];
    
    NSLog(@"chestRGBLedWithColor: mipId=%@, red=%@, green=%@, blue=%@", mipId, red, green, blue);
    
    MipRobot* mip = [self getMipByFoundList:mipId];
    
    if (mip != nil)
    {
        UIColor* color = [UIColor colorWithRed:red.intValue/255.0f green:green.intValue/255.0f blue:blue.intValue/255.0f alpha:1];
        [mip setMipChestRGBLedWithColor: color];
        
        [self justSendOkPluginResult:command.callbackId];
    }
    else
    {
        [self justSendErrorPluginResult:command.callbackId WithMsg:[NSString stringWithFormat:@"MipRobot(%@) is not found", mipId]];
    }
}

- (void)cordovaFalloverWithStyle:(CDVInvokedUrlCommand *)command
{
    NSString* mipId = [command argumentAtIndex:0];
    NSNumber* styleId = [command argumentAtIndex:1];
    
    NSLog(@"falloverWithStyle: mipId=%@, styleId=%@", mipId, styleId);
    
    MipRobot* mip = [self getMipByFoundList:mipId];
    
    if (mip != nil)
    {
        [mip mipFalloverWithStyle: (kMipPositionValue)styleId.intValue];
        
        [self justSendOkPluginResult:command.callbackId];
    }
    else
    {
        [self justSendErrorPluginResult:command.callbackId WithMsg:[NSString stringWithFormat:@"MipRobot(%@) is not found", mipId]];
    }
}

//Private Function
- (void) justSendOkPluginResult:(NSString*)callbackId
{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [pluginResult setKeepCallbackAsBool:false];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (void) justSendErrorPluginResult:(NSString*)callbackId WithMsg:(NSString*)msg
{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:msg];
    [pluginResult setKeepCallbackAsBool:false];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (int) getMipId:(MipRobot*)mip
{
    //use its pointer address as mipId....
    return &(*mip);
}

- (MipRobot*) getMipByFoundList:(NSString*) _mipId
{
    int mipId = _mipId.intValue;
    
    for (MipRobot* mip in [MipRobotFinder sharedInstance].mipsFound)
    {
        if ([self getMipId:mip] == mipId)
        {
            return mip;
        }
    }
    
    return nil;
}

- (MipRobot*) getMipByConnectedList:(NSString*) _mipId
{
    int mipId = _mipId.intValue;
    
    for (MipRobot* mip in [MipRobotFinder sharedInstance].mipsConnected)
    {
        if ([self getMipId:mip] == mipId)
        {
            return mip;
        }
    }
    
    return nil;
}

- (MipRobotSound*) getMipRobotSound:(int) soundId
{
    @try
    {
        kMipSoundFileValue _soundId = (kMipSoundFileValue)soundId;
        
        return [MipRobotSound mipRobotSoundWithFile:_soundId];
    }
    @catch (NSException* e)
    {
        return nil;
    }
}

//Notification
- (void)addMipNotifcation
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(mipFoundNotification:) name:MipRobotFinderNotificationID object:nil];
}

- (void)removeMipNotification
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:MipRobotFinderNotificationID object:nil];
}

- (void)mipFoundNotification:(NSNotification *)note
{
    NSDictionary *noteDict = note.userInfo;
    if (!noteDict || !noteDict[@"code"]) {
        return;
    }
    MipRobotFinderNote noteType = (MipRobotFinderNote)[noteDict[@"code"] integerValue];
    
    if (noteType == MipRobotFinderNote_MipFound) {
        MipRobot *mip = noteDict[@"data"];
        // Normally you might want to add this object to an array and use a UITableView to display all the found devices for the user to select. For now we just want to print this MiP to the console
        NSLog(@"Found: %@", mip);
    } else if (noteType == MipRobotFinderNote_BluetoothError) {
        CBCentralManagerState errorCode = (CBCentralManagerState)[noteDict[@"data"] integerValue];
        if (errorCode == CBCentralManagerStateUnsupported) {
            NSLog(@"Bluetooth Unsupported on this device");
        } else if (errorCode == CBCentralManagerStatePoweredOff) {
            NSLog(@"Bluetooth is turned off");
        }
    } else if (noteType == MipRobotFinderNote_BluetoothIsAvailable) {
    }
}

//MipRobot Callbacks
- (void) MipDeviceReady:(MipRobot *)mip
{
    if (connectMipCallback != nil)
    {
        [self justSendOkPluginResult: connectMipCallback];
    }
}

- (void) MipDeviceDisconnected:(MipRobot *)mip error:(NSError *)error
{
}

@end