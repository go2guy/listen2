package com.interact.listen.api;

import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.exception.BadRequestServletException;

import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterSpotSystemServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        String system = request.getParameter("system");
        if(system == null)
        {
            throw new BadRequestServletException("Missing required parameter [system]");
        }
        addSystem(system);
    }

    public static void addSystem(String system)
    {
        String property = Configuration.get(Property.Key.SPOT_SYSTEMS);
        Set<String> current = Property.delimitedStringToSet(property, ",");

        current.add(system);

        property = Property.setToDelimitedString(current, ",");
        Configuration.set(Property.Key.SPOT_SYSTEMS, property);
    }
}
