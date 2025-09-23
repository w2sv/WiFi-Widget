<p align="center">
  <a href=""><img width="200" height="200" src="https://github.com/w2sv/WiFi-Widget/blob/main/app/src/main/res/mipmap-xxxhdpi/logo_round.png" alt=""></a>
</p>

<div id="user-content-toc">
  <ul style="list-style: none; padding-left: 0;">
    <summary>
      <div align="center">
        <h1>WiFi Widget</h1>
      </div>
    </summary>
  </ul>
</div>

<p align="center">
  <img src="https://img.shields.io/endpoint?color=green&logo=google-play&logoColor=green&url=https%3A%2F%2Fplay.cuzi.workers.dev%2Fplay%3Fi%3Dcom.w2sv.wifiwidget%26l%3DPlay%2520Store%26m%3D%24version"/>
  <img alt="F-Droid" src="https://img.shields.io/f-droid/v/com.w2sv.wifiwidget">
  <img src="https://img.shields.io/f-droid/v/com.w2sv.wifiwidget?baseUrl=https://apt.izzysoft.de/fdroid&label=IzzyOnDroid">
  <img alt="GitHub release (latest by date including pre-releases)" src="https://img.shields.io/github/v/release/w2sv/WiFi-Widget?include_prereleases"/>

  <br>

  <a href="https://github.com/w2sv/WiFi-Widget/releases">
    <img src="https://img.shields.io/github/downloads/w2sv/WiFi-Widget/total?label=Downloads&logo=github" alt=""/>
  </a>
  <img src="https://img.shields.io/endpoint?color=green&logo=google-play&logoColor=green&url=https%3A%2F%2Fplay.cuzi.workers.dev%2Fplay%3Fi%3Dcom.w2sv.wifiwidget%26l%3DDownloads%26m%3D%24totalinstalls" alt=""/>

  <br>

  <img src="https://img.shields.io/github/license/w2sv/WiFi-Widget" alt="">
  <img alt="GitHub code size in bytes" src="https://img.shields.io/github/languages/code-size/w2sv/WiFi-Widget">
  <a href="https://github.com/w2sv/WiFi-Widget/actions/workflows/workflow.yaml"><img alt="Check & Assemble Debug" src="https://github.com/w2sv/WiFi-Widget/actions/workflows/workflow.yaml/badge.svg"></a>

</p>

------

<p align="center">
<b>Android app providing a fully configurable widget for the monitoring of your WiFi connection details.</b>
</p>

------

| ![](https://github.com/w2sv/WiFi-Widget/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/1.jpg) | ![](https://github.com/w2sv/WiFi-Widget/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/2.jpg) | ![](https://github.com/w2sv/WiFi-Widget/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/3.jpg) |
|----------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| ![](https://github.com/w2sv/WiFi-Widget/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/4.jpg) | ![](https://github.com/w2sv/WiFi-Widget/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/5.jpg) | ![](https://github.com/w2sv/WiFi-Widget/blob/main/app/src/main/play/listings/en-US/graphics/phone-screenshots/6.jpg) |

<div id="user-content-toc">
  <ul style="list-style: none; padding-left: 0;">
    <summary>
      <div align="center">
        <h1>Where to get it</h1>
      </div>
    </summary>
  </ul>
</div>

<p align="center">
<a href="https://play.google.com/store/apps/details?id=com.w2sv.wifiwidget"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" height="80"/></a>
<a href="https://f-droid.org/packages/com.w2sv.wifiwidget/"><img alt="Download from F-Droid" src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="80"/></a>
<a href="https://apt.izzysoft.de/packages/com.w2sv.wifiwidget"><img alt="Download from IzzyOnDroid" src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" height="80"/></a>
<a href="https://github.com/w2sv/WiFi-Widget/releases/latest"><img alt="Get it on GitHub" src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" height="80"/></a>
</p>

<div id="user-content-toc">
  <ul style="list-style: none; padding-left: 0;">
    <summary>
      <div align="center">
        <h1>Features</h1>
      </div>
    </summary>
  </ul>
</div>

### In-App

- Neat Material 3 Design
- Configurable theme:
    - Light / dark
    - Dynamic / static colors
    - AMOLED black
- Adaptive layouts for landscape & portrait mode
- Live WiFi Status display with property copy-to-clipboard functionality on click

### Widget
- Property copy-to-clipboard functionality on click
- **Configuration options:**
    - Appearance:
        - Size: from 2x1 to fullscreen
        - Light/dark theme with static/dynamic, or entirely custom colors
        - Background opacity
        - Font size
        - Property value alignment (left | right)
    - Displayed properties:
        - **Basic Info**: SSID, BSSID, Frequency, Channel, Link Speed, RSSI, Signal Strength, Standard, WiFi Generation, Security Protocol  
        - **IP Addresses**:
          - Types: Loopback, Site Local, Link Local, Unique Local, Multicast, Global Unicast, Public (via [api.ipify.org](https://api.ipify.org))
          - Options: IPv4, IPv6, or both; show prefix lengths (IPv4/IPv6) and subnet masks (IPv4)
        - **Network Details**: Gateway, DNS, DHCP, NAT64 Prefix  
        - **Location Info** (via [ip-api.com](https://ip-api.com/)):
          - Region: Zip Code, District, City, Region, Country, Continent  
        - **Other**: GPS Location, ASN, ISP
    - Property appearance order
    - Bottom bar elements:
        - Last refresh date time
        - Buttons:
            - Refresh data
            - Open WiFi settings
            - Open widget settings
    - Automatic data refreshing:
        - Interval: between 15 min and 24h 
        - Whether to refresh on low battery

<div id="user-content-toc">
  <ul style="list-style: none; padding-left: 0;">
    <summary>
      <div align="center">
        <h1>Tech Stack</h1>
      </div>
    </summary>
  </ul>
</div>

- Kotlin only
- Jetpack Compose for in-app UI, xml & RemoteViews for widget UI
- [Jetpack Navigation 3](https://developer.android.com/guide/navigation/navigation-3?hl=de)
- Coroutines & flows
- [Dagger-Hilt](https://dagger.dev/hilt/) for dependency injection
- [OkHttp](https://square.github.io/okhttp/) for network requests, [kotlinx serialization](https://github.com/Kotlin/kotlinx.serialization) for JSON parsing
- Proto & Preferences data storage
- JUnit 4, [mockito](https://github.com/mockito/mockito), [robolectric](https://robolectric.org/) & [turbine](https://github.com/cashapp/turbine) for unit testing
- JUnit 4 Compose android (instrumented) testing
- Androidx Macro benchmarking & baseline profile generation with app-specific usage journey, implemented with [UI Automator](https://developer.android.com/training/testing/other-components/ui-automator)

<div id="user-content-toc">
  <ul style="list-style: none; padding-left: 0;">
    <summary>
      <div align="center">
        <h1>Architecture</h1>
      </div>
    </summary>
  </ul>
</div>

- **Multi-modular build**
- **Gradle Convention plugins** for gradle code reuse whilst keeping modules independent from one another
- **Clean architecture** (or however you wanna call it), with the UI and data layers depending on the domain layer, which exposes the data model and repository interfaces:

<p align="center">
    <img src="docs/graphs/dependency_graph.svg" alt=""/>
</p>

<div id="user-content-toc">
  <ul style="list-style: none; padding-left: 0;">
    <summary>
      <div align="center">
        <h1>Credits</h1>
      </div>
    </summary>
  </ul>
</div>

<p align="center">
Logo foreground by <a href="https://freeicons.io/profile/75801">Hilmy Abiyyu Asad</a> taken
from <a href="https://freeicons.io/computer-devices-3/router-wifi-internet-hotspot-icon-487667#">here</a>,
where it is licensed
under <a href="https://creativecommons.org/licenses/by/3.0/">Creative Commons(Attribution 3.0 unported)</a>.
</p>

<div id="user-content-toc">
  <ul style="list-style: none; padding-left: 0;">
    <summary>
      <div align="center">
        <h1>Donations</h1>
      </div>
    </summary>
  </ul>
</div>

<p align="center">
<a href="https://www.buymeacoffee.com/w2sv" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: 41px !important;width: 174px !important" ></a>
</p>

<div id="user-content-toc">
  <ul style="list-style: none; padding-left: 0;">
    <summary>
      <div align="center">
        <h1>License</h1>
      </div>
    </summary>
  </ul>
</div>

<p align="center">
<a href="https://github.com/w2sv/WiFi-Widget/blob/main/LICENSE">GPL-3.0 License</a> © <a href="https://github.com/w2sv">w2sv</a> [2022 - Present]
</p>
