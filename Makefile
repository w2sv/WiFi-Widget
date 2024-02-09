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
	@echo "Generate baseline profile"
	@./gradlew :app:generateReleaseBaselineProfile

build-aab:
	@echo "Build AAB"
	@./gradlew :app:bundleRelease

build-apk:
	@echo "Build APK"
	@./gradlew assembleRelease

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
	@./gradlew publishBundle --track internal

build-and-publish:
	@echo -e "Retrieved Version: ${VERSION}\n\n Hit enter if you have\n 1. Incremented the version\n 2. Updated the release notes\n\n Otherwise cancel target now."
	@read

	@echo "Lint"
	@$(MAKE) lint

	@$(MAKE) baseline-profile

	@echo "Pushing latest changes";git add .;git commit -m "${VERSION}";git push

	@$(MAKE) clean  # Required as 'publishBundle' publishes all .aab's in archive dir

	@$(MAKE) build-aab
	@$(MAKE) build-apk

	@echo "Publish Bundle"
	@./gradlew publishBundle --track production --console verbose --no-configuration-cache  # usage of configuration cache throws error for task

	@$(MAKE) create-gh-release

create-gh-release:
	@echo "Create GitHub Release"
	@gh release create $(VERSION) app/build/outputs/apk/release/$(VERSION).apk -F app/src/main/play/release-notes/en-US/production.txt
