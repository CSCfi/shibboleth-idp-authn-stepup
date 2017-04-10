#!/bin/bash

threadCount=2
roundCount=2
otpSecret=secretStr

jmxFile=./shibbo-sp-MFA-OIDC-Flow-Haka-test.jmx
otpProg=~/git/oathgen/oathgen_mac
jmeterProg=~/apache-jmeter-3.1/bin/jmeter

t=0
i=0

function testRun {
${jmeterProg} -n -t "${jmxFile}" \
		-JOTPCode=$(${otpProg} -s ${otpSecret}) \
		-JNumberOfUsers=${threadCount} | \
		perl -e 'while (<>) { $c += $1 if /Err:\s+(\d+)/ }; print "$c";'
}

while [ $i -lt 4 ] && [ $t -lt ${roundCount} ]
do
	i=$[$i+$(testRun)]
	t=$[$t+1]
	echo -e "$t | $i"
done
