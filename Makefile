SHELL=/bin/bash

VERSION := $(shell ./get-version.sh)

optimize-drawables:
	@avocado app/src/main/res/drawable/*.xml

clean:
	@echo "Clean"
	@./gradlew clean

lint:
	@./gradlew lint

check:
	@./gradlew check

# ==============
# Building
# ==============

baseline-profile:
	@echo "Build AAB"
	@./gradlew :app:generateReleaseBaselineProfile --console verbose

build-aab:
	@echo "Build AAB"
	@./gradlew :app:bundleRelease --console verbose

build-apk:
	@echo "Build APK"
	@./gradlew assembleRelease --console verbose

# ==============
# Publishing
# ==============

publish-listing:
	@./gradlew publishListing  --console verbose

build-and-publish-to-test-track:
	@echo -e "Retrieved Version: ${VERSION}\nHit enter to continue"
	@read

	@$(MAKE) clean  # Required as 'publishBundle' publishes all .aab's in specified archive dir
	@$(MAKE) build-aab

	@echo "Publish Bundle"
	@./gradlew publishBundle --track internal --console verbose

build-and-publish:
	@echo -e "Retrieved Version: ${VERSION}\n\n Hit enter if you have\n 1. Incremented the version\n 2. Updated the release notes\n 3. Pushed the latest changes\n\n Otherwise cancel target now."
	@read																			

	@$(MAKE) clean  # Required as 'publishBundle' publishes all .aab's in archive dir

	@$(MAKE) baseline-profile
	@$(MAKE) build-aab
	@$(MAKE) build-apk

	@$(MAKE) create-gh-release
	@echo "Publish Bundle"
	@./gradlew publishBundle --track production --console verbose --no-configuration-cache  # usage of configuration cache throws error for task

create-gh-release:
	@echo "Create GitHub Release"
	@gh release create $(VERSION) app/build/outputs/apk/release/$(VERSION).apk -F app/src/main/play/release-notes/en-US/production.txt
