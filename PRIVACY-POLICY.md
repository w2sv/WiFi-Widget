# WiFi Widget Privacy Policy

**Last updated: November 19, 2026**

WiFi Widget is an open-source Android application developed by Janek Zangenberg (w2sv). Its source code is publicly available on [GitHub](https://github.com/w2sv/WiFi-Widget) under the [GNU General Public License v3.0](https://github.com/w2sv/WiFi-Widget/blob/main/LICENSE).

## Data collection and storage

WiFi Widget does not collect personal information or operate servers that store user data.

The app retrieves information about your Wi-Fi connection directly from Android and processes it locally on your device. Network information is retrieved as needed, and certain values may be temporarily cached in memory. WiFi Widget does not maintain a history of your connected networks.

Your app preferences and widget configuration are stored locally on your device.

Some optional features retrieve additional network information from third-party services, as explained below.

## Third-party services

WiFi Widget uses external services to retrieve certain network properties:

- **ipify** provides your public IP address.
- **ip-api.com** provides IP-based location information, ISP details, and other network information.

These requests are made only when the corresponding properties are enabled. The services receive your network's public IP address as part of the connection and may process it according to their own privacy policies.

Requests to ip-api.com currently use HTTP, meaning the transmitted information is not encrypted in transit.

WiFi Widget does not transmit your Wi-Fi network name (SSID), access point identifier (BSSID), or GPS coordinates to these services.

## Location permissions

Android requires precise location permission to access Wi-Fi network identifiers such as the SSID and BSSID. This restriction exists because these identifiers can potentially be used to determine a device's approximate location.

WiFi Widget uses this permission exclusively to retrieve and display Wi-Fi network information. It does not use Android's location permission to determine or track your physical location.

**Background location access is optional.** Granting it allows WiFi Widget to keep your network identifiers updated on the home screen widget even when the app isn't open. Without background access, these updates may be limited.

You can use WiFi Widget without granting location permissions. However, network properties that require these permissions will remain unavailable.

## Contact

If you have any questions or concerns about this privacy policy, you can contact the developer at:

**zangenbergjanek@googlemail.com**
