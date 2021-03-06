package com.interact.listen.gui;

import com.interact.listen.ServletUtil;
import com.interact.listen.api.GetDnisServlet;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.config.Property.Key;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class SetPropertiesServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(SetPropertiesServlet.class);

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_SET_PROPERTIES);

        Subscriber currentSubscriber = ServletUtil.currentSubscriber(request);
        if(currentSubscriber == null)
        {
            throw new UnauthorizedServletException();
        }

        if(!currentSubscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException();
        }

        boolean alertChanged = false;

        for(String name : (List<String>)Collections.list(request.getParameterNames()))
        {
            String value = request.getParameter(name);
            Property.Key key = Property.Key.findByKey(name);
            if(key == null)
            {
                LOG.warn("Tried to set unrecognized parameter [" + name + "] to [" + value + "]; ignoring");
                continue;
            }

            // validation for properties that need it
            switch(key)
            {
                case DNIS_MAPPING:
                    Map<String, String> mappings = GetDnisServlet.dnisConfigurationToMap(value);
                    for(Map.Entry<String, String> entry : mappings.entrySet())
                    {
                        if(entry.getKey().contains("*") && entry.getKey().indexOf("*") != entry.getKey().length() - 1)
                        {
                            throw new BadRequestServletException("Wildcard (*) may only be at the end of mapping " +
                                                                 entry.getKey());
                        }
                    }

                    List<String> mappingKeys = GetDnisServlet.dnisConfigurationKeys(value);
                    Set<String> verify = new HashSet<String>(mappingKeys.size());
                    for(String mappingKey : mappingKeys)
                    {
                        if(verify.contains(mappingKey))
                        {
                            throw new BadRequestServletException("Mapping [" + mappingKey + "] cannot be defined twice");
                        }
                        verify.add(mappingKey);
                    }
                    break;

                case CONFERENCING_PINLENGTH:
                    try
                    {
                        int intValue = Integer.parseInt(value);
                        if(intValue < 3 || intValue > 16)
                        {
                            throw new BadRequestServletException("Conferencing PIN length [" + value +
                                                                 "] must be between 3 and 16 (inclusive)");
                        }
                    }
                    catch(NumberFormatException e)
                    {
                        throw new BadRequestServletException("Conferencing PIN length [" + value + "] must be a number");
                    }

                case REALIZE_ALERT_NAME:
                    alertChanged = value != null && !value.equals(Configuration.get(Key.REALIZE_ALERT_NAME));
                    break;

                default:
                    // no validation
                    break;
            }

            Configuration.set(key, value);
            LOG.debug("Set parameter [" + name + "] to [" + request.getParameter(name) + "]");
        }

        if(alertChanged)
        {
            EditPagerServlet.updateRealizeAlert(Configuration.get(Key.REALIZE_URL),
                                                Configuration.get(Key.REALIZE_ALERT_NAME), null,
                                                Configuration.get(Key.ALTERNATE_NUMBER));
        }
    }
}
