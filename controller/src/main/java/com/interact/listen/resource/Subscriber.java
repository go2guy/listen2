package com.interact.listen.resource;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.c2dm.C2DMessaging;
import com.interact.listen.exception.NumberAlreadyInUseException;
import com.interact.listen.exception.UnauthorizedModificationException;
import com.interact.listen.history.HistoryService;
import com.interact.listen.resource.CallRestriction.Directive;
import com.interact.listen.resource.DeviceRegistration.DeviceType;
import com.interact.listen.resource.TimeRestriction.Action;
import com.interact.listen.spot.SpotCommunicationException;
import com.interact.listen.spot.SpotSystem;
import com.interact.listen.util.ComparisonUtil;
import com.interact.listen.util.WildcardNumberMatcher;
import com.interact.listen.util.WildcardNumberMatcherImpl;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

@Entity
@Table(name = "subscriber")
public class Subscriber extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(Subscriber.class);

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @javax.persistence.Version
    private Integer version = Integer.valueOf(0);

    @OneToMany(mappedBy = "subscriber", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.EAGER)
    private Set<AccessNumber> accessNumbers = new HashSet<AccessNumber>();

    @Column(name = "VOICEMAIL_PIN")
    private String voicemailPin;

    @OneToMany(mappedBy = "subscriber", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
    private List<Voicemail> voicemails = new ArrayList<Voicemail>();

    @OneToMany(mappedBy = "subscriber", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.EAGER)
    private Set<DeviceRegistration> devices = new HashSet<DeviceRegistration>();

    @Column(name = "USERNAME", nullable = false, unique = true)
    private String username;

    @Column(name = "PASSWORD", nullable = true)
    private String password;

    @Column(name = "REAL_NAME")
    private String realName;

    @Column(name = "LAST_LOGIN")
    private Date lastLogin;

    /*@Column(name = "IS_ADMINISTRATOR")
    private Boolean isAdministrator = Boolean.FALSE;*/

    @OneToMany(mappedBy = "subscriber", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.EAGER)
    private Set<Conference> conferences = new HashSet<Conference>();

    @Column(name = "EMAIL_NOTIFICATION_ENABLED")
    private Boolean isEmailNotificationEnabled = Boolean.FALSE;

    @Column(name = "SMS_NOTIFICATION_ENABLED")
    private Boolean isSmsNotificationEnabled = Boolean.FALSE;

    @Column(name = "EMAIL_ADDRESS")
    private String emailAddress = "";

    @Column(name = "SMS_ADDRESS")
    private String smsAddress = "";

    @Column(name = "IS_SUBSCRIBED_TO_PAGING", nullable = false)
    private Boolean isSubscribedToPaging = Boolean.FALSE;

    @Column(name = "IS_SUBSCRIBED_TO_TRANSCRIPTION", nullable = false)
    private Boolean isSubscribedToTranscription = Boolean.FALSE;

    @Column(name = "VOICEMAIL_PLAYBACK_ORDER", nullable = false)
    @Enumerated(EnumType.STRING)
    private PlaybackOrder voicemailPlaybackOrder = PlaybackOrder.OLDEST_TO_NEWEST;

    @Column(name = "IS_ACTIVE_DIRECTORY", nullable = false)
    private Boolean isActiveDirectory = Boolean.FALSE;

    @Column(name = "WORK_EMAIL_ADDRESS", nullable = false)
    private String workEmailAddress = "";

    @Column(name = "FIND_ME_EXPIRATION", nullable = true)
    private Date findMeExpiration; // null means the configuration IS expired

    @Column(name = "SEND_FIND_ME_REMINDER", nullable = true)
    private Boolean sendFindMeReminder = Boolean.FALSE;

    @Column(name = "FIND_ME_REMINDER_DESTINATION", nullable = true)
    private String findMeReminderDestination;
    
    @Column(name = "ROLE", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    public enum PlaybackOrder
    {
        NEWEST_TO_OLDEST, OLDEST_TO_NEWEST;
    }
    
    public enum Role
    {
        CUSTODIAN, ADMINISTRATOR, USER;
    }

    public Set<AccessNumber> getAccessNumbers()
    {
        return accessNumbers;
    }

    public void setAccessNumbers(Set<AccessNumber> accessNumbers)
    {
        this.accessNumbers = accessNumbers;
        for(AccessNumber accessNumber : accessNumbers)
        {
            accessNumber.setSubscriber(this);
        }
    }

    public void addToAccessNumbers(AccessNumber accessNumber)
    {
        accessNumber.setSubscriber(this);
        this.accessNumbers.add(accessNumber);
    }

    public void removeFromAccessNumbers(AccessNumber accessNumber)
    {
        this.accessNumbers.remove(accessNumber);
        accessNumber.setSubscriber(null);
    }

    public String accessNumberString()
    {
        StringBuilder numbers = new StringBuilder();
        for(AccessNumber accessNumber : accessNumbers)
        {
            numbers.append(accessNumber.getNumber()).append(",");
        }
        if(accessNumbers.size() > 0)
        {
            numbers.deleteCharAt(numbers.length() - 1); // last comma
        }
        return numbers.toString();
    }

    public void setDevices(Set<DeviceRegistration> devices)
    {
        this.devices = devices;
        for(DeviceRegistration device : devices)
        {
            device.setSubscriber(this);
        }
    }

    public void addToDevices(DeviceRegistration device)
    {
        device.setSubscriber(this);
        this.devices.add(device);
    }

    public void removeFromDevices(DeviceRegistration device)
    {
        this.devices.remove(device);
        device.setSubscriber(null);
    }
    
    public Set<DeviceRegistration> getDevices()
    {
        return devices;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public void setId(Long id)
    {
        this.id = id;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public String getVoicemailPin()
    {
        return voicemailPin;
    }

    public void setVoicemailPin(String voicemailPin)
    {
        this.voicemailPin = voicemailPin;
    }

    public List<Voicemail> getVoicemails()
    {
        return voicemails;
    }

    public void setVoicemails(List<Voicemail> voicemails)
    {
        this.voicemails = voicemails;
    }

    public PlaybackOrder getVoicemailPlaybackOrder()
    {
        return voicemailPlaybackOrder;
    }

    public void setVoicemailPlaybackOrder(PlaybackOrder voicemailPlaybackOrder)
    {
        this.voicemailPlaybackOrder = voicemailPlaybackOrder;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getRealName()
    {
        return realName;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }

    public Date getLastLogin()
    {
        return lastLogin == null ? null : new Date(lastLogin.getTime());
    }

    public void setLastLogin(Date lastLogin)
    {
        this.lastLogin = lastLogin == null ? null : new Date(lastLogin.getTime());
    }

    @Deprecated
    public Boolean getIsAdministrator()
    {
        return role.equals(Role.ADMINISTRATOR);
    }

    @Deprecated
    public void setIsAdministrator(Boolean isAdministrator)
    {
        if(isAdministrator)
        {
            this.role = Role.ADMINISTRATOR;
        }
        else
        {
            this.role = Role.USER;
        }
    }

    public Set<Conference> getConferences()
    {
        return conferences;
    }

    public void setConferences(Set<Conference> conferences)
    {
        this.conferences = conferences;
        for(Conference conference : this.conferences)
        {
            conference.setSubscriber(this);
        }
    }

    public void addToConferences(Conference conference)
    {
        conference.setSubscriber(this);
        this.conferences.add(conference);
    }

    public void removeFromConferences(Conference conference)
    {
        conference.setSubscriber(null);
        this.conferences.remove(conference);
    }

    public Boolean getIsEmailNotificationEnabled()
    {
        return isEmailNotificationEnabled;
    }

    public void setIsEmailNotificationEnabled(Boolean isEmailNotificationEnabled)
    {
        this.isEmailNotificationEnabled = isEmailNotificationEnabled;
    }

    public Boolean getIsSmsNotificationEnabled()
    {
        return isSmsNotificationEnabled;
    }

    public void setIsSmsNotificationEnabled(Boolean isSmsNotificationEnabled)
    {
        this.isSmsNotificationEnabled = isSmsNotificationEnabled;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    public String getWorkEmailAddress()
    {
        return workEmailAddress;
    }

    public void setWorkEmailAddress(String workEmailAddress)
    {
        this.workEmailAddress = workEmailAddress;
    }

    public String getSmsAddress()
    {
        return smsAddress;
    }

    public void setSmsAddress(String smsAddress)
    {
        this.smsAddress = smsAddress;
    }

    public Boolean getIsSubscribedToPaging()
    {
        return isSubscribedToPaging;
    }

    public void setIsSubscribedToPaging(Boolean isSubscribedToPaging)
    {
        this.isSubscribedToPaging = isSubscribedToPaging;
    }

    public Boolean getIsSubscribedToTranscription()
    {
        return isSubscribedToTranscription;
    }

    public void setIsSubscribedToTranscription(Boolean isSubscribedToTranscription)
    {
        this.isSubscribedToTranscription = isSubscribedToTranscription;
    }

    public Boolean getIsActiveDirectory()
    {
        return isActiveDirectory;
    }

    public void setIsActiveDirectory(Boolean isActiveDirectory)
    {
        this.isActiveDirectory = isActiveDirectory;
    }

    public Date getFindMeExpiration()
    {
        return findMeExpiration == null ? null : new Date(findMeExpiration.getTime());
    }

    public void setFindMeExpiration(Date findMeExpiration)
    {
        this.findMeExpiration = findMeExpiration == null ? null : new Date(findMeExpiration.getTime());
    }

    public Boolean getSendFindMeReminder()
    {
        return sendFindMeReminder;
    }

    public void setSendFindMeReminder(Boolean sendFindMeReminder)
    {
        this.sendFindMeReminder = sendFindMeReminder;
    }

    public String getFindMeReminderDestination()
    {
        return findMeReminderDestination;
    }

    public void setFindMeReminderDestination(String findMeReminderDestination)
    {
        this.findMeReminderDestination = findMeReminderDestination;
    }

    public Role getRole()
    {
        return role;
    }

    public void setRole(Role role)
    {
        this.role = role;
    }

    @Override
    public boolean validate()
    {
        if(username == null || username.trim().equals(""))
        {
            addToErrors("Please provide a Username");
        }

        if(isSubscribedToTranscription == null)
        {
            addToErrors("Please provide a value for isSubscribedToTranscription");
        }

        if(voicemailPin != null && !voicemailPin.matches("^[0-9]{0,10}$"))
        {
            addToErrors("Voicemail passcode must contain 0 to 10 numeric characters");
        }

        if(isEmailNotificationEnabled && (emailAddress == null || emailAddress.equals("")))
        {
            addToErrors("Please provide an Email address");
        }

        if(isSmsNotificationEnabled && (smsAddress == null || smsAddress.equals("")))
        {
            addToErrors("Please provide an SMS address");
        }

        if(isSubscribedToPaging && (smsAddress == null || smsAddress.equals("")))
        {
            addToErrors("Please provide an SMS address for paging");
        }

        return !hasErrors();
    }

    @Override
    public Subscriber copy(boolean withIdAndVersion)
    {
        Subscriber copy = new Subscriber();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }

        copy.setIsAdministrator(role.equals(Role.ADMINISTRATOR) ? true : false);
        copy.setLastLogin(lastLogin);
        for(AccessNumber accessNumber : accessNumbers)
        {
            copy.addToAccessNumbers(accessNumber.copy(false));
        }
        for(DeviceRegistration device : devices)
        {
            copy.addToDevices(device.copy(false));
        }
        copy.setEmailAddress(emailAddress);
        //copy.setConferences(conferences); -- this screws stuff up
        copy.setIsEmailNotificationEnabled(isEmailNotificationEnabled);
        copy.setIsSmsNotificationEnabled(isSmsNotificationEnabled);
        copy.setSmsAddress(smsAddress);
        copy.setIsSubscribedToPaging(isSubscribedToPaging);
        copy.setIsSubscribedToTranscription(isSubscribedToTranscription);
        copy.setVoicemailPlaybackOrder(voicemailPlaybackOrder);
        copy.setIsActiveDirectory(isActiveDirectory);
        copy.setPassword(password);
        copy.setRealName(realName);
        copy.setUsername(username);
        copy.setVoicemailPin(voicemailPin);
        copy.setVoicemails(voicemails);
        copy.setWorkEmailAddress(workEmailAddress);
        copy.setFindMeExpiration(findMeExpiration);
        copy.setSendFindMeReminder(sendFindMeReminder);
        copy.setFindMeReminderDestination(findMeReminderDestination);
        copy.setRole(role);
        return copy;
    }

    @Override
    public boolean equals(Object that)
    {
        if(this == that)
        {
            return true;
        }

        if(that == null)
        {
            return false;
        }

        if(!(that instanceof Subscriber))
        {
            return false;
        }

        Subscriber subscriber = (Subscriber)that;

        if(!ComparisonUtil.isEqual(subscriber.getUsername(), getUsername()))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int hash = 1;
        hash *= prime + (getUsername() == null ? 0 : getUsername().hashCode());
        return hash;
    }

    @Override
    public void afterSave(PersistenceService persistenceService, HistoryService historyService)
    {
        historyService.writeCreatedSubscriber(this);
        
        if(getWorkEmailAddress() != null && getWorkEmailAddress().length() > 0)
        {
            LOG.debug("C2DM subscriber saved: " + getWorkEmailAddress());
            sendDeviceSync(persistenceService);
        }
    }

    @Override
    public void afterUpdate(PersistenceService persistenceService, HistoryService historyService, Resource original)
    {
        Subscriber subscriber = (Subscriber)original;
        if(!ComparisonUtil.isEqual(subscriber.getVoicemailPin(), getVoicemailPin()))
        {
            historyService.writeChangedVoicemailPin(this, subscriber.getVoicemailPin(), getVoicemailPin());
        }
        if(!ComparisonUtil.isEqual(subscriber.getWorkEmailAddress(), getWorkEmailAddress()))
        {
            LOG.debug("C2DM subscriber updated '" + getWorkEmailAddress() + "' from '" + subscriber.getWorkEmailAddress() + "'");
            sendDeviceSync(persistenceService);
        }
    }

    @Override
    public void afterDelete(PersistenceService persistenceService, HistoryService historyService)
    {
        historyService.writeDeletedSubscriber(this);

        if(getWorkEmailAddress() != null && getWorkEmailAddress().length() > 0)
        {
            LOG.debug("C2DM subscriber deleted: " + getWorkEmailAddress());
            sendDeviceSync(persistenceService);
        }
        
        SpotSystem spotSystem = new SpotSystem(persistenceService.getCurrentSubscriber());
        try
        {
            spotSystem.deleteAllSubscriberArtifacts(this);
        }
        catch(SpotCommunicationException e)
        {
            LOG.error(e);
        }
        catch(IOException e)
        {
            LOG.error(e);
        }
    }

    public static Subscriber queryById(Session session, Long id)
    {
        return (Subscriber)session.get(Subscriber.class, id);
    }

    public static Subscriber queryByUsername(Session session, String username)
    {
        // FIXME this query is not eagerly fetching associations, (e.g. the accessNumbers collection)
        // need to figure out why
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("username", username));
        criteria.setMaxResults(1);
        return (Subscriber)criteria.uniqueResult();
    }

    public static Long count(Session session)
    {
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());
        return (Long)criteria.list().get(0);
    }

    public static List<Subscriber> queryAll(Session session)
    {
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return (List<Subscriber>)criteria.list();
    }
    
    public static List<Subscriber> queryAllAlphabeticallyByRealName(Session session)
    {
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc("realName"));
        return (List<Subscriber>)criteria.list();
    }

    @SuppressWarnings("unchecked")
    public static List<Subscriber> queryAllPaged(Session session, int first, int max)
    {
        DetachedCriteria subquery = DetachedCriteria.forClass(Subscriber.class);
        subquery.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        subquery.setProjection(Projections.id());

        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Subqueries.propertyIn("id", subquery));
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        criteria.setFirstResult(first);
        criteria.setMaxResults(max);
        criteria.addOrder(Order.asc("id"));

        criteria.setFetchMode("accessNumbers", FetchMode.SELECT);
        criteria.setFetchMode("conferences", FetchMode.SELECT);

        return (List<Subscriber>)criteria.list();
    }

    @SuppressWarnings("unchecked")
    public static List<Subscriber> queryByIsSubscribedToPaging(Session session, boolean isSubscribedToPaging)
    {
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("isSubscribedToPaging", isSubscribedToPaging));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        criteria.setFetchMode("conference", FetchMode.SELECT);
        return (List<Subscriber>)criteria.list();
    }

    public boolean canModifyParticipant(Participant participant)
    {
        if(participant == null)
        {
            throw new IllegalArgumentException("Participant cannot be null");
        }

        if(participant.getIsAdmin())
        {
            return false;
        }

        if(getIsAdministrator())
        {
            return true;
        }

        Conference conference = participant.getConference();
        if(conference == null)
        {
            throw new IllegalStateException("Participant [" + participant.getNumber() +
                                            "] does not belong to a Conference");
        }

        // doesn't belong to a subscriber, and if we're here, they're not an admin. PWND.
        if(conference.getSubscriber() == null)
        {
            return false;
        }

        return conference.getSubscriber().equals(this);
    }

    public boolean ownsConference(Conference conference)
    {
        if(getIsAdministrator())
        {
            return true;
        }

        if(conference.getSubscriber() == null)
        {
            return false;
        }

        return conference.getSubscriber().equals(this);
    }

    public String conferenceDescription()
    {
        return friendlyName() + "'s Conference";
    }

    public String friendlyName()
    {
        return realName != null && !realName.trim().equals("") ? realName : username;
    }

    public void updateAccessNumbers(Session session, PersistenceService persistenceService, String accessNumberString,
                                    boolean allowSystem) throws NumberAlreadyInUseException,
        UnauthorizedModificationException // SUPPRESS CHECKSTYLE RedundantThrowsCheck
    {
        List<AccessNumber> newNumbers = new ArrayList<AccessNumber>();
        if(accessNumberString.length() > 0)
        {
            String[] split = accessNumberString.split(";");
            for(String an : split)
            {
                String[] parts = an.split(":");

                AccessNumber newNumber = new AccessNumber();
                newNumber.setNumber(parts[0].trim());
                newNumber.setSupportsMessageLight(Boolean.valueOf(parts[1]));

                try
                {
                    newNumber.setNumberType(AccessNumber.NumberType.valueOf(parts[2]));
                }
                catch(Exception e)
                {
                    LOG.error("Unknown number type: " + parts[2]);
                    newNumber.setNumberType(AccessNumber.NumberType.OTHER);
                }

                newNumber.setPublicNumber(Boolean.valueOf(parts[3]));

                newNumbers.add(newNumber);
            }

            updateAccessNumbers(session, persistenceService, newNumbers, allowSystem);
        }
    }
    
    public void updateAccessNumbers(Session session, PersistenceService persistenceService,
                                    List<AccessNumber> newNumbers, boolean allowSystem)
        throws NumberAlreadyInUseException, UnauthorizedModificationException // SUPPRESS CHECKSTYLE RedundantThrowsCheck
    {
        Map<String, AccessNumber> existingNumbers = new HashMap<String, AccessNumber>();
        for(AccessNumber accessNumber : AccessNumber.queryBySubscriber(session, this))
        {
            existingNumbers.put(accessNumber.getNumber(), accessNumber);
        }

        for(Map.Entry<String, AccessNumber> entry : existingNumbers.entrySet())
        {
            if(!newNumbers.contains(entry.getValue()))
            {
                if(entry.getValue().getNumberType().isSystem() && !allowSystem)
                {
                    continue;
                }
//                if(entry.getValue().getNumberType().isSystem() && !allowSystem)
//                {
//                    throw new UnauthorizedModificationException("Attempted delete on system access number '" +
//                                                                entry.getValue().getNumber() + "' by non-admin.");
//                }
                persistenceService.delete(entry.getValue());
            }
        }

        for(AccessNumber newNumber : newNumbers)
        {
            AccessNumber result = AccessNumber.queryByNumber(session, newNumber.getNumber());
            if(result != null && !result.getSubscriber().equals(this))
            {
                throw new NumberAlreadyInUseException(newNumber.getNumber());
            }
            else if(result == null)
            {
                if(newNumber.getNumberType().isSystem() && !allowSystem)
                {
                    throw new UnauthorizedModificationException("Attempted creation of system access number '" +
                                                                newNumber.getNumber() + "' by non-admin.");
                }
                newNumber.setSubscriber(this);
                addToAccessNumbers(newNumber);
                persistenceService.save(newNumber);
            }
            else if(!ComparisonUtil.isEqual(newNumber.getSupportsMessageLight(), result.getSupportsMessageLight()) ||
                    newNumber.getNumberType() != result.getNumberType() ||
                    newNumber.getPublicNumber().booleanValue() != result.getPublicNumber().booleanValue() ||
                    !newNumber.getForwardedTo().equals(result.getForwardedTo()))
            {
                AccessNumber original = result.copy(false);

                if(result.getNumberType().isSystem() && !allowSystem)
                {
                    result.setForwardedTo(newNumber.getForwardedTo());
                    persistenceService.update(result, original);
                    continue;
                }

                // updating an existing record
                result.setSupportsMessageLight(newNumber.getSupportsMessageLight());
                result.setNumberType(newNumber.getNumberType());
                result.setPublicNumber(newNumber.getPublicNumber());
                result.setForwardedTo(newNumber.getForwardedTo());
                persistenceService.update(result, original);
            }
        }
    }
    
    private void sendDeviceSync(PersistenceService persistenceService)
    {
        if(persistenceService != null)
        {
            final Session session = persistenceService.getSession();
            final C2DMessaging.Type type = C2DMessaging.Type.SYNC_CONTACTS;
            
            C2DMessaging.INSTANCE.enqueueAllSyncMessages(session, DeviceType.ANDROID, type, null);
        }
    }

    public boolean shouldSendNewVoicemailEmail()
    {
        if(!isEmailNotificationEnabled.booleanValue())
        {
            return false;
        }

        if(emailAddress == null || emailAddress.trim().equals(""))
        {
            return false;
        }

        return isNotificationAllowedForTime(Action.NEW_VOICEMAIL_EMAIL, new LocalDateTime());
    }

    public boolean shouldSendNewVoicemailSms()
    {
        if(!isSmsNotificationEnabled.booleanValue())
        {
            return false;
        }

        if(smsAddress == null || smsAddress.trim().equals(""))
        {
            return false;
        }

        return isNotificationAllowedForTime(Action.NEW_VOICEMAIL_SMS, new LocalDateTime());
    }

    public boolean canDial(Session session, String destination)
    {
        List<String> denied = queryRestrictions(session, Directive.DENY);
        List<String> allowed = queryRestrictions(session, Directive.ALLOW);
        
        return canDial(session, destination, denied, allowed);
    }
    
    public boolean canDial(Session session, String destination, List<String> denied, List<String> allowed)
    {
        // query to see if any deny records exist for this subscriber or for everyone
        
        LOG.debug("Found [" + denied.size() + "] denied numbers");
        if(denied.size() == 0)
        {
            return true; // no restrictions in place, everything is allowed
        }

        WildcardNumberMatcher matcher = new WildcardNumberMatcherImpl();

        boolean isDenied = matcher.findMatch(destination, denied);
        if(!isDenied)
        {
            return true; // if no deny match, the subscriber can call
        }

        // There was a match, now query all the allow records to see if they have an overriding allow
        LOG.debug("Found [" + allowed.size() + "] allowed numbers");
        return matcher.findMatch(destination, allowed);
    }

    private List<String> queryRestrictions(Session session, Directive directive)
    {
        LOG.debug("Sub = [" + this.getId() + "], dir = [" + directive + "]");
        List<CallRestriction> res = CallRestriction.queryEveryoneAndSubscriberSpecficByDirective(session, this, directive);
        return extractDestinationsFromRestrictions(res);
    }

    private List<String> extractDestinationsFromRestrictions(List<CallRestriction> restrictions)
    {
        List<String> destinations = new ArrayList<String>();
        for(CallRestriction restriction : restrictions)
        {
            destinations.add(restriction.getDestination());
        }
        return destinations;
    }

    /**
     * Whether or not the subscriber has allowed notifications for the specified action during the specified time.
     * 
     * @param action type of notification
     * @param forDateTime date and time to see if notification is allowed for
     * @return {@code true} if notification should be sent, {@code false} otherwise
     */
    private boolean isNotificationAllowedForTime(Action action, LocalDateTime forDateTime)
    {
        // open up a new session - normally we'd have one passed in, but for this, it's just a simple,
        // non-modifying query. if we need to make it use the current session later, we can refactor.
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        try
        {
            List<TimeRestriction> restrictions = TimeRestriction.queryBySubscriberAndAction(session, this, action);
            session.flush();
            tx.commit();

            // no restrictions means they haven't configured any, which means we should always send it
            if(restrictions.size() == 0)
            {
                return true;
            }

            for(TimeRestriction range : restrictions)
            {
                // ignore it if it doesn't even apply to the target date
                if(!range.appliesToJodaDayOfWeek(forDateTime.getDayOfWeek()))
                {
                    continue;
                }

                LocalTime start = range.getStartTime();
                LocalTime end = range.getEndTime();
                LocalTime forTime = forDateTime.toLocalTime();

                // start time should be at the beginning of the minute...
                start = new LocalTime(start.getHourOfDay(), start.getMinuteOfHour(), 0, 0);
                // ... and end time should be at the end of the minute
                end = new LocalTime(end.getHourOfDay(), start.getMinuteOfHour(), 59, 999);

                // comparisons should be inclusive
                if(forTime.equals(start) || forTime.equals(end) || (forTime.isAfter(start) && forTime.isBefore(end)))
                {
                    return true;
                }
            }
            return false; // we didn't find a time period that forTime fit into
        }
        finally
        {
            session.close();
        }
    }
}
