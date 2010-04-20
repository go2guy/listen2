package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.resource.*;
import com.interact.listen.resource.Pin.PinType;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class GetConferenceInfoServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private HttpServlet servlet = new GetConferenceInfoServlet();

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void test_doGet_withNoSessionUser_returnsUnauthorized() throws IOException, ServletException
    {
        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized", response.getContentAsString());
    }

    @Test
    public void test_doGet_withNonexistentConference_returns500() throws IOException, ServletException
    {
        final Long id = System.currentTimeMillis();

        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(id));
        User user = new User();
        user.setSubscriber(subscriber);

        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Conference not found", response.getContentAsString());
    }

    @Test
    public void test_doGet_withExistingConference_returns200AndConferenceJSON() throws IOException, ServletException
    {
        final Long id = System.currentTimeMillis();

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(id));

        Conference conference = new Conference();
        conference.setIsStarted(true);
        conference.setId(System.currentTimeMillis());

        Pin pin = Pin.newInstance(subscriber.getNumber(), PinType.ADMIN);
        session.save(pin);

        conference.addToPins(pin);
        session.save(conference);

        tx.commit();

        User user = new User();
        user.setSubscriber(subscriber);

        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("user", user);

        request.setMethod("GET");
        servlet.service(request, response);

        String hrefString = "\"href\":\"/conferences/" + conference.getId() + "\"";
        assertTrue(response.getContentAsString().contains(hrefString));
    }
}
