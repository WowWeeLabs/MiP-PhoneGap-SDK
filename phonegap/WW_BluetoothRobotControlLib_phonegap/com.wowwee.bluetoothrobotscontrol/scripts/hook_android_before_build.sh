INSTALL_SERVICE=$(grep "<service.*com\.wowwee\.bluetoothrobotcontrollib\.BluetoothLeService" platforms/android/AndroidManifest.xml | wc -l | bc)

if [ "$INSTALL_SERVICE" -ne "1" ]; then
    mv platforms/android/AndroidManifest.xml platforms/android/AndroidManifest.xml.bak
    sed "s/<\/application/<service android:name=\"com\.wowwee\.bluetoothrobotcontrollib\.BluetoothLeService\" android:enabled=\"true\" \/><\/application/" platforms/android/AndroidManifest.xml.bak > platforms/android/AndroidManifest.xml
    
    echo "==================="
    echo "install service(com.wowwee.bluetoothrobotcontrollib.BluetoothLeService) successful"
    echo "==================="
fi

echo "==================="
WHERE_IS_CORDOVA_APP_JAVA=$(find . -name "CordovaApp.java" | sed "s/CordovaApp\.java//")
cp plugins/com.wowwee.bluetoothrobotscontrol/scripts/CordovaApp.diff $WHERE_IS_CORDOVA_APP_JAVA
pushd $WHERE_IS_CORDOVA_APP_JAVA
echo "install patch($WHERE_IS_CORDOVA_APP_JAVA)"
patch -p1 -N < CordovaApp.diff
rm -f CordovaApp.java.rej CordovaApp.diff
popd
echo "==================="

echo "==================="
SETUP_MIN_SDK_VERSION=$(grep "<preference name=\"android-minSdkVersion\"" config.xml | wc -l | bc)

#haven't setup yet
if [ "$SETUP_MIN_SDK_VERSION" -ne "1" ]; then
    mv config.xml config.xml.bak
    sed "s/<\/widget>/<preference name=\"android-minSdkVersion\" value=\"18\" \/>\\$(printf '\012\015')<\/widget>/" config.xml.bak > config.xml
    echo "add android-minSdkVersion to 18 on config.xml"
#already setup, but override it
else
    mv config.xml config.xml.bak
    sed "s/<preference name=\"android-minSdkVersion\".*value=\"[0-9]*\".*\/>/<preference name=\"android-minSdkVersion\" value=\"18\" \/>/" config.xml.bak > config.xml
    echo "update android-minSdkVersion to 18 on config.xml"
fi
echo "==================="