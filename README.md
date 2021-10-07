# Note!
This is not meant for general public to use. If you are looking for *Shibboleth Second Factor solution* please *look else where*. There is no support and principles like semantic versioning is not respected.
# shibboleth-idp-authn-stepup
This project offers a shibboleth auhentication flow *authn/Stepup*. The flow may be configured to be used in myriad of ways. All the configuration is done by editing *stepup.properties* - file.
## Configuration by *stepup.properties*
### stepup.selfService = false
The default value is false. *selfService* false indicates that whatever the authentication method is used, user not having it initialized leads eventually to authentication error. The authentication method used is set by property *stepup.authenticationManager* and suitable values for this case are *AttributeSeededGoogleAuthStepUpManager*, *GoogleAuthStepUpManager*, *MailStepUpManager*, *SMSStepUpManager* and *LogStepUpManager* (for test).   
### stepup.selfService = true
Having selfService means that if there is no authentication method intialized for the user, a registration process is offered. This makes sense only if *stepup.authenticationManager* has value *GoogleAuthStepUpManager* that implements locally maintained totp seeds. The registration process requires authentication and it is controlled by property *stepup.registration.authenticationManager*. Sensible values for that would *MailStepUpManager*, *SMSStepUpManager* or *LogStepUpManager* (test).
### stepup.idpMFAFlow = true
The default value is true. Having this property on indicates any input required by the authentication method can be located from *AttributeResolutionContext*. See *stepup.properties* - file on how to name input for any given authentication method. This is for a use case you run this flow as part of Shibboleth MFA configuration having some other flow to perform the initial authentication.
### stepup.idpMFAFlow = false
Having this property off indicates any input required by the authentication method can be located from OIDC authentication request requested claims. This is for a use case you want to run the IdP as a second factor service for other services that have already performed the initial authentication. See Wiki on details how to form the request. 
## Authentication Methods
### AttributeSeededGoogleAuthStepUpManager
Perform TOTP. TOTP seed must be given as input - like with all inputs, either as attribute in AttributeResolutionContext or as OIDC requested claim value. The seed is assumed to be encrypted with a proprietary method.
### GoogleAuthStepUpManager
Perform TOTP. User key must be given as input. Key is used to fetch the TOTP seed from local storage.
### MailStepUpManager
Send user one time password in email. User email is required as input.
### SMSStepUpManager
Send user one time password in sms. User phone number is required as input.
### LogStepUpManager
Write one time password to debug logs. No input is required.
## API and stuff.
See Wiki.

## Build
    git clone https://github.com/CSCfi/shibboleth-idp-authn-stepup.git
    cd shibboleth-idp-authn-stepup
    mvn package

## Prerequisite for installation
- Shibboleth IdP 4.0+ 
- Shibboleth IdP OP plugin if run as OIDC second factor service. 

## Installation
First you need extract the archive and rebuild the package. Please not that you most likely *need* to change the owner and group information of the extracted files to suite your installation.

    cd /opt/shibboleth-idp
    tar -xf path/to/idp-stepup-distribution-X.X.X-bin.tar.gz  --strip-components=1
    bin/build.sh

Restart the Shibboleth IdP.

