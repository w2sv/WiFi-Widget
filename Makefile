SHELL=/bin/bash

# ==============
# Publishing
# ==============

VERSION := $(shell ./gradlew properties -q | grep "version:" | cut -d " " -f2)

publish-listing:
	@./gradlew publishListing  --console verbose

build-and-publish-to-test-track:
	@echo -e "Retrieved Version: ${VERSION}\nHit enter to continue"
	@read

	@echo "Clean"  # Required as 'publishBundle' publishes all .aab's in specified archive dir
	@./gradlew clean

	@echo "Build AAB"
	@./gradlew :app:bundleRelease --console verbose

	@echo "Publish Bundle"
	@./gradlew publishBundle --track internal --console verbose

build-and-publish:
	@echo -e "Retrieved Version: ${VERSION}\n\n Hit enter if you have\n 1. Incremented the version\n 2. Updated the release notes\n 3. Pushed the latest changes\n\n Otherwise cancel target now."
	@read

	@echo "Clean"  # Required as 'publishBundle' publishes all .aab's in specified archive dir
	@./gradlew clean

	@echo "Build APK"
	@./gradlew assembleRelease --console verbose
	@echo "Build AAB"
	@./gradlew :app:bundleRelease --console verbose

	@echo "Create GitHub Release"
	@gh release create $(VERSION) app/build/outputs/apk/release/$(VERSION).apk -F app/src/main/play/release-notes/en-US/production.txt
	@echo "Publish Bundle"
	@./gradlew publishBundle --track production --console verbose --no-configuration-cache  # as usage of configuration cache throws error for task
