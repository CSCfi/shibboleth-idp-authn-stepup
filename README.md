# stepup-proxy
Authentation flow performing TOTP for the user and a API to manage the secrets. This module turns a Shibboleth OP (i.e. Shibboleth IdP shibboleth-idp-oidc-extension) to a stepup service authentication users already initially authenticated by client. 

## Prerequisite for installation
- Shibboleth IdP 3.4+ 
- [shibboleth-idp-oidc-extension](https://github.com/CSCfi/shibboleth-idp-oidc-extension) v1.0.0

## Installation
First you need extract the archive and rebuild the package. Please not that you most likely *need* to change the owner and group information of the extracted files to suite your installation.

    cd /opt/shibboleth-idp
    tar -xf path/to/x.tar.gz --strip-components=1
    bin/build.sh

...

## Configuring the client
