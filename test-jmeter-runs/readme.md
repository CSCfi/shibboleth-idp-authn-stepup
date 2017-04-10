# Testrun with JMeter

This is a simple performance/functionality test with [JMeter](http://jmeter.apache.org).
Required TOTP is generated on the command line and passed for the JMeter as command line
property.

## Requirements

* [JMeter](http://jmeter.apache.org)
* [oathgen](https://github.com/w8rbt/oathgen)
   * [Crypto++ library](https://www.cryptopp.com)
   
## Installing

First, install requirements. Note that Crypto++ library need to be built and installed
with Base32 support before building oathgen. See
[this](https://github.com/w8rbt/oathgen/tree/master/alt_base32).

Clone or copy jmx-file and bash script from this folder.

## Running

* Define the secret of your totp authentication method as appropriate parameters 
of the script file
* Replace your user name on the test-idp in the jmx-file
* Run the script: `./test.sh`