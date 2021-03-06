package com.interact.listen;

import com.interact.listen.attendant.*;
import com.interact.listen.config.Property;
import com.interact.listen.history.Channel;
import com.interact.listen.jobs.FindMeExpirationReminderJob;
import com.interact.listen.jobs.NewVoicemailPagerJob;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.resource.*;
import com.interact.listen.resource.AccessNumber.NumberType;
import com.interact.listen.resource.Pin.PinType;
import com.interact.listen.security.SecurityUtil;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;

import liquibase.ClassLoaderFileOpener;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public final class HibernateUtil
{
    public static final Environment ENVIRONMENT = Environment.valueOf(System.getProperty("com.interact.listen.env", "PROD"));

    private static final Logger LOG = Logger.getLogger(HibernateUtil.class);
    private static final SessionFactory SESSION_FACTORY;

    /**
     * Provides default configuration values for various environments.
     */
    public static enum Environment
    {
        DEV   ("jdbc:hsqldb:mem:listendb",      "sa",   "", "org.hibernate.dialect.HSQLDialect",        "org.hsqldb.jdbcDriver", "super", "SELECT 1 FROM SUBSCRIBER"),
        TEST  ("jdbc:hsqldb:mem:listendb",      "sa",   "", "org.hibernate.dialect.HSQLDialect",        "org.hsqldb.jdbcDriver", "super", "SELECT 1 FROM SUBSCRIBER"),
//        STAGE ("jdbc:mysql://localhost/listen2", "root", "", "org.hibernate.dialect.MySQLInnoDBDialect", "com.mysql.jdbc.Driver", "Int3ract!Inc", "SELECT 1"),
//        PROD  ("jdbc:mysql://localhost/listen2?zeroDateTimeBehavior=convertToNull", "root", "", "org.hibernate.dialect.MySQLInnoDBDialect", "com.mysql.jdbc.Driver", "Int3ract!Inc", "SELECT 1");

        private final String dbUrl, dbUsername, dbPassword, dbDialect, dbDriver, dbTestQuery;
        private final String guiPassword;

        private Environment(String dbUrl, String dbUsername, String dbPassword, String dbDialect, String dbDriver, String guiPassword, String dbTestQuery)
        {
            this.dbUrl = dbUrl;
            this.dbUsername = dbUsername;
            this.dbPassword = dbPassword;
            this.dbDialect = dbDialect;
            this.dbDriver = dbDriver;
            this.guiPassword = guiPassword;
            this.dbTestQuery = dbTestQuery;
        }

        public String getDbUrl()
        {
            return dbUrl;
        }

        public String getDbUsername()
        {
            return dbUsername;
        }

        public String getDbPassword()
        {
            return dbPassword;
        }

        public String getDbDialect()
        {
            return dbDialect;
        }

        public String getDbDriver()
        {
            return dbDriver;
        }

        public String getGuiPassword()
        {
            return guiPassword;
        }

        public String getDbTestQuery()
        {
            return dbTestQuery;
        }
    }
    
    private HibernateUtil()
    {
        throw new AssertionError("Cannot instantiate utility class HibernateUtil");
    }

    static
    {
        StatSender statSender = new InsaStatSender();
        statSender.send(Stat.CONTROLLER_STARTUP);

        try
        {
            final String dbUrl = System.getProperty("com.interact.listen.db.url", ENVIRONMENT.getDbUrl());
            final String dbUsername = System.getProperty("com.interact.listen.db.username", ENVIRONMENT.getDbUsername());
            final String dbPassword = System.getProperty("com.interact.listen.db.password", ENVIRONMENT.getDbPassword());
            final String dbDialect = System.getProperty("com.interact.listen.db.dialect", ENVIRONMENT.getDbDialect());
            final String dbDriver = System.getProperty("com.interact.listen.db.driver", ENVIRONMENT.getDbDriver());
            final String dbTestQuery = System.getProperty("com.interact.listen.db.testQuery", ENVIRONMENT.getDbTestQuery());

            LOG.debug("DB connection string = [" + dbUrl + "]");
            LOG.debug("DB username =          [" + dbUsername + "]");
            LOG.debug("DB password =          [*]");
            LOG.debug("DB dialect =           [" + dbDialect + "]");
            LOG.debug("DB driver =            [" + dbDriver + "]");
           
            AnnotationConfiguration config = new AnnotationConfiguration();
            config.setProperty("hibernate.c3p0.acquire_increment", "3");
            config.setProperty("hibernate.c3p0.idle_test_period", "14400"); // seconds, must be less than mysql wait_timeout
            config.setProperty("hibernate.c3p0.max_size", System.getProperty("com.interact.listen.db.maxConnections", "50"));
            config.setProperty("hibernate.c3p0.max_statements", "0");
            config.setProperty("hibernate.c3p0.min_size", System.getProperty("com.interact.listen.db.minConnections", "5"));
            config.setProperty("hibernate.c3p0.preferredTestQuery", dbTestQuery);
            config.setProperty("hibernate.c3p0.timeout", "7200");
            config.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
            config.setProperty("hibernate.connection.autocommit", "false");
            config.setProperty("hibernate.connection.driver_class", dbDriver);
            config.setProperty("hibernate.connection.url", dbUrl);
            config.setProperty("hibernate.connection.username", dbUsername);
            config.setProperty("hibernate.connection.password", dbPassword);
            //config.setProperty("hibernate.connection.pool_size", "1");
            config.setProperty("hibernate.connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider");
            config.setProperty("hibernate.current_session_context_class", "thread");
            config.setProperty("hibernate.dialect", dbDialect);
            config.setProperty("hibernate.show_sql", "true");
            config.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");

            // application classes
            config.addAnnotatedClass(AccessNumber.class);
            config.addAnnotatedClass(Action.class);
            config.addAnnotatedClass(ActionHistory.class);
            config.addAnnotatedClass(Audio.class);
            config.addAnnotatedClass(CallDetailRecord.class);
            config.addAnnotatedClass(Conference.class);
            config.addAnnotatedClass(ConferenceHistory.class);
            config.addAnnotatedClass(ConferenceRecording.class);
            config.addAnnotatedClass(DeviceRegistration.class);
            config.addAnnotatedClass(DialNumberAction.class);
            config.addAnnotatedClass(DialPressedNumberAction.class);
            config.addAnnotatedClass(FindMeNumber.class);
            config.addAnnotatedClass(GoToMenuAction.class);
            config.addAnnotatedClass(History.class);
            config.addAnnotatedClass(LaunchApplicationAction.class);
            config.addAnnotatedClass(Menu.class);
            config.addAnnotatedClass(Participant.class);
            config.addAnnotatedClass(Pin.class);
            config.addAnnotatedClass(Property.class);
            config.addAnnotatedClass(ScheduledConference.class);
            config.addAnnotatedClass(Subscriber.class);
            config.addAnnotatedClass(TimeRestriction.class);
            config.addAnnotatedClass(Voicemail.class);
            config.addAnnotatedClass(GroupMember.class);
            config.addAnnotatedClass(CallRestriction.class);

            SESSION_FACTORY = config.buildSessionFactory();

//            doLiquibaseUpgrades();

            Session session = getSessionFactory().getCurrentSession();
            Transaction transaction = session.beginTransaction();

            PersistenceService persistenceService = new DefaultPersistenceService(session, null, Channel.GUI);

            // at this point, we've been persisting every Resource, which means things like
            // Participants and Conference status get saved. resetting these data on startup
            // restores all conferences to a known state and removes the transient data.

            // IMPORTANT if we ever implement an acive-active (and/or clustered) solution, this
            // deletion needs to be moved or removed. if the transient data is distributed
            // throughout the cluster, this would cause it to be removed everywhere if another
            // node was started while the cluster was running
            resetTransientData(session);
            createAdminSubscriberIfNotPresent(session, persistenceService);
            createDefaultAttendantMenuIfNotPresent(session);

            if(Boolean.valueOf(System.getProperty("bootstrap", "false")))
            {
                bootstrap(persistenceService);
            }
            
            startBackgroundJobs();

            transaction.commit();
        }
        catch(Throwable t) // SUPPRESS CHECKSTYLE IllegalCatchCheck
        {
            LOG.error("SessionFactory creation failed", t);
            throw new ExceptionInInitializerError(t);
        }
    }

    public static SessionFactory getSessionFactory()
    {
        return SESSION_FACTORY;
    }

    // TODO get rid of this eventually (replace with some sort of SQL script, maybe? or don't even need it?)
    private static void bootstrap(PersistenceService persistenceService)
    {
        // provisions subscribers/users/conferences/participants

        // for N=0..5

        // subscriber number = 10N
        // subscriber VM pin = 10N
        // user username = 10N
        // user password = super
        // conference activePin = 11110N
        // conference adminPin = 99910N
        // conference passivePin = 00010N
        // participant numbers are 40200N000M, where M=0..9

        // dummy accounts
        for(int i = 0; i < 25; i++)
        {
            String extension = new DecimalFormat("000").format(100 + i);

            Subscriber subscriber = new Subscriber();
            subscriber.setPassword(SecurityUtil.hashPassword(ENVIRONMENT.getGuiPassword()));
            subscriber.setUsername(extension);
            subscriber.setVoicemailPin(extension);
            if(!subscriber.validate())
            {
                throw new ExceptionInInitializerError(subscriber.errors().get(0));
            }
            persistenceService.save(subscriber);

            AccessNumber accessNumber = new AccessNumber();
            accessNumber.setGreetingLocation("/greetings/" + extension);
            accessNumber.setNumber(extension);
            accessNumber.setSubscriber(subscriber);
            accessNumber.setNumberType(NumberType.EXTENSION);
            persistenceService.save(accessNumber);

            subscriber.addToAccessNumbers(accessNumber);

            //Pin activePin = Pin.newInstance("111" + basePin, PinType.ACTIVE);
            //Pin adminPin = Pin.newInstance("999" + basePin, PinType.ADMIN);
            //Pin passivePin = Pin.newInstance("000" + basePin, PinType.PASSIVE);

            Pin activePin = Pin.newRandomInstance(PinType.ACTIVE);
            Pin adminPin = Pin.newRandomInstance(PinType.ADMIN);
            Pin passivePin = Pin.newRandomInstance(PinType.PASSIVE);

            persistenceService.save(activePin);
            persistenceService.save(adminPin);
            persistenceService.save(passivePin);

            Conference conference = new Conference();
            conference.addToPins(activePin);
            conference.addToPins(adminPin);
            conference.addToPins(passivePin);

            conference.setIsStarted(true);
            conference.setIsRecording(false);
            conference.setDescription(subscriber.getUsername() + "'s Conference");
            persistenceService.save(conference);

            subscriber.addToConferences(conference);

            LOG.debug("Saved Conference " + conference.getId());

            for(int j = 0; j < 10; j++)
            {
                Participant participant = new Participant();
                participant.setAudioResource("/foo/bar");
                participant.setConference(conference);
                participant.setIsAdmin(j == 0);
                participant.setIsAdminMuted(false);
                participant.setIsMuted(false);
                participant.setIsPassive(j == 6);
                participant.setNumber("402" + extension + new DecimalFormat("0000").format(j));
                participant.setSessionID(participant.getNumber() + String.valueOf(System.currentTimeMillis()));
                persistenceService.save(participant);

                LOG.debug("Saved Participant " + participant.getId());
            }
        }
    }

    private static void createAdminSubscriberIfNotPresent(Session session, PersistenceService persistenceService)
    {
        Long count = Subscriber.count(session);
        if(count == 0)
        {
            LOG.debug("Created admin Subscriber");
            Subscriber subscriber = new Subscriber();
            subscriber.setIsAdministrator(Boolean.TRUE);
            subscriber.setPassword(SecurityUtil.hashPassword(ENVIRONMENT.getGuiPassword()));
            subscriber.setUsername("Admin");
            subscriber.setVoicemailPin("000");
            persistenceService.save(subscriber);
        }
    }

    private static void createDefaultAttendantMenuIfNotPresent(Session session)
    {
        if(!License.isLicensed(ListenFeature.ATTENDANT))
        {
            LOG.debug("Will not create default Attendant menu, feature is not licensed");
            return;
        }

        if(Menu.queryByName(session, Menu.TOP_MENU_NAME).size() > 0)
        {
            LOG.debug("Top menu already exists, will not create a new one");
            return;
        }

        Action defaultAction = new DialPressedNumberAction();
        session.save(defaultAction);
        Action timeoutAction = new GoToMenuAction();
        session.save(timeoutAction);

        Menu menu = new Menu();
        menu.setAudioFile("");
        menu.setName(Menu.TOP_MENU_NAME);
        menu.setDefaultAction(defaultAction);
        menu.setTimeoutAction(timeoutAction);
        session.save(menu);

        // some random actions for testing, these can be deleted after development
        DialNumberAction dialNumberAction = new DialNumberAction();
        dialNumberAction.setMenu(menu);
        dialNumberAction.setKeyPressed("1");
        dialNumberAction.setNumber("123");
        session.save(dialNumberAction);

        DialPressedNumberAction dialPressedNumberAction = new DialPressedNumberAction();
        dialPressedNumberAction.setMenu(menu);
        dialPressedNumberAction.setKeyPressed("???");
        session.save(dialPressedNumberAction);

        LaunchApplicationAction launchApplicationAction = new LaunchApplicationAction();
        launchApplicationAction.setMenu(menu);
        launchApplicationAction.setKeyPressed("3");
        launchApplicationAction.setApplicationName("conferencing");
        session.save(launchApplicationAction);

        ((GoToMenuAction)timeoutAction).setGoToMenu(menu);
        session.save(timeoutAction);
    }

    private static void startBackgroundJobs() throws SchedulerException, ParseException
    {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        scheduleVoicemailPagerJob(scheduler);
        scheduleFindMeReminderJob(scheduler);
        scheduler.start();
    }

    private static void scheduleVoicemailPagerJob(Scheduler scheduler) throws SchedulerException, ParseException
    {
        JobDetail job = new JobDetail("voicemailPager", NewVoicemailPagerJob.class);
        CronTrigger trigger = new CronTrigger("voicemailPagerTrigger");
        trigger.setCronExpression("0 0/1 * * * ?");
        scheduler.scheduleJob(job, trigger);
    }

    private static void scheduleFindMeReminderJob(Scheduler scheduler) throws SchedulerException, ParseException
    {
        JobDetail job = new JobDetail("findMeReminder", FindMeExpirationReminderJob.class);
        CronTrigger trigger = new CronTrigger("findMeReminderTrigger");
        trigger.setCronExpression("0 0/30 * * * ?");
        scheduler.scheduleJob(job, trigger);
    }

    private static void resetTransientData(Session session)
    {
        int removed = session.createQuery("delete Participant").executeUpdate();
        if(removed > 0)
        {
            LOG.warn("Removed " + removed + " existing participants on startup");
        }

        int updated = session.createQuery("update Conference set isStarted = false, isRecording = false, arcadeId = null, recordingSessionId = NULL").executeUpdate();
        if(updated > 0)
        {
            LOG.warn("Updated " + updated + " existing conference statuses on startup");
        }
    }
}
