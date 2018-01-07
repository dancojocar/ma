#!/usr/bin/env node
'use strict';

var meow = require('meow');
var eddystone = require('./');

var cli = meow({
	help: [
		'Usage',
		'  $ eddystone-beacon-emulator --config --name eddystone emulator --uri http://goo.gl/eddystone',
		'',
		'Options',
		'  --config: Run configuration service firstly and then the other advertisings will be starting',
		'  --uri: URI for advertising',
		'  --nid: Namespace ID, FQDN or UUID which ID will be hashed and truncated in 10Byte',
		'  --bid: Beacon ID for UID advertising',
		'  --voltage: Battery voltage, default is 0mV, or using a range like 500~10000 to randomize',
		'  --temperature: Temperature, default is -128(0x8000), or using a range like -128~128 to randomize'
	]
}, {
	string: 'bid',
	default: {
		config: false,
		name: 'Eddystone beacon emulator',
		uri: 'https://goo.gl/r8iJqW',
		nid: '8b0ca750-e7a7-4e14-bd99-095477cb3e77',
		bid: 'bid001',
		voltage: 0,
		temperature: -128,
	}
});

eddystone.advertise(cli.flags);
