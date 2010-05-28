package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.ResourceListService.Builder;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.*;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class GetConferenceInfoServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        if(!License.isLicensed(ListenFeature.CONFERENCING))
        {
            throw new ServletException(new NotLicensedException(ListenFeature.CONFERENCING));
        }

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_GET_CONFERENCE_INFO);

        User user = (User)(request.getSession().getAttribute("user"));
        if(user == null)
        {
            throw new ListenServletException(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized - not logged in",
                                             "text/plain");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session);

        String id = request.getParameter("id");

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

        Conference conference = GuiServletUtil.getConferenceFromIdOrUser(id, user, persistenceService);

        if(conference == null)
        {
            throw new ListenServletException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Conference not found",
                                             "text/plain");
        }

        if(!user.equals(conference.getUser()) && !user.getIsAdministrator())
        {
            throw new ListenServletException(HttpServletResponse.SC_UNAUTHORIZED,
                                             "Unauthorized - conference does not belong to user", "text/plain");
        }

        StringBuilder content = new StringBuilder();
        try
        {
            content.append("{");
            content.append("\"info\":").append(getInfo(conference, marshaller)).append(",");
            content.append("\"participants\":").append(getParticipants(conference, marshaller, session)).append(",");
            content.append("\"pins\":").append(getPins(conference, marshaller, session)).append(",");
            content.append("\"history\":").append(getHistory(conference, marshaller, session)).append(",");
            content.append("\"recordings\":").append(getRecordings(conference, marshaller, session));
            content.append("}");
        }
        catch(CriteriaCreationException e)
        {
            throw new ServletException(e);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        OutputBufferFilter.append(request, content.toString(), marshaller.getContentType());
    }

    private String getInfo(Conference conference, Marshaller marshaller)
    {
        return marshaller.marshal(conference);
    }

    private String getParticipants(Conference conference, Marshaller marshaller, Session session) throws CriteriaCreationException
    {
        Builder builder = new ResourceListService.Builder(Participant.class, session, marshaller)
            .addSearchProperty("conference", "/conferences/" + conference.getId())
            .addReturnField("id")
            .addReturnField("isAdmin")
            .addReturnField("isAdminMuted")
            .addReturnField("isMuted")
            .addReturnField("isPassive")
            .addReturnField("number");
        ResourceListService service = builder.build();
        return service.list();
    }

    private String getPins(Conference conference, Marshaller marshaller, Session session) throws CriteriaCreationException
    {
        Builder builder = new ResourceListService.Builder(Pin.class, session, marshaller)
            .addSearchProperty("conference", "/conferences/" + conference.getId())
            .addReturnField("id")
            .addReturnField("number")
            .addReturnField("type");
        ResourceListService service = builder.build();
        return service.list();
    }

    private String getHistory(Conference conference, Marshaller marshaller, Session session) throws CriteriaCreationException
    {
        Builder builder = new ResourceListService.Builder(ConferenceHistory.class, session, marshaller)
            .addSearchProperty("conference", "/conferences/" + conference.getId())
            .addReturnField("dateCreated")
            .addReturnField("description")
            .addReturnField("id")
            .sortBy("dateCreated", ResourceListService.SortOrder.DESCENDING)
            .withMax(15);
        ResourceListService service = builder.build();
        return service.list();
    }

    private String getRecordings(Conference conference, Marshaller marshaller, Session session) throws CriteriaCreationException
    {
        Builder builder = new ResourceListService.Builder(ConferenceRecording.class, session, marshaller)
            .addSearchProperty("conference", "/conferences/" + conference.getId())
            .addReturnField("dateCreated")
            .addReturnField("description")
            .addReturnField("fileSize")
            .addReturnField("id")
            .sortBy("dateCreated", ResourceListService.SortOrder.ASCENDING)
            .withMax(15);
        ResourceListService service = builder.build();
        return service.list();
    }
}
