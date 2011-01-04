package com.interact.listen.gui;

import com.interact.listen.ServletUtil;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.resource.Subscriber;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FindMeServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        ServletUtil.requireLicensedFeature(ListenFeature.FINDME);
        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            ServletUtil.redirect("/login", request, response);
            return;
        }
        ServletUtil.forward("/WEB-INF/jsp/findme.jsp", request, response);
    }
}