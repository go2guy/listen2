package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.NumberAlreadyInUseException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Subscriber.PlaybackOrder;
import com.interact.listen.security.SecurityUtil;
import com.interact.listen.stats.Stat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.classic.Session;
import org.hibernate.exception.ConstraintViolationException;

public class AddSubscriberServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_ADD_SUBSCRIBER);

        Subscriber currentSubscriber = ServletUtil.currentSubscriber(request);
        if(currentSubscriber == null)
        {
            throw new UnauthorizedServletException();
        }

        if(!currentSubscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException();
        }

        Subscriber subscriber = new Subscriber();

        String username = request.getParameter("username");
        subscriber.setUsername(username);

        if(username == null || username.trim().equals(""))
        {
            // TODO this should be done in Subscriber.validate(), but that method needs to handle
            // password and passwordConfirm first
            throw new BadRequestServletException("Please provide a Username");
        }

        String password = request.getParameter("password");
        if(password == null || password.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a Password");
        }

        String confirmPassword = request.getParameter("confirmPassword");
        if(confirmPassword == null || confirmPassword.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a Confirm Password");
        }

        if(!password.equals(confirmPassword))
        {
            throw new BadRequestServletException("Password and Confirm Password do not match");
        }

        subscriber.setPassword(SecurityUtil.hashPassword(password));

        if(License.isLicensed(ListenFeature.VOICEMAIL))
        {
            String voicemailPin = request.getParameter("voicemailPin");
            subscriber.setVoicemailPin(voicemailPin);

            Boolean enableEmail = Boolean.valueOf(request.getParameter("enableEmail"));
            Boolean enableSms = Boolean.valueOf(request.getParameter("enableSms"));
            String emailAddress = request.getParameter("emailAddress");
            String smsAddress = request.getParameter("smsAddress");
            Boolean enablePaging = Boolean.valueOf(request.getParameter("enablePaging"));
            Boolean enableAdmin = Boolean.valueOf(request.getParameter("enableAdmin"));
            PlaybackOrder playbackOrder = PlaybackOrder.valueOf(request.getParameter("voicemailPlaybackOrder"));

            subscriber.setIsEmailNotificationEnabled(enableEmail);
            subscriber.setIsSmsNotificationEnabled(enableSms);
            subscriber.setEmailAddress(emailAddress);
            subscriber.setSmsAddress(smsAddress);
            subscriber.setIsSubscribedToPaging(enablePaging);
            subscriber.setIsAdministrator(enableAdmin);
            subscriber.setVoicemailPlaybackOrder(playbackOrder);
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session, currentSubscriber, Channel.GUI);

        subscriber.setRealName(request.getParameter("realName"));

        try
        {
            if(!subscriber.validate())
            {
                throw new BadRequestServletException(subscriber.errors().get(0));
            }
            persistenceService.save(subscriber);
        }
        catch(ConstraintViolationException e)
        {
            throw new BadRequestServletException("A subscriber with that username already exists");
        }

        String accessNumbers = request.getParameter("accessNumbers");
        if(accessNumbers != null && accessNumbers.length() > 0)
        {
            try
            {
                subscriber.updateAccessNumbers(session, persistenceService, accessNumbers);
            }
            catch(NumberAlreadyInUseException e)
            {
                throw new BadRequestServletException("Access number [" + e.getNumber() +
                                                     "] is already in use by another account");
            }
        }

        Conference.createNew(persistenceService, subscriber);
    }
}
