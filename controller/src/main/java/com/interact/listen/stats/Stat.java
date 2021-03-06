package com.interact.listen.stats;

import com.interact.insa.client.StatId;

/**
 * Statistics specific to the Listen application. Each {@code Stat} is associated with its corresponding {@link StatId}
 * constant.
 */
public enum Stat
{
    API_ACCESSNUMBER_DELETE(StatId.LISTEN_CONTROLLER_API_ACCESSNUMBER_DELETE),
    API_ACCESSNUMBER_GET(StatId.LISTEN_CONTROLLER_API_ACCESSNUMBER_GET),
    API_ACCESSNUMBER_POST(StatId.LISTEN_CONTROLLER_API_ACCESSNUMBER_POST),
    API_ACCESSNUMBER_PUT(StatId.LISTEN_CONTROLLER_API_ACCESSNUMBER_PUT),
    
    API_ACTIONHISTORY_DELETE(StatId.LISTEN_CONTROLLER_API_ACTIONHISTORY_DELETE),
    API_ACTIONHISTORY_GET(StatId.LISTEN_CONTROLLER_API_ACTIONHISTORY_GET),
    API_ACTIONHISTORY_POST(StatId.LISTEN_CONTROLLER_API_ACTIONHISTORY_POST),
    API_ACTIONHISTORY_PUT(StatId.LISTEN_CONTROLLER_API_ACTIONHISTORY_PUT),
    
    API_AUDIO_DELETE(StatId.LISTEN_CONTROLLER_API_AUDIO_DELETE),
    API_AUDIO_GET(StatId.LISTEN_CONTROLLER_API_AUDIO_GET),
    API_AUDIO_POST(StatId.LISTEN_CONTROLLER_API_AUDIO_POST),
    API_AUDIO_PUT(StatId.LISTEN_CONTROLLER_API_AUDIO_PUT),

    API_CALLDETAILRECORD_DELETE(StatId.LISTEN_CONTROLLER_API_CALLDETAILRECORD_DELETE),
    API_CALLDETAILRECORD_GET(StatId.LISTEN_CONTROLLER_API_CALLDETAILRECORD_GET),
    API_CALLDETAILRECORD_POST(StatId.LISTEN_CONTROLLER_API_CALLDETAILRECORD_POST),
    API_CALLDETAILRECORD_PUT(StatId.LISTEN_CONTROLLER_API_CALLDETAILRECORD_PUT),

    API_CONFERENCE_DELETE(StatId.LISTEN_CONTROLLER_API_CONFERENCE_DELETE),
    API_CONFERENCE_GET(StatId.LISTEN_CONTROLLER_API_CONFERENCE_GET),
    API_CONFERENCE_POST(StatId.LISTEN_CONTROLLER_API_CONFERENCE_POST),
    API_CONFERENCE_PUT(StatId.LISTEN_CONTROLLER_API_CONFERENCE_PUT),

    API_CONFERENCEHISTORY_DELETE(StatId.LISTEN_CONTROLLER_API_CONFERENCEHISTORY_DELETE),
    API_CONFERENCEHISTORY_GET(StatId.LISTEN_CONTROLLER_API_CONFERENCEHISTORY_GET),
    API_CONFERENCEHISTORY_POST(StatId.LISTEN_CONTROLLER_API_CONFERENCEHISTORY_POST),
    API_CONFERENCEHISTORY_PUT(StatId.LISTEN_CONTROLLER_API_CONFERENCEHISTORY_PUT),

    API_CONFERENCERECORDING_DELETE(StatId.LISTEN_CONTROLLER_API_CONFERENCERECORDING_DELETE),
    API_CONFERENCERECORDING_GET(StatId.LISTEN_CONTROLLER_API_CONFERENCERECORDING_GET),
    API_CONFERENCERECORDING_POST(StatId.LISTEN_CONTROLLER_API_CONFERENCERECORDING_POST),
    API_CONFERENCERECORDING_PUT(StatId.LISTEN_CONTROLLER_API_CONFERENCERECORDING_PUT),
    
    API_HISTORY_DELETE(StatId.LISTEN_CONTROLLER_API_HISTORY_DELETE),
    API_HISTORY_GET(StatId.LISTEN_CONTROLLER_API_HISTORY_GET),
    API_HISTORY_POST(StatId.LISTEN_CONTROLLER_API_HISTORY_POST),
    API_HISTORY_PUT(StatId.LISTEN_CONTROLLER_API_HISTORY_PUT),

    API_LISTENSPOTSUBSCRIBER_DELETE(StatId.LISTEN_CONTROLLER_API_LISTENSPOTSUBSCRIBER_DELETE),
    API_LISTENSPOTSUBSCRIBER_GET(StatId.LISTEN_CONTROLLER_API_LISTENSPOTSUBSCRIBER_GET),
    API_LISTENSPOTSUBSCRIBER_POST(StatId.LISTEN_CONTROLLER_API_LISTENSPOTSUBSCRIBER_POST),
    API_LISTENSPOTSUBSCRIBER_PUT(StatId.LISTEN_CONTROLLER_API_LISTENSPOTSUBSCRIBER_PUT),

    API_PARTICIPANT_DELETE(StatId.LISTEN_CONTROLLER_API_PARTICIPANT_DELETE),
    API_PARTICIPANT_GET(StatId.LISTEN_CONTROLLER_API_PARTICIPANT_GET),
    API_PARTICIPANT_POST(StatId.LISTEN_CONTROLLER_API_PARTICIPANT_POST),
    API_PARTICIPANT_PUT(StatId.LISTEN_CONTROLLER_API_PARTICIPANT_PUT),

    API_PIN_DELETE(StatId.LISTEN_CONTROLLER_API_PIN_DELETE),
    API_PIN_GET(StatId.LISTEN_CONTROLLER_API_PIN_GET),
    API_PIN_POST(StatId.LISTEN_CONTROLLER_API_PIN_POST),
    API_PIN_PUT(StatId.LISTEN_CONTROLLER_API_PIN_PUT),

    API_SUBSCRIBER_DELETE(StatId.LISTEN_CONTROLLER_API_SUBSCRIBER_DELETE),
    API_SUBSCRIBER_GET(StatId.LISTEN_CONTROLLER_API_SUBSCRIBER_GET),
    API_SUBSCRIBER_POST(StatId.LISTEN_CONTROLLER_API_SUBSCRIBER_POST),
    API_SUBSCRIBER_PUT(StatId.LISTEN_CONTROLLER_API_SUBSCRIBER_PUT),

    API_VOICEMAIL_DELETE(StatId.LISTEN_CONTROLLER_API_VOICEMAIL_DELETE),
    API_VOICEMAIL_GET(StatId.LISTEN_CONTROLLER_API_VOICEMAIL_GET),
    API_VOICEMAIL_POST(StatId.LISTEN_CONTROLLER_API_VOICEMAIL_POST),
    API_VOICEMAIL_PUT(StatId.LISTEN_CONTROLLER_API_VOICEMAIL_PUT),

    GUI_DELETE_ATTENDANT_MENU(StatId.LISTEN_CONTROLLER_GUI_DELETE_ATTENDANT_MENU),
    GUI_DELETE_SUBSCRIBER(StatId.LISTEN_CONTROLLER_GUI_DELETE_SUBSCRIBER),
    GUI_DELETE_VOICEMAIL(StatId.LISTEN_CONTROLLER_GUI_DELETE_VOICEMAIL),
    GUI_DISABLE_SUBSCRIBER(StatId.LISTEN_CONTROLLER_GUI_DISABLE_SUBSCRIBER),
    GUI_DOWNLOAD_VOICEMAIL(StatId.LISTEN_CONTROLLER_GUI_DOWNLOAD_VOICEMAIL),
    GUI_DROP_PARTICIPANT(StatId.LISTEN_CONTROLLER_GUI_DROP_PARTICIPANT),
    GUI_EDIT_SUBSCRIBER(StatId.LISTEN_CONTROLLER_GUI_EDIT_SUBSCRIBER),
    GUI_GET_ACD_CONFIGURATION(StatId.LISTEN_CONTROLLER_GUI_GET_ACD_CONFIGURATION),
    GUI_GET_ATTENDANT_MENU_LIST(StatId.LISTEN_CONTROLLER_GUI_GET_ATTENDANT_MENU_LIST),
    GUI_GET_CONFERENCE_INFO(StatId.LISTEN_CONTROLLER_GUI_GET_CONFERENCE_INFO),
    GUI_GET_CONFERENCE_LIST(StatId.LISTEN_CONTROLLER_GUI_GET_CONFERENCE_LIST),
    GUI_GET_CONFERENCE_PARTICIPANTS(StatId.LISTEN_CONTROLLER_GUI_GET_CONFERENCE_PARTICIPANTS),
    GUI_GET_CONFERENCE_RECORDING(StatId.LISTEN_CONTROLLER_GUI_GET_CONFERENCE_RECORDING),
    GUI_GET_CONFERENCE_RECORDING_LIST(StatId.LISTEN_CONTROLLER_GUI_GET_CONFERENCE_RECORDING_LIST),
    GUI_GET_FINDME_CONFIGURATION(StatId.LISTEN_CONTROLLER_GUI_GET_FINDME_CONFIGURATION),
    GUI_GET_SUBSCRIBER(StatId.LISTEN_CONTROLLER_GUI_GET_SUBSCRIBER),
    GUI_GET_HISTORY_LIST(StatId.LISTEN_CONTROLLER_GUI_GET_HISTORY_LIST),
    GUI_GET_SCHEDULED_CONFERENCE_LIST(StatId.LISTEN_CONTROLLER_GUI_GET_SCHEDULED_CONFERENCE_LIST),
    GUI_GET_SUBSCRIBER_LIST(StatId.LISTEN_CONTROLLER_GUI_GET_SUBSCRIBER_LIST),
    GUI_GET_PROPERTIES(StatId.LISTEN_CONTROLLER_GUI_GET_PROPERTIES),
    GUI_GET_VOICEMAIL_LIST(StatId.LISTEN_CONTROLLER_GUI_GET_VOICEMAIL_LIST),
    GUI_LOGIN(StatId.LISTEN_CONTROLLER_GUI_LOGIN),
    GUI_LOGOUT(StatId.LISTEN_CONTROLLER_GUI_LOGOUT),
    GUI_MARK_VOICEMAIL_READ_STATUS(StatId.LISTEN_CONTROLLER_GUI_MARK_VOICEMAIL_READ_STATUS),
    GUI_MUTE_PARTICIPANT(StatId.LISTEN_CONTROLLER_GUI_MUTE_PARTICIPANT),
    GUI_OUTDIAL(StatId.LISTEN_CONTROLLER_GUI_OUTDIAL_PARTICIPANT),
    GUI_ADD_SUBSCRIBER(StatId.LISTEN_CONTROLLER_GUI_PROVISION_ACCOUNT),
    GUI_SAVE_ACD_CONFIGURATION(StatId.LISTEN_CONTROLLER_GUI_SAVE_ACD_CONFIGURATION),
    GUI_SAVE_ATTENDANT_MENU(StatId.LISTEN_CONTROLLER_GUI_SAVE_ATTENDANT_MENU),
    GUI_SAVE_FINDME_CONFIGURATION(StatId.LISTEN_CONTROLLER_GUI_SAVE_FINDME_CONFIGURATION),
    GUI_SCHEDULE_CONFERENCE(StatId.LISTEN_CONTROLLER_GUI_SCHEDULE_CONFERENCE),
    GUI_SET_PROPERTIES(StatId.LISTEN_CONTROLLER_GUI_SET_PROPERTIES),
    GUI_START_RECORDING(StatId.LISTEN_CONTROLLER_GUI_START_RECORDING),
    GUI_STOP_RECORDING(StatId.LISTEN_CONTROLLER_GUI_STOP_RECORDING),
    GUI_UNMUTE_PARTICIPANT(StatId.LISTEN_CONTROLLER_GUI_UNMUTE_PARTICIPANT),
    GUI_TEST_NOTIFICATION_SETTINGS(StatId.LISTEN_CONTROLLER_GUI_TEST_NOTIFICATION_SETTINGS),
    GUI_EDIT_PAGER(StatId.LISTEN_CONTROLLER_GUI_EDIT_PAGER),
    GUI_GOOGLE_AUTH_POST(StatId.LISTEN_CONTROLLER_GUI_GOOGLE_AUTH_POST),

    PUBLISHED_EVENT_TO_SPOT(StatId.LISTEN_CONTROLLER_PUBLISHED_EVENT_TO_SPOT),
    ADMIN_PARTICIPANT_JOIN(StatId.LISTEN_CONTROLLER_ADMIN_PARTICIPANT_JOIN),
    ACTIVE_PARTICIPANT_JOIN(StatId.LISTEN_CONTROLLER_ACTIVE_PARTICIPANT_JOIN),
    PASSIVE_PARTICIPANT_JOIN(StatId.LISTEN_CONTROLLER_PASSIVE_PARTICIPANT_JOIN),
    CONFERENCE_START(StatId.LISTEN_CONTROLLER_CONFERENCE_START),
    CONFERENCE_LENGTH(StatId.LISTEN_CONTROLLER_CONFERENCE_LENGTH),
    CONFERENCE_RECORDING_START(StatId.LISTEN_CONTROLLER_CONFERENCE_RECORDING_START),
    CONFERENCE_RECORDING_STOP(StatId.LISTEN_CONTROLLER_CONFERENCE_RECORDING_STOP),
    VOICEMAIL_EMAIL_NOTIFICATION(StatId.LISTEN_CONTROLLER_VOICEMAIL_EMAIL_NOTIFICATION),
    VOICEMAIL_SMS_NOTIFICATION(StatId.LISTEN_CONTROLLER_VOICEMAIL_SMS_NOTIFICATION),
    VOICEMAIL_PAGE_SENT(StatId.LISTEN_CONTROLLER_VOICEMAIL_PAGE_SENT),
    VOICEMAIL_ALTERNATE_NUMBER_PAGE_SENT(StatId.LISTEN_CONTROLLER_VOICEMAIL_ALTERNATE_PAGE_SENT),
    CONTROLLER_STARTUP(StatId.LISTEN_CONTROLLER_STARTUP),

    META_API_GET_DNIS(StatId.LISTEN_CONTROLLER_META_API_GET_DNIS),
    META_API_GET_PAGER_NUMBERS(StatId.LISTEN_CONTROLLER_META_API_GET_PAGER_NUMBERS),
    META_API_GET_MENU_ACTION(StatId.LISTEN_CONTROLLER_META_API_GET_MENU_ACTION),
    META_API_GET_DEVICE_REGISTER(StatId.LISTEN_CONTROLLER_META_API_GET_DEVICE_REGISTER),
    META_API_PUT_DEVICE_REGISTER(StatId.LISTEN_CONTROLLER_META_API_PUT_DEVICE_REGISTER),
    META_API_GET_EMAIL_CONTACTS(StatId.LISTEN_CONTROLLER_META_API_GET_EMAIL_CONTACTS),
    META_API_GET_NUMBER_CONTACTS(StatId.LISTEN_CONTROLLER_META_API_GET_NUMBER_CONTACTS),
    
    C2DM_DISCARD_DUE_TO_RETRYS(StatId.LISTEN_C2DM_DISCARD_DUE_TO_RETRYS),
    C2DM_QUOTA_EXCEEDED(StatId.LISTEN_C2DM_QUOTA_EXCEEDED),
    C2DM_DEVICE_QUOTA_EXCEEDED(StatId.LISTEN_C2DM_DEVICE_QUOTA_EXCEEDED),
    C2DM_SERVICE_UNAVAILABLE(StatId.LISTEN_C2DM_SERVICE_UNAVAILABLE),
    C2DM_INVALID_REGISTRATION(StatId.LISTEN_C2DM_INVALID_REGISTRATION),
    C2DM_NOT_REGISTERED(StatId.LISTEN_C2DM_NOT_REGISTERED),
    C2DM_INVALID_AUTH_TOKEN(StatId.LISTEN_C2DM_INVALID_AUTH_TOKEN),
    C2DM_UNKNOWN_ERROR(StatId.LISTEN_C2DM_UNKNOWN_ERROR),
    C2DM_QUEUED_MESSAGE(StatId.LISTEN_C2DM_QUEUED_MESSAGE),
    C2DM_QUEUED_RETRY(StatId.LISTEN_C2DM_QUEUED_RETRY),
    C2DM_SENT_SUCCESFULLY(StatId.LISTEN_C2DM_SENT_SUCCESFULLY),
    C2DM_REGISTERED_DEVICE(StatId.LISTEN_C2DM_REGISTERED_DEVICE),
    C2DM_UNREGISTERED_DEVICE(StatId.LISTEN_C2DM_UNREGISTERED_DEVICE);
    
    private String statId;
    
    private Stat(String statId)
    {
        this.statId = statId;
    }

    public String getStatId()
    {
        return statId;
    }
}
