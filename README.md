# shibboleth-idp-authn-stepup
Authentication flow performing TOTP (and other methods) for the user and a API to manage the TOTP secrets. This module turns a Shibboleth OP (i.e. Shibboleth IdP having shibboleth-idp-oidc-extension) to a step up service requesting a second factor from the user. User must have been initially authenticated by the client.

The flow is not a Shibboleth compliant authentication flow. The flow may be perfomed successfully to only oidc clients. 

The overall idea is for the client to initially authenticate the user with first factor. Then the client may create oidc authentication request to the step up service. This authentication request must contain as requested claim information necessary for the service to perform second factor. The second factor may be OTP code, SMS or email depending on the configuration. 

## Prerequisite for installation
- Shibboleth IdP 4.0+ 
- [shibboleth-idp-oidc-extension](https://github.com/CSCfi/shibboleth-idp-oidc-extension/wiki) v2.0.0+

## Installation
First you need extract the archive and rebuild the package. Please not that you most likely *need* to change the owner and group information of the extracted files to suite your installation.

    cd /opt/shibboleth-idp
    tar -xf path/to/idp-stepup-distribution-0.10.0-bin.tar.gz  --strip-components=1
    bin/build.sh

Restart the Shibboleth IdP.

Include stepup.properties in idp.properties

    edit /opt/shibboleth-idp/conf/idp.properties
    
    # Load any additional property resources from a comma-delimited list
    idp.additionalProperties=/conf/ldap.properties, /conf/saml-nameid.properties, /conf/services.properties, /conf/authn/duo.properties, /conf/idp-oidc.properties, /conf/authn/stepup.properties
    


## Basic Configuration
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

### Second Factor properties
The second factor, the actaul method and configuration is configured in /opt/shibboleth-idp/conf/authn/stepup.properties
  
### Attribute definition
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

