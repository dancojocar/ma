CanvasExample - Solution code
=============================

Draw on your device screen using your finger.

Introduction
------------

This app uses a custom view to implement drawing on a canvas in response
to touch events. All drawing happens on the UI thread by overriding the
custom view's onDraw() method. You can use this technique when drawing
always takes less time than a screen refresh cycle on all target devices.

Pre-requisites
--------------

You need to know:
- How to open, build, and run apps with Android Studio.
- How to extract string resources and use string resources in the code.
- How to create a custom view that handles touch or click events.
- Basic understanding of the activity lifecycle.

Getting Started
---------------

1. Download and run the app.

License
-------

Copyright 2017 Google, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
