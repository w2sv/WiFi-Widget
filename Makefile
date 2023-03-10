SHELL=/bin/bash

clean:
	@./gradlew clean

# -------------
# Building
# -------------

build-apk:
	@echo "Building APK"
	@./gradlew assembleRelease --console verbose

build-aab:
	@echo "Building AAB"
	@./gradlew :app:bundleRelease --console verbose

# -------------
# Publishing
# -------------

VERSION := $(shell ./gradlew properties -q | grep "version:" | cut -d " " -f2)

publish-bundle-to-play:
	@./gradlew publishBundle

create-gh-release:
	@gh release create $(VERSION) app/build/outputs/apk/release/$(VERSION).apk -F app/src/main/play/release-notes/en-US/production.txt

publishListing:
	@./gradlew publishListing
# -------------
# Building & Publishing
# -------------

build-and-publish:
	@echo -e "Retrieved Version: ${VERSION}\n Hit enter if you have\n 1. Incremented the version\n 2. Updated the release notes\n 3. Pushed the latest changes\n\n Otherwise cancel target now."
	@read

	@echo "Clean"  # Required as 'publishBundle' publishes all .aab's in specified archive dir
	@$(MAKE) clean

	@echo "Build APK"
	@$(MAKE) build-apk
	@echo "Build AAB"
	@$(MAKE) build-aab

	@echo "Publish Bundle"
	@$(MAKE) publish-bundle-to-play
	@echo "Create GitHub Release"
	@$(MAKE) create-gh-release
