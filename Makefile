SHELL=/bin/bash

build-apk:
	@./gradlew assembleRelease --console verbose

build-aab:
	@./gradlew :app:bundleRelease --console-verbose
