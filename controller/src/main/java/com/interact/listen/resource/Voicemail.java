package com.interact.listen.resource;

import com.interact.listen.EmailerService;
import com.interact.listen.PersistenceService;
import com.interact.listen.c2dm.C2DMessaging;
import com.interact.listen.history.HistoryService;
import com.interact.listen.spot.*;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;
import com.interact.listen.stats.StatSenderFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;

@Entity
public class Voicemail extends Audio implements Serializable
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(Voicemail.class);
    
    private static final String TRANSCRIPTION_PENDING = "Transcription pending";

    @JoinColumn(name = "FORWARDED_BY_SUBSCRIBER_ID", nullable = true)
    @ManyToOne
    private Subscriber forwardedBy;

    @Column(name = "IS_NEW", nullable = true)
    private Boolean isNew = Boolean.TRUE;

    @Column(name = "LEFT_BY", nullable = true)
    private String leftBy;

    @JoinColumn(name = "SUBSCRIBER_ID")
    @ManyToOne
    private Subscriber subscriber;
    
    @Column(name = "HAS_NOTIFIED", nullable = true)
    private Boolean hasNotified = Boolean.FALSE;
    
    @Column(name = "LEFT_BY_NAME", nullable = true)
    private String leftByName = "";

    @Transient
    private MessageLightToggler messageLightToggler = new SpotSystemMessageLightToggler();

    public enum MessageLightState
    {
        ON, OFF;
    }

    public Subscriber getForwardedBy()
    {
        return forwardedBy;
    }

    public void setForwardedBy(Subscriber forwardedBy)
    {
        this.forwardedBy = forwardedBy;
    }

    public Boolean getIsNew()
    {
        return isNew;
    }

    public void setIsNew(Boolean isNew)
    {
        this.isNew = isNew;
    }

    public String getLeftBy()
    {
        return leftBy;
    }

    public void setLeftBy(String leftBy)
    {
        this.leftBy = leftBy;
    }

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }
    
    public Boolean getHasNotified()
    {
        return hasNotified;
    }

    public void setHasNotified(Boolean hasNotified)
    {
        this.hasNotified = hasNotified;
    }

    public void useMessageLightToggler(MessageLightToggler toggler)
    {
        this.messageLightToggler = toggler;
    }
    
    public String getLeftByName()
    {
        return leftByName;
    }

    public void setLeftByName(String leftByName)
    {
        this.leftByName = leftByName;
    }
    
    public String friendlyFrom()
    {
        if(!this.leftByName.equals(""))
        {
            return this.leftByName + " (" + this.leftBy + ")"; 
        }
        
        return this.leftBy;
    }

    @Override
    public boolean validate()
    {
        super.validate();

        if(subscriber == null)
        {
            addToErrors("subscriber cannot be null");
        }

        if(isNew == null)
        {
            addToErrors("isNew cannot be null");
        }

        return !hasErrors();
    }

    @Override
    public Voicemail copy(boolean withIdAndVersion)
    {
        Voicemail copy = new Voicemail();
        if(withIdAndVersion)
        {
            copy.setId(getId());
            copy.setVersion(getVersion());
        }

        copy.setDateCreated(getDateCreated() == null ? null : new Date(getDateCreated().getTime()));
        copy.setDescription(getDescription());
        copy.setDuration(getDuration());
        copy.setFileSize(getFileSize());
        copy.setForwardedBy(getForwardedBy());
        copy.setIsNew(isNew);
        copy.setLeftBy(leftBy);
        copy.setSubscriber(subscriber);
        copy.setTranscription(getTranscription());
        copy.setUri(getUri());
        copy.setLeftByName(leftByName);
        return copy;
    }
    
    @Override
    public void beforeSave(PersistenceService persistenceService, HistoryService historyService)
    {
        updateLeftByName(persistenceService);
    }
    
    @Override
    public void afterSave(PersistenceService persistenceService, HistoryService historyService)
    {
        if(!getTranscription().equals(TRANSCRIPTION_PENDING))
        {
            sendNotification(persistenceService);
        }
        
        if(getForwardedBy() == null)
        {
            historyService.writeLeftVoicemail(this);
        }
        else
        {
            historyService.writeForwardedVoicemail(this);
        }
        sendDeviceSync(persistenceService);

        messageLightToggler.toggleMessageLight(persistenceService, getSubscriber());
    }

    @Override
    public void afterDelete(PersistenceService persistenceService, HistoryService historyService)
    {
        historyService.writeDeletedVoicemail(this);

        SpotSystem spotSystem = new SpotSystem(persistenceService.getCurrentSubscriber());
        try
        {
            spotSystem.deleteArtifact(getUri());
        }
        catch(SpotCommunicationException e)
        {
            LOG.error(e);
        }
        catch(IOException e)
        {
            LOG.error(e);
        }
        sendDeviceSync(persistenceService);

        messageLightToggler.toggleMessageLight(persistenceService, getSubscriber());
    }
    
    @Override
    public void beforeUpdate(PersistenceService persistenceService, HistoryService historyService, Resource original)
    {
        Voicemail originalVoicemail = (Voicemail)original;
        
        if(!this.leftBy.equals(originalVoicemail.getLeftBy()))
        {
            updateLeftByName(persistenceService);
        }
    }

    @Override
    public void afterUpdate(PersistenceService persistenceService, HistoryService historyService, Resource original)
    {
        Voicemail originalVoicemail = (Voicemail)original;
        
        //Only send a notification for a new message that had it's transcription updated from "Transcription pending" 
        //to something that is not "Transcription pending"
        if(getIsNew() && originalVoicemail.getTranscription().equals(TRANSCRIPTION_PENDING)
           && !getTranscription().equals(TRANSCRIPTION_PENDING))
        {
            sendNotification(persistenceService);
        }
        sendDeviceSync(persistenceService);

        messageLightToggler.toggleMessageLight(persistenceService, getSubscriber());
    }

    public static Voicemail queryById(Session session, Long id)
    {
        return (Voicemail)session.get(Voicemail.class, id);
    }

    public static List<Voicemail> queryBySubscriberPaged(Session session, Subscriber subscriber, int first, int max,
                                                         boolean bubbleNew, String sort, boolean ascending)
    {
        LOG.debug("Querying voicemails, paged by subscriber [" + subscriber + "]; first = [" + first + "], max = [" +
                  max + "], sort = [" + sort + "], ascending = [" + ascending + "], bubble = [" + bubbleNew + "]");

        DetachedCriteria subquery = DetachedCriteria.forClass(Voicemail.class);
        subquery.createAlias("subscriber", "subscriber_alias");
        subquery.add(Restrictions.eq("subscriber_alias.id", subscriber.getId()));
        subquery.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        subquery.setProjection(Projections.id());

        Criteria criteria = session.createCriteria(Voicemail.class);
        criteria.add(Subqueries.propertyIn("id", subquery));

        criteria.setFirstResult(first);
        criteria.setMaxResults(max);

        if(bubbleNew)
        {
            criteria.addOrder(Order.desc("isNew")).addOrder(ascending ? Order.asc(sort) : Order.desc(sort));
        }
        else
        {
            criteria.addOrder(ascending ? Order.asc(sort) : Order.desc(sort));
        }
//        
//        if(bubbleNew)
//        {
//            criteria.addOrder(Order.desc("isNew")).addOrder(Order.asc("dateCreated"));
//        }
//        else
//        {
//            criteria.addOrder(Order.desc("dateCreated"));
//        }

        criteria.setFetchMode("subscriber", FetchMode.SELECT);
        criteria.setFetchMode("forwardedBy", FetchMode.SELECT);

        return (List<Voicemail>)criteria.list();
    }

    public static Long countBySubscriber(Session session, Subscriber subscriber)
    {
        Criteria criteria = session.createCriteria(Voicemail.class);
        criteria.setProjection(Projections.rowCount());
        criteria.createAlias("subscriber", "subscriber_alias");
        criteria.add(Restrictions.eq("subscriber_alias.id", subscriber.getId()));
        return (Long)criteria.list().get(0);
    }

    public static Long countNewBySubscriber(Session session, Subscriber subscriber)
    {
        Criteria criteria = session.createCriteria(Voicemail.class);
        criteria.add(Restrictions.eq("isNew", true));
        criteria.createAlias("subscriber", "subscriber_alias");
        criteria.add(Restrictions.eq("subscriber_alias.id", subscriber.getId()));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());
        return (Long)criteria.list().get(0);
    }

    public static List<Voicemail> queryNewVoicemailsBySubscriberList(Session session, List<Long> subscriberIds)
    {
        Criteria criteria = session.createCriteria(Voicemail.class);
        
        // only new records
        criteria.add(Restrictions.eq("isNew", true));

        // belonging to this subscriber
        criteria.createAlias("subscriber", "subscriber_alias");
        criteria.add(org.hibernate.criterion.Property.forName("subscriber_alias.id").in(subscriberIds));

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return (ArrayList<Voicemail>)criteria.list();
    }
    
    private void sendDeviceSync(PersistenceService persistenceService)
    {
        if(persistenceService != null)
        {
            final Subscriber vSubscriber = (Subscriber)persistenceService.get(Subscriber.class, getSubscriber().getId());
            final Session session = persistenceService.getSession();
            final String deviceId = persistenceService.getCurrentDeviceId();
            final C2DMessaging.Type type = C2DMessaging.Type.SYNC_VOICEMAILS;
            
            C2DMessaging.INSTANCE.enqueueDeviceSyncMessage(session, vSubscriber, type, deviceId);
        }
    }
    
    private void sendNotification(PersistenceService persistenceService)
    {
        EmailerService emailService = new EmailerService(persistenceService);
        StatSender statSender = StatSenderFactory.getStatSender();
        Subscriber voicemailSubscriber = (Subscriber)persistenceService.get(Subscriber.class, getSubscriber().getId());

        if(voicemailSubscriber.shouldSendNewVoicemailEmail())
        {
            LOG.debug("Sending email notification for [" + getId() + "] to subscriber [" +
                      voicemailSubscriber.getEmailAddress() + "]");
            statSender.send(Stat.VOICEMAIL_EMAIL_NOTIFICATION);
            emailService.sendEmailVoicmailNotification(this, voicemailSubscriber);
        }

        if(voicemailSubscriber.shouldSendNewVoicemailSms())
        {
            //only send this one-off notification if they aren't getting pages every 10 mins already
            if(!voicemailSubscriber.getIsSubscribedToPaging().booleanValue())
            {
                LOG.debug("Sending SMS notification for [" + getId() + "] to subscriber [" + voicemailSubscriber.getSmsAddress() + "]");
                statSender.send(Stat.VOICEMAIL_SMS_NOTIFICATION);
                emailService.sendSmsVoicemailNotification(this, voicemailSubscriber);
            }
        }
    }
    
    private void updateLeftByName(PersistenceService persistenceService)
    {
        String potentialRealName = AccessNumber.querySubscriberNameByAccessNumber(persistenceService.getSession(), this.leftBy);
        
        if(!potentialRealName.equals(""))
        {
            this.leftByName = potentialRealName;
        }
    }
}
