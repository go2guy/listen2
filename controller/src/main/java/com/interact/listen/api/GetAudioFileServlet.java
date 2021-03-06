package com.interact.listen.api;

import com.interact.listen.*;
import com.interact.listen.api.security.AuthenticationFilter;
import com.interact.listen.api.security.AuthenticationFilter.Authentication;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.gui.DownloadVoicemailServlet;
import com.interact.listen.history.Channel;
import com.interact.listen.history.DefaultHistoryService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;
import com.interact.listen.stats.Stat;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

public class GetAudioFileServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(DownloadVoicemailServlet.class);
    private static final String MP3_FILE_TYPE = "audio/mpeg";
    private static final String WAV_FILE_TYPE = "audio/x-wav";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if(!License.isLicensed(ListenFeature.VOICEMAIL))
        {
            throw new NotLicensedException(ListenFeature.VOICEMAIL);
        }
        
        request.setAttribute(OutputBufferFilter.OUTPUT_SUPPRESS_KEY, Boolean.TRUE);

        ServletUtil.sendStat(request, Stat.GUI_DOWNLOAD_VOICEMAIL);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        
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
                throw new UnauthorizedServletException("Not logged in");
            }
        }
        else
        {
            // for testing purposes, we are going to pull the user name out of the authentication header
            String subHeader = request.getHeader("X-Listen-AuthenticationUsername");
            if(subHeader != null)
            {
                String username = new String(Base64.decodeBase64(subHeader));
                subscriber = Subscriber.queryByUsername(session, username);
    
                if(subscriber == null)
                {
                    throw new UnauthorizedServletException("Unable to find subscriber from encoded header [" + subHeader + "]");
                }
            }
        }

        String id = ((HttpServletRequest)request).getPathInfo().substring(1);
        if(id == null || id.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide an id");
        }
        
        String acceptType = request.getHeader("Accept");
        boolean doesAcceptMp3 = acceptType != null && acceptType.equals(MP3_FILE_TYPE);

        PersistenceService persistenceService = new DefaultPersistenceService(session, subscriber, Channel.TUI);

        Voicemail voicemail = Voicemail.queryById(session, Long.valueOf(id));

        InputStream input = null;

        try
        {
            String audioUri = null;
            if(doesAcceptMp3)
            {
                audioUri = voicemail.mp3FileUri();
            }
            else
            {
                audioUri = voicemail.getUri();
            }
            URL url = ServletUtil.encodeUri(audioUri);
            LOG.debug("Getting Audio File: " + url);

            HistoryService historyService = new DefaultHistoryService(persistenceService);
            historyService.writeDownloadedVoicemail(voicemail);
            
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            String fileName = audioUri.substring(audioUri.lastIndexOf("/") + 1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName);

            int fileSize = connection.getContentLength();
            LOG.debug("Audio Content Length " + fileSize + " {" + voicemail.getFileSize() + "}");
            response.setContentLength(fileSize);

            if(doesAcceptMp3)
            {
                response.setContentType(MP3_FILE_TYPE);
            }
            else
            {
                response.setContentType(WAV_FILE_TYPE);
            }
            
            input = connection.getInputStream();
            OutputStream output = response.getOutputStream();
            IOUtils.copy(input, output);
        }
        catch(MalformedURLException e)
        {
            request.setAttribute(OutputBufferFilter.OUTPUT_SUPPRESS_KEY, Boolean.FALSE);
            throw new ListenServletException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
        finally
        {
            if(input != null)
            {
                try
                {
                    input.close();
                }
                catch(IOException e)
                {
                    LOG.warn("Unable to close InputStream when reading [" + voicemail.getUri() + "]");
                }
            }
        }
    }
    
}
