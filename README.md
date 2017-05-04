# Shibboleth IdP Stepup Proxy

[![Build Status](https://travis-ci.org/CSCfi/stepup-proxy.svg?branch=master)](https://travis-ci.org/CSCfi/stepup-proxy)
[![Coverage Status](https://coveralls.io/repos/github/CSC-IT-Center-for-Science/stepup-proxy/badge.svg?branch=master)](https://coveralls.io/github/CSC-IT-Center-for-Science/stepup-proxy?branch=master)

## Please Note

Project can be deployed for testing purposes but is not fully production quality yet. Many of the features are still under development and deployment may require help from the development team.  

## Overview
These modules contain extensions to achieve MFA service for both SPs and IdPs. The modules may act both as SAML2 proxy or as OIDC provider to offer the MFA service. SAML2 proxy is based on Shibboleth IdP 3 and Shibboleth SP. Proxying use case relies on [MPASS Shibboleth SP Authentication](https://github.com/Digipalvelutehdas/MPASS-proxy/tree/master/idp-authn-impl-shibsp) project for a authentication flow that actually enables to deploy a proxy based on Shibboleth IdP 3 and Shibboleth SP. If proxying is left out and only OIDC part is used Shibboleth SP is not needed nor is there any dependency to [MPASS Shibboleth SP Authentication](https://github.com/Digipalvelutehdas/MPASS-proxy/tree/master/idp-authn-impl-shibsp). 

### Short Summary of flows

#### StepUp
Stepup flow is applied if the intention is to  build a SAML2 proxy providing MFA.

Stepup flow is a post authentication interceptor flow. In practise this means that stepup flow receives control at a point where user has been  authenticated, the attributes have been resolved and filtered but the actual assertion has not been formed yet. At this point flow makes decisions whether a stepup is needed, what kind of stepup that would be and how that is communicated to client SP in the assertion. Stepup flow uses Mfa flow to perform the user reauthentication.

The flow provided with the project is an example flow that may have to be heavily modified to suite the target environment. The basic building blocks consist of following functionality:

- Possibility to translate requested and provided authentication methods between SP and IdP.
- Possibility to trust the home organization IdP to provide already the authentication level requiring stepup. 

By modifying the flow numerous other use cases may also be achieved. See Wiki for more details. 

#### MfaRequest
MfaRquest flow is a flow implementing oidc provider. Mfarequest flow expects a oidc authentication request of type id token (implicit). The user parameters of initial authentication are expected to be found in a signed request object containing id token. Flow uses Mfa flow to perform the user reauthentication. See Wiki for more details.   


#### Mfa
Mfa flow is a authentication flow. It is the workhorse of the other flows described here but may be used also independently. 

The flow provided with the is an example flow that may have to be heavily modified to suite the target environment. The basic building blocks consist of following functionality:

- Different user authentication mechanisms: email, sms, totp (google authenticator).
- Possibility to choose authentication mechanisms based on requested authentication method.
- Registration of authentication mechanism and maintaining them.

How these building blocks are applied depends on implemented flow and may result in very different use cases. The example flow here implements two separate cases:

- User is requested to reply to received sms by his/her mobile to continue. If user has no mobile number email verification is used instead.
- User is requested to enter code TOTP code. Maintaining accounts requires successful sms authentication.

See Wiki for more details.

## Prerequisities and compilation

- Java 7+
- [Apache Maven 3](https://maven.apache.org/)
- Products of [MPASS project](https://github.com/Digipalvelutehdas/MPASS-proxy/tree/master/idp-mpass-parent) installed to  your local repository

```
cd idp-stepup-parent
mvn package
```


## Deployment Prerequisities
[MPASS Shibboleth SP Authentication](https://github.com/Digipalvelutehdas/MPASS-proxy/tree/master/idp-authn-impl-shibsp) is required to be deployed for the proxy use case.

## Deployment
After compilation, the _../idp-ssointerceptor-impl-stepup/target/idp-ssointerceptor-impl-stepup-\<version\>.jar_  and it's dependencies must be deployed to the IdP Web application and it must be configured. Depending on the IdP installation, the module deployment may be achieved for instance with the following sequence:

```
cp ../idp-ssointerceptor-impl-stepup/target/idp-ssointerceptor-impl-stepup-\<version\>.jar /opt/shibboleth-idp/edit-webapp/WEB-INF/lib/.
cp ../idp-ssointerceptor-impl-stepup/target/dependency/idp-ssointerceptor-api-stepup-\<version\>.jar /opt/shibboleth-idp/edit-webapp/WEB-INF/lib/.
```
We have also some partially optional dependencies (depends on your configuration) you need to copy. Most of them can be found from _../idp-ssointerceptor-impl-stepup/target/dependency_
```
cp ../idp-ssointerceptor-impl-stepup/target/dependency/googleauth-\<version\>.jar /opt/shibboleth-idp/edit-webapp/WEB-INF/lib/.
cp ../idp-ssointerceptor-impl-stepup/target/dependency/HikariCP-\<version\>.jar /opt/shibboleth-idp/edit-webapp/WEB-INF/lib/.

cp ../idp-ssointerceptor-impl-stepup/target/dependency/jackson-annotations-\<version\>.jar /opt/shibboleth-idp/edit-webapp/WEB-INF/lib/.
cp ../idp-ssointerceptor-impl-stepup/target/dependency/jackson-core-\<version\>.jar /opt/shibboleth-idp/edit-webapp/WEB-INF/lib/.
cp ../idp-ssointerceptor-impl-stepup/target/dependency/jackson-databind-\<version\>.jar /opt/shibboleth-idp/edit-webapp/WEB-INF/lib/.

cp ../idp-ssointerceptor-impl-stepup/target/dependency/spring-security-crypto-\<version\>.jar /opt/shibboleth-idp/edit-webapp/WEB-INF/lib/.
cp ../idp-ssointerceptor-impl-stepup/target/dependency/twilio-\<version\>.jar /opt/shibboleth-idp/edit-webapp/WEB-INF/lib/.
```
If you are using a database as in example to store TOTP keys add also the relevant jdbc client library. Now finally you can build the project.
```
cd /opt/shibboleth-idp
sh bin/build.sh
```

The final command will rebuild the _war_-package for the IdP application. Please note that products of [MPASS Shibboleth SP Authentication](https://github.com/Digipalvelutehdas/MPASS-proxy/tree/master/idp-authn-impl-shibsp) are expected to be in the directory _/opt/shibboleth-idp/edit-webapp/WEB-INF/lib/_ (Deployment prerequisite).

At the time of writing the instructions the http libraries provided by IdP were too old for twilio. If you have problems replace the http libraries with newer versions, for instance:
```
httpclient-4.5.2.jar
httpclient-cache-4.5.2.jar
httpcore-4.4.5.jar
```

### Views
Copy the necessary views to place. Create the directories if needed. 

```
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/views/authn/* /opt/shibboleth-idp/views/authn/.
```
### Flows
Copy the flow to it's correct place. Create the directories if needed.
```
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/flows/intercept/stepup/stepup-flow.xml /opt/shibboleth-idp/flows/intercept/stepup/.
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/flows/authn/mfa/mfa-flow.xml /opt/shibboleth-idp/flows/authn/mfa/.
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/flows/oidc/mfarequest/mfarequest-flow.xml /opt/shibboleth-idp/flows/oidc/mfarequest/.
```

### Beans
Copy the bean definition to it's correct place. Create the directories if needed.
```
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/flows/intercept/stepup/stepup-beans.xml /opt/shibboleth-idp/flows/intercept/stepup/.
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/flows/authn/mfa/mfa-beans.xml /opt/shibboleth-idp/flows/authn/mfa/.
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/flows/oidc/mfarequest/mfarequest-beans.xml /opt/shibboleth-idp/flows/oidc/mfarequest/.
```

### Enable interceptor (SAML2 proxy deployment only)
You need to add the new flow to list of avalable intercept flows in file _/opt/shibboleth-idp/conf/intercept/profile-intercept.xml_

```
<bean id="shibboleth.AvailableInterceptFlows" parent="shibboleth.DefaultInterceptFlows" lazy-init="true">
        <property name="sourceList">
            <list merge="true">
            
            ...
            
            <bean id="intercept/stepup" parent="shibboleth.InterceptFlow" /> 
            
            ...
```
Then you need to also define the intercept point. Open file _/opt/shibboleth-idp/conf/relying-party.xml_ and add _stepup_ as post authentication intercept flow.
```
 <bean id="shibboleth.DefaultRelyingParty" parent="RelyingParty">
        <property name="profileConfigurations"> status
            <list>
            ...
            <bean parent="SAML2.SSO" p:postAuthenticationFlows="stepup" />
            ...
```
Please consult [Shibboleth documentation](https://wiki.shibboleth.net/confluence/display/IDP30/ProfileInterceptConfiguration#ProfileInterceptConfiguration-EnablingIntercepts) on details.

## Configuration 

Note! You will need to modify both beans and flow to meet your use case. Please note that example bean configurations are missing vital deployment specific parameters like key values and account identifiers.  

See Wiki for more details.


