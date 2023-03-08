SHELL=/bin/bash

build-apk:
	@./gradlew assembleRelease --console verbose

build-aab:
	@./gradlew :app:bundleRelease --console verbose

publish-release-gh:
	@gh release create $(version) --generate-notes app/build/outputs/apk/release/$(version).apk