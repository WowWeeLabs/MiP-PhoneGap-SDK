var robotscontrol;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        $("#btnSearchMip").click(function() {
            $("#hint").css("display", "block");

            robotscontrol.startScanMips();

            app._connectAnyoneMipIntervalId = setInterval(app.connectAnyoneMip, 1000);
        });
    },
    connectedMip: undefined,
    _connectAnyoneMipIntervalId: undefined,
    connectAnyoneMip: function() {
        robotscontrol.getMips(function(mips) {
            //stop the pooling
            clearInterval(app._connectAnyoneMipIntervalId);

            var connectingMip = mips[0];

            robotscontrol.connectMip(connectingMip.id, function(obj) {
                app.connectedMip = connectingMip;

                $("#btnSearchMip").css("display", "none");
                $("#pageConnectedMip").css("display", "block");
                $("#pageDrive").css("display", "none");

                $("#mip_info").html("connect to "+app.connectedMip.name);

                console.log("connected to "+JSON.stringify(app.connectedMip));

                //stop the scanning
                robotscontrol.stopScanMips();
            }, function(obj) {
                console.log("can't connnect to mip: "+JSON.stringify(connectingMip));

                //stop the scanning
                robotscontrol.stopScanMips();
            });
        });
    },
    moveX: 0,
    moveY: 0,
    moveMip: function() {
        if (app.moveX != 0 || app.moveY != 0) {
            if (robotscontrol !== undefined && app.connectedMip !== undefined) {
                robotscontrol.moveMip(app.connectedMip.id, app.moveX, app.moveY, function(obj){}, function(obj){});
            }
        }
    },
    playSound: function() {
        if (robotscontrol !== undefined && app.connectedMip !== undefined) {
            robotscontrol.playSound(app.connectedMip.id, 43, function(obj){
                console.log(obj);
            }, function(obj){
                console.log(obj);
            });
        }
    },
    changeChest: function() {
        if (robotscontrol !== undefined && app.connectedMip !== undefined) {
            robotscontrol.chestRGBLedWithColor(app.connectedMip.id, 255, 255, 255, function(obj){
                console.log(obj);
            }, function(obj){
                console.log(obj);
            });
        }
    },
    fallover: function() {
        if (robotscontrol !== undefined && app.connectedMip !== undefined) {
            robotscontrol.falloverWithStyle(app.connectedMip.id, 0, function(obj){
                console.log(obj);
            }, function(obj){
                console.log(obj);
            });
        }
    }
};

app.initialize();

$(function() {
    var joystick_draggable_options = {
        scroll: false,
        powerRatio: 0.01,
        radius: $("#joystick_single").width()/2,

        _dragX: function(event, ui) {
            var x = ui.position.left - joystick_draggable_options.radius;
            var y = ui.position.top - joystick_draggable_options.radius;

            var distance = Math.sqrt(x * x + y * y);
            if (distance > joystick_draggable_options.radius) {
                x *= joystick_draggable_options.radius / distance;
                y *= joystick_draggable_options.radius / distance;

                ui.position.left = x + joystick_draggable_options.radius;
                ui.position.top = y + joystick_draggable_options.radius;
            }

            app.moveX = x * joystick_draggable_options.powerRatio;
//            app.moveY = -y * joystick_draggable_options.powerRatio;

            console.log(app.moveX+", "+app.moveY);
        },
        _dragY: function(event, ui) {
            var x = ui.position.left - joystick_draggable_options.radius;
            var y = ui.position.top - joystick_draggable_options.radius;

            var distance = Math.sqrt(x * x + y * y);
            if (distance > joystick_draggable_options.radius) {
                x *= joystick_draggable_options.radius / distance;
                y *= joystick_draggable_options.radius / distance;

                ui.position.left = x + joystick_draggable_options.radius;
                ui.position.top = y + joystick_draggable_options.radius;
            }

//            app.moveX = x * joystick_draggable_options.powerRatio;
            app.moveY = -y * joystick_draggable_options.powerRatio;

            console.log(app.moveX+", "+app.moveY);
        },
        _dragXY: function(event, ui) {
            var x = ui.position.left - joystick_draggable_options.radius;
            var y = ui.position.top - joystick_draggable_options.radius;

            var distance = Math.sqrt(x * x + y * y);
            if (distance > joystick_draggable_options.radius) {
                x *= joystick_draggable_options.radius / distance;
                y *= joystick_draggable_options.radius / distance;

                ui.position.left = x + joystick_draggable_options.radius;
                ui.position.top = y + joystick_draggable_options.radius;
            }

            app.moveX = x * joystick_draggable_options.powerRatio;
            app.moveY = -y * joystick_draggable_options.powerRatio;

            console.log(app.moveX+", "+app.moveY);
        },
        _stopX: function(event, ui) {
            app.moveX = 0;
//            app.moveY = 0;

            console.log(app.moveX+", "+app.moveY);
        },
        _stopY: function(event, ui) {
//            app.moveX = 0;
            app.moveY = 0;

            console.log(app.moveX+", "+app.moveY);
        },
        _stopXY: function(event, ui) {
            app.moveX = 0;
            app.moveY = 0;

            console.log(app.moveX+", "+app.moveY);
        },
        revert: true,
        revertDuration: 0
    };

    var joystick_single = $("#joystick_single");
    var joystick_single_option = {};
    jQuery.extend(joystick_single_option, joystick_draggable_options);
    joystick_single_option.drag = joystick_single_option._dragXY;
    joystick_single_option.stop = joystick_single_option._stopXY;
    joystick_single.draggable(joystick_single_option);

    var joystick_left = $("#joystick_left");
    var joystick_left_option = {};
    jQuery.extend(joystick_left_option, joystick_draggable_options);
    joystick_left_option.drag = joystick_left_option._dragY;
    joystick_left_option.stop = joystick_left_option._stopY;
    joystick_left.draggable(joystick_left_option);

    var joystick_right = $("#joystick_right");
    var joystick_right_option = {};
    jQuery.extend(joystick_right_option, joystick_draggable_options);
    joystick_right_option.drag = joystick_right_option._dragX;
    joystick_right_option.stop = joystick_right_option._stopX;
    joystick_right.draggable(joystick_right_option);

    const fps = 20;
    setInterval(app.moveMip , 1000 / fps);

    $("#btnSearchMip").css("display", "block");
    $("#pageConnectedMip").css("display", "none");
    $("#pageDrive").css("display", "none");

    $("#btnPlaySound").click(function() {
        app.playSound();
    });

    $("#btnChangeColor").click(function() {
        app.changeChest();
    });

    $("#btnFalldown").click(function() {
        app.fallover();
    });

    $("#btnDrive").click(function() {
        $("#btnSearchMip").css("display", "none");
        $("#pageConnectedMip").css("display", "none");
        $("#pageDrive").css("display", "block");
    });

    $("#btnBack").click(function() {
        $("#btnSearchMip").css("display", "none");
        $("#pageConnectedMip").css("display", "block");
        $("#pageDrive").css("display", "none");
    });
});
