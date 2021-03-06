package com.interact.listen;

import com.interact.listen.api.security.AuthenticationFilter;
import com.interact.listen.api.security.AuthenticationFilter.Authentication;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.hibernate.Session;

public final class ServletUtil
{
    private static final Logger LOG = Logger.getLogger(ServletUtil.class);

    private ServletUtil()
    {
        throw new AssertionError("Cannot instantiate utility class ServletUtil");
    }

    public static Subscriber currentSubscriber(HttpServletRequest request)
    {
        Long id = (Long)request.getSession().getAttribute("subscriber");
        if(id == null)
        {
            return null;
        }
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        return Subscriber.queryById(session, id);
    }

    /**
     * Requires that the application be licensed for the specified ListenFeature. If not, throws a NotLicensedException.
     * 
     * @param feature feature to verify
     * @throws NotLicensedException if feature is not licensed
     */
    public static void requireLicensedFeature(ListenFeature feature) throws NotLicensedException
    {
        if(!License.isLicensed(feature))
        {
            throw new NotLicensedException(feature);
        }
    }

    public static Subscriber requireCurrentSubscriber(HttpServletRequest request, boolean requireAdministrator) throws UnauthorizedServletException
    {
        Subscriber subscriber = currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }
        if(requireAdministrator && !subscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException("Insufficient permissions");
        }
        return subscriber;
    }

    public static Map<String, String> getQueryParameters(HttpServletRequest request)
    {
        Map<String, String> map = new HashMap<String, String>();
        String query = request.getQueryString();

        if(query == null || query.trim().length() == 0)
        {
            return map;
        }

        String[] params = query.split("&", 0);
        if(params.length == 0)
        {
            return map;
        }

        for(String param : params)
        {
            String[] pair = param.split("=", 0);
            if(pair.length != 2)
            {
                LOG.warn("Pair [" + Arrays.toString(pair) + "] had a length of one, skipping");
                continue;
            }

            try
            {
                String key = URLDecoder.decode(pair[0], "UTF-8");
                String value = URLDecoder.decode(pair[1], "UTF-8");
                map.put(key, value);
            }
            catch(UnsupportedEncodingException e)
            {
                throw new AssertionError(e);
            }
        }

        return map;
    }

    public static void forward(String to, HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        LOG.debug("Forwarding to [" + to + "]");
        request.getRequestDispatcher(to).forward(request, response);
    }

    public static void redirect(String to, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String destination = request.getContextPath() + to;
        LOG.debug("Redirecting to [" + destination + "]");
        response.sendRedirect(destination);
    }
    
    public static URL encodeUri(String stringUri)
    {
        URI uri = null;
        URL url, returnUrl = null;
        
        try
        {
            url = new URL(stringUri);
            uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), null);
            returnUrl = uri.toURL();
        }
        catch(Exception e)
        {
            LOG.error("Error URI encoding audio resource location.", e);
            return null;
        }
        
        return returnUrl;
    }

    public static void sendStat(HttpServletRequest request, Stat stat)
    {
        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(stat);
    }

    public static String getNotNullNotEmptyString(String parameter, HttpServletRequest request, String fieldName)
        throws BadRequestServletException
    {
        String p = getNotNullString(parameter, request, fieldName);
        if(p.trim().equals(""))
        {
            throw new BadRequestServletException(fieldName + " cannot be empty");
        }
        return p;
    }

    public static Integer getNotNullInteger(String parameter, HttpServletRequest request, String fieldName)
        throws BadRequestServletException
    {
        String p = getNotNullString(parameter, request, fieldName);
        try
        {
            return Integer.valueOf(p);
        }
        catch(NumberFormatException e)
        {
            throw new BadRequestServletException(fieldName + " must be a number");
        }
    }

    public static Long getNotNullLong(String parameter, HttpServletRequest request, String fieldName)
        throws BadRequestServletException
    {
        String p = getNotNullString(parameter, request, fieldName);
        try
        {
            return Long.valueOf(p);
        }
        catch(NumberFormatException e)
        {
            throw new BadRequestServletException(fieldName + " must be a number");
        }
    }

    public static String getNotNullString(String parameter, HttpServletRequest request, String fieldName)
        throws BadRequestServletException
    {
        String p = request.getParameter(parameter);
        if(p == null)
        {
            throw new BadRequestServletException(fieldName + " cannot be null");
        }
        return p;
    }

    public static Boolean getNotNullBoolean(String parameter, HttpServletRequest request, String fieldName)
        throws BadRequestServletException
    {
        String p = request.getParameter(parameter);
        if(p == null)
        {
            throw new BadRequestServletException(fieldName + " cannot be null");
        }
        return Boolean.valueOf(p);
    }

    public static PersistenceService getAuthPersistenceService(Session session, HttpServletRequest request) throws UnauthorizedServletException
    {
        Subscriber subscriber = null;

        Authentication auth = (Authentication)request.getAttribute(AuthenticationFilter.AUTHENTICATION_KEY);
        if(auth != null)
        {
            if(auth.getType() == AuthenticationFilter.AuthenticationType.SUBSCRIBER && auth.getSubscriber() != null)
            {
                subscriber = auth.getSubscriber();
            }
            if(subscriber == null)
            {
                throw new UnauthorizedServletException("Unathorized Subscriber");
            }
        }
        else
        {
            String subHeader = request.getHeader("X-Listen-Subscriber");
            if(subHeader == null)
            {
                subHeader = request.getHeader("X-Listen-AuthenticationUsername");
                if(subHeader != null)
                {
                    subHeader = new String(Base64.decodeBase64(subHeader));
                    subscriber = Subscriber.queryByUsername(session, subHeader);
                }
                if(subscriber == null)
                {
                    throw new UnauthorizedServletException("Unable to get subscriber from encoded header [" + subHeader + "]");
                }
            }
            else
            {
                Long id = Marshaller.getIdFromHref(subHeader);
                if(id != null)
                {
                    subscriber = Subscriber.queryById(session, id);
                }
                if(subscriber == null)
                {
                    throw new UnauthorizedServletException("X-Listen-Subscriber HTTP header contained unknown subscriber href [" + subHeader + "]");
                }
            }
        }
        
        Channel channel = (Channel)request.getAttribute(RequestInformationFilter.CHANNEL_KEY);
        return new DefaultPersistenceService(session, subscriber, channel);
    }

}
