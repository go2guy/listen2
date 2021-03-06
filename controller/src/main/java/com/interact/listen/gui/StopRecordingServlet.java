package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.spot.SpotCommunicationException;
import com.interact.listen.spot.SpotSystem;
import com.interact.listen.stats.Stat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class StopRecordingServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if(!License.isLicensed(ListenFeature.CONFERENCING))
        {
            throw new NotLicensedException(ListenFeature.CONFERENCING);
        }

        ServletUtil.sendStat(request, Stat.GUI_STOP_RECORDING);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException();
        }

        Long id = ServletUtil.getNotNullLong("id", request, "Id");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        Conference conference = Conference.queryById(session, id);
        
        // System admins and owners of the conference are the only ones that can start recording.
        if(!subscriber.ownsConference(conference))
        {
            throw new UnauthorizedServletException("Not allowed to stop recording");
        }
        
        if(!conference.getIsStarted())
        {
            throw new BadRequestServletException("Conference must be started for recording");
        }

        String adminSessionId = conference.firstAdminSessionId(session);

        SpotSystem spotSystem = new SpotSystem(subscriber);
        try
        {
            spotSystem.stopRecording(conference, adminSessionId);
        }
        catch(SpotCommunicationException e)
        {
            throw new ServletException(e);
        }
    }
}
