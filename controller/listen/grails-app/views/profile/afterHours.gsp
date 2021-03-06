<html>
  <head>
    <title><g:message code="page.profile.afterHours.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="profile"/>
    <meta name="button" content="afterHours"/>
    <meta name="page-header" content="${g.message(code: 'page.profile.afterHours.header')}"/>
    <style type="text/css">
fieldset.vertical label {
    display: inline-block;
}

fieldset.vertical input[type="text"],
fieldset.vertical select {
    display: block;
}
    </style>
  </head>
  <body>
      <g:if test="${afterHoursConfiguration?.mobilePhone?.number}">
          <span>The after hours mobile phone number is <b>${afterHoursConfiguration?.mobilePhone?.number?.encodeAsHTML()}</b></span>
      </g:if>
      <g:else>
          <span>The after hours mobile phone number has not been configured!</b></span>
      </g:else>
      
      <g:form controller="profile" action="saveAfterHours">
        <fieldset class="vertical">
          <label for="alternateNumber">
            <g:message code="afterHoursConfiguration.alternateNumber.label"/>
            <g:textField name="alternateNumber" value="${afterHoursConfiguration?.alternateNumberComponents()?.number?.encodeAsHTML()}" class="${listen.validationClass(bean: afterHoursConfiguration, field: 'alternateNumber')}"/>
          </label>

          <label for="provider">
            Mobile Provider
            <listen:mobileProviderSelect name="provider" value="${afterHoursConfiguration?.alternateNumberComponents()?.provider}"/>
          </label>

          <listen:autocomplete selector="#alternateNumber" data="my.mobiles" providerSelector="#provider"/>

          <ul class="form-buttons">
            <li><g:submitButton name="save" value="${g.message(code: 'default.button.save.label')}"/></li>
          </ul>
        </fieldset>
      </g:form>

  </body>
</html>