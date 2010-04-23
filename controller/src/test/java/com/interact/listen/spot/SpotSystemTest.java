package com.interact.listen.spot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interact.listen.httpclient.HttpClient;
import com.interact.listen.resource.Participant;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class SpotSystemTest
{
    private SpotSystem spotSystem;
    private HttpClient mockHttpClient;
    private StatSender mockStatSender;

    private Participant participant;

    private final String httpInterfaceUri = "http://www.example.com";

    @Before
    public void setUp()
    {
        mockHttpClient = mock(HttpClient.class);
        mockStatSender = mock(StatSender.class);

        spotSystem = new SpotSystem(httpInterfaceUri);
        spotSystem.setHttpClient(mockHttpClient);
        spotSystem.setStatSender(mockStatSender);

        participant = new Participant();
        participant.setSessionID(String.valueOf(System.currentTimeMillis()));
    }

    @Test
    public void test_dropParticipant_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        Map<String, String> expectedParams = new HashMap<String, String>();
        expectedParams.put("sessionid", participant.getSessionID());
        expectedParams.put("name", "dialog.user.customEvent");
        expectedParams.put("II_SB_eventToPass", "DROP");
        expectedParams.put("II_SB_valueToPass", "");

        when(mockHttpClient.getResponseStatus()).thenReturn(200);

        spotSystem.dropParticipant(participant);

        verify(mockHttpClient).post(httpInterfaceUri, expectedParams);
    }

    @Test
    public void test_dropParticipant_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        when(mockHttpClient.getResponseStatus()).thenReturn(400);

        try
        {
            spotSystem.dropParticipant(participant);
            fail("Expected SpotCommunicationException for non-200 HTTP status");
        }
        catch(SpotCommunicationException e)
        {
            assertEquals("Received HTTP Status 400 from SPOT System at [" + httpInterfaceUri + "]", e.getMessage());
        }
    }

    @Test
    public void test_dropParticipant_whenSpotRespondsWith200_sendsStat() throws SpotCommunicationException, IOException
    {
        when(mockHttpClient.getResponseStatus()).thenReturn(200);
        spotSystem.dropParticipant(participant);        

        verify(mockStatSender).send(Stat.PUBLISHED_EVENT_TO_SPOT);
    }

    @Test
    public void test_dropParticipant_whenSpotRespondsWith400_sendsStat() throws SpotCommunicationException, IOException
    {
        when(mockHttpClient.getResponseStatus()).thenReturn(400);
        try
        {
            spotSystem.dropParticipant(participant);
        }
        catch(SpotCommunicationException e)
        {
            // expected
        }

        verify(mockStatSender).send(Stat.PUBLISHED_EVENT_TO_SPOT);
    }

    @Test
    public void test_muteParticipant_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        Map<String, String> expectedParams = new HashMap<String, String>();
        expectedParams.put("sessionid", participant.getSessionID());
        expectedParams.put("name", "dialog.user.customEvent");
        expectedParams.put("II_SB_eventToPass", "MUTE");
        expectedParams.put("II_SB_valueToPass", "");

        when(mockHttpClient.getResponseStatus()).thenReturn(200);

        spotSystem.muteParticipant(participant);

        verify(mockHttpClient).post(httpInterfaceUri, expectedParams);
    }

    @Test
    public void test_muteParticipant_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        when(mockHttpClient.getResponseStatus()).thenReturn(400);

        try
        {
            spotSystem.muteParticipant(participant);
            fail("Expected SpotCommunicationException for non-200 HTTP status");
        }
        catch(SpotCommunicationException e)
        {
            assertEquals("Received HTTP Status 400 from SPOT System at [" + httpInterfaceUri + "]", e.getMessage());
        }
    }

    @Test
    public void test_muteParticipant_whenSpotRespondsWith200_sendsStat() throws SpotCommunicationException, IOException
    {
        when(mockHttpClient.getResponseStatus()).thenReturn(200);
        spotSystem.muteParticipant(participant);        

        verify(mockStatSender).send(Stat.PUBLISHED_EVENT_TO_SPOT);
    }

    @Test
    public void test_muteParticipant_whenSpotRespondsWith400_sendsStat() throws SpotCommunicationException, IOException
    {
        try
        {
            spotSystem.muteParticipant(participant);
        }
        catch(SpotCommunicationException e)
        {
            // expected
        }     

        verify(mockStatSender).send(Stat.PUBLISHED_EVENT_TO_SPOT);
    }

    @Test
    public void test_unmuteParticipant_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        Map<String, String> expectedParams = new HashMap<String, String>();
        expectedParams.put("sessionid", participant.getSessionID());
        expectedParams.put("name", "dialog.user.customEvent");
        expectedParams.put("II_SB_eventToPass", "UNMUTE");
        expectedParams.put("II_SB_valueToPass", "");

        when(mockHttpClient.getResponseStatus()).thenReturn(200);

        spotSystem.unmuteParticipant(participant);

        verify(mockHttpClient).post(httpInterfaceUri, expectedParams);
    }

    @Test
    public void test_unmuteParticipant_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        when(mockHttpClient.getResponseStatus()).thenReturn(400);

        try
        {
            spotSystem.unmuteParticipant(participant);
            fail("Expected SpotCommunicationException for non-200 HTTP status");
        }
        catch(SpotCommunicationException e)
        {
            assertEquals("Received HTTP Status 400 from SPOT System at [" + httpInterfaceUri + "]", e.getMessage());
        }
    }

    @Test
    public void test_unmuteParticipant_whenSpotRespondsWith200_sendsStat() throws SpotCommunicationException, IOException
    {
        when(mockHttpClient.getResponseStatus()).thenReturn(200);
        spotSystem.unmuteParticipant(participant);        

        verify(mockStatSender).send(Stat.PUBLISHED_EVENT_TO_SPOT);
    }

    @Test
    public void test_unmuteParticipant_whenSpotRespondsWith400_sendsStat() throws SpotCommunicationException, IOException
    {
        try
        {
            spotSystem.unmuteParticipant(participant);
        }
        catch(SpotCommunicationException e)
        {
            // expected
        }

        verify(mockStatSender).send(Stat.PUBLISHED_EVENT_TO_SPOT);
    }
}
