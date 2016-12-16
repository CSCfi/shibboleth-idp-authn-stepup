# Shibboleth IdP Stepup Proxy

[![Build Status](https://travis-ci.org/CSC-IT-Center-for-Science/stepup-proxy.svg?branch=master)](https://travis-ci.org/CSC-IT-Center-for-Science/stepup-proxy)
[![Coverage Status](https://coveralls.io/repos/github/CSC-IT-Center-for-Science/stepup-proxy/badge.svg?branch=master)](https://coveralls.io/github/CSC-IT-Center-for-Science/stepup-proxy?branch=master)

##Please Note

Project can be deployed for testing purposes but is not production quality yet. Many of the features are still under development and deployment may require help from the development team.  

## Overview
These modules contain extensions to achieve stepup authentication in a proxy based on Shibboleth IdP 3 and Shibboleth SP. You may want to read overall [description](https://confluence.csc.fi/display/HAKA/Description+of+SAML2+Proxy+capable+of+elevating+authentication) of the whole proxy before going forward.

This project relies on [MPASS Shibboleth SP Authentication](https://github.com/Digipalvelutehdas/MPASS-proxy/tree/master/idp-authn-impl-shibsp) project for a authentication flow that actually enables to deploy a proxy based on Shibboleth IdP 3 and Shibboleth SP. The aim of this stepup project is to enhance that by providing a mechanisms for performing stepup authentication. 

The components can be deployed also to the home organization IdP. This has impact on how the flow should be configured and there is no such example yet. 

### Short Summary of flows

#### StepUp
Stepup flow is applied if the intention is to provide Mfa by a proxy.

Stepup flow is a post authentication interceptor flow. In practise this means that stepup flow receives control at a point where user has been  authenticated, the attributes have been resolved and filtered but the actual assertion has not been formed yet. At this point flow makes decisions whether a stepup is needed, what kind of stepup that would be and how that is communicated to client SP in the assertion. Stepup flow uses Mfa flow to perform the user reauthentication.

The flow provided with the is an example flow that may have to be heavily modified to suite the target environment. The basic building blocks consist of following functionality:

- Possibility to translate requested and provided authentication methods between SP and IdP.
- Possibility to trust the home organization IdP to provide already the authentication level requiring stepup. 

By modifying the flow numerous other use cases may also be achieved. 

#### MfaRequest
MfaRquest flow is a flow implementing oidc provider. Mfarequest flow expects a oidc authentication request of type id token (implicit). Login hint parameters are parsed to perform mfa for the user. Flow uses Mfa flow to perform the user reauthentication. The flow implementation is still in very early stages and should not be used.  


#### Mfa
Mfa flow is a authentication flow. It is the workhorse of the other flows described here but may be used also independently. 

The flow provided with the is an example flow that may have to be heavily modified to suite the target environment. The basic building blocks consist of following functionality:

- Different user authentication mechanisms: email, sms, totp (google authenticator).
- Possibility to choose authentication mechanisms based on requested authentication method.
- Registration of authentication mechanism and maintaining them.

How these building blocks are applied depends on implemented flow and may result in very different use cases. The example flow here implements two separate cases:

- User is requested to reply to received sms by his/her mobile to continue. If user has no mobile number email verification is used instead.
- User is requested to enter code TOTP code. Maintaining accounts requires successful sms authentication.

## Prerequisities and compilation

- Java 7+
- [Apache Maven 3](https://maven.apache.org/)
- Products of [MPASS project](https://github.com/Digipalvelutehdas/MPASS-proxy/tree/master/idp-mpass-parent) installed to  your local repository

```
cd idp-stepup-parent
mvn package
```


## Deployment Prerequisities
[MPASS Shibboleth SP Authentication](https://github.com/Digipalvelutehdas/MPASS-proxy/tree/master/idp-authn-impl-shibsp) is required to be deployed.

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

###Views
Copy the necessary views to place. Create the directories if needed. 

```
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/views/authn/* /opt/shibboleth-idp/views/authn/.
```
###Flows
Copy the flow to it's correct place. Create the directories if needed.
```
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/flows/intercept/stepup/stepup-flow.xml /opt/shibboleth-idp/flows/intercept/stepup/.
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/flows/authn/mfa/mfa-flow.xml /opt/shibboleth-idp/flows/authn/mfa/.
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/flows/oidc/mfarequest/mfarequest-flow.xml /opt/shibboleth-idp/flows/oidc/mfarequest/.
```

###Beans
Copy the bean definition to it's correct place. Create the directories if needed.
```
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/flows/intercept/stepup/stepup-beans.xml /opt/shibboleth-idp/flows/intercept/stepup/.
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/flows/authn/mfa/mfa-beans.xml /opt/shibboleth-idp/flows/authn/mfa/.
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/flows/oidc/mfarequest/mfarequest-beans.xml /opt/shibboleth-idp/flows/oidc/mfarequest/.
```


##Configuration 

Note! You will need to modify both beans and flow to meet your use case. Please note that example bean configurations are missing vital deployment specific parameters like key values and account identifiers.  

###Configuring the beans

Beans are configured in file _/opt/shibboleth-idp/flows/intercept/stepup/stepup-beans.xml_. In the following section we describe some of the relevant example bean configuration to help get an overall idea. Bean configuration file should be studied for thorough understanding.

####CheckRequestedAuthenticationContext - fi.csc.idp.stepup.impl.CheckRequestedAuthenticationContext

This bean is used to configure which requested authentication methods are considered stepup methods. The example flow performs stepup process only to requests containing requested method listed in the beans configuration. 

####CheckProvidedAuthenticationContext - fi.csc.idp.stepup.impl.CheckProvidedAuthenticationContext

If requested method is determined to require stepup, this bean is configured for a list of acceptable idp, sp and method triplets for stepup. This is for a case IdP and method is explicitly whitelisted for SP to be accepted as stepup. Use case for this would be a Idp having support for stronger authentication than others (the others requiring stepup performed by proxy) and making the action unncessary in the proxy. To make this work sensibly request method mapping feature of [MPASS Shibboleth SP Authentication](https://github.com/Digipalvelutehdas/MPASS-proxy/tree/master/idp-authn-impl-shibsp) is propably needed. 

####InitializeStepUpChallengeContext - fi.csc.idp.stepup.impl.InitializeStepUpChallengeContext

This bean is configured to initialize correctly the stepup methods and accounts for the user. Bean is configured with supported authentication manager beans (i.e. method) and a list of authentication context values they support. The action selects from all sucessfully initialized authentication managers and their accounts the first one matching the requested authentication context as the default method and account. 

####GoogleAuthStepUpManager - fi.csc.idp.stepup.impl.AttributeKeyBasedStorageStepUpAccountManager

This bean is a example of a method that requires attribute context to inilitialize the accounts and those accounts are permanent i.e. stored to database. The account type is Google Authenticator i.e. user has to provide a TOTP passcode of a account registered to proxy.

####SMSReceiverStepUpManager - fi.csc.idp.stepup.impl.AttributeTargetBasedStepUpAccountManager

This bean is a example of a method that requires attribute context to inilitialize the accounts but accounts do not have to be stored. In this example case the account is initialized with user mobile number.

####SMSReceiverStepUpAccount - fi.csc.idp.stepup.impl.TvilioSMSReceiverStepUpAccount

This bean is an account implementation used by SMSReceiverStepUpManager. This implementation is meant for user authentication performed by user receiving and replying to sms. Follow how the bean is wired to get an idea how to create a new account type.

####SetRequestedAuthenticationContext - fi.csc.idp.stepup.impl.SetRequestedAuthenticationContext

This bean is used for configuring which authentication method is sent to client SP in assertion in the case stepup has not been performed. There are two maps, a specific and default map. If there is a mapping of Idp, Sp, Method->new Method, the specified new Method is used in assertion. If there is no such mapping but there is default
mapping of Idp and SP, the method received from IdP is used in the new assertion. If there are no mappings at all, normal IdP logic takes place.

The idea here is to allow proxy to deliver the actual used method provided by originating Idp and also to translate between incompatible SP & IdP pairs that have different understanding of methods.

###Enable interceptor
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
