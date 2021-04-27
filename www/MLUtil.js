/**
 * cordova is available under the MIT License (2008).
 * See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) Matt Kane 2010
 * Copyright (c) 2011, IBM Corporation
 * Copyright (c) 2012-2017, Adobe Systems
 */

var exec = cordova.require("cordova/exec");

var scanInProgress = false;

function MLUtil() {

}

MLUtil.prototype.decode = function fun(successCallback, errorCallback, data) {
    if (typeof successCallback != 'Function') {
        console.log('User did not set success callback');
        return;
    }
    if (typeof errorCallback != 'Function') {
        console.log('User did not set error callback');
        return;
    }
    exec(successCallback, errorCallback,  'MLUtils', 'decode', [{data}]);  
}

var mlutil = new MLUtil();
module.exports = mlutil;