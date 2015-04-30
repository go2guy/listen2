<html>
<head>
    <title><g:message code="page.administration.phones.toggleMsgLight.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="phones"/>
    <tooltip:resources/>
    <style type="text/css">
    table { margin-bottom: 10px; }

    .add tbody td.button { text-align: center; }

    .col-number input[type=text], .col-number, .col-ip input[type=text] { width: 100px; }
    .col-owner { width: 225px;}
    .col-password input[type=password], .col-username input[type=text] { width: 145px; }

    .col-dateTime { width: 100px; }
    .col-reg { width: 80px;}
    .col-button { width: 50px; }
    .add .col-button {
        text-align: center;
        width: 110px;
    }
    td.col-reg {
        padding-left: 30px;
    }

    tbody .col-light { text-align: center; }
    </style>
</head>
<body>

<table>
    <caption><g:message code="page.administration.phones.toggleMsgLight.caption"/></caption>
    <thead>
    <g:sortableColumn property="number" title="${g.message(code: 'page.administration.phones.column.number')}" class="col-number"/>
    <g:sortableColumn property="owner.realName" title="${g.message(code: 'page.administration.phones.column.owner')}" class="col-owner"/>
    <g:sortableColumn property="sipPhone.ip" title="${g.message(code: 'page.administration.phones.column.ip')}" class="col-ip"/>
    <g:sortableColumn property="sipPhone.dateRegistered" title="${g.message(code: 'page.administration.phones.column.dateRegistered')}" class="col-ip"/>
    <th class="col-button"></th>
    <th class="col-button"></th>
    </thead>
    <tbody>
    <g:if test="${extensionList.size() > 0}">
        <g:each in="${extensionList}" var="extension" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
                <g:form controller="administration" action="updateMsgLight" method="post">
                    <g:if test="${extension.sipPhone}">
                        <td class="col-number"onmouseover="tooltip.show('${extension.sipPhone?.userAgent}');" onmouseout="tooltip.hide();">${fieldValue(bean: extension, field: 'number')}</td>
                        <td class="col-owner" onmouseover="tooltip.show('${extension.sipPhone?.realName}');" onmouseout="tooltip.hide();">${extension.owner.realName}</td>
                        <td class="col-ip">${extension.sipPhone.ip}</td>
                        <g:if test="${extension.sipPhone.dateRegistered}">
                            <td class="col-datereg" onmouseover="tooltip.show('<joda:format value="${extension.sipPhone?.dateRegistered}" pattern="yyyy-MM-dd HH:mm:ss"/>');" onmouseout="tooltip.hide();"><listen:prettytime date="${extension?.sipPhone?.dateRegistered}"/></td>
                        </g:if>
                        <g:else>
                            <td class="col-datereg">${g.message(code: 'page.administration.phones.noSIPActivity.placeholder')}</td>
                        </g:else>
                    </g:if>
                    <g:else>
                        <td class="col-number">${fieldValue(bean: extension, field: 'number')}</td>
                        <td class="col-owner">${extension.owner.realName}</td>
                        <td class="col-ip"></td>
                        <td class="col-datereg">${g.message(code: 'page.administration.phones.noSIPActivity.placeholder')}</td>
                    </g:else>

                    <td class="col-button">
                        <g:hiddenField name="id" value="${extension.id}"/>
                        <g:submitButton name="lightStatus" value="${g.message(code: 'page.administration.phones.toggleMsgLight.button.on')}"/>
                    </td>
                    <td class="col-button">
                        <g:hiddenField name="id" value="${extension.id}"/>
                        <g:submitButton name="lightStatus" value="${g.message(code: 'page.administration.phones.toggleMsgLight.button.off')}"/>
                    </td>
                </g:form>
            </tr>
        </g:each>
    </g:if>
    <g:else>
        <tr class="even"><td colspan="6"><g:message code="page.administration.phones.current.noExtensions"/></td></tr>
    </g:else>
    </tbody>
</table>
<g:if test="${extensionList.size() > 0}">
    <listen:paginateTotal total="${extensionTotal}" messagePrefix="paginate.total.extensions"/>
    <div class="pagination">
        <g:paginate total="${extensionTotal}" maxsteps="5"/>
    </div>
</g:if>
<script type="text/javascript">

    $(document).ready(function() {
        $('#number').focus();
    });
</script>
</body>
</html>