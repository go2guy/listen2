package com.interact.listen.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.*;

import com.interact.listen.ListenTest;
import com.interact.listen.PersistenceService;
import com.interact.listen.TestUtil;
import com.interact.listen.history.HistoryService;
import com.interact.listen.spot.MessageLightToggler;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class VoicemailTest extends ListenTest
{
    private Voicemail voicemail;

    @Before
    public void setUp()
    {
        voicemail = new Voicemail();
    }

    @Test
    public void test_version_defaultsToZero()
    {
        assertEquals(Integer.valueOf(0), voicemail.getVersion());
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        voicemail.setId(id);

        assertEquals(id, voicemail.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        voicemail.setVersion(version);

        assertEquals(version, voicemail.getVersion());
    }

    @Test
    public void test_setDateCreated_withValidDate_setsDateCreated()
    {
        final Date dateCreated = new Date();
        voicemail.setDateCreated(dateCreated);

        assertEquals(dateCreated, voicemail.getDateCreated());
    }

    @Test
    public void test_setIsNew_withValidBoolean_setsIsNew()
    {
        final Boolean isNew = Boolean.TRUE;
        voicemail.setIsNew(isNew);

        assertEquals(isNew, voicemail.getIsNew());
    }

    @Test
    public void test_setTranscription_withValidString_setsTranscription()
    {
        final String transcription = TestUtil.randomString();
        voicemail.setTranscription(transcription);
        
        assertEquals(transcription, voicemail.getTranscription());
        assertTrue(voicemail.hasTranscription());
    }

    @Test
    public void test_hasTranscription_withNullTranscription_returnsFalse()
    {
        voicemail.setTranscription(null);
        assertFalse(voicemail.hasTranscription());
    }

    @Test
    public void test_hasTranscription_withBlankTranscription_returnsFalse()
    {
        voicemail.setTranscription("   ");
        assertFalse(voicemail.hasTranscription());
    }

    @Test
    public void test_validate_validProperties_returnsNoErrors()
    {
        voicemail = getPopulatedVoicemail();

        assertTrue(voicemail.validate());
        assertFalse(voicemail.hasErrors());
    }

    @Test
    public void test_validate_nullSubscriber_returnsHasErrors()
    {
        voicemail = getPopulatedVoicemail();
        voicemail.setSubscriber(null);

        assertFalse(voicemail.validate());
        assertTrue(voicemail.hasErrors());
    }

    @Test
    public void test_validate_nullDateCreated_returnsHasErrors()
    {
        voicemail = getPopulatedVoicemail();
        voicemail.setDateCreated(null);

        assertFalse(voicemail.validate());
        assertTrue(voicemail.hasErrors());
    }

    @Test
    public void test_validate_nullUri_returnsHasErrors()
    {
        voicemail = getPopulatedVoicemail();
        voicemail.setUri(null);

        assertFalse(voicemail.validate());
        assertTrue(voicemail.hasErrors());
    }

    @Test
    public void test_validate_blankUri_returnsHasErrors()
    {
        voicemail = getPopulatedVoicemail();
        voicemail.setUri("");

        assertFalse(voicemail.validate());
        assertTrue(voicemail.hasErrors());
    }

    @Test
    public void test_validate_whitespaceUri_returnsHasErrors()
    {
        voicemail = getPopulatedVoicemail();
        voicemail.setUri("  ");

        assertFalse(voicemail.validate());
        assertTrue(voicemail.hasErrors());
    }

    @Test
    public void test_validate_nullIsNew_returnsHasErrors()
    {
        voicemail = getPopulatedVoicemail();
        voicemail.setIsNew(null);

        assertFalse(voicemail.validate());
        assertTrue(voicemail.hasErrors());
    }

    @Test
    public void test_copy_withoutIdAndVersion_createsShallowCopyWithoutIdAndVersion()
    {
        Voicemail original = getPopulatedVoicemail();
        Voicemail copy = original.copy(false);

        assertEquals(original.getDateCreated(), copy.getDateCreated());
        assertFalse(original.getDateCreated() == copy.getDateCreated()); // different reference
        assertEquals(original.getUri(), copy.getUri());
        assertEquals(original.getIsNew(), copy.getIsNew());
        assertTrue(original.getSubscriber() == copy.getSubscriber()); // same reference

        assertNull(copy.getId());
        assertEquals(Integer.valueOf(0), copy.getVersion());
    }

    @Test
    public void test_copy_withIdAndVersion_createsShallowCopyWithIdAndVersion()
    {
        Voicemail original = getPopulatedVoicemail();
        Voicemail copy = original.copy(true);

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getVersion(), copy.getVersion());
    }

    @Test
    public void test_equals_sameObject_returnsTrue()
    {
        assertTrue(voicemail.equals(voicemail));
    }

    @Test
    public void test_equals_thatNull_returnsFalse()
    {
        assertFalse(voicemail.equals(null));
    }

    @Test
    public void test_equals_thatNotAVoicemail_returnsFalse()
    {
        assertFalse(voicemail.equals(new String()));
    }

    @Test
    public void test_equals_uriNotEqual_returnsFalse()
    {
        voicemail.setUri(String.valueOf(System.currentTimeMillis()));

        Voicemail that = new Voicemail();
        that.setUri(null);

        assertFalse(voicemail.equals(that));
    }

    @Test
    public void test_equals_allPropertiesEqual_returnsTrue()
    {
        String uri = String.valueOf(System.currentTimeMillis());
        voicemail.setUri(uri);

        Voicemail that = new Voicemail();
        that.setUri(uri);

        assertTrue(voicemail.equals(that));
    }

    @Test
    public void test_hashCode_returnsUniqueHashcodeForRelevantFields()
    {
        Voicemail obj = new Voicemail();

        // hashcode-relevant properties set to static values for predictability
        obj.setUri("Billy Club");

        // set a property that has no effect on hashcode to something dynamic
        obj.setDateCreated(new Date());

        assertEquals(-1082258653, obj.hashCode());
    }

    // TODO currently failing because we're doing a requery for the subscriber when sending the notif
    
//    @Test
//    public void test_afterSave_withForwardedByNull_writesLeftVoicemailHistoryAndInvokesMessageLightToggle()
//    {
//        Subscriber s = createSubscriber(session);
//        Voicemail v = createVoicemail(session, s);
//        
//        MessageLightToggler mlt = mock(MessageLightToggler.class);
//        PersistenceService ps = mock(PersistenceService.class);
//        HistoryService hs = mock(HistoryService.class);
//
//        v.setForwardedBy(null);
//        v.useMessageLightToggler(mlt);
//
//        v.afterSave(ps, hs);
//        verify(hs).writeLeftVoicemail(v);
//        verify(mlt).toggleMessageLight(ps, s);
//    }
//    
//    @Test
//    public void test_afterSave_withForwardedBySet_writesForwardedVoicemailHistoryAndInvokesMessageLightToggle()
//    {
//        Subscriber s = createSubscriber(session);
//        Voicemail v = createVoicemail(session, s);
//        
//        MessageLightToggler mlt = mock(MessageLightToggler.class);
//        PersistenceService ps = mock(PersistenceService.class);
//        HistoryService hs = mock(HistoryService.class);
//
//        v.setForwardedBy(createSubscriber(session));
//        v.useMessageLightToggler(mlt);
//
//        v.afterSave(ps, hs);
//        verify(hs).writeForwardedVoicemail(v);
//        verify(mlt).toggleMessageLight(ps, s);
//    }
    
    @Test
    public void test_afterUpdate_invokesMessageLightToggle()
    {
        Subscriber s = createSubscriber(session);
        Voicemail v = createVoicemail(session, s);

        MessageLightToggler mlt = mock(MessageLightToggler.class);
        PersistenceService ps = mock(PersistenceService.class);
        HistoryService hs = mock(HistoryService.class);

        v.useMessageLightToggler(mlt);

        v.afterUpdate(ps, hs, v.copy(true));
        verify(mlt).toggleMessageLight(ps, s);
    }
    
    private Voicemail getPopulatedVoicemail()
    {
        Subscriber s = new Subscriber();
        s.setId(System.currentTimeMillis());
        s.setVersion(1);

        Voicemail v = new Voicemail();
        v.setDateCreated(new Date());
        v.setDescription(String.valueOf(System.currentTimeMillis()));
        v.setFileSize("1024");
        v.setForwardedBy(s);
        v.setUri("/foo/bar/baz");
        v.setId(System.currentTimeMillis());
        v.setIsNew(Boolean.TRUE);
        v.setSubscriber(s);
        v.setVersion(1);

        return v;
    }
}
