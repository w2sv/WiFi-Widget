# Widget integration tests

Run the seven behavior tests independently of store screenshot generation:

```bash
./gradlew :widget-integration-tests:widgetPixel6Api35DebugAndroidTest --quiet --console=plain --warning-mode=summary
```

The module targets `:app` with a Hilt test application and Android Test Orchestrator. It uses one AOSP Pixel 6, API 35 managed device with Launcher3, a connected validated Wi-Fi default route, and available app-widget pinning. A missing route fails with a diagnostic; the tests never contact ip-api or ipify because the injected HTTP interceptor supplies all responses and rejects unexpected endpoints.

Each test initializes WorkManager with the real `HiltWorkerFactory` and `work-testing` driver, persists a minimal widget configuration with periodic refreshing disabled, pins through Launcher3, and removes the widget and work during cleanup. The observer is explicitly initialized because `HiltTestApplication` does not execute the production `Application.onCreate()`.

Recovery tests control WorkManager constraints and retry advancement. Launcher rendering and broadcasts use bounded real-time polling. Factory recreation is verified by invoking `onCreate()` on a fresh Hilt-created production factory; Android's choice of when to recreate the collection service is outside deterministic test control. Test WorkManager scheduling is deterministic and does not exercise JobScheduler timing or device idle policies.
