<p align="center">
  <a href=""><img width="200" height="200" src="https://github.com/w2sv/WiFi-Widget/blob/main/app/src/main/res/mipmap-xxxhdpi/logo_round.png"></a>
</p>
<h1 align="center">WiFi Widget</h1>

<p align="center">
  <img src="https://img.shields.io/endpoint?color=green&logo=google-play&logoColor=green&url=https%3A%2F%2Fplay.cuzi.workers.dev%2Fplay%3Fi%3Dcom.w2sv.wifiwidget%26l%3DPlay%2520Store%26m%3D%24version"/>
  <img alt="F-Droid" src="https://img.shields.io/f-droid/v/com.w2sv.wifiwidget">
  <img alt="GitHub release (latest by date including pre-releases)" src="https://img.shields.io/github/v/release/w2sv/WiFi-Widget?include_prereleases"/>

  <br>

  <a href="https://github.com/w2sv/WiFi-Widget/releases">
    <img src="https://img.shields.io/github/downloads/w2sv/WiFi-Widget/total?label=Downloads&logo=github"/>
  </a>
  <img src="https://img.shields.io/endpoint?color=green&logo=google-play&logoColor=green&url=https%3A%2F%2Fplay.cuzi.workers.dev%2Fplay%3Fi%3Dcom.w2sv.wifiwidget%26l%3DDownloads%26m%3D%24totalinstalls"/>

  <br>

  <img src="https://img.shields.io/github/license/w2sv/WiFi-Widget">
  <img alt="GitHub code size in bytes" src="https://img.shields.io/github/languages/code-size/w2sv/WiFi-Widget">
  <a href="https://github.com/w2sv/WiFi-Widget/actions/workflows/workflow.yaml"><img alt="Check & Assemble Debug" src="https://github.com/w2sv/WiFi-Widget/actions/workflows/workflow.yaml/badge.svg"></a>

</p>

------

<p align="center">
<b>Android app, providing a fully configurable widget for the monitoring of your WiFi connection details.</b>
</p>

------

<h2 align="center">Download</h2>

<p align="center">
<a href="https://play.google.com/store/apps/details?id=com.w2sv.wifiwidget"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" height="80"/></a>
<a href="https://f-droid.org/packages/com.w2sv.wifiwidget/"><img alt="Download from F-Droid" src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="80"/></a>
<a href="https://github.com/w2sv/WiFi-Widget/releases/latest"><img alt="Get it on GitHub" src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" height="80"/></a>
</p>

<h2 align="center">Features</h2>

### In-App

- Clean Material 3 Design, featuring plenty of animations
- Supports light & dark theme, as well as dynamic colors
- Different layouts for landscape & portrait mode
- Live WiFi Status display with property copy-to-clipboard functionality

### Widget

- Resizable
- Configurable appearance:
    - Light/dark theme with static/dynamic colors, or custom colors
    - Configurable background opacity
    - Configurable font size
- Configurable WiFi property inclusion:
    - SSID, BSSID
    - IPs: Loopback, Site Local, Link Local, ULA, Multicast, Global Unicast, Public (fetched
      from https://api.ipify.org)
        - For address types that support IPv4 & IPv6, you may choose which versions to include
    - Frequency, Channel, Link Speed, RSSI, Standard, Gateway, DNS, DHCP
- Configurable button row elements
- Configurable automatic refreshing

<h2 align="center">Tech Stack</h2>

- Kotlin
- Jetpack Compose
- All sorts of Androidx stuff
- Coroutines
- Dagger-Hilt
- KSP
- OkHttp 3

<h2 align="center">Screenshots</h2>

| ![](https://github.com/w2sv/WiFi-Widget/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/1.jpg) | ![](https://github.com/w2sv/WiFi-Widget/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/2.jpg) | ![](https://github.com/w2sv/WiFi-Widget/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/3.jpg) |
|----------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| ![](https://github.com/w2sv/WiFi-Widget/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/4.jpg) | ![](https://github.com/w2sv/WiFi-Widget/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/5.jpg) | ![](https://github.com/w2sv/WiFi-Widget/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/6.jpg) |

<h2 align="center">Credits</h2>

<p align="center">
Logo foreground by <a href="https://freeicons.io/profile/75801">Hilmy Abiyyu Asad</a> taken
from <a href="https://freeicons.io/computer-devices-3/router-wifi-internet-hotspot-icon-487667#">here</a>,
where it is licensed
under <a href="https://creativecommons.org/licenses/by/3.0/">Creative Commons(Attribution 3.0 unported)</a>.
</p>

<h2 align="center">Donations</h2>
<br>
<p align="center">
<a href="https://www.buymeacoffee.com/w2sv" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: 41px !important;width: 174px !important" ></a>
</p>

<h2 align="center">License</h2>

<p align="center">
<a href="https://github.com/w2sv/WiFi-Widget/blob/main/LICENSE">GPL-3.0 License</a> © <a href="https://github.com/w2sv">w2sv</a> [2022 - Present]
</p>
