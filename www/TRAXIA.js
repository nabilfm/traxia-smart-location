var exec = require('cordova/exec');

exports.saveLocalData = function(arg0, success, error) {
    exec(success, error, "TRAXIALocationPlugin", "saveLocalData", [arg0]);
};

exports.clearLocalData = function(success, error) {
    exec(success, error, "TRAXIALocationPlugin", "clearLocalData", []);
};

exports.saveDOData = function(arg0, success, error) {
    exec(success, error, "TRAXIALocationPlugin", "saveDOData", [arg0]);
};