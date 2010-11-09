package com.interact.listen.gui;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;
import com.interact.listen.TestUtil;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

public class MarkVoicemailReadStatusServletTest extends ListenServletTest
{
    private MarkVoicemailReadStatusServlet servlet = new MarkVoicemailReadStatusServlet();

    @Test
    public void test_doPost_withNoCurrentSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("POST");
        testForListenServletException(servlet, 401, "Unauthorized - Not logged in");
    }

    @Test
    public void test_doPost_withNullIdParameter_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("POST");
        request.setParameter("id", (String)null);
        testForListenServletException(servlet, 400, "Please provide an id");
    }

    @Test
    public void test_doPost_withBlankIdParameter_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("POST");
        request.setParameter("id", " ");
        testForListenServletException(servlet, 400, "Please provide an id");
    }

    @Test
    public void test_doPost_withNullReadStatusParameter_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("POST");
        request.setParameter("id", TestUtil.randomString());
        request.setParameter("readStatus", (String)null);
        testForListenServletException(servlet, 400, "Please provide a readStatus");
    }

    @Test
    public void test_doPost_withBlankReadStatusParameter_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("POST");
        request.setParameter("id", TestUtil.randomString());
        request.setParameter("readStatus", "  ");
        testForListenServletException(servlet, 400, "Please provide a readStatus");
    }
}