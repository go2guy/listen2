package com.interact.listen.acd

import com.interact.listen.User
import com.interact.listen.exceptions.ListenAcdException
import grails.util.Holders
import org.joda.time.DateTime
import org.joda.time.Seconds

/**
 * User: knovak
 * Date: 12/13/13
 * Time: 10:08 AM
 */
class AcdCleanupJob
{
    def acdService;

    static triggers =
    {
    }

    def group = "acd"

    def execute()
    {
        log.info("Executing AcdCleanupJob");

        int waitingMax = acdService.getWaitingMax();
        int connectMax = acdService.getConnectMax();
        int enqueueMax = acdService.getEnqueueMax();

        //Get all calls enqueued too long

        //Get all calls where there are no agents with their skill

        //Get all agents who show on a call but who aren't

        def calls = acdService.listAllCalls();

        log.info("Number of waiting calls: " + calls.size());
        for(AcdCall thisCall : calls)
        {
            try
            {
                //Find calls enqueued too long
                if (thisCall.enqueueTime != null)
                {
                    int enqueueTime = Seconds.secondsBetween(thisCall.enqueueTime, DateTime.now()).getSeconds();
                    if (enqueueTime > enqueueMax)
                    {
                        log.error("AcdCall[" + thisCall.id + "] queued beyond the maximum allowed time!");
                        //Send them to voicemail
                        //                    acdService.acdCallVoicemail(thisCall);
                        //Not doing this now, need to revisit when we have a status transaction with ivr
                    }
                }

                switch(thisCall.callStatus)
                {
                    case AcdCallStatus.WAITING:
                        //Verify they haven't been waiting too long
                        int waitingTime = Seconds.secondsBetween(thisCall.lastModified, DateTime.now()).getSeconds();
                        if(waitingTime > waitingMax)
                        {
                            log.error("AcdCall[" + thisCall.id + "] waiting beyond the maximum allowed time!");
                            //Send them to voicemail
                            acdService.acdCallVoicemail(thisCall);
                        }
                        break;
                   case AcdCallStatus.CONNECT_REQUESTED:
                       //Verify they haven't been in this state too long
                       int requestTime = Seconds.secondsBetween(thisCall.lastModified, DateTime.now()).getSeconds();
                       if(requestTime > connectMax)
                       {
                           //Set back to waiting
                           log.error("AcdCall[" + thisCall.id + "] connect_requested beyond the maximum allowed time!");
                           acdService.acdCallStatusUpdate(thisCall.sessionId, AcdCallStatus.WAITING.toString());
                       }
                       break;
                }
            }
            catch(ListenAcdException ace)
            {
                log.error("Exception processing cleanup job: " + ace, ace);
            }
        }

        cleanOnACallUsers();
    }

    private void cleanOnACallUsers()
    {
        List<User>  badUsers = AcdUserStatus.executeQuery("Select a.owner from AcdUserStatus a where onACall=1 " +
                "and a.owner not in (select b.user from AcdCall b where user is not null)");

        if(badUsers != null && !badUsers.isEmpty())
        {
            if(log.isInfoEnabled())
            {
                log.info("Number of users with incorrect onACall status:" + badUsers.size());
            }

            for(User thisUser : badUsers)
            {
                thisUser.acdUserStatus.onACall = false;
                thisUser.save(flush: true);
            }
        }
    }
}
