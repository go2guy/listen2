function getHrefId(jsonObj, key) {
    var returnObj = getJsonVal(jsonObj, key);
    return getNextElement('L',returnObj.href ,'/');
}

function getInfo(conferenceObj, flag) {
    var result = eval("("+conferenceObj+")");
    if ((result[flag]) && (result[flag] != null))
        result = 'true';
    else
        result = 'false';

    return result;
}

function getParticipantsOnHold(participantObj) {
    var result;
    var tmpVal = eval("("+participantObj+")");
    if (tmpVal.total != null)
        result = parseInt(tmpVal.total,10) - 1;
    else
        result = 0;
    return result;
}

function getParticipantsInConf(participantObj) {
    var result;
    var tmpVal = eval("("+participantObj+")");
    if ((tmpVal.count != null) && (tmpVal.count != null)) {
        if ((tmpVal.count == 1) && (tmpVal.total == 1))
            result = 1;
        else
            result = tmpVal.count;
    }
    else
        result = 0;
    return result;
}

function checkMenuKeyPress(helpCnt, maxHelp, keyPress, validKeys) {
    var menuKeys = new String(validKeys);
    if (menuKeys.match(keyPress))
        return keyPress;
    else if ((keyPress == '*') && (helpCnt < maxHelp))
        return keyPress;
    else
        return "ERROR";
}

function checkTotalCount(returnVal, indexOfRequest) {
    var tmpVal = eval("("+returnVal+")");
    var total = parseInt(tmpVal.total);
    var count = parseInt(tmpVal.count) + parseInt(indexOfRequest);
    if(total > count)
        return 1;
    else
        return 0;
}

function getAudioURL(participantObj, index, audioURL, hostName) {
    var result = new String();
    var tmpVal = eval("("+participantObj+")");
    result = tmpVal.results[index].audioResource;
    var tmpArray = result.split(hostName);
    if ((result == null) || (tmpArray[1] == audioURL))
        result = "";
    return result;
}

function getResultsKeyValue(jsonObj, index, key) {
    var result;
    var tmpVal = eval("("+jsonObj+")");
    result = tmpVal.results[index][key];
    if (result == null)
        result = "";
    return result;
}

function rollCallKeyPress(keyPress, returnVal, index, isAdmin) {
    var result;
    var tmpVal = eval("("+returnVal+")");
    var isAdminStatus = tmpVal.results[index].isAdmin;
    var isPassiveStatus = tmpVal.results[index].isPassive;
    var options = new String("1,2,3");
    if(keyPress == 4)
        result = "RESTART";
    else if (isAdmin == 'true') {
        if (isAdminStatus && (options.match(keyPress)))
            result = "INVALID_ADMIN";
        else if (isPassiveStatus && (options.match(keyPress))) {
            if ((keyPress == 1) || (keyPress == 2))
                result ="INVALID_PASSIVE";
            else
                result = "DROP";
        }
        else if (options.match(keyPress)) {
            if(keyPress == 1)
                result = "MUTE";
            else if (keyPress == 2)
                result = "UNMUTE";
            else
                result = "DROP";
        }
        else
            result = "REJOIN";
    }
    else
        result = "REJOIN";
    return result;
}

function getDestination (phoneNumber, maxHelp, helpCnt, isInteractiveCall, pstnLength) {
    var ipAddSect = 4;
    var num = /^\d+$/;
    if ((phoneNumber == '*') && (helpCnt < maxHelp))
        return "HELP";
    else if (num.test(phoneNumber) && (phoneNumber.length >= pstnLength))
        return isInteractiveCall;
    else if (iiStrCnt(phoneNumber,'*') >= ipAddSect)
        return isInteractiveCall;
    else
        return "ERROR";
}

function setCallerID (phoneNumber, sipURL, ANI) {
    var str = new String(phoneNumber);
    var num = /^\d+$/;
    if ((num.test(str)) && (sipURL != 'null'))
        return ANI + "@" + sipURL;
    else
        return ANI + "@iipbx";
}

function setDestination (phoneNumber, sipURL) {
    var str = new String(phoneNumber);
    var num = /^\d+$/;
    if ((num.test(str)) && (sipURL != 'null'))
        return phoneNumber + "@" + sipURL;
    else
        return phoneNumber;
}

function chkErrorCount (errCnt, maxErr, helpCnt, maxHelp) {
    if (helpCnt == maxHelp)
        return "MAX_HELP";
    else if (errCnt < maxErr)
        return errCnt;
    else
        return maxErr;
}

function getNextPath (whichPath) {
    if ((whichPath == 'getOnDemand') || (whichPath == 'enterDestination'))
        return "NEXT";
    else
        return "END";
}

function getSessionID (returnVal, index, SID) {
    var result;
    var tmpVal = eval("("+returnVal+")");
    result =  tmpVal.results[index].sessionID;
    if ((result == null) || (result == SID))
        result = "";

    return result;
}

function confMenuKeyPress (getInput, isAdmin, isAdminMuted, isRecording) {
    var opts = new String("3,4,5");
    if (getInput == '1')
        return getInput;
    else if ((getInput == '2') && (isAdminMuted == 'false'))
        return getInput;
    else if ((isAdmin == 'true') &&  (opts.match(getInput))) {
        if ((getInput == '3') && (isRecording == "ERROR"))
            return "REJOIN";
        else
            return getInput;
        }
    else
        return "REJOIN";
}

function checkArcadeId (returnVal) {
    var result = eval("("+returnVal+")");
    if ((result.arcadeId == "null") || (result.arcadeId.length == 0))
        return "UPDATE";
    else
        return "SKIP";
}

function checkAdminStatus (isAdmin, whichPath) {
    if((isAdmin == 'true') && ((whichPath == 'mainMenu') || (whichPath == 'endCall')))
        return "RESET";
    else
        return "NEXT";
}

function hangUpStatus (userID, isInfoSaved) {
    if((userID.length !=0) && (isInfoSaved == 'true'))
        return "NEXT";
    else
        return "END";
}

function isConfRecording (isRecording, isAdmin) {
    if ((isRecording == 'true') && (isAdmin == 'true'))
        return "true";
    else
        return "false";
}

function getTimeDifference(startTime, endTime) {
    var num = /^\d+$/;
    if ((num.test(startTime)) && (num.test(endTime)))
        return Math.round((endTime - startTime)/1000);
    else
        return 1;
}