package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.*;
import com.interact.listen.resource.History;

import java.io.IOException;

import javax.servlet.ServletException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Test;

public class GetHistoryListServletTest extends ListenServletTest
{
    private GetHistoryListServlet servlet = new GetHistoryListServlet();

    @Test
    public void test_doGet_withNoCurrentUser_throwsUnauthorized() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("GET");
        testForListenServletException(servlet, 401, "Unauthorized - Not logged in");
    }

    @Test
    public void test_doGet_returnsJsonHistoryList() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        Long count = History.count(session);
        servlet.doGet(request, response);

        assertOutputBufferContentTypeEquals("application/json");

        StringBuilder buffer = (StringBuilder)request.getAttribute(OutputBufferFilter.OUTPUT_BUFFER_KEY);
        JSONObject output = (JSONObject)JSONValue.parse(buffer.toString());
        assertEquals(count, output.get("total"));
    }
}
