# CSE218_Fa19_HoloVitality

> HoloVitality is an opensource project aiming to track human vital sign, process and display in real-time on HoloLens.


[![Build Status](http://img.shields.io/travis/badges/badgerbadgerbadger.svg?style=flat-square)](https://travis-ci.org/badges/badgerbadgerbadger) [![Coverage Status](http://img.shields.io/coveralls/badges/badgerbadgerbadger.svg?style=flat-square)](https://coveralls.io/r/badges/badgerbadgerbadger) [![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org) 

---

## Table of Contents 

- [Background](#background)
- [Design](#design)
- [Architecture](#architecture)
- [Usage](#usage)
- [Tests](#tests)
- [Team](#team)
- [FAQ](#faq)
- [Support](#support)
- [License](#license)

---

## Background

Exercise is any bodily activity that enhances or maintains physical fitness and overall health and wellness. In 2017, U.S. fitness centers had a total membership of 60.87 million.

Our motivations include:
- Exercising incorrectly is inefficient and may even cause injuries.
- Tracking user vital sign is important during the exercise.
- Most wearables are for personal use, lacking real-time feedback to coaches.

---

## Design
Our aim is to provide gym coaches with user vital signs including heartbeats and medical history so that coaches will be able to react to potentially dangerous situations caused by over-reaching cardio exercise in real-time.
Gym coaches are able to see multi-user real-time heart beat rate, heart beat variability, medical history, and heart data analysis through the Hololens.

### Features
- Smartphone APP Features:
  * Capture the seismocardiogram (SCG) signals by using the built-in accelerometer 
  * Segment the SCG signals to get the heartbeat rate (HR) and heartbeat rate variability (HRV) in real-time
  * Transmit the HR & HRV to Server in real-time

- Server Features:
  * Receive the HR&HRV data from smartphone in real-time
  * Transmit the HR&HRV data to Hololens in real-time
  * Log the HR&HRV data 
  * Transmit the data analysis result to Hololens
  * Transmit the medical history to Hololens
  * Enable multi-user mode

- Hololens Features: 
  * Recognize the Server IP using QRCode
  * Identify different users using Vuforia image recognition
  * Locate different users position using Vuforia image localization
  * Receive the HR&HRV data from Server in real-time
  * Receive the data analysis result from Server
  * Receive the medical history from Server
  * Display the data based on the image localization
  * Display menu UI and back UI

---

## Architecture
There are three main parts of the whole system, i.e.
- the smartphone app capturing the vital sign
- the server to control the data flow
- the HoloLens app to receive data and display

The diagram of the architecture is shown below
![Repo List](picutres/system architecture.png)

---

## Usage
1. Start Python server
2. Start smartphone app, and click "Data Transmission" to connect to server
3. Start Hololens app, scan the QR code generated by the server to establish connection
---

## Tests 

---

## Team
> This is a team project for UCSD CSE218, Fall 19. Team members are listed below.

| **Ke Sun**</a> | **Jiayou Guo** | **Yi Xu** | **Yiyun Fan** | **Wenyu Zhang** |
| :---: |:---:| :---:| :---:| :---:|

---

## FAQ

---

## Support

Reach out to me at one of the following places!
- Insert more social links here.

---

## License

[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org)
- Copyright 2019 © HoloVitality
