# stepup-proxy
Authentation flow performing TOTP for the user and a API to manage the TOTP secrets. This module turns a Shibboleth OP (i.e. Shibboleth IdP having shibboleth-idp-oidc-extension) to a stepup service. Service requests OTP code from users initially authenticated by the client.

The flow of events is that the client creates a oidc authentication request to authenticate the user. The request contains in claims parameter the information necessary for the operaton. In standard use case this would be claims containing values for subject (always needed), key to users TOTP secret and phone number for bootstrapping the TOTP secret if such does not exist. The properties file can then be used to alter the behaviour. Once the user has been successfully authenticated the response is sent back.

## Prerequisite for installation
- Shibboleth IdP 3.4+ 
- [shibboleth-idp-oidc-extension](https://github.com/CSCfi/shibboleth-idp-oidc-extension) v1.0.0

## Installation
First you need extract the archive and rebuild the package. Please not that you most likely *need* to change the owner and group information of the extracted files to suite your installation.

    cd /opt/shibboleth-idp
    tar -xf path/to/idp-stepup-distribution-0.10.0-bin.tar.gz  --strip-components=1
    bin/build.sh

Restart the Shibboleth IdP.

## Configuring the OP
Minimal instructions for the OP. See Wiki for detailed instructions.    
### Authentication flow
You will need to create entry for the flow to general-authn.xml

    edit /opt/shibboleth-idp/conf/authn/general-authn.xml
    
    <bean id="authn/Stepup" parent="shibboleth.AuthenticationFlow"
            p:passiveAuthenticationSupported="false"
            p:forcedAuthenticationSupported="true" >
      <property name="supportedPrincipals">
        <list>
          <bean parent="shibboleth.OIDCAuthnContextClassReference"
              c:classRef="https://refeds.org/profile/mfa" />
        </list>
      </property>
    </bean>
    
and activate it.    

    edit /opt/shibboleth-idp/conf/idp.properties
    
    # Regular expression matching login flows to enable, e.g. IPAddress|Password
    idp.authn.flows=Stepup
  
### Attributes
We will resolve the subject from the request. For that we need to replace the default subject resolver with following one:

    <AttributeDefinition id="subject" xsi:type="ScriptedAttribute">
    <Script><![CDATA[
    logger = Java.type("org.slf4j.LoggerFactory").getLogger("fi.csc.idp.attribute.resolver.subjectbuilder");
    outboundMessageCtx = profileContext.getOutboundMessageContext();
    if (outboundMessageCtx != null) {
        oidcResponseContext = outboundMessageCtx.getSubcontext("org.geant.idpextension.oidc.messaging.context.OIDCAuthenticationResponseContext");
        if (oidcResponseContext != null) {
            subject.addValue(oidcResponseContext.getRequestedSubject());
            logger.debug("subject value: " + subject.getValues().get(0));
        } else {
            logger.warn("RP has not requested subject, unable to produce subject");
        }
    } else {
	logger.warn("No oidc response context, unable to produce subject");
    }
    ]]></Script>
    <AttributeEncoder xsi:type="oidcext:OIDCString" name="sub" />
    </AttributeDefinition>
    
## Trust and configuring the client
See Wiki.

