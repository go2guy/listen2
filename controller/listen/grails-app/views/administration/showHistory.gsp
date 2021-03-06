<%--
  Created by IntelliJ IDEA.
  User: bjohnston
  Date: 12/15/2016
  Time: 1:42 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title><g:message code="callHistory.detail.label"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="callHistory"/>
    <tooltip:resources/>

    <style type="text/css">
        table { margin-bottom: 10px; }


    </style>
</head>

<body>

<h3><g:message code="callHistory.detail.label"/></h3>


    <table id="twoColumns">
    <tbody>
        <tbody>
            <tr>
                <th><g:message code="callHistory.timeStamp.label"/></th>
                <td>${callHistoryInstance.dateTime.getMillis()}</td>
            </tr>
            <tr>
                <th><g:message code="callHistory.startDate.label"/></th>
                <td><joda:format value="${callHistoryInstance.dateTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            </tr>
            <tr>
                <th><g:message code="callHistory.ani.label"/></th>
                <td><listen:numberWithRealName number="${fieldValue(bean: callHistoryInstance, field: 'ani')}"
                                               user="${callHistoryInstance.fromUser}" personalize="false"/></td>
            </tr>
            <tr>
                <th><g:message code="callHistory.outboundAni.label"/></th>
                <td>${fieldValue(bean: callHistoryInstance, field: 'outboundAni')}</td>
            </tr>
            <tr>
                <th><g:message code="callHistory.dnis.label"/></th>
                <td><listen:numberWithRealName number="${fieldValue(bean: callHistoryInstance, field: 'dnis')}"
                                               user="${callHistoryInstance.toUser}" personalize="false"/></td>
            </tr>
            <tr>
                <th><g:message code="callHistory.inboundDnis.label"/></th>
                <td><listen:numberWithRealName number="${fieldValue(bean: callHistoryInstance, field: 'inboundDnis')}"
                                               user="${callHistoryInstance.toUser}" personalize="false"/></td>
            </tr>
            <tr>
                <th><g:message code="callHistory.duration.label"/></th>
                <td><listen:formatduration duration="${callHistoryInstance.duration}" millis="false"/></td>
            </tr>
            <tr>
                <th><g:message code="callHistory.organization.label"/></th>
                <td>${callHistoryInstance.organization.name}</td>
            </tr>
            <tr>
                <th><g:message code="callHistory.callResult.label"/></th>
                <td>${callHistoryInstance.result}</td>
            </tr>
            <tr>
                <th><g:message code="callHistory.sessionId.label"/></th>
                <td>${callHistoryInstance.sessionId}</td>
            </tr>
            <tr>
                <th><g:message code="callHistory.commonCallId.label"/></th>
                <td>${callHistoryInstance.commonCallId}</td>
            </tr>
        </tbody>
    </table>

    <g:if test="${acdCallHistoryInstance}">

                <g:each in="${acdCallHistoryInstance}" status="i" var="acdCallHistory">
                    <table id="twoColumns">
                    <tbody>
                        <h3><g:message code="acdCallHistory.detail.label" args="[i+1]"/></h3>
                        <tr>
                            <th><g:message code="acdCallHistory.skill.label"/></th>
                            <td>${acdCallHistory.skill.skillname}</td>
                        </tr>
                        <tr>
                            <th><g:message code="acdCallHistory.enqueueTime.label"/></th>
                            <g:if test="${acdCallHistory.enqueueTime}">
                                <td><joda:format value="${acdCallHistory.enqueueTime}"/></td>
                            </g:if>
                            <g:else>
                                <td>N/A</td>
                            </g:else>
                        </tr>
                        <tr>
                            <th><g:message code="acdCallHistory.dequeueTime.label"/></th>
                            <g:if test="${acdCallHistory.dequeueTime}">
                                <td><joda:format value="${acdCallHistory.dequeueTime}"/></td>
                            </g:if>
                            <g:else>
                                <td>N/A</td>
                            </g:else>
                        </tr>
                        <tr>
                            <th><g:message code="acdCallHistory.totalQueueTime.label"/></th>
                            <td><listen:computeDuration start="${acdCallHistory.enqueueTime}" end="${acdCallHistory.dequeueTime}"/></td>
                        </tr>
                        <tr>
                            <th><g:message code="acdCallHistory.callStatus.label"/></th>
                            <td>${acdCallHistory.callStatus}</td>
                        </tr>
                        <tr>
                            <th><g:message code="acdCallHistory.user.label"/></th>
                            <td><listen:numberWithRealName number="${fieldValue(bean: acdCallHistory, field: 'agentNumber')}"
                                                           user="${acdCallHistory.user}" personalize="false"/></td>
                        </tr>
                        <tr>
                            <th><g:message code="acdCallHistory.agentCallStart.label"/></th>
                            <td><joda:format value="${acdCallHistory.agentCallStart}"/></td>
                        </tr>
                        <tr>
                            <th><g:message code="acdCallHistory.agentCallEnd.label"/></th>
                            <td><joda:format value="${acdCallHistory.agentCallEnd}"/></td>
                        </tr>
                        <tr>
                            <th><g:message code="acdCallHistory.totalAgentTime.label"/></th>
                            <td><listen:computeDuration start="${acdCallHistory.agentCallStart ? acdCallHistory.agentCallStart : 0}" end="${acdCallHistory.agentCallEnd ? acdCallHistory.agentCallEnd : 0}"/></td>
                        </tr>
                    </tbody>
                    </table>
                </g:each>

    </g:if>


</body>
</html>
