# Note!
This is not meant for general public to use. If you are looking for *Shibboleth Second Factor solution* please *look else where*. There is no support and principles like semantic versioning is not respected.
# shibboleth-idp-authn-stepup
This project offers a shibboleth auhentication flow *authn/Stepup*. The flow may be configured to be used in myriad of ways. All the configuration is done by editing *stepup.properties* - file.
## Configuration by *stepup.properties*
## Authentication Methods
### AttributeSeededGoogleAuthStepUpManager
Perform TOTP. TOTP seed must be given as input - like with all inputs, either as attribute in AttributeResolutionContext or as OIDC requested claim value. The seed is assumed to be encrypted with a proprietary method.
### MailStepUpManager
Send user one time password in email. User email is required as input.
### SMSStepUpManager
Send user one time password in sms. User phone number is required as input.
### LogStepUpManager
Write one time password to debug logs. No input is required.

## Build
    git clone https://github.com/CSCfi/shibboleth-idp-authn-stepup.git
    cd shibboleth-idp-authn-stepup
    mvn package

## Prerequisite for installation
- Shibboleth IdP 5.0+ 

## Installation
First you need extract the archive and rebuild the package. Please not that you most likely *need* to change the owner and group information of the extracted files to suite your installation.

    cd /opt/shibboleth-idp
    tar -xf path/to/idp-stepup-distribution-X.X.X-bin.tar.gz  --strip-components=1
    bin/build.sh

Restart the Shibboleth IdP.

