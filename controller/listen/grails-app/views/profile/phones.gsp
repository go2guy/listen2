<html>
  <head>
    <title><g:message code="page.profile.phones.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="profile"/>
    <meta name="button" content="phones"/>
    <style type="text/css">
table { margin-bottom: 10px; }

#extensions .col-number { width: 30%; }
#extensions .col-public { width: 10%; }
#extensions .col-forward { width: 55%; }
#extensions .col-button { width: 5%; }

#directInwardDial .col-number { width: 30%; }
#directInwardDial .col-public { width: 10%; }
#directInwardDial .col-forward { width: 55%; }
#directInwardDial .col-button { width: 5%; }

#directMessageNumber .col-number { width: 30%; }
#directMessageNumber .col-public { width: 10%; }
#directMessageNumber .col-forward { width: 55%; }
#directMessageNumber .col-button { width: 5%; }

.mobile-phones .col-number { width: 30%; }
.mobile-phones .col-provider { width: 30%; }
.mobile-phones .col-public { width: 26%; }
.mobile-phones .col-button { width: 7%; }

.other-phones .col-number { width: 60%; }
.other-phones .col-public { width: 26%; }
.other-phones .col-button { width: 7%; }

.add .col-button { width: 14%; }

.col-forward input[type=text] { width: 150px; }
.col-number input[type=text] { width: 150px; }

td.col-public {
    padding-left: 20px;
}

.col-button { text-align: center; }
    </style>
  </head>
  <body>
    <g:if test="${extensionList.size() > 0}">
      <table id="extensions">
        <caption><g:message code="page.profile.phones.caption.extensions"/></caption>
        <thead>
          <th class="col-number"><g:message code="extension.number.label"/></th>
          <th class="col-public"><g:message code="mobilePhone.isPublic.label"/></th>
          <th class="col-forward"><g:message code="extension.forwardedTo.label"/></th>
          <th class="col-button"></th>
        </thead>
        <tbody>
          <g:each in="${extensionList}" var="extension" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
              <g:form controller="profile" action="updateExtension" method="post">
                <td class="col-number">${fieldValue(bean: extension, field: 'number')}</td>
                <td class="col-public"><listen:checkMark value="true"/></td>
                <td class="col-forward">
                  <g:textField name="forwardedTo" value="${fieldValue(bean: extension, field: 'forwardedTo')}" class="check-for-blocked forwarded-to ${listen.validationClass(bean: extension, field: 'forwardedTo')}" autocomplete="off"/>
                  <listen:ifCannotDial number="${fieldValue(bean: extension, field: 'forwardedTo')}">
                    <span class="blocked-number error" title="You are not allowed to dial ${fieldValue(bean: extension, field: 'forwardedTo')}">Blocked</span>
                  </listen:ifCannotDial>
                </td>
                <td class="col-button">
                  <g:hiddenField name="id" value="${extension.id}"/>
                  <g:submitButton name="save" value="Save"/>
                </td>
              </g:form>
            </tr>
          </g:each>
        </tbody>
      </table>
    </g:if>

    <g:if test="${externalDIDList.size() > 0}">
      <table id="directInwardDial">
            <caption><g:message code="page.profile.phones.caption.dids"/></caption>
            <thead>
              <th class="col-number"><g:message code="did.number.label"/></th>
              <th class="col-public"><g:message code="mobilePhone.isPublic.label"/></th>
              <th class="col-forward"><g:message code="did.forwardedTo.label"/></th>
              <th class="col-button"></th>
            </thead>
            <tbody>
            <g:each in="${externalDIDList}" var="did" status="i">
              <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
                <g:form controller="profile" action="updateDid" method="post">
                  <td class="col-number">${fieldValue(bean: did, field: 'number')}</td>
                  <td class="col-public"><listen:checkMark value="true"/></td>
                  <td class="col-forward">
                    <g:textField name="forwardedTo" value="${fieldValue(bean: did, field: 'forwardedTo')}" class="check-for-blocked forwarded-to ${listen.validationClass(bean: did, field: 'forwardedTo')}" autocomplete="off"/>
                    <listen:ifCannotDial number="${fieldValue(bean: did, field: 'forwardedTo')}">
                      <span class="blocked-number error" title="You are not allowed to dial ${fieldValue(bean: did, field: 'forwardedTo')}">Blocked</span>
                    </listen:ifCannotDial>
                  </td>
                  <td class="col-button">
                    <g:hiddenField name="id" value="${did.id}"/>
                    <g:submitButton name="save" value="Save"/>
                  </td>
                </g:form>
              </tr>
            </g:each>
          </tbody>
      </table>
    </g:if>

    <g:if test="${externalDMNList.size() > 0}">
      <table id="directMessageNumber">
          <caption><g:message code="page.profile.phones.caption.dmns"/></caption>
          <thead>
          <th class="col-number"><g:message code="dmn.number.label"/></th>
          <th class="col-public"><g:message code="mobilePhone.isPublic.label"/></th>
          <th class="col-forward"></th>
          <th class="col-button"></th>
          </thead>
          <tbody>
          <g:each in="${externalDMNList}" var="dmn" status="i">
              <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
                  <g:form controller="profile" action="updateDid" method="post">
                      <td class="col-number">${fieldValue(bean: dmn, field: 'number')}</td>
                      <td class="col-public"><listen:checkMark value="true"/></td>
                      <td class="col-forward">
                      </td>
                      <td class="col-button">

                      </td>
                  </g:form>
              </tr>
          </g:each>
          </tbody>
      </table>
    </g:if>

    <table class="mobile-phones">
      <caption><g:message code="page.profile.phones.caption.mobilePhones"/></caption>
      <thead>
        <th class="col-number"><g:message code="phoneNumber.number.label"/></th>
        <th class="col-provider"><g:message code="mobilePhone.smsDomain.label"/></th>
        <th class="col-public"><g:message code="mobilePhone.isPublic.label"/></th>
        <th class="col-button"></th>
        <th class="col-button"></th>
      </thead>
      <tbody>
        <tr class="add highlighted">
          <g:form controller="profile" action="addMobilePhone" method="post">
            <td class="col-number"><g:textField name="number" value="${fieldValue(bean: newMobilePhone, field: 'number')}" class="${listen.validationClass(bean: newMobilePhone, field: 'number')}"/></td>
            <td class="col-provider"><listen:mobileProviderSelect name="smsDomain" value="${newMobilePhone?.smsDomain}" noSelection="${['':'-- Choose Provider --']}"/></td>
            <td class="col-public"><g:checkBox name="isPublic" value="${newMobilePhone?.isPublic}"/></td>
            <td class="col-button">
              <g:submitButton name="add" value="${g.message(code: 'page.profile.phones.mobilePhones.addButton')}"/>
            </td>
          </g:form>
        </tr>
      </tbody>
    </table>

    <table class="mobile-phones">
      <tbody>
        <g:if test="${mobilePhoneList.size() > 0}">
          <g:each in="${mobilePhoneList}" var="mobilePhone" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
              <g:form controller="profile" action="updateMobilePhone" method="post">
                <td class="col-number"><g:textField name="number" value="${fieldValue(bean: mobilePhone, field: 'number')}" class="${listen.validationClass(bean: mobilePhone, field: 'number')}"/></td>
                <td class="col-provider"><listen:mobileProviderSelect name="smsDomain" value="${mobilePhone.smsDomain}"/></td>
                <td class="col-public"><g:checkBox name="isPublic" value="${mobilePhone.isPublic}"/></td>
                <td class="col-button">
                  <g:hiddenField name="id" value="${mobilePhone.id}"/>
                  <g:submitButton name="save" value="${g.message(code: 'default.button.save.label')}"/>
                </td>
              </g:form>
              <g:form controller="profile" action="deleteMobilePhone" method="post">
                <td class="col-button">
                  <g:hiddenField name="id" value="${mobilePhone.id}"/>
                  <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                </td>
              </g:form>
            </tr>
          </g:each>
        </g:if>
        <g:else>
          <tr class="even"><td colspan="5"><g:message code="page.profile.phones.noMobilePhones"/></td></tr>
        </g:else>
      </tbody>
    </table>

    <table class="other-phones">
      <caption><g:message code="page.profile.phones.caption.otherPhones"/></caption>
      <thead>
        <th class="col-number"><g:message code="phoneNumber.number.label"/></th>
        <th class="col-public"><g:message code="otherPhone.isPublic.label"/></th>
        <th class="col-button"></th>
        <th class="col-button"></th>
      </thead>
      <tbody>
        <tr class="add highlighted">
          <g:form controller="profile" action="addOtherPhone" method="post">
            <td class="col-number"><g:textField name="number" value="${fieldValue(bean: newOtherPhone, field: 'number')}" class="${listen.validationClass(bean: newOtherPhone, field: 'number')}"/></td>
            <td class="col-public"><g:checkBox name="isPublic" value="${newOtherPhone?.isPublic}"/></td>
            <td class="col-button">
              <g:submitButton name="add" value="${g.message(code: 'page.profile.phones.otherPhones.addButton')}"/>
            </td>
          </g:form>
        </tr>
      </tbody>
    </table>

    <table class="other-phones">
      <tbody>
        <g:if test="${otherPhoneList.size() > 0}">
          <g:each in="${otherPhoneList}" var="otherPhone" status="i">
            <tr class="${i % 2 == 0 ? 'even' : 'odd'}">
              <g:form controller="profile" action="updateOtherPhone" method="post">
                <td class="col-number"><g:textField name="number" value="${fieldValue(bean: otherPhone, field: 'number')}" class="${listen.validationClass(bean: newOtherPhone, field: 'number')}"/></td>
                <td class="col-public"><g:checkBox name="isPublic" value="${otherPhone.isPublic}"/></td>
                <td class="col-button">
                  <g:hiddenField name="id" value="${otherPhone.id}"/>
                  <g:submitButton name="save" value="${g.message(code: 'default.button.save.label')}"/>
                </td>
              </g:form>
              <g:form controller="profile" action="deleteOtherPhone" method="post">
                <td class="col-button">
                  <g:hiddenField name="id" value="${otherPhone.id}"/>
                  <g:submitButton name="delete" value="${g.message(code: 'default.button.delete.label')}"/>
                </td>
              </g:form>
            </tr>
          </g:each>
        </g:if>
        <g:else>
          <tr class="even"><td colspan="4"><g:message code="page.profile.phones.noOtherPhones"/></td></tr>
        </g:else>
      </tbody>
    </table>

    <script type="text/javascript">
        checkAndIndicateBlocked: function(el) {
            var field = $(el);
            var number = field.val();
            $.ajax({
                type: 'GET',
                url: '${createLink(controller: 'profile', action: 'canDial', mapping: 'internalApi')}',
                data: {
                    number: number
                },
                success: function(data) {
                    var hasIndicator = field.next('.blocked-number').length > 0;
                    if(data.canDial && hasIndicator) {
                        field.next('.blocked-number').remove()
                    } else if(!data.canDial && !hasIndicator) {
                        field.after('<span class="blocked-number error" title="You are not allowed to dial ' + number + '">Blocked</span>');
                    }
                },
                dataType: 'json'
            });
         }
                
        $(document).ready(function() {
            $('.check-for-blocked').keyup(function(e) {
                util.typewatch(function() {
                    checkAndIndicateBlocked(e.target);
                }, 500);
            }).bind('paste', function(e) {
                // delay since it takes time for the text to actually paste
                setTimeout(checkAndIndicateBlocked(e.target), 100);
            }).change(function(e) {
                checkAndIndicateBlocked(e.target);
            });
        });
    </script>
    <listen:autocomplete selector=".forwarded-to" data="my.phones"/>
  </body>
</html>