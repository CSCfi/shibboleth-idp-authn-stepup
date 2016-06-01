# Shibboleth IdP Stepup Proxy

## Overview
These modules contain extensions to achieve stepup authentication in a proxy based on Shibboleth IdP 3 and Shibboleth SP. You may want to read overall [description](https://confluence.csc.fi/display/HAKA/Description+of+SAML2+Proxy+capable+of+elevating+authentication) of the whole proxy before going forward.

This project relies on [MPASS Shibboleth SP Authentication](https://github.com/Digipalvelutehdas/MPASS-proxy/tree/master/idp-authn-impl-shibsp) project for a authentication flow that actually enables to deploy a proxy based on Shibboleth IdP 3 and Shibboleth SP. The aim of this stepup project is to enhance that by providing a mechanisms for performing stepup authentication.

### Short Summary of flow
Stepup flow is a post authentication interceptor flow. In practise this means that Stepup flow receives control at a point where user has been  authenticated by the actual home organization, the results of this authentication have been populated by the MPASS Shibboleth SP Authentication module, the attributes have been resolved and filtered but the actual assertion has not been formed yet. At this point interceptor flow makes decisions whether a stepup is needed, what kind of stepup that would be and how that is communicated to client SP in the assertion.

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
cp ./idp-ssointerceptor-impl-stepup/target/idp-ssointerceptor-impl-stepup-\<version\>.jar /opt/shibboleth-idp/edit-webapp/WEB-INF/lib/.
cp ./idp-ssointerceptor-impl-stepup/target/dependency/idp-ssointerceptor-api-stepup-\<version\>.jar /opt/shibboleth-idp/edit-webapp/WEB-INF/lib/.
cd /opt/shibboleth-idp
sh bin/build.sh
```

The final command will rebuild the _war_-package for the IdP application. Please note that products of [MPASS Shibboleth SP Authentication](https://github.com/Digipalvelutehdas/MPASS-proxy/tree/master/idp-authn-impl-shibsp) are expected to be in the directory _/opt/shibboleth-idp/edit-webapp/WEB-INF/lib/_ (Deployment prerequisite).

###Views
Copy the necessary views to place. Create the directories if needed. 

```
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/views/* /opt/shibboleth-idp/views/intercept/.
```
###Flows
Copy the flow to it's correct place. Create the directories if needed.
```
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/flows/intercept/stepup/stepup/stepup-flow.xml /opt/shibboleth-idp/flows/intercept/stepup/.
```

###Beans
Copy the bean definition to it's correct place. Create the directories if needed.
```
cp ../idp-ssointerceptor-impl-stepup/src/main/resources/flows/intercept/stepup/stepup/stepup-beans.xml /opt/shibboleth-idp/flows/intercept/stepup/.
```


##Configuration 

Note! These Configuration instructions are yet not final. The contents of the flows and views will change before release still. 

###Configuring the beans

Beans are configured in file _/opt/shibboleth-idp/flows/intercept/stepup/stepup-beans.xml_

####CheckRequestedAuthenticationContext

This bean is used to configure which requested authentication methods are considered stepup methods. Stepup may be performed only to requests containing requested method listed in the beans configuration.

####CheckProvidedAuthenticationContext

If requested method is determined to require stepup, this bean is configured for a list of acceptable idp,sp,method triplets for stepup. This is for a case IdP and method is explicitly whitelisted for SP to be accepted as stepup. Use case for this would be a Idp having support for stronger authentication than others (the others requiring stepup performed by proxy). To make this work sensibly request method mapping feature of [MPASS Shibboleth SP Authentication](https://github.com/Digipalvelutehdas/MPASS-proxy/tree/master/idp-authn-impl-shibsp) is propably needed. 

####GenerateStepUpChallenge

This bean needs to be configured for all stepup methods perfomed by proxy. There are challenge creation, challenge sending and challenge verification implementations that need to be mapped for each of the supported methods. This can be used to control for instance wheter the challenge is send by sms or email to client depending on requested method.

####VerifyPasswordFromFormRequest
This bean needs to be configured for all stepup methods perfomed by proxy. Bean needs to be instructed which verification implementation is used for which method.


####SetRequestedAuthenticationContext

This bean is used for configuring which authentication method is sent to client SP in assertion. There are two maps, a specific and default map.
If there is a mapping of Idp,Sp,Method->new Method, the specified new Method is used in assertion. If there is no such mapping but there is default
mapping of Idp,SP, the method received from IdP is used in the new assertion. If there is no mappings, normal IdP logic takes place.

The idea here is to allow proxy to deliver the actual used method provided by originating Idp and also to translate between incompatible SP & IdP pairs that have different understanding of methods.

###Enable interceptor
Note! It is essential to configure the beans before enabling the interceptor. The provided configuration will not match your use case (and at the time of the writing, the definitions are not sane yet anyway).

...