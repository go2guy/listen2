package com.interact.listen.resource;

import static org.junit.Assert.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.ListenTest;
import com.interact.listen.PersistenceService;
import com.interact.listen.history.HistoryService;

import org.junit.Before;
import org.junit.Test;

public class ParticipantTest extends ListenTest
{
    private Participant participant;

    @Before
    public void setUp()
    {
        participant = new Participant();
    }

    @Test
    public void test_version_defaultsToZero()
    {
        assertEquals(Integer.valueOf(0), participant.getVersion());
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        participant.setId(id);

        assertEquals(id, participant.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        participant.setVersion(version);

        assertEquals(version, participant.getVersion());
    }

    @Test
    public void test_validate_validProperties_returnsNoErrors()
    {
        participant = getPopulatedParticipant();

        assertTrue(participant.validate());
        assertFalse(participant.hasErrors());
    }

    @Test
    public void test_validate_nullAudioResource_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setAudioResource(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_nullConference_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setConference(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_nullIsAdmin_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setIsAdmin(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_nullIsMuted_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setIsMuted(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_nullIsAdminMuted_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setIsAdminMuted(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_nullIsPassive_returnsFalseAndHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setIsPassive(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_isAdminAndIsAdminMuted_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setIsAdmin(Boolean.TRUE);
        participant.setIsAdminMuted(Boolean.TRUE);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_nullNumber_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setNumber(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_blankNumber_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setNumber("");

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_whitespaceNumber_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setNumber(" ");

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_nullSessionID_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setSessionID(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_blankSessionID_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setSessionID("");

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_whitespaceSessionID_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setSessionID(" ");

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_copy_withoutIdAndVersion_createsShallowCopyWithoutIdAndVersion()
    {
        Participant original = getPopulatedParticipant();
        Participant copy = original.copy(false);

        assertEquals(original.getAudioResource(), copy.getAudioResource());
        assertTrue(original.getConference() == copy.getConference()); // same reference
        assertEquals(original.getIsAdmin(), copy.getIsAdmin());
        assertEquals(original.getIsAdminMuted(), copy.getIsAdminMuted());
        assertEquals(original.getIsMuted(), copy.getIsMuted());
        assertEquals(original.getIsPassive(), copy.getIsPassive());
        assertEquals(original.getNumber(), copy.getNumber());
        assertEquals(original.getSessionID(), copy.getSessionID());

        assertNull(copy.getId());
        assertEquals(Integer.valueOf(0), copy.getVersion());
    }

    @Test
    public void test_copy_withIdAndVersion_createsShallowCopyWithIdAndVersion()
    {
        Participant original = getPopulatedParticipant();
        Participant copy = original.copy(true);

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getVersion(), copy.getVersion());
    }

    @Test
    public void test_equals_sameObject_returnsTrue()
    {
        assertTrue(participant.equals(participant));
    }

    @Test
    public void test_equals_thatNull_returnsFalse()
    {
        assertFalse(participant.equals(null));
    }

    @Test
    public void test_equals_thatNotAParticipant_returnsFalse()
    {
        assertFalse(participant.equals(new String()));
    }

    @Test
    public void test_equals_sessionIDNotEqual_returnsFalse()
    {
        participant.setSessionID(String.valueOf(System.currentTimeMillis()));

        Participant that = new Participant();
        that.setSessionID(null);

        assertFalse(participant.equals(that));
    }

    @Test
    public void test_equals_allPropertiesEqual_returnsTrue()
    {
        String sessionID = String.valueOf(System.currentTimeMillis());

        participant.setSessionID(sessionID);

        Participant that = new Participant();
        that.setSessionID(sessionID);

        assertTrue(participant.equals(that));
    }

    @Test
    public void test_hashCode_returnsUniqueHashCodeForRelevantFields()
    {
        Participant obj = new Participant();

        // hashcode-relevant properties set to static values for predictability
        obj.setSessionID("Vito");

        // set a property that has no effect on hashcode to something dynamic
        obj.setAudioResource(String.valueOf(System.currentTimeMillis()));

        assertEquals(2666669, obj.hashCode());
    }

    @Test
    public void test_queryConferenceAdminSessionId_withNoAdminParticipant_returnsNull()
    {
        Subscriber s = ListenTest.createSubscriber(session);
        Conference c = ListenTest.createConference(session, s);
        Participant p = ListenTest.createParticipant(session, c, false);
        
        String result = Participant.queryConferenceAdminSessionId(session, c);
        assertNull(result);
    }

    @Test
    public void test_queryConferenceAdminSessionId_withNoParticipants_returnsNull()
    {
        Subscriber s = ListenTest.createSubscriber(session);
        Conference c = ListenTest.createConference(session, s);
        Participant p = ListenTest.createParticipant(session, c, false);
        
        String result = Participant.queryConferenceAdminSessionId(session, c);
        assertNull(result);
    }

    @Test
    public void test_queryConferenceAdminSessionId_withOnlyAdminParticipant_returnsAdminSessionId()
    {
        Subscriber s = ListenTest.createSubscriber(session);
        Conference c = ListenTest.createConference(session, s);
        Participant p = ListenTest.createParticipant(session, c, true);

        String result = Participant.queryConferenceAdminSessionId(session, c);
        assertEquals(p.getSessionID(), result);
    }

    @Test
    public void test_queryConferenceAdminSessionId_withNonAdminAndAdminParticipants_returnsAdminSessionId()
    {
        Subscriber s = ListenTest.createSubscriber(session);
        Conference c = ListenTest.createConference(session, s);
        Participant admin = ListenTest.createParticipant(session, c, true);
        ListenTest.createParticipant(session, c, false);

        String result = Participant.queryConferenceAdminSessionId(session, c);
        assertEquals(admin.getSessionID(), result);
    }

    @Test
    public void test_afterSave_writesOneConferenceHistory()
    {
        PersistenceService ps = mock(PersistenceService.class);
        
        Subscriber subscriber = createSubscriber(session);
        Conference conference = createConference(session, subscriber);
        Participant participant = createParticipant(session, conference, false);

        participant.afterSave(ps, null);

        verify(ps).save(isA(ConferenceHistory.class));
    }
    
    @Test
    public void test_afterUpdate_whenParticipantAdminMutedStatusChangedToTrue_writesMutedHistory()
    {
        PersistenceService ps = mock(PersistenceService.class);
        HistoryService hs = mock(HistoryService.class);
        
        Subscriber subscriber = createSubscriber(session);
        Conference conference = createConference(session, subscriber);

        Participant participant = createParticipant(session, conference, false);
        Participant original = participant.copy(true);

        original.setIsAdminMuted(false);
        participant.setIsAdminMuted(true);
        
        participant.afterUpdate(ps, hs, original);

        verify(hs).writeMutedConferenceCaller(participant.getNumber(), conference.getDescription());
    }
    
    @Test
    public void test_afterDelete_writesDroppedConferenceCallerHistory()
    {
        PersistenceService ps = mock(PersistenceService.class);
        HistoryService hs = mock(HistoryService.class);
        
        Subscriber subscriber = createSubscriber(session);
        Conference conference = createConference(session, subscriber);

        Participant participant = createParticipant(session, conference, false);
        
        participant.afterDelete(ps, hs);
        
        verify(hs).writeDroppedConferenceCaller(participant.getNumber(), conference.getDescription());
    }
    
    private Participant getPopulatedParticipant()
    {
        Conference c = new Conference();
        c.setId(System.currentTimeMillis());
        c.setIsStarted(Boolean.TRUE);
        c.setIsRecording(Boolean.FALSE);
        c.setVersion(1);

        Participant p = new Participant();
        p.setAudioResource("/audio/resource");
        p.setConference(c);
        p.setId(System.currentTimeMillis());
        p.setIsAdmin(Boolean.TRUE);
        p.setIsAdminMuted(Boolean.FALSE);
        p.setIsMuted(Boolean.TRUE);
        p.setIsPassive(Boolean.FALSE);
        p.setNumber(String.valueOf(System.currentTimeMillis()));
        p.setSessionID(String.valueOf(System.currentTimeMillis()));
        p.setVersion(1);

        return p;
    }
}
