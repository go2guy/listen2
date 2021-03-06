package com.interact.listen.resource;

import static org.junit.Assert.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.interact.listen.ListenTest;
import com.interact.listen.PersistenceService;
import com.interact.listen.history.DefaultHistoryService;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class ConferenceTest extends ListenTest
{
    private Conference conference;

    @Before
    public void setUp()
    {
        conference = new Conference();
    }

    @Test
    public void test_version_defaultsToZero()
    {
        assertEquals(Integer.valueOf(0), conference.getVersion());
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        conference.setId(id);

        assertEquals(id, conference.getId());
    }

    @Test
    public void test_setIsStarted_withValidIsStarted_setsIsStarted()
    {
        final Boolean isStarted = true;
        conference.setIsStarted(isStarted);

        assertEquals(isStarted, conference.getIsStarted());
    }
    
    @Test
    public void test_setIsRecording_withValidIsRecording_setsIsRecording()
    {
        final Boolean isRecording = true;
        conference.setIsRecording(isRecording);

        assertEquals(isRecording, conference.getIsRecording());
    }

    @Test
    public void test_setStartTime_withNull_setsNullStartTime()
    {
        conference.setIsStarted(null);
        assertEquals(null, conference.getIsStarted());
    }

    @Test
    public void test_setStartTime_withValidDate_setsDateWithDifferentInstance()
    {
        Date startTime = new Date();
        conference.setStartTime(startTime);

        assertEquals(startTime, conference.getStartTime());
        assertFalse(startTime == conference.getStartTime()); // reference comparison
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        conference.setVersion(version);

        assertEquals(version, conference.getVersion());
    }

    @Test
    public void test_validate_validParameters_returnsNoErrors()
    {
        conference = getPopulatedConference();

        assertTrue(conference.validate());
        assertFalse(conference.hasErrors());
    }

    @Test
    public void test_validate_nullDescription_returnsTrueAndHasErrors()
    {
        conference = getPopulatedConference();
        conference.setDescription(null);

        assertFalse(conference.validate());
        assertTrue(conference.hasErrors());
    }

    @Test
    public void test_validate_blankDescription_returnsTrueAndHasErrors()
    {
        conference = getPopulatedConference();
        conference.setDescription(" ");

        assertFalse(conference.validate());
        assertTrue(conference.hasErrors());
    }

    @Test
    public void test_validate_nullIsStarted_returnsHasErrors()
    {
        conference = getPopulatedConference();
        conference.setIsStarted(null);

        assertFalse(conference.validate());
        assertTrue(conference.hasErrors());
    }
    
    @Test
    public void test_validate_nullIsRecording_returnsHasErrors()
    {
        conference = getPopulatedConference();
        conference.setIsRecording(null);

        assertFalse(conference.validate());
        assertTrue(conference.hasErrors());
    }
    
    @Test
    public void test_validate_falseIsStartedAndIsRecording_returnsHasErrors()
    {
        conference = getPopulatedConference();
        conference.setIsRecording(true);
        conference.setIsStarted(false);

        assertFalse(conference.validate());
        assertTrue(conference.hasErrors());
    }

    @Test
    public void test_copy_withoutIdAndVersion_createsCopyWithoutIdAndVersion()
    {
        Conference original = getPopulatedConference();
        Conference copy = original.copy(false);

        assertTrue(original.getConferenceHistorys() == copy.getConferenceHistorys()); // same reference
        assertTrue(original.getConferenceRecordings() == copy.getConferenceRecordings()); // same reference
        assertEquals(original.getIsStarted(), copy.getIsStarted());
        assertEquals(original.getIsRecording(), copy.getIsRecording());
        assertTrue(original.getParticipants() == copy.getParticipants()); // same reference

        // pins cannot be the same reference - in fact, i don't think any collection can
        // FIXME make all collections and references deep copies
        assertFalse(original.getPins() == copy.getPins()); // different reference
        // TODO containsAll check here

        assertNull(copy.getId());
        assertEquals(Integer.valueOf(0), copy.getVersion());
    }

    @Test
    public void test_copy_withIdAndVersion_createsCopyWithIdAndVersion()
    {
        Conference original = getPopulatedConference();
        Conference copy = original.copy(true);

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getVersion(), copy.getVersion());
    }

    @Test
    public void test_equals_sameObject_returnsTrue()
    {
        assertTrue(conference.equals(conference));
    }

    @Test
    public void test_equals_thatNull_returnsFalse()
    {
        assertFalse(conference.equals(null));
    }

    @Test
    public void test_equals_thatNotAConference_returnsFalse()
    {
        assertFalse(conference.equals(new String()));
    }

    @Test
    public void test_equals_descriptionNotEqual_returnsFalse()
    {
        conference.setDescription("foo");

        Conference that = new Conference();
        that.setDescription(null);

        assertFalse(conference.equals(that));
    }

    @Test
    public void test_equals_subscriberNotEqual_returnsFalse()
    {
        conference.setSubscriber(new Subscriber());

        Conference that = new Conference();
        that.setSubscriber(null);

        assertFalse(conference.equals(that));
    }

    @Test
    public void test_equals_allPropertiesEqual_returnsTrue()
    {
        Subscriber subscriber = new Subscriber();
        String description = String.valueOf(System.currentTimeMillis());

        conference.setDescription(description);
        conference.setSubscriber(subscriber);

        Conference that = new Conference();
        that.setDescription(description);
        that.setSubscriber(subscriber);

        assertTrue(conference.equals(that));
    }

    @Test
    public void test_hashCode_returnsUniqueHashCodeForRelevantFields()
    {
        Conference obj = new Conference();

        // hashcode-relevant properties set to static values for predictability
        obj.setDescription("Sorry Charlie");
        obj.setSubscriber(new Subscriber());

        // set a property that has no effect on hashcode to something dynamic
        obj.setStartTime(new Date());

        assertEquals(1925950052, obj.hashCode());
    }

    @Test
    public void test_afterSave_withConferenceRecordingTrue_writesTwoConferenceHistories()
    {
        PersistenceService ps = mock(PersistenceService.class);
        
        Subscriber subscriber = createSubscriber(session);
        Conference conference = createConference(session, subscriber);
        
        conference.setIsRecording(true);
        conference.afterSave(ps, new DefaultHistoryService(ps));

        verify(ps, times(2)).save(isA(ConferenceHistory.class));
    }

    @Test
    public void test_afterSave_withConferenceRecordingFalse_writesOneConferenceHistory()
    {
        PersistenceService ps = mock(PersistenceService.class);
        
        Subscriber subscriber = createSubscriber(session);
        Conference conference = createConference(session, subscriber);
        
        conference.setIsRecording(false);
        conference.afterSave(ps, new DefaultHistoryService(ps));

        verify(ps).save(isA(ConferenceHistory.class));
    }
    
    @Test
    public void test_afterUpdate_whenConferenceStartedStatusChangesToStarted_writesOneConferenceHistory()
    {
        PersistenceService ps = mock(PersistenceService.class);
        
        Subscriber subscriber = createSubscriber(session);
        Conference original = createConference(session, subscriber);
        Conference updated = original.copy(true);
        
        original.setIsStarted(false);
        updated.setIsStarted(true);

        original.setIsRecording(false); // keep recording the same, we don't want to write the recording history
        updated.setIsRecording(false);
        
        updated.afterUpdate(ps, new DefaultHistoryService(ps), original);
        
        verify(ps).save(isA(ConferenceHistory.class));
    }
    
    @Test
    public void test_afterUpdate_whenConferenceStartedStatusChangesToStopped_writesOneConferenceHistory()
    {
        PersistenceService ps = mock(PersistenceService.class);
        
        Subscriber subscriber = createSubscriber(session);
        Conference original = createConference(session, subscriber);
        original.setStartTime(new Date());
        Conference updated = original.copy(true);
        
        original.setIsStarted(true);
        updated.setIsStarted(false);

        original.setIsRecording(false); // keep recording the same, we don't want to write the recording history
        updated.setIsRecording(false);
        
        updated.afterUpdate(ps, new DefaultHistoryService(ps), original);
        
        verify(ps).save(isA(ConferenceHistory.class));
    }
    
    @Test
    public void test_afterUpdate_whenConferenceRecordingStatusChangesToTrue_writesOneConferenceHistory()
    {
        PersistenceService ps = mock(PersistenceService.class);
        
        Subscriber subscriber = createSubscriber(session);
        Conference original = createConference(session, subscriber);
        Conference updated = original.copy(true);

        original.setIsRecording(false);
        updated.setIsRecording(true);
        
        original.setIsStarted(true); // keep status the same, we don't want to write the started history
        updated.setIsStarted(true);

        updated.afterUpdate(ps, new DefaultHistoryService(ps), original);

        verify(ps).save(isA(ConferenceHistory.class));
    }
    
    @Test
    public void test_afterUpdate_whenConferenceRecordingStatusChangesToFalse_writesOneConferenceHistory()
    {
        PersistenceService ps = mock(PersistenceService.class);
        
        Subscriber subscriber = createSubscriber(session);
        Conference original = createConference(session, subscriber);
        Conference updated = original.copy(true);

        original.setIsRecording(true);
        updated.setIsRecording(false);
        
        original.setIsStarted(true); // keep status the same, we don't want to write the started history
        updated.setIsStarted(true);

        updated.afterUpdate(ps, new DefaultHistoryService(ps), original);

        verify(ps).save(isA(ConferenceHistory.class));
    }
    
    private Conference getPopulatedConference()
    {
        Conference c = new Conference();
        c.setDescription(String.valueOf(System.currentTimeMillis()));
        c.setId(System.currentTimeMillis());
        c.setIsStarted(Boolean.TRUE);
        c.setIsRecording(Boolean.FALSE);
        c.setVersion(1);
        return c;
    }
}
