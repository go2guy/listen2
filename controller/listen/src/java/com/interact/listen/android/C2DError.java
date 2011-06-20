package com.interact.listen.android;

import com.interact.insa.client.StatId;

import org.apache.log4j.Logger;

enum C2DError
{
    QuotaExceeded       (true , false, StatId.LISTEN_C2DM_QUOTA_EXCEEDED       , "Too many messages sent by the sender."),
    DeviceQuotaExceeded (true , false, StatId.LISTEN_C2DM_DEVICE_QUOTA_EXCEEDED, "Too many messages sent by the sender to a specific device."),
    InvalidRegistration (false, true , StatId.LISTEN_C2DM_INVALID_REGISTRATION , "Missing or bad registration_id."),
    NotRegistered       (false, true , StatId.LISTEN_C2DM_NOT_REGISTERED       , "The registration_id is no longer valid."),
    MessageTooBig       (false, false, StatId.LISTEN_C2DM_UNKNOWN_ERROR        , "The payload of the message is too big."),
    MissingCollapseKey  (false, false, StatId.LISTEN_C2DM_UNKNOWN_ERROR        , "Collapse key is required."),
    InvalidAuthToken    (false, false, StatId.LISTEN_C2DM_INVALID_AUTH_TOKEN   , "Invalid authorization token."),
    ServiceUnavailable  (true , false, StatId.LISTEN_C2DM_SERVICE_UNAVAILABLE  , "Service unavailable."),
    Unknown             (false, false, StatId.LISTEN_C2DM_UNKNOWN_ERROR        , "Unknown error.");

    private static final Logger LOG = Logger.getLogger(C2DError.class);

    private final boolean retry;
    private final boolean deviceInvalid;
    private final String statId;
    private final String description;

    private C2DError(boolean retry, boolean deviceInvalid, String statId, String description)
    {
        this.retry = retry;
        this.deviceInvalid = deviceInvalid;
        this.statId = statId;
        this.description = description;
    }

    public boolean isRetryable()
    {
        return retry;
    }

    public boolean isDeviceInvalid()
    {
        return deviceInvalid;
    }

    public String getStatId()
    {
        return statId;
    }

    public String getDescription()
    {
        return description;
    }
    
    public static C2DError getError(String value)
    {
        C2DError error = Unknown;
        if(value == null || value.length() == 0)
        {
            LOG.error("Empty C2DM error received ");
        }
        else
        {
            try
            {
                error = valueOf(value);
                LOG.info("Received error: " + error.name());
            }
            catch(IllegalArgumentException e)
            {
                LOG.error("Unknown C2DM error received: " + value);
                error = Unknown;
            }
        }
        return error;
    }
}
