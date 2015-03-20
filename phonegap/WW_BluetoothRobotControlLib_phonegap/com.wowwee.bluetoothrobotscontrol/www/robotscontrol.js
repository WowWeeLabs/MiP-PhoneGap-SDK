var className = "RobotsControlPlugin";
var robotscontrol = {
    test: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, className, "cordovaTest", []);
    },
    testMultiArguments: function(msg, value, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, className, "cordovaTestMultiArguments", [msg, value]);
    },
    startScanMips: function() {
        cordova.exec(function(obj) {}, function(obj) {}, className, "cordovaStartScanMips", []);
    },
    stopScanMips: function() {
        cordova.exec(function(obj) {}, function(obj) {}, className, "cordovaStopScanMips", []);
    },
    getMips: function(getMipsCallback) {
        cordova.exec(function(mips) {
            getMipsCallback(mips);
        }, function(obj) {}, className, "cordovaGetMips", []);
    },
    connectMip: function(mipId, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, className, "cordovaConnectMip", [mipId]);
    },
    moveMip: function(mipId, x, y, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, className, "cordovaMoveMip", [mipId, x, y]);
    },
    playSound: function(mipId, soundId, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, className, "cordovaPlaySound", [mipId, soundId]);
    },
    chestRGBLedWithColor: function(mipId, red, green, blue, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, className, "cordovaChestRGBLedWithColor", [mipId, red, green, blue]);
    },
    falloverWithStyle: function(mipId, styleId, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, className, "cordovaFalloverWithStyle", [mipId, styleId]);
    }
};
module.exports = robotscontrol;