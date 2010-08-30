-- MySQL dump 10.11
--
-- Host: localhost    Database: spotbuild
-- ------------------------------------------------------
-- Server version	5.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `audio_file`
--

LOCK TABLES `audio_file` WRITE;
/*!40000 ALTER TABLE `audio_file` DISABLE KEYS */;
INSERT INTO `audio_file` VALUES (52,'thankYou',37,'00'),(54,'serverErr',37,'00'),(130,'acceptRequest',39,'00'),(131,'activeByAdmin',39,'00'),(132,'activeMode',39,'00'),(133,'addMenu',39,'00'),(134,'adminConfMenu',39,'00'),(135,'adminRollInfo',39,'00'),(136,'callFailed',39,'00'),(137,'confEndErr',39,'00'),(138,'confNotStarted',39,'00'),(139,'confRequest',39,'00'),(140,'currently',39,'00'),(141,'digitsLong',39,'00'),(142,'droppedByAdmin',39,'00'),(143,'endByAdmin',39,'00'),(144,'endRoll',39,'00'),(145,'err',39,'00'),(146,'err2',39,'00'),(147,'errActiveEvent',39,'00'),(148,'errDropEvent',39,'00'),(149,'errPassiveEvent',39,'00'),(150,'getDestHelp',39,'00'),(151,'getDestHelp2',39,'00'),(152,'getDestination',39,'00'),(153,'getPin',39,'00'),(154,'goodbye',39,'00'),(155,'hasJoined',39,'00'),(156,'hasLeft',39,'00'),(157,'hello',39,'00'),(158,'joinningConf',39,'00'),(159,'mainMenu',39,'00'),(160,'mainMenuHelp',39,'00'),(161,'maxError',39,'00'),(162,'memberConfMenu',39,'00'),(163,'memberConfMenu2',39,'00'),(164,'memberRollInfo',39,'00'),(165,'noAnswer',39,'00'),(166,'noneWaiting',39,'00'),(167,'noParticipants',39,'00'),(168,'notAllowedAdmin',39,'00'),(169,'notAllowedPassive',39,'00'),(170,'oneParticipant',39,'00'),(171,'participantActive',39,'00'),(172,'participantDropped',39,'00'),(173,'participantPassive',39,'00'),(174,'participantsWaiting',39,'00'),(175,'passiveByAdmin',39,'00'),(176,'passiveMode',39,'00'),(177,'pinEntry',39,'00'),(178,'pleaseHold',39,'00'),(179,'recName',39,'00'),(180,'reoriginate',39,'00'),(181,'restartRoll',39,'00'),(182,'rollUnavailable',39,'00'),(183,'serviceErr',39,'00'),(184,'serviceErr2',39,'00'),(185,'startMenu',39,'00'),(186,'startMenu2',39,'00'),(187,'startMenu2Help',39,'00'),(188,'startMenuHelp',39,'00'),(189,'thankYou',39,'00'),(190,'thankYouService',39,'00'),(191,'tryAgainLater',39,'00'),(192,'tryAgainNow',39,'00'),(193,'welcome',39,'00'),(194,'confIn',39,'00'),(195,'confOut',39,'00'),(196,'onHoldMusic',39,'00'),(200,'beenDropped',39,'00'),(201,'inActiveMode',39,'00'),(202,'inPassiveMode',39,'00'),(203,'toDrop',39,'00'),(204,'confirmDrop',39,'00'),(205,'notBeenDropped',39,'00'),(207,'serviceError',35,'00'),(208,'serviceError2',35,'00'),(209,'tryAgainLater',35,'00'),(210,'thankYou',35,'00'),(211,'goodbye',35,'00'),(212,'serviceErr',41,'00'),(213,'serviceErr2',41,'00'),(214,'tryAgainLater',41,'00'),(215,'thankYouService',41,'00'),(251,'adminConfMenuNoRec',39,'00'),(253,'hello',43,'00'),(254,'confRequest',43,'00'),(255,'acceptRequest',43,'00'),(256,'thankYouService',43,'00'),(257,'goodbye',43,'00'),(277,'getMbx',44,'00'),(280,'getPasscode',44,'00'),(282,'messageSaved',44,'00'),(283,'requestFailed',44,'00'),(284,'messageDeleted',44,'00'),(285,'lastOldMessage',44,'00'),(286,'lastMessage',44,'00'),(287,'lastNewMessage',44,'00'),(288,'vmNoMsgs',44,'00'),(289,'vmNoNewMsgs',44,'00'),(290,'vmOneNewMsg',44,'00'),(291,'vmNewMsgs',44,'00'),(292,'youHave',44,'00'),(293,'vmNoOldMsgs',44,'00'),(294,'vmOneOldMsg',44,'00'),(295,'vmOldMsgs',44,'00'),(296,'enterMailbox',44,'00'),(297,'messageForwarded',44,'00'),(298,'adminConfMenuStopRec',39,'00'),(299,'adminConfMenuStartRec',39,'00'),(300,'getOnDemand',39,'00'),(301,'getOnDemandHelp',39,'00'),(302,'connectingCall',39,'00'),(303,'inviteSent',39,'00'),(304,'recNameErr',39,'00'),(305,'recStartSuccess',39,'00'),(306,'recStpSuccess',39,'00'),(307,'getInviteDest',39,'00'),(308,'goodbye',38,'00'),(309,'beginRec',38,'00'),(310,'recOpts',38,'00'),(311,'grtngSaved',38,'00'),(312,'err1',38,'00'),(313,'err2',38,'00'),(314,'maxErr',38,'00'),(315,'mbxMenu',38,'00'),(316,'ServiceErr',38,'00'),(317,'getMbx',38,'00'),(319,'youHave',38,'00'),(320,'messageDeleted',38,'00'),(321,'messageSaved',38,'00'),(322,'vmNoMsgs',38,'00'),(323,'vmNewMsgs',38,'00'),(324,'vmNoNewMsgs',38,'00'),(325,'vmNoOldMsgs',38,'00'),(326,'vmOldMsgs',38,'00'),(327,'vmOneNewMsg',38,'00'),(328,'vmOneOldMsg',38,'00'),(329,'lastMessage',38,'00'),(330,'lastNewMessage',38,'00'),(331,'lastOldMessage',38,'00'),(332,'newPasscode',38,'00'),(333,'newPasscodeAgain',38,'00'),(334,'followedByPound',38,'00'),(335,'passcodeChange',38,'00'),(336,'lastGreeting',38,'00'),(337,'poundPrevMenu',38,'00'),(339,'menuGreetingChange',38,'00'),(341,'voicemailMenu',38,'00'),(342,'messageForwarded',38,'00'),(343,'mbxGrtng',37,'00'),(344,'beginRec',37,'00'),(345,'msgSaved',37,'00'),(346,'noMailbox',37,'00'),(347,'failedDelete',38,'00'),(348,'failedForwarded',38,'00'),(349,'failedSave',38,'00'),(350,'forwardFailed',44,'00'),(351,'deleteFailed',44,'00'),(352,'failedSave',44,'00'),(353,'enterMailbox',38,'00'),(354,'err2_noHelp',39,'00'),(355,'err_noHelp',39,'00'),(358,'maintMenu',38,'00'),(360,'getPasscode',38,'00'),(361,'err1noHelp',38,'00'),(362,'err2noHelp',38,'00'),(363,'voicemailMenu',44,'00'),(364,'mbxMenu',44,'00'),(365,'err1',44,'00'),(366,'err2',44,'00'),(367,'maxErr',44,'00'),(368,'err1noHelp',44,'00'),(369,'err2noHelp',44,'00'),(371,'goodbye',44,'00'),(372,'serverErr',44,'00'),(373,'enterAnotherMbx',44,'00'),(374,'err1',48,'00'),(375,'err1noHelp',48,'00'),(376,'err2',48,'00'),(377,'err2noHelp',48,'00'),(378,'goodbye',48,'00'),(379,'maxErr',48,'00'),(380,'serviceErr2',48,'00'),(381,'serviceError',48,'00'),(382,'thankYouService',48,'00'),(385,'greeting',48,'00');
/*!40000 ALTER TABLE `audio_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `entry_point`
--

LOCK TABLES `entry_point` WRITE;
/*!40000 ALTER TABLE `entry_point` DISABLE KEYS */;
INSERT INTO `entry_point` VALUES (72,'recordMsg',54),(75,'mbxMaint',60),(77,'recGrtng',61),(79,'recOpts',63),(84,'error',65),(85,'endCall',65),(86,'nextPath',64),(91,'passCode',67),(92,'mbxMenu',66),(94,'mbxInfo',67),(97,'saveGrtng',68),(98,'updateGrtng',68),(99,'mbxMenuA',66),(100,'maintMenuA',60),(101,'recOptsA',63),(159,'invalidMbx',53),(184,'getPin',79),(185,'error',80),(186,'nextPath',81),(187,'endCall',82),(188,'validatePin',83),(189,'serviceError',84),(190,'recordName',85),(191,'setParams',85),(192,'confCheck',86),(193,'chkStart',86),(194,'joinConf',86),(202,'confMenu',90),(203,'userEntry',90),(212,'startConf',94),(213,'getIDs',94),(214,'setRequest',94),(215,'getTotal',94),(216,'nextSID',94),(217,'updateCnt',94),(230,'mainMenu',104),(231,'Msg Entry',101),(232,'getEntry',104),(235,'Msg Play',102),(236,'Msg Menu',103),(237,'callFailed',105),(238,'noAnswer',105),(239,'dropKey',105),(240,'calleeHungUp',105),(241,'pathUpdate',105),(243,'Next Message',102),(258,'playWelcome',109),(259,'rollCall',110),(260,'rstrtRoll',110),(261,'setRequest',110),(262,'sendHTTP',110),(263,'nextName',110),(264,'updateCount',110),(265,'rollOpts',111),(266,'endConference',113),(268,'nextSID',113),(269,'sendHTTP',113),(270,'updateCount',113),(271,'setRequest',113),(273,'onDemand',105),(274,'startMenu',115),(276,'setPath',115),(277,'conference',116),(278,'rejoinConf',116),(279,'keyPress',116),(281,'updateStatus',117),(282,'deleteUser',117),(283,'notify',117),(284,'chkUser',117),(285,'setPath',113),(290,'recordConf',121),(291,'saveMsg',120),(295,'chkInfo',122),(298,'stopRecord',123),(301,'getDestination',125),(302,'enterDestination',125),(303,'interactiveCall',91),(304,'automatedCall',114),(306,'getDest',125),(307,'demandType',125),(308,'startRecord',127),(309,'stopRecord',128),(311,'updateInterface',129),(322,'endSession',132),(323,'postRecord',133),(324,'stopError',123),(325,'confStatus',134),(326,'nextOption',123),(327,'automatedCall',136),(328,'end',137),(329,'invite',136),(330,'Change Pwd',140),(331,'Next Message',103),(335,'mbxNumber',141),(336,'validateMbx',53),(339,'Set File',143),(341,'sendCDR',105),(343,'sendCDR',117),(344,'chkPath',117),(345,'chkPath',105),(346,'memberCnt',115),(347,'forward msg',144),(348,'endCall',145),(349,'copyMsg',144),(350,'exitApp',146),(352,'Reply Msg',147),(353,'Voicemail',147),(354,'Fwd Failed',144),(355,'Error',150),(356,'End Call',150),(357,'Service Error',150),(359,'Passcode',149),(360,'Mbx Number',149),(362,'Msg Entry',151),(363,'Mbx Info',149),(364,'postCDR',120),(365,'Msg Play',152),(366,'Msg Menu',153),(367,'Save Msg',153),(368,'Msg Delete',153),(369,'Next Message',153),(370,'Forward Msg',154),(371,'Copy Msg',154),(372,'Fwd Failed',154),(376,'Play Menu',103),(378,'deleteTmp',68),(379,'Conf Check Entry A',86),(380,'Get Subscriber',86),(381,'Record Error',85),(383,'Record Error',127),(384,'notifyConf',122),(386,'chkEndCall',123),(387,'Msg Count',156),(388,'Service Error',65),(389,'Old Count',101),(390,'No Input',65),(391,'Pwd First Entry',140),(392,'Pwd Second Entry',140),(393,'Set New Password',140),(394,'Forward Menu',144),(395,'Check Directory',144),(396,'Delete Message',157),(397,'Msg Deleted',157),(398,'Save Message',158),(399,'Message Saved',158),(400,'Play Message',102),(401,'Play Menu',153),(403,'QA Bypass Passcode',67),(408,'Msg Count',161),(409,'Old Count',151),(410,'Error No Help',65),(411,'Next Message',152),(412,'chkAccess',162),(413,'Mbx Menu',163),(414,'Menu A',163),(415,'exit',164),(416,'Error No Help',150),(417,'Next Path',165),(418,'No Input',150),(419,'Restart',102),(420,'Message End',153),(421,'exit',166),(422,'getConfInfo',167),(423,'chkPath',167),(425,'Check Mbx',154),(426,'Get Mailbox',154),(430,'getSID',167),(431,'getInfo',169),(432,'setVars',169),(434,'Service Error Msg',65),(435,'Service Error Msg',145),(437,'Service Error Msg',84),(438,'Service Error',137),(440,'getInfo',142),(441,'Service Error Msg',150),(442,'Next Path',171),(444,'chkFolder',53),(445,'getGreeting',173),(446,'chkURL',173),(447,'defaultGrtng',173),(449,'chkRecord',174),(450,'sendStat',175),(451,'serviceError',175),(452,'Error No Help',172),(453,'Error Help',172),(454,'No Input',172),(455,'End Call',172),(456,'Service Error',172),(457,'Send Stat',172),(458,'Service Error Stat',172),(463,'chkEntry',143),(465,'Play Menu',179),(466,'chkArtifactDir',177),(468,'getAccessNum',143),(469,'getGrtngURL',143),(470,'getSubID',141),(471,'Menu Options',180),(473,'Menu Opt - menu',180),(474,'Menu Opt - app',180),(475,'getEvent',126),(476,'getSIP',109),(477,'Dial',181);
/*!40000 ALTER TABLE `entry_point` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `globals`
--

LOCK TABLES `globals` WRITE;
/*!40000 ALTER TABLE `globals` DISABLE KEYS */;
INSERT INTO `globals` VALUES (279,'userID',39),(281,'getInput',39),(283,'errCnt',39),(284,'confID',39),(285,'whichPath',39),(286,'maxErr',39),(287,'MODE',39),(289,'DATA',39),(294,'SID',39),(295,'tempSID',39),(297,'audioURL',39),(298,'request',39),(299,'params',39),(300,'id',39),(301,'cntrlURL',39),(302,'status',39),(303,'returnVal',39),(304,'returnStr',39),(305,'isAdmin',39),(306,'reqMinCnt',39),(307,'reqMaxCnt',39),(308,'reqCnt',39),(309,'sipURL',39),(310,'FILE1',39),(311,'OPERATION',39),(312,'isMuted',39),(313,'isAdminMuted',39),(315,'isPassive',39),(316,'isActive',39),(318,'resource',39),(319,'ANI',39),(320,'index',39),(321,'memberCnt',39),(322,'prevPath',39),(324,'phoneNumber',39),(325,'whichEvent',39),(326,'helpCnt',39),(327,'maxHelp',39),(330,'hostName',39),(331,'fileName',39),(332,'outBoundID',39),(336,'callType',39),(337,'isInfoSaved',39),(339,'maxReqCnt',39),(340,'pstnLength',39),(341,'DNIS',39),(342,'appToAccess',35),(343,'DNIS',35),(344,'cntrlURL',35),(346,'subID',35),(350,'passValues',35),(406,'isRecording',39),(410,'recSID',39),(415,'params',35),(416,'resource',35),(417,'request',35),(418,'status',35),(423,'argList',42),(425,'recEvent',42),(426,'recSID',42),(430,'recFile',42),(431,'recPath',42),(432,'recSize',42),(433,'recLength',42),(434,'resource',42),(435,'request',42),(436,'cntrlURL',42),(437,'params',42),(438,'id',42),(439,'status',42),(440,'returnVal',42),(441,'returnStr',42),(442,'isRecording',42),(443,'isStarted',42),(447,'FILE1',42),(448,'OPERATION',42),(449,'hostName',42),(451,'confID',42),(452,'arcadeID',42),(455,'recInfo',42),(456,'hostName',35),(471,'passValues',39),(472,'entryTone',39),(473,'isInteractiveCall',39),(474,'destAudioFile',39),(475,'timeStamp',39),(476,'rollcallPath',39),(479,'isStarted',39),(480,'CONF_ID',39),(484,'argList',43),(485,'audioURL',43),(486,'phoneNumber',43),(487,'outBoundID',43),(488,'confID',43),(489,'cntrlURL',43),(490,'hostName',43),(491,'userID',43),(492,'maxErr',43),(493,'errCnt',43),(495,'getInput',43),(496,'sipURL',35),(503,'index',43),(519,'ANI',37),(520,'DNIS',37),(521,'status',37),(522,'fileLocation',37),(523,'isNew',37),(524,'subID',37),(525,'returnVal',37),(526,'returnStr',37),(527,'FILE1',37),(528,'OPERATION',37),(529,'request',37),(530,'resource',37),(531,'params',37),(532,'cntrlURL',37),(533,'audioURL',37),(534,'index',37),(535,'hostName',37),(536,'fileSize',37),(537,'passValues',37),(538,'duration',37),(543,'subID',39),(544,'confStartTime',39),(545,'tmpVal',39),(546,'sysAccessTime',35),(547,'ANI',35),(548,'sysAccessTime',39),(549,'request',43),(550,'resource',43),(551,'status',43),(553,'params',43),(554,'sysAccessTime',43),(556,'outDialStart',39),(557,'sysAccessTime',37),(560,'toggleLight',37),(563,'accessNum',37),(564,'sysAccessTime',42),(588,'confDescription',39),(589,'confDescription',42),(602,'accessID',37),(609,'toExt',37),(613,'outDialStop',39),(617,'confStatus',39),(619,'subscriber',39),(620,'rollcallPathSuf',39),(621,'recLoopAttempt',39),(622,'subscriber',42),(623,'recPathSuf',42),(624,'subscriber',43),(625,'audioURLsuf',43),(626,'returnVal',43),(627,'returnStr',43),(628,'FILE1',43),(629,'OPERATION',43),(634,'quickAccess',38),(635,'nxtPath',38),(636,'maxErr',38),(637,'subID',38),(638,'errCnt',38),(639,'FILE1',38),(640,'FILE2',38),(641,'OPERATION',38),(642,'recDIR',38),(643,'status',38),(644,'ANI',38),(645,'userInput',38),(646,'request',38),(647,'resource',38),(648,'params',38),(649,'cntrlURL',38),(650,'id',38),(651,'returnVal',38),(652,'returnStr',38),(653,'pin',38),(654,'reason',38),(655,'numOfVoicemails',38),(656,'count',38),(657,'voicemails',38),(658,'result',38),(659,'fileLocation',38),(660,'menuOption',38),(661,'hostName',38),(662,'passValues',38),(664,'newResult',38),(665,'oldResult',38),(666,'newCount',38),(667,'oldCount',38),(668,'vmPathOption',38),(669,'vmDateCreated',38),(670,'date',38),(671,'time',38),(672,'pinFirst',38),(673,'pinSecond',38),(674,'noInputCnt',38),(675,'subscriberInfo',38),(676,'subscriber',38),(677,'grtngList',38),(678,'grtngSelect',38),(679,'subFwd',38),(680,'DNIS',38),(681,'sysAccessTime',38),(682,'toggleLight',38),(683,'accessNum',38),(685,'vmPathSuffex',38),(686,'grtPathSuffex',38),(687,'application',38),(688,'voicemailInfo',38),(689,'playHelp',39),(699,'playbackOrder',38),(705,'HTTPcontroller',35),(706,'HTTPcontroller',38),(707,'HTTPcontroller',37),(708,'HTTPcontroller',42),(709,'HTTPcontroller',43),(710,'HTTPcontroller',39),(713,'cntrlURL',44),(714,'params',44),(715,'resource',44),(716,'request',44),(717,'status',44),(718,'returnVal',44),(719,'returnStr',44),(720,'userInput',44),(721,'pin',44),(722,'nxtPath',44),(723,'subscriber',44),(724,'maxErr',44),(725,'errCnt',44),(726,'fileLocation',44),(727,'afterHourSub',44),(728,'passValues',44),(729,'reason',44),(730,'newResult',44),(731,'newCount',44),(732,'oldResult',44),(733,'oldCount',44),(734,'vmPathOption',44),(735,'numOfVoicemails',44),(736,'count',44),(737,'voicemails',44),(738,'result',44),(739,'vmDateCreated',44),(740,'date',44),(741,'time',44),(742,'FILE1',44),(743,'FILE2',44),(744,'vmPathPrefix',44),(745,'hostName',44),(746,'menuOption',44),(747,'id',44),(748,'OPERATION',44),(749,'errLoop',44),(750,'subFwd',44),(751,'playbackOrder',44),(752,'voicemailInfo',44),(753,'subscriberInfo',44),(754,'HTTPcontroller',44),(755,'argList',46),(756,'FILE1',46),(757,'OPERATION',46),(758,'status',46),(759,'artifacts_dir',46),(760,'HTTP_dir',46),(761,'callType',35),(762,'eventName',47),(763,'sessionID',47),(764,'argList',47),(766,'sessionID',42),(767,'STATcontroller',35),(768,'id',35),(769,'source',35),(770,'value',35),(771,'operation',35),(772,'STATcontroller',38),(773,'source',38),(774,'value',38),(775,'operation',38),(776,'STATcontroller',37),(777,'id',37),(778,'source',37),(779,'value',37),(780,'operation',37),(781,'STATcontroller',39),(782,'source',39),(783,'value',39),(784,'operation',39),(785,'STATcontroller',43),(786,'id',43),(787,'source',43),(788,'value',43),(789,'operation',43),(790,'STATcontroller',42),(791,'source',42),(792,'value',42),(793,'operation',42),(795,'artifactsDIR',35),(796,'artifactsDIR',37),(797,'msgDIR',37),(798,'STATcontroller',44),(799,'source',44),(800,'value',44),(801,'operation',44),(802,'ANI',48),(803,'errCnt',48),(804,'maxErr',48),(806,'helpCnt',48),(807,'maxHelp',48),(808,'params',48),(809,'request',48),(810,'id',48),(811,'cntrlURL',48),(812,'resource',48),(813,'status',48),(814,'returnVal',48),(815,'returnStr',48),(816,'DNIS',48),(817,'passValues',48),(818,'HTTPcontroller',48),(819,'STATcontroller',48),(820,'source',48),(821,'value',48),(823,'hostName',48),(824,'sipURL',48),(825,'nxtPath',48),(827,'menu',48),(830,'artifactsDIR',38),(831,'FILE',48),(832,'operation',48),(833,'userInput',48),(834,'configurationLocation',48),(835,'appOption',48),(836,'appDirection',48),(838,'artifactsDIR',43),(840,'artifactsDIR',42),(841,'artifactsDIR',39),(842,'appString',48),(843,'sipServer',48),(844,'sipPrefix',48),(845,'application',48);
/*!40000 ALTER TABLE `globals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `page`
--

LOCK TABLES `page` WRITE;
/*!40000 ALTER TABLE `page` DISABLE KEYS */;
/*!40000 ALTER TABLE `page` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
INSERT INTO `project` VALUES (35,'listen_main'),(37,'listen_voicemail'),(38,'listen_mailbox'),(39,'listen_conference'),(41,'listen_findme'),(42,'listen_record'),(43,'listen_autoDial'),(44,'after_hours'),(46,'listen_artifacts'),(47,'listen_confEvents'),(48,'listen_autoAttendant');
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `prompt_group`
--

LOCK TABLES `prompt_group` WRITE;
/*!40000 ALTER TABLE `prompt_group` DISABLE KEYS */;
INSERT INTO `prompt_group` VALUES (1,'00','English',1);
/*!40000 ALTER TABLE `prompt_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `server_info`
--

LOCK TABLES `server_info` WRITE;
/*!40000 ALTER TABLE `server_info` DISABLE KEYS */;
INSERT INTO `server_info` VALUES (1,'listenspot32','root','super');
/*!40000 ALTER TABLE `server_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `status`
--

LOCK TABLES `status` WRITE;
/*!40000 ALTER TABLE `status` DISABLE KEYS */;
/*!40000 ALTER TABLE `status` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-08-27 21:05:31