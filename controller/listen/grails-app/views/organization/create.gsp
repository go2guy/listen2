<html>
  <head>
    <title><g:message code="page.organization.create.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="organization"/>
    <meta name="button" content="create"/>
    <meta name="page-header" content="${g.message(code: 'page.organization.create.header')}"/>
  </head>
  <body>
    <g:form controller="organization" action="save" method="post">
      <fieldset class="vertical">
        <h3><g:message code="page.organization.create.details.header"/></h3>

        <label for="name"><g:message code="organization.name.label"/></label>
        <g:textField name="name" value="${fieldValue(bean: organization, field: 'name')}" maxlength="100" class="${listen.validationClass(bean: organization, field: 'name')}"/>

        <h3><g:message code="page.organization.create.features.header"/></h3>

        <g:each in="${enableableFeatures}" var="feature">
          <label for="enabledFeature-${feature}">
            <input type="checkbox" id="enabledFeature-${feature}" name="enabledFeature-${feature}" value="${feature}" ${organization?.enabledFeatures?.contains(feature) ? "checked=checked" : ""}/>
            ${feature.displayName}
          </label>
        </g:each>

        <h3><g:message code="page.organization.create.operator.header"/></h3>

        <label for="username"><g:message code="user.username.label"/></label>
        <g:textField name="username" value="${fieldValue(bean: user, field: 'username')}" maxlength="50" class="${listen.validationClass(bean: user, field: 'username')}"/>

        <label for="pass"><g:message code="user.pass.label"/></label>
        <g:passwordField name="pass" class="${listen.validationClass(bean: user, field: 'pass')}"/>

        <label for="confirm"><g:message code="user.confirm.label" class="${listen.validationClass(bean: user, field: 'confirm')}"/></label>
        <g:passwordField name="confirm"/>

        <label for="realName"><g:message code="user.realName.label"/></label>
        <g:textField name="realName" value="${fieldValue(bean: user, field: 'realName')}" maxlength="50" class="${listen.validationClass(bean: user, field: 'realName')}"/>

        <label for="emailAddress"><g:message code="user.emailAddress.label"/></label>
        <g:textField name="emailAddress" value="${fieldValue(bean: user, field: 'emailAddress')}" class="${listen.validationClass(bean: user, field: 'emailAddress')}"/>

        <ul class="form-buttons">
          <li><g:submitButton name="submit" value="${g.message(code: 'default.button.create.label')}"/></li>
        </ul>
      </fieldset>
    </g:form>
    <script type="text/javascript">
$(document).ready(function() {
    $('#name').focus();
});
    </script>
  </body>
</html>