<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="listen" uri="http://www.iivip.com/taglibs/listen" %>
<%@ page import="com.interact.listen.ServletUtil" %>
<%@ page import="com.interact.listen.resource.Subscriber" %>
<%@ page import="com.interact.listen.util.DateUtil" %>
<%@ page import="org.joda.time.DateTime" %>
<%@ page import="org.joda.time.format.DateTimeFormat" %>
<%@ page import="org.joda.time.format.DateTimeFormatter" %>
<html>
  <head>
    <title>Find Me / Follow Me</title>
    <script type="text/javascript" src="./resources/app/js/app-findme-min.js"></script>
    <script type="text/javascript" src="./resources/jquery/plugins/jquery-ui-timepicker-addon.js"></script>
    <link rel="stylesheet" type="text/css" href="<listen:resource path="/resources/app/css/findme-min.css"/>">
    
    <meta name="body-class" content="application-findme"/>
    <meta name="page-title" content="Find Me / Follow Me"/>
  </head>
  <body>
    <div class="help">
      <b>Find Me / Follow Me</b> lets you control how you're contacted when someone tries to call you. You can have the call forwarded to several numbers at once, different numbers in succession, or any combination of both.
    </div><%
Subscriber subscriber = ServletUtil.currentSubscriber(request);
DateTime expires = null;
if(subscriber.getFindMeExpiration() == null) {
    expires = new DateTime().minusDays(1); // any date in the past
} else {
    expires = DateUtil.toJoda(subscriber.getFindMeExpiration()).toDateTime();
}
DateTime now = new DateTime();
DateTime value = new DateTime().plusDays(1); // default to +1 day ahead of now...
if(expires.isAfter(now)) { // ...unless the expiration is valid; if it is, use it instead of the default
    value = expires;
}

DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE, MMMM d, yyyy 'at' K:mm aa");
%>

    <div id="expiration-expired" class="error-message" style="display: <%= expires.isBefore(now) ? "block" : "none" %>;">
      The following Find Me / Follow Me configuration <b>is expired</b> (and does not apply to incoming calls). <button id="expiration-change" type="button" class="button-edit">Change Expiration</button>
    </div>

    <div id="expiration-configuration" class="warning-message" style="display: <%= expires.isBefore(now) ? "none" : "block" %>;">
      The following Find Me / Follow Me configuration will expire on <input type="text" id="expires" value="<%= formatter.print(value) %>"/>
      <div id="expiration-notify">
        <input type="checkbox" id="notify-enable"<%= subscriber.getSendFindMeReminder() ? " checked=\"checked\"" : "" %>> Shortly before the configuration expires, send me an SMS message<span id="notify-destination-area" style="display: <%= subscriber.getSendFindMeReminder() ? "inline" : "none" %>;"> to phone number <input type="text" id="notify-destination" value="<%= subscriber.getFindMeReminderDestination() != null ? subscriber.getFindMeReminderDestination() : "" %>"/></span>
      </div>
    </div>

    <div class="when-somebody-calls">When somebody calls me,</div>

    <div class="if-i-dont-answer">
      <span>If I don't answer</span>
      <select id="if-i-dont-answer-select">
        <option selected="selected" value="voicemail">Send the caller to my voicemail</option>
        <option value="dial">Dial...</option>
      </select>
    </div>

    <div class="save-button-container">
      <button class="button-save" id="findme-save">Save</button>
    </div>
  </body>
</html>