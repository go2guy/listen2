<html>
  <head>
    <title><g:message code="page.administration.configuration.title"/></title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <meta name="button" content="configuration"/>
    <style type="text/css">
.transcriptionDetails {
    margin-left: 20px;
}
    </style>
  </head>
  <body>
    <g:form controller="administration" action="saveConfiguration">
    <listen:canAccess feature="VOICEMAIL">
      <fieldset class="vertical">
        <h3>Audio Transcription</h3>

        <g:checkBox name="transcription.isEnabled" value="${transcription?.isEnabled}" class="transcriptionIsEnabled"/>
        <label for="transcription.isEnabled" class="inline-label"><g:message code="transcriptionConfiguration.isEnabled.label"/></label>

        <div class="transcriptionDetails">
          <label for="transcription.phoneNumber"><g:message code="transcriptionConfiguration.phoneNumber.label"/></label>
          <g:textField name="transcription.phoneNumber" value="${fieldValue(bean: transcription, field: 'phoneNumber')}"/>
        </div>
      </fieldset>
     </listen:canAccess>

      <fieldset class="vertical">
        <h3>After Hours Support</h3>

        <label for="afterHours.phoneNumber.id"><g:message code="afterHoursConfiguration.phoneNumber.label"/></label>
        <listen:phoneNumberSelect name="afterHours.phoneNumber.id" type="MOBILE" optionKey="id" optionValue="number" noSelection="['': 'None']" value="${afterHours?.phoneNumber?.id}"/>

        <label for="afterHours.alternateNumber"><g:message code="afterHoursConfiguration.alternateNumber.label"/></label>
        <g:textField name="afterHours.alternateNumber" value="${afterHours?.alternateNumberComponents()?.number?.encodeAsHTML()}"/>

        <label for="afterHours.provider"><g:message code="page.administration.configuration.afterHours.alternateNumberProvider.label"/></label>
        <listen:mobileProviderSelect name="afterHours.provider" value="${afterHours?.alternateNumberComponents()?.provider}"/>

        <label for="afterHours.realizeUrl"><g:message code="afterHoursConfiguration.realizeUrl.label"/></label>
        <g:textField name="afterHours.realizeUrl" value="${fieldValue(bean: afterHours, field: 'realizeUrl')}"/>
        
        <label for="afterHours.realizeAlertName"><g:message code="afterHoursConfiguration.realizeAlertName.label"/></label>
        <g:textField name="afterHours.realizeAlertName" value="${fieldValue(bean: afterHours, field: 'realizeAlertName')}"/>

      </fieldset>

      <ul class="form-buttons">
        <li><g:submitButton name="save" value="${g.message(code: 'default.button.save.label')}"/></li>
      </ul>
    </g:form>
    <script type="text/javascript">
var configuration = {
    toggleTranscriptionDetails: function() {
        $('.transcriptionDetails').toggle($('.transcriptionIsEnabled').is(':checked'));
    }
};
$(document).ready(function() {
    configuration.toggleTranscriptionDetails(); // toggle on page load
    $('.transcriptionIsEnabled').click(function() {
        configuration.toggleTranscriptionDetails();
    });
});
    </script>
  </body>
</html>