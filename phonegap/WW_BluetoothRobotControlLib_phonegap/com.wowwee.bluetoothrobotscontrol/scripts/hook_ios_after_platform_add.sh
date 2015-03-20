#ref: http://ezosaleh.com/adjust-ios-deployment-target-with-cordova-hooks-nodejs

TARGET_IOS=8.0

XCODE_PROJECT_PATH=$(find platforms/ios/ -name "project.pbxproj" | grep -v "CordovaLib")
IPHONEOS_DEPLOYMENT_TARGET=$(grep "IPHONEOS_DEPLOYMENT_TARGET" $XCODE_PROJECT_PATH | wc -l | bc)
IPHONEOS_DEPLOYMENT_TARGET_IS_8_X=$(grep "IPHONEOS_DEPLOYMENT_TARGET[ ]*=[ ]*8\.[0-9\.]*" $XCODE_PROJECT_PATH | wc -l | bc)

if [ "$IPHONEOS_DEPLOYMENT_TARGET" -ne "$IPHONEOS_DEPLOYMENT_TARGET_IS_8_X" ]; then
    mv $XCODE_PROJECT_PATH $XCODE_PROJECT_PATH.bak
    sed "s/IPHONEOS_DEPLOYMENT_TARGET[ =0-9\.]*/IPHONEOS_DEPLOYMENT_TARGET = $TARGET_IOS/" $XCODE_PROJECT_PATH.bak | sed "s/CLANG_ENABLE_MODULES.*//" | sed "s/\(CLANG_ENABLE_OBJC_ARC.*\)/CLANG_ENABLE_MODULES = YES;\\$(printf '\012\015')\1/" > $XCODE_PROJECT_PATH
    
    echo "==================="
    echo "upgrade iOS deployment target to $TARGET_IOS"
    echo "==================="
fi

echo "==================="
INFO_PLIST=$(find platforms/ios -name "*-Info.plist")
cp $INFO_PLIST $INFO_PLIST.bak
patch -N $INFO_PLIST plugins/com.wowwee.bluetoothrobotscontrol/scripts/info_plist.diff
echo "apply patch for iPhone orientation on $INFO_PLIST"
echo "==================="