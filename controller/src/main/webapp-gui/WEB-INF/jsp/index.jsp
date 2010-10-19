<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<%@ page import="com.interact.listen.license.License" %>
<%@ page import="com.interact.listen.license.ListenFeature" %>
<%@ page import="com.interact.listen.resource.Subscriber" %>
<%@ page import="com.interact.listen.EmailerUtil" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html><%
Subscriber subscriber = (Subscriber)session.getAttribute("subscriber"); %>
  <head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <title>Listen</title>
    <link rel="SHORTCUT ICON" href="./resources/app/images/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="./resources/yui-2.8.0r4/reset-fonts/reset-fonts.css">
    <link rel="stylesheet" type="text/css" href="./resources/jquery/skin/css/custom-theme/jquery-ui-1.8.2.custom.css">
    <link rel="stylesheet" type="text/css" href="./resources/app/css/all-min.css">
    <link rel="stylesheet" type="text/css" href="./resources/app/css/index-min.css">
    <script type="text/javascript" src="./resources/jquery/jquery-1.4.2.min.js"></script>
    <script type="text/javascript" src="./resources/jquery/jquery-ui-1.8rc3.custom.min.js"></script>
    <script type="text/javascript" src="./resources/jquery/plugins/jquery.simplemodal-1.3.5.min.js"></script>
    <script type="text/javascript" src="./resources/json.org/json2-min.js"></script>
    <script type="text/javascript" src="./resources/app/js/index-min.js"></script>
    <script type="text/javascript" src="./resources/app/js/server-min.js"></script><%
if(subscriber != null && subscriber.getIsAdministrator()) { %>
    <script type="text/javascript" src="./resources/app/js/app-system-configuration-min.js"></script>
    <script type="text/javascript" src="./resources/app/js/app-subscribers-min.js"></script>
    <script type="text/javascript" src="./resources/app/js/app-history-min.js"></script><%
}
if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
    <script type="text/javascript" src="./resources/app/js/app-voicemail-min.js"></script><%
}
if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
    <script type="text/javascript" src="./resources/app/js/app-conferencing-min.js"></script><%
    if(subscriber != null && subscriber.getIsAdministrator()) { %>
    <script type="text/javascript" src="./resources/app/js/app-conference-list-min.js"></script><%
    }
}
if(License.isLicensed(ListenFeature.ATTENDANT)) { %>
    <script type="text/javascript" src="./resources/app/js/app-attendant-min.js"></script><%
}
if(License.isLicensed(ListenFeature.FINDME)) { %>
    <script type="text/javascript" src="./resources/app/js/app-findme-min.js"></script><%
} %>
    <script type="text/javascript" src="./resources/app/js/app-profile-min.js"></script>
  </head>
  <body>
    <div id="header">
      <div class="logo"><img src="resources/app/images/new/listen_logo_50x24.png" alt="Listen"/></div>
      <div class="info"><b><%= subscriber.getUsername() %></b>&nbsp;&bull;&nbsp;<a href="#" id="profile-button" name="profile-button" title="Settings">Settings</a>&nbsp;<a href="/logout" id="logoutButton" name="logoutButton">Logout</a></div>
    </div>
    <div class="column-mask">
      <div class="two-column">
        <div class="content-column-wrapper">
          <div class="content-column">
            <div id="notification"></div><%

if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
            <div id="conferencing-application" class="application">
              <div class="application-header"><div class="title">Conferencing</div></div>
              <div class="application-content">
                <div class="conference-content">
                  <div class="tab-container">
                    <div class="tabs">
                      <ul>
                        <li>Main</li>
                        <li>Scheduling</li>
                        <li>Recordings</li>
                      </ul>
                    </div>
                    <div class="tab-content-default">
                      <div class="left">
                        <div class="panel">
                          <div class="panel-content">
                            <table>
                              <tbody>
                                <tr><td>Conference Description</td><td id="conference-info-description" class="conference-info-value"></td></tr>
                                <tr><td>Conference Status</td><td id="conference-info-status" class="conference-info-value"></td></tr>
                                <tr>
                                  <td>
                                    <div class="control-panel-button">
                                      <button type="button" id="outdial-show" name="outdial-show" class="button-outdial">OnDemand</button>
                                    </div>
                                    <div id="outdial-dialog" class="inline-dialog">
                                      <form name="outdial-form" id="outdial-form">
                                        <div class="form-error-message"></div>
                                        <div>
                                          <label for="outdial-number">Phone number to dial:</label> <input type="text" name="outdial-number" id="outdial-number"/>
                                        </div>
                                        <div>
                                          <label for="outdial-interrupt">Interrupt when answered:</label> <input type="checkbox" id="outdial-interrupt" name="outdial-interrupt" value="interrupt"/>
                                        </div>
                                        <div>
                                          <button type="button" class="button-cancel" name="outdial-cancel" id="outdial-cancel">Cancel</button>
                                          <button type="button" class="button-outdial" name="outdial-submit" id="outdial-submit">Make Call</button>
                                        </div>
                                      </form>
                                    </div>
                                    <div id="record-button-div" class="control-panel-button">
                                      <button type="button" id="record-button" class="button-record">Record</button>
                                    </div>
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                          </div>
                        </div>
                        <div class="panel">
                          <div class="panel-header"><div class="title">On The Call (<span id="conference-caller-count">0</span>)</div></div>
                          <div class="panel-content">
                            <table id="conference-caller-table">
                              <tbody>
                                <tr class="placeholder"><td colspan="3">Nobody</td></tr>
                              </tbody>
                            </table>
                            <div class="pagination" id="conference-caller-pagination">
                              <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
                            </div>
                            <div class="cleaner">&nbsp;</div>
                          </div>
                        </div>
                      </div>
                      <div class="right">
                        <div class="panel">
                          <div class="panel-header"><div class="title">Available PINs (<span id="conference-pin-count">0</span>)</div></div>
                          <div class="panel-content">
                            <table id="conference-pin-table">
                              <tbody>
                                <tr class="placeholder"><td colspan="3">No PINs available</td></tr>
                              </tbody>
                            </table>
                          </div>
                        </div>
                        <div class="panel">
                          <div class="panel-header"><div class="title">Recent History</div></div>
                          <div class="panel-content">
                            <table id="conference-history-table">
                              <tbody>
                                <tr class="placeholder"><td colspan="2">No history records</td></tr>
                              </tbody>
                            </table>
                          </div>
                        </div>
                      </div>
                      <div class="cleaner">&nbsp;</div>
                    </div>
                    <div class="tab-content">
                      <div class="panel">
                        <div class="panel-header"><div class="title">Future Scheduled Conferences</div></div>
                        <div class="panel-content">
                          <ul id="scheduled-conference-table" class="data-table">
                            <li class="placeholder">No conferences have been scheduled.</li>
                          </ul>
                          <div class="pagination" id="scheduled-conference-pagination">
                            <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
                          </div>
                        </div>
                      </div>

                      <div class="panel">
                        <div class="panel-header"><div class="title">Schedule a Conference</div></div>
                        <div class="panel-content">
                          <form id="scheduleConferenceForm">
                            <fieldset>
                              <div class="form-error-message"></div>
                              <div class="form-success-message"></div>
                              <table>
                                <caption>This will send an email to the specified recipients with a date, time, phone number, and PIN.</caption>
                                <tbody>
                                  <tr>
                                    <th><label for="scheduleConferenceDate" class="required">Date &amp; Time</label></th>
                                    <td>
                                      <input type="text" id="scheduleConferenceDate" name="scheduleConferenceDate"/><br/>
                                       From <select id="scheduleConferenceTimeHour" name="scheduleConferenceTimeHour"><%
for(int i = 1; i <= 12; i++) { %>
                                        <option value="<%= i %>"><%= i %></option><%
} %>
                                      </select>
                                      <select id="scheduleConferenceTimeMinute" name="scheduleConferenceTimeMinute">
                                        <option value="00">00</option>
                                        <option value="15">15</option>
                                        <option value="30">30</option>
                                        <option value="45">45</option>
                                      </select>
                                      <select id="scheduleConferenceTimeAmPm" name="scheduleConferenceTimeAmPm">
                                        <option value="AM">AM</option>
                                        <option value="PM">PM</option>
                                      </select> until <select id="scheduleConferenceEndTimeHour" name="scheduleConferenceEndTimeHour"><%
for(int i = 1; i <= 12; i++) { %>
                                         <option value="<%= i %>"><%= i %></option><%
} %>
                                      </select>
                                      <select id="scheduleConferenceEndTimeMinute" name="scheduleConferenceEndTimeMinute">
                                        <option value="00">00</option>
                                        <option value="15">15</option>
                                        <option value="30">30</option>
                                        <option value="45">45</option>
                                      </select>
                                      <select id="scheduleConferenceEndTimeAmPm" name="scheduleConferenceTimeEndAmPm">
                                        <option value="AM">AM</option>
                                        <option value="PM">PM</option>
                                      </select>
                                    </td>
                                  </tr>
                                  <tr>
                                    <th><label for="scheduleConferenceSubject">Subject of email</label></th>
                                    <td><input type="text" id="scheduleConferenceSubject" name="scheduleConferenceSubject"/></td>
                                  </tr>
                                  <tr>
                                    <th><label for="scheduleConferenceDescription">Memo to include in email</label></th>
                                    <td><textarea id="scheduleConferenceDescription" name="scheduleConferenceDescription"></textarea>
                                  </tr>
                                  <tr>
                                    <td colspan="2">Enter the email addresses (comma-separated) for participants who should receive the appropriate PIN in the fields below.</td>
                                  </tr>
                                   <tr>
                                    <th><label for="scheduleConferenceActiveParticipants">Active caller email addresses</label></th>
                                    <td><textarea id="scheduleConferenceActiveParticipants" name="scheduleConferenceActiveParticipants"></textarea></td>
                                  </tr>
                                  <tr>
                                    <th><label for="scheduleConferencePassiveParticipants">Passive caller email addresses</label></th>
                                    <td><textarea id="scheduleConferencePassiveParticipants" name="scheduleConferencePassiveParticipants"></textarea></td>
                                  </tr>
                                  <tr>
                                    <td colspan="2" class="buttons">
                                      <button type="submit" class="button-schedule">Send Emails</button>
                                    </td>
                                  </tr>
                                </tbody>
                              </table>
                            </fieldset>
                          </form>
                        </div>
                      </div>
                      
                      <div class="panel">
                        <div class="panel-header"><div class="title">Past Scheduled Conferences</div></div>
                        <div class="panel-content">
                          <ul id="historic-scheduled-conference-table" class="data-table">
                            <li class="placeholder">No past conferences.</li>
                          </ul>
                          <div class="pagination" id="historic-scheduled-conference-pagination">
                            <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
                          </div>
                        </div>
                      </div>
                      
                    </div>
                    <div class="tab-content">

                            <table id="conference-recording-table" class="data-table">
                              <thead>
                                <tr>
                                  <th>Date</th>
                                  <th>Duration</th>
                                  <th>Size</th>
                                  <th></th>
                                </tr>
                              </thead>
                              <tbody>
                                <tr class="placeholder"><td colspan="4">No recordings</td></tr>
                              </tbody>
                            </table>
                            <div class="pagination" id="conference-recording-pagination">
                              <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
                            </div>

                    </div>
                    <div class="cleaner">&nbsp;</div>
                  </div>
                </div><!-- conference-content -->
                <div class="conference-notloaded">
                  Conference not found.
                </div>
              </div>
            </div><%
}

if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
            <div id="voicemail-application" class="application">
              <div class="application-header"><div class="title">Voicemail (<span id="voicemail-new-count">0</span> New)</div></div>
              <div class="application-content">
                <ul id="voicemail-table">
                  <li class="placeholder">No voicemail</li>
                </ul>
                <div class="pagination" id="voicemail-pagination">
                  <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
                </div>
                <div class="cleaner">&nbsp;</div>
              </div>
            </div><%
} 

if(License.isLicensed(ListenFeature.FINDME)) { %>
            <div id="findme-application" class="application">
              <div class="application-header"><div class="title">FindMe</div></div>
              <div class="application-content">
                FindMe Stuff
              </div>
            </div><%
}

if(subscriber != null && subscriber.getIsAdministrator()) {

    if(License.isLicensed(ListenFeature.ATTENDANT)) { %>
            <div id="attendant-application" class="application">
              <div class="application-header"><div class="title">Attendant</div></div>
              <div class="application-content">

                  <div id="attendant-menu-list-container">
                    <table id="attendant-menu-list">
                      <thead>
                        <tr>
                          <td colspan="3"><button type="button" id="attendant-menu-add-new-menu" class="button-add">New Menu</button></td>
                        </tr>
                      </thead>
                      <tbody></tbody>
                    </table>
                  </div>

                  <div class="form-error-message"></div>
                  <div class="form-success-message"></div>

                  <div id="attendant-menu-builder-container-placeholder"></div>

                  <div id="attendant-menu-builder-container">
                    <div id="attendant-menu-name"><label>Name:</label>&nbsp;<input type="text"/></div>

                    <div id="attendant-menu-information">
                      <input type="hidden" id="attendant-menu-id" name="attendant-menu-id" value=""/>
                      <div id="attendant-menu-information-caption">When caller enters this menu...</div>
                      <div id="attendant-menu-information-input">
                        <label for="attendant-menu-audio-file">Play</label>
                        <select id="attendant-menu-audio-file" name="attendant-menu-audio-file" size="1"></select>
                      </div>
                      <div id="attendant-menu-audio-file-transcription">
                        This section would have a transcription of the audio file that is selected in the box above.
                      </div>
                    </div>

                    <div id="attendant-menu-actions">
                      <div id="attendant-menu-actions-caption">After playing the audio file...</div>
                      <div id="attendant-menu-actions-container"></div>
                      <div class="attendant-menu-button-container">
                        <button id="attendant-menu-add-new-action" type="button" class="button-add">Add Action</button>
                      </div>
                      <div id="attendant-menu-actions-caption">Otherwise...</div>
                      <div id="attendant-menu-actions-default-action-container"></div>
                      <div id="attendant-menu-actions-timeout-action-container"></div>
                      <div class="attendant-menu-button-container">
                        <button type="button" class="button-cancel" id="attendant-menu-cancel">Cancel</button>
                        <button type="button" class="button-save" id="attendant-menu-save">Save Menu</button>
                      </div>
                    </div>
                  </div>

                <div class="cleaner">&nbsp;</div>
              </div>
            </div><%
    } %>

            <div id="sysconfig-application" class="application">
              <div class="application-header"><div class="title">Configuration</div></div>
              <div class="application-content">
                <div class="tab-container">
                  <div class="tabs">
                    <ul>
                      <li>Phone Numbers</li>
                      <li>Mail</li><%
if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
                      <li>Conferencing</li><%
} %>
                      <li>Alerts</li>
                      <li>Authentication</li>
                    </ul>
                  </div>
                  <div class="tab-content-default">
                    <form id="dnis-mapping-form">
                      <fieldset>
                        <legend>Phone Numbers</legend>
                        <div class="form-error-message"></div>
                        <div class="form-success-message"></div>
                        <table>
                          <tbody>
                            <tr>
                              <td colspan="6" class="buttons">
                                <button type="button" class="button-add" id="add-dnis-mapping" title="Add a new phone number">Add</button>
                                <button type="submit" class="button-save" title="Save phone numbers">Save</button>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div>
                  <div class="tab-content">
                    <form id="mail-form">
                      <fieldset>
                        <legend>Mail</legend>
                        <div class="form-error-message"></div>
                        <div class="form-success-message"></div>
                        <table>
                          <tbody>
                            <tr><td><label for="smtp-server">SMTP Server</label></td><td><input type="text" id="smtp-server" name="smtp-server"/></td></tr>
                            <tr><td><label for="smtp-username">SMTP Username</label></td><td><input type="text" id="smtp-username" name="smtp-username"/></td></tr>
                            <tr><td><label for="smtp-password">SMTP Password</label></td><td><input type="password" id="smtp-password" name="smtp-password"/></td></tr>
                            <tr><td><label for="from-address">From Address</label></td><td><input type="text" id="from-address" name="from-address"/></td></tr>
                            <tr><td colspan="2" class="buttons"><button type="submit" class="button-save" title="Save mail settings">Save</button></td></tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div><%
if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
                  <div class="tab-content">
                    <form id="conferencing-configuration-form">
                      <fieldset>
                        <legend>Conferencing</legend>
                        <div class="form-error-message"></div>
                        <div class="form-success-message"></div>
                        <table>
                          <tbody>
                            <tr>
                              <td><label for="conferencing-configuration-pinLength" class="required">PIN length</label></td>
                              <td><input type="text" id="conferencing-configuration-pinLength" name="conferencing-configuration-pinLength"/></td>
                            </tr>
                            <tr><td colspan="2" class="buttons"><button type="submit" class="button-save" title="Save configuration">Save</button></td></tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div><%
} %>
                  <div class="tab-content">
                    <form id="alerts-configuration-form">
                      <fieldset>
                        <legend>Alerts</legend>
                        <div class="form-error-message"></div>
                        <div class="form-success-message"></div>
                        <div class="help">Interact's Realize&trade; monitoring system can send alerts if the Listen&trade; system goes down. Enter the URL for Realize&trade; and the Alert to be updated. When the Alternate Pager Number changes, the configured Alert will be updated with the new number.</div>
                        <table>
                          <tbody>
                            <tr>
                              <td><label for="alerts-configuration-realizeUrl">Realize URL</label></td>
                              <td><input type="text" id="alerts-configuration-realizeUrl" name="alerts-configuration-realizeUrl"/></td>
                            </tr>
                            <tr>
                              <td><label for="alerts-configuration-realizeAlertName">Realize Alert Name</label></td>
                              <td><input type="text" id="alerts-configuration-realizeAlertName" name="alerts-configuration-realizeAlertName"/></td>
                            </tr>
                            <tr><td colspan="2" class="buttons"><button type="submit" class="button-save" title="Save configuration">Save</button></td></tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div>

                  <div class="tab-content">
                    <form id="sysconfig-authentication-form">
                      <fieldset>
                        <legend>Authentication</legend>
                        <div class="form-error-message"></div>
                        <div class="form-success-message"></div>
                        <table>
                          <tbody>
                            <tr>
                              <td colspan="2">
                                <input type="checkbox" name="sysconfig-authentication-activeDirectoryEnabled" id="sysconfig-authentication-activeDirectoryEnabled"/>
                                <label for="sysconfig-authentication-activeDirectoryEnabled">Use Active Directory?</label>
                              </td>
                            </tr>
                            <tr><td><label for="sysconfig-authentication-activeDirectoryServer">Active Directory Server</label></td><td><input type="text" id="sysconfig-authentication-activeDirectoryServer" name="sysconfig-authentication-activeDirectoryServer"/></td></tr>
                            <tr><td><label for="sysconfig-authentication-activeDirectoryDomain">Active Directory Domain</label></td><td><input type="text" id="sysconfig-authentication-activeDirectoryDomain" name="sysconfig-authentication-activeDirectoryDomain"/></td></tr>
                            <tr><td colspan="2" class="buttons"><button type="submit" class="button-save" title="Save authentication settings">Save</button></td></tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div>
                  <div class="cleaner">&nbsp;</div>
                </div>
              </div>
            </div>

            <div id="conference-list-application" class="application">
              <div class="application-header"><div class="title">Conference List</div></div>
              <div class="application-content">
                <table id="conference-list-table" class="data-table">
                  <thead>
                    <tr>
                      <th>Description</th>
                      <th>Status</th>
                      <th>Callers</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr class="placeholder"><td colspan="4">No conferences</td></tr>
                  </tbody>
                </table>
                <div class="pagination" id="conference-list-pagination">
                  <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
                </div>
                <div class="cleaner">&nbsp;</div>
              </div>
            </div>

            <div id="subscribers-application" class="application">
              <div class="application-header"><div class="title">Subscribers</div></div>
              <div class="application-content">
                <div class="left">
                  <div class="panel">
                    <div class="panel-header"><div class="title">Subscriber List</div></div>
                    <div class="panel-content">
                      <table id="subscribers-table" class="data-table">
                        <thead>
                          <tr>
                            <th>Username</th>
                            <th>Access Numbers</th>
                            <th>Last Login</th>
                            <th></th>
                            <th></th>
                          </tr>
                        </thead>
                        <tbody></tbody>
                      </table>
                      <div class="pagination" id="subscribers-pagination">
                        <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
                      </div>
                      <div class="cleaner">&nbsp;</div>
                    </div>
                  </div>
                </div>
                <div class="right">
                  <div class="panel">
                    <div class="panel-header"><div class="title">Subscriber Info</div></div>
                    <div class="panel-content">
                      <form id="subscriber-form">
                        <fieldset>
                          <div class="form-error-message"></div>
                          <div class="form-success-message"></div>
                          <input type="hidden" id="subscriber-form-id" name="subscriber-form-id"/>
                          <table>
                            <tbody>
                              <tr><td><label for="subscriber-form-username" class="required">Username</label></td><td><input type="text" id="subscriber-form-username" name="subscriber-form-username"/></td></tr>
                              <tr><td>Account Type</td><td id="subscriber-form-accountType">Local</td></tr>
                              <tr><td><label for="subscriber-form-password" class="required">Password</label></td><td><input type="password" id="subscriber-form-password" name="subscriber-form-password"/></td></tr>
                              <tr><td><label for="subscriber-form-confirmPassword" class="required">Confirm Password</label></td><td><input type="password" id="subscriber-form-confirmPassword" name="subscriber-form-confirmPassword"/></td></tr>
                              <tr><td><label for="subscriber-form-realName">Real Name</label></td><td><input type="text" id="subscriber-form-realName" name="subscriber-form-realName"/></td></tr>
                              <tr><td><label for="subscriber-form-isAdmin">Administrator</label></td><td><input type="checkbox" id="subscriber-form-isAdmin" name="subscriber-form-isAdmin" value="enableAdmin"/></td></tr><%
if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
                              <tr><td><label for="subscriber-form-voicemailPin">Voicemail Passcode</label></td><td><input type="text" id="subscriber-form-voicemailPin" name="subscriber-form-voicemailPin" maxlength="10"/></td></tr><%
} %>
                              <tr><td colspan="2"><label>Access Numbers</label></td></tr>
                              <tr>
                                <td colspan="2">
                                  <table id="subscriber-form-accessNumbersTable">
                                    <tbody>
                                      <tr>
                                        <td colspan="3" class="buttons">
                                          <button type="button" class="button-add" id="subscriber-form-addAccessNumber" title="New access number">New access number</button>
                                        </td>
                                      </tr>
                                    </tbody>
                                  </table>
                                </td>
                              </tr><%
if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
                              <tr><td colspan="2"><label for="subscriber-form-enableEmailNotification">Send e-mail when voicemail received</label></td></tr>
                              <tr>
                                <td colspan="2">
                                  <input type="checkbox" id="subscriber-form-enableEmailNotification" name="subscriber-form-enableEmailNotification" value="enableEmail"/>
                                  <input type="text" id="subscriber-form-emailAddress" name="subscriber-form-emailAddress"/>
                                  <button type="button" class="button-save" id="subscriber-form-testEmail-button" name="subscriber-form-testEmail-button" title="Test Email Address">Verify</button>
                                </td>
                              </tr>
                              <tr><td colspan="2"><label for="subscriber-form-enableSmsNotification">Send SMS when voicemail received</label></td></tr>
                              <tr>
                                <td colspan="2">
                                  <input type="checkbox" id="subscriber-form-enableSmsNotification" name="subscriber-form-enableSmsNotification" value="enableSms"/>
                                  <input type="text" id="subscriber-form-smsAddress" name="subscriber-form-smsAddress"/>
                                  <button type="button" class="button-save" id="subscriber-form-testSms-button" name="subscriber-form-testSms-button" title="Test SMS Address">Verify</button>
                                </td>
                              </tr>
                              <tr><td><label for="subscriber-form-paging">Page on new voicemail</label></td><td><input type="checkbox" id="subscriber-form-paging" name="subscriber-form-paging" value="enablePaging"/></td></tr>
                              <tr>
                                <td><label for="subscriber-form-voicemailPlaybackOrder">Voicemail Playback Order</label></td>
                                <td>
                                  <select name="subscriber-form-voicemailPlaybackOrder" id="subscriber-form-voicemailPlaybackOrder">
                                    <option value="NEWEST_TO_OLDEST" selected="selected">Newest to Oldest</option>
                                    <option value="OLDEST_TO_NEWEST">Oldest to Newest</option>
                                  </select>
                                </td>
                              </tr><%
} %>
                              <tr>
                                <td colspan="2" class="buttons">
                                  <button type="submit" class="button-add" id="subscriber-form-add-button" name="subscriber-form-add-button" title="Add">Add</button>
                                  <button type="submit" class="button-edit" id="subscriber-form-edit-button" name="subscriber-form-edit-button" title="Edit">Edit</button>
                                  <button type="reset" class="button-cancel" id="subscriber-form-cancel-button" name="subscriber-form-cancel-button" title="Cancel Edit">Cancel Edit</button>
                                </td>
                              </tr>
                            </tbody>
                          </table>
                        </fieldset>
                      </form>
                    </div>
                  </div>
                </div>
                <div class="cleaner">&nbsp;</div>
              </div>
            </div>

            <div id="history-application" class="application">
              <div class="application-header"><div class="title">History</div></div>
              <div class="application-content">
                <ul id="history-list">
                  <li class="placeholder">No history records</li>
                </ul>
                <div class="pagination" id="history-pagination">
                  <button type="button" class="icon-pageleft"></button><span class="pagination-current">0-0</span>of<span class="pagination-total">0</span><button type="button" class="icon-pageright"></button>
                </div>
                <div class="cleaner">&nbsp;</div>
              </div>
            </div><%
}

if(subscriber != null) { %>
            <div id="profile-application" class="application">
              <div class="application-header"><div class="title">Settings</div></div>
              <div class="application-content">
                <div class="tab-container">
                  <div class="tabs">
                    <ul>
                      <li>Account</li>
                      <li>After Hours Pager</li>
                    </ul>
                  </div>
                  <div class="tab-content-default">
                    <form id="profile-form">
                      <fieldset>
                        <legend>Account</legend>
                        <div class="form-error-message"></div>
                        <div class="form-success-message"></div>
                        <input type="hidden" id="profile-form-id" name="profile-form-id"/>
                        <table>
                          <tbody>
                            <tr><td><label for="profile-form-username">Username</label></td><td><input type="text" id="profile-form-username" name="profile-form-username"<%= !subscriber.getIsAdministrator() ? " readonly=\"readonly\" class=\"disabled\"" : "" %>/></td></tr>
                            <tr><td>Account Type</td><td id="profile-form-accountType"></td></tr> <%
    if(!subscriber.getIsActiveDirectory()) { %>
                            <tr><td><label for="profile-form-password">Password</label></td><td><input type="password" id="profile-form-password" name="profile-form-password"/></td></tr>
                            <tr><td><label for="profile-form-confirmPassword">Confirm Password</label></td><td><input type="password" id="profile-form-confirmPassword" name="profile-form-confirmPassword"/></td></tr> <%
    } %>
                            <tr><td><label for="profile-form-realName">Real Name</label></td><td><input type="text" id="profile-form-realName" name="profile-form-realName"/></td></tr>                        
                            <tr><td><label for="profile-form-number">Access Numbers</label></td><td id="profile-form-accessNumbers" name="profile-form-accessNumbers"></td></tr><%
    if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
                            <tr><td><label for="profile-form-voicemailPin">Voicemail Passcode</label></td><td><input type="text" id="profile-form-voicemailPin" name="profile-form-voicemailPin" maxlength="10"/></td></tr>
                            <tr>
                              <td><label for="profile-form-enableEmailNotification">Send e-mail when voicemail received</label></td>
                              <td>
                                <input type="checkbox" id="profile-form-enableEmailNotification" name="profile-form-enableEmailNotification" value="enableEmail"/>
                                <input type="text" id="profile-form-emailAddress" name="profile-form-emailAddress"/>
                                <button type="button" class="button-save" id="profile-form-testEmail-button" name="profile-form-testEmail-button" title="Test Email Address">Verify</button>
                              </td>
                            </tr>
                            <tr>
                              <td><label for="profile-form-enableSmsNotification">Send SMS when voicemail received</label></td>
                              <td>
                                <input type="checkbox" id="profile-form-enableSmsNotification" name="profile-form-enableSmsNotification" value="enableSms"/>
                                <input type="text" id="profile-form-smsAddress" name="profile-form-smsAddress"/>
                                <button type="button" class="button-save" id="profile-form-testSms-button" name="profile-form-testSms-button" title="Test SMS Address">Verify</button>
                              </td>
                            </tr>
                            <tr><td><label for="profile-form-paging">Send SMS notification for voicemails until read</label></td><td><input type="checkbox" id="profile-form-paging" name="profile-form-paging" value="enablePaging"/></td></tr>
                            <tr>
                              <td><label for="profile-form-voicemailPlaybackOrder">Voicemail Playback Order</label></td>
                              <td>
                                <select name="profile-form-voicemailPlaybackOrder" id="profile-form-voicemailPlaybackOrder">
                                  <option value="NEWEST_TO_OLDEST">Newest to Oldest</option>
                                  <option value="OLDEST_TO_NEWEST">Oldest to Newest</option>
                                </select>
                              </td>
                            </tr><%
    } %>
                            <tr>
                              <td colspan="2" class="buttons">
                                <button type="submit" class="button-edit" id="profile-form-edit-button" name="profile-form-edit-button" title="Edit">Save</button>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div>
                  <div class="tab-content">
                    <form id="pager-form">
                      <fieldset>
                        <div class="form-error-message"></div>
                        <div class="form-success-message"></div>
                        <legend>After Hours Pager</legend>
                        <table>
                          <tbody>
                            <tr><td><label for="pager-form-number">Pager Number</label></td><td id="pager-form-number" name="pager-form-number"></td></tr>
                            <tr>
                              <td><label for="pager-form-alternate-number">Alternate Number</label></td><td><input type="text" id="pager-form-alternate-number" name="pager-form-alternate-number" maxlength="14"/></td>
                              <td>
                                <select id="pager-form-alternate-address" name="pager-form-alternate-address"><%
    for(EmailerUtil.SmsEmailAddress entry : EmailerUtil.SmsEmailAddress.values()) { %>
                                     <option value="<%= entry.getEmailAddress() %>"><%= entry.getProvider() %></option><%
    } %>
                                </select>
                              </td>
                            </tr>
                            <tr><td><label for="pager-form-page-prefix">Page Prefix</label></td><td><input type="text" id="pager-form-page-prefix" name="pager-form-page-prefix" maxlength="20"/></td></tr>
                            <tr>
                              <td colspan="2" class="buttons">
                                <button type="submit" class="button-edit" id="pager-form-edit-button" name="pager-form-edit-button" title="Edit">Save</button>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </fieldset>
                    </form>
                  </div>
                  <div class="cleaner">&nbsp;</div>
                </div>
              </div>
            </div> <%
} %>
          </div>
        </div>
        <div class="menu-column"><%
if(License.isLicensed(ListenFeature.CONFERENCING)
    || License.isLicensed(ListenFeature.VOICEMAIL)
    || License.isLicensed(ListenFeature.FINDME)) { %>
          <div class="menu">
            <ul><%
    if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
              <li id="menu-conferencing">Conferencing</li><%
    }
    if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
              <li id="menu-voicemail">Voicemail</li><%
    }
    if(License.isLicensed(ListenFeature.FINDME)) { %>
              <li id="menu-findme">FindMe</li><%
    } %>
            </ul>
          </div><%
}

if(subscriber != null && subscriber.getIsAdministrator()) { %>
          <hr style="width: 75%;"/>
          <div class="menu">
            <ul><%
    if(License.isLicensed(ListenFeature.ATTENDANT)) { %>
              <li id="menu-attendant">Attendant</li><%
    } %>
              <li id="menu-sysconfig">Configuration</li><%
    if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
              <li id="menu-conference-list">Conferences</li><%
    } %>
              <li id="menu-subscribers">Subscribers</li>
              <li id="menu-history">History</li>
            </ul>
          </div><%
} %>
        </div>
      </div>
    </div>

    <div id="footer">
      Listen &copy;2010 Interact Incorporated, <a href="http://www.iivip.com/">iivip.com</a>
    </div>

    <div id="templates">
      <table><tbody>
        <tr id="caller-row-template">
          <td class="caller-cell-number"></td>
          <td class="caller-cell-muteIcon"></td>
          <td class="caller-cell-dropIcon"></td>
        </tr>

        <tr id="conferencehistory-row-template">
          <td class="conferencehistory-cell-date"></td>
          <td class="conferencehistory-cell-description"></td>
        </tr>

        <tr id="pin-row-template">
          <td class="pin-cell-type"></td>
          <td class="pin-cell-number"></td>
          <td class="pin-cell-removeIcon"></td>
        </tr>

        <tr id="recording-row-template">
          <td class="recording-cell-dateCreated"></td>
          <td class="recording-cell-duration"></td>
          <td class="recording-cell-fileSize"></td>
          <td class="recording-cell-download"></td>
        </tr>

        <tr id="conference-row-template">
          <td class="conference-cell-description"></td>
          <td class="conference-cell-status"></td>
          <td class="conference-cell-callerCount"></td>
          <td class="conference-cell-view"></td>
        </tr>

        <tr id="subscriber-row-template">
          <td class="subscriber-cell-username"></td>
          <td class="subscriber-cell-accessNumbers"></td>
          <td class="subscriber-cell-lastLogin"></td>
          <td class="subscriber-cell-editButton"></td>
          <td class="subscriber-cell-deleteButton"></td>
        </tr>

        <tr id="dnis-row-template">
          <td>Number</td>
          <td><input type="text" value="" class="dnis-mapping-number"/></td>
          <td>maps to</td>
          <td>
            <select><%
if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
              <option value="conferencing">Conferencing</option><%
}
if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
              <option value="mailbox">Mailbox</option>
              <option value="voicemail">Voicemail</option>
              <option value="directVoicemail">Direct Voicemail</option><%
} %>
              <option value="custom">Custom</option>
            </select>
          </td>
          <td><input type="text" value="" class="dnis-mapping-custom-destination"/></td>
          <td><button type="button" class="icon-delete" title="Remove this phone number"></button></td>
        </tr>

        <tr id="accessNumber-row-template">
          <td><input type="text" class="accessNumber-row-number" value=""/></td>
          <td><input type="checkbox" class="accessNumber-row-messageLight"/>&nbsp;<label>Message Light</label></td>
          <td><button type="button" class="icon-delete" title="Remove this phone number"></button></td>
        </tr>

        <tr id="attendant-menu-list-row-template">
          <td class="attendant-menu-list-cell-name"></td>
          <td class="attendant-menu-list-cell-edit"></td>
          <td class="attendant-menu-list-cell-delete"></td>
        </tr>
      </tbody></table>

      <ul>
        <li id="history-call-template" class="history-row-unknown">
          <div class="history-call-date" title="Call start date"></div>
          <div class="history-call-type" title="Call history">Call</div>
          <div class="history-call-subscriber"></div>
          <div class="history-call-description"></div>
          <div class="history-call-duration" title="Duration"></div>
        </li>
        <li id="history-action-template" class="history-row-unknown">
          <div class="history-action-date" title="Date"></div>
          <div class="history-action-type" title="Action history">Action</div>
          <div class="history-action-subscriber"></div>
          <div class="history-action-description"></div>
          <div class="history-action-onSubscriber"></div>
        </li>
        <li id="voicemail-row-template">
          <div class="voicemail-cell-from"></div>
          <div class="voicemail-cell-received"></div>
          <div class="voicemail-cell-play"></div>
          <div class="voicemail-cell-actions"></div>
          <div class="voicemail-cell-transcription"></div>
          <div class="voicemail-cell-transcription-abbr"></div>
          <div class="voicemail-cell-transcription-full"></div>
          <div class="cleaner">&nbsp;</div>
        </li>
        <li id="scheduled-conference-row-template">
          <div class="scheduled-conference-cell-when"></div>
          <div class="scheduled-conference-cell-topic"></div>
          <div class="scheduled-conference-cell-callers"></div>
          <div class="scheduled-conference-cell-view"></div>
          <div class="scheduled-conference-cell-notes"></div>
          <div class="scheduled-conference-cell-activeCallers"></div>
          <div class="scheduled-conference-cell-passiveCallers"></div>
          <div class="cleaner">&nbsp;</div>
        </li>
      </ul>

      <div id="attendant-menu-action-template" class="attendant-menu-action">
        <div class="attendant-menu-action-delete"></div>
        <div class="attendant-menu-action-keypressLabel">
          <label>If Caller Presses</label>
        </div>
        <div class="attendant-menu-action-defaultLabel">
          <label>If Caller Presses Something Else</label>
        </div>
        <div class="attendant-menu-action-timeoutLabel">
          <label>If Caller Waits 5 Seconds</label><!-- TODO use property value -->
        </div>
        <div class="attendant-menu-action-keypressInput">
          <input type="text" class="attendant-menu-number-input"/>
        </div>
        <div class="attendant-menu-action-actionSelect">
          <select>
            <option value="DialNumber">Dial A Number</option>
            <option value="DialPressedNumber">Dial What They Pressed</option>
            <option value="GoToMenu">Go To A Menu</option>
            <option value="LaunchApplication">Launch An Application</option>
          </select>
        </div>
        <div class="attendant-menu-action-menuSelect">
          <select></select>
        </div>
        <div class="attendant-menu-action-dialNumberInput">
          <input type="text" class="attendant-menu-number-input"/>
        </div>
        <div class="attendant-menu-action-launchApplicationSelect">
          <select><% /* ordered alphabetically */
if(License.isLicensed(ListenFeature.CONFERENCING)) { %>
            <option value="conferencing">Conferencing</option><%
}
if(License.isLicensed(ListenFeature.VOICEMAIL)) { %>
            <option value="directVoicemail">Direct Voicemail</option>
            <option value="mailbox">Mailbox</option>
            <option value="voicemail">Voicemail</option><%
} %>
            <option value="custom">Custom</option>
          </select>
        </div>
        <div class="attendant-menu-action-launchApplicationCustomInput">
          <input type="text"/>
        </div>
      </div>
    </div>

    <div id="communication-error">Server is unavailable, please wait...</div>
    <div id="pinglatency"></div>
    <div id="latency"></div>
  </body>
</html>