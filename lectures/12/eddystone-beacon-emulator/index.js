'use strict';

var debug = require('debug')('eddystone');
var bleno = require('bleno');
var beacon = require('eddystone-beacon');
var uid = require('eddystone-uid');
var RandomMeasure = require('random-measure');
var PulseBeat = require('pulsebeat');
var assign = require('object-assign');

var TX_POWER_MODE_LOW = 1;
var BEACON_PERIOD_LOWEST = 10;

var defaultBeaconConfig = {};
var beaconConfig = {};

function createMeasurer(range) {
	return RandomMeasure.isRange(range) ? new RandomMeasure(range) : {
		measure: function() {
			return range;
		}
	};
}

function advertise(opts) {
	var voltMeasurer = createMeasurer(opts.voltage);
	var tempMeasurer = createMeasurer(opts.temperature);
	var namespace = uid.toNamespace(opts.nid);
	var instanceId = uid.toBeaconId(opts.bid);
	var advertiseOpts = {
	  tlmCount: 2,
	  tlmPeriod: 10,
		txPowerLevel: beaconConfig.txPowerLevel
	};

	var advertiers = new PulseBeat([
		function() {
			debug('UID advertising with namespace: %s, instance ID: %s', namespace, instanceId);
			beacon.advertiseUid(namespace, instanceId, advertiseOpts);
		},
		function() {
			debug('URL advertising with url: %s', beaconConfig.uri);
			beacon.advertiseUrl(beaconConfig.uri, advertiseOpts);
		},
		function() {
			var volt = voltMeasurer.measure();
			var temp = tempMeasurer.measure();

			debug('TLM data has been updated, voltage: %s, temprature: %s', volt, temp);

			beacon.setBatteryVoltage(volt);
			beacon.setTemperature(temp);
		}
	]);

	// start TLM advertising
	beacon.advertiseTlm();

	// loop UID / URL advertising
	advertiers.beat({timeout: 2000, interval:true});

	// bind to bleno events
	bleno.on('advertisingError', function (err) {
		debug('advertising has been failed', err);
	});

	debug('UID / URL / TLM advertising has been started');
}

function resetConfig() {
	beaconConfig = assign(defaultBeaconConfig);
	return beaconConfig;
}

function configure(done) {
	var config = require('eddystone-beacon-config');

	// handle update event from configuration
	config.on('update', function (e) {
		if (e.name === 'reset' && e.value === 1) {
			debug('have got reset request, all of values will be updated to defaults');
			config.configure(resetConfig());
		} else {
			debug('%s has been updated %s', e.name, e.value);
			beaconConfig[e.name] = e.value;

			if (e.name === 'uriData') {
				debug('have got uri request, configuration mode will be terminated');
				done();
			}
		}
	});

	config.on('disable', function (e) {
		debug('Have got zero-beacon-period to stop transmitting URL');
	});

	// change lock state to false
	config.unlock();

	// starter advertising for configuration
	debug('beacon configuration service is starting');
	config.advertise(beaconConfig, {
		periodLowest: BEACON_PERIOD_LOWEST
	});
}

function start(opts) {
	// configure default data with options passing from cli
	beaconConfig = defaultBeaconConfig = assign({
		config: false,
		name: 'Eddystone beacon emulator',
		uriData: opts.uri || 'https://goo.gl/r8iJqW',
		nid: '8b0ca750-e7a7-4e14-bd99-095477cb3e77',
		bid: 'bid001',
		voltage: 0,
		temperature: -128,
		flags: 0,
		txPowerMode: TX_POWER_MODE_LOW,
		txPowerLevel: -18,
		beaconPeriod: 1000
	}, opts);

	// run advertising or configuration service
	if (opts.config) {
		configure(function () {
			advertise(beaconConfig);
		});
	} else {
		advertise(beaconConfig);
	}
}

module.exports = {
	advertise: start
};
