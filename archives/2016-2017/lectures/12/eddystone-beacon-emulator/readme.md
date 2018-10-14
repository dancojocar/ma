# eddystone-beacon-emulator

![](http://g.recordit.co/PCLN1yC1Ol.gif)

> Emulator for eddystone beacon peripherals to test and develop eddystone application

## Install

```
$ npm install --global eddystone-beacon-emulator
```

```
$ eddystone-beacon-emulator --help

  Usage
    eddystone-beacon-emulator --config --uri=http://goo.gl/eddystone
    eddystone-beacon-emulator --uri=http://goo.gl/eddystone
  	eddystone-beacon-emulator --nid=http://google.com --bid=123456
  	eddystone-beacon-emulator --voltage=0 --temperature=-128
  	eddystone-beacon-emulator --voltage=5000~10000 --temperature=-128~128
```

## Options

-  --config: Run configuration service firstly and then the other advertisings will be starting',
-  --uri: URI for advertising',
-  --nid: Namespace ID, FQDN or UUID which ID will be hashed and truncated in 10Byte',
-  --bid: Beacon ID for UID advertising',
-  --voltage: Battery voltage, default is 0mV, or using a range like 500~10000 to randomize',
-  --temperature: Temperature, default is -128(0x8000), or using a range like -128~128 to randomize'

## Usage

```sh
DEBUG=eddystone node cli.js --uri=http://goo.gl/eddystone
```

## Won't Support Yet

- TLM period configuration is not supported
- TX Power Mode and TX Power Level configuration is not supported
- beacon period configuration is not supported
- flags configuration is not supported

## License

MIT © [Jimmy Moon](http://ragingwind.me)
