SHELL=/bin/bash

VERSION := $(shell grep '^version=' gradle.properties | cut -d'=' -f2)

optimize-drawables:
	@avocado app/src/main/res/drawable/*.xml

clean:
	@echo "Clean"
	@./gradlew clean

lint:
	@./gradlew lint

check:
	@./gradlew check

ktlint-format:
	@./gradlew ktlintFormat

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

publish-to-test-track:
	@echo -e "Retrieved Version: ${VERSION}\nHit enter to continue"
	@read

	@$(MAKE) clean  # Required as 'publishBundle' publishes all .aab's in specified archive dir
	@$(MAKE) build-aab

	@echo "Publish Bundle"
	@./gradlew publishBundle --track internal

publish:
	@echo -e "Retrieved Version: ${VERSION}\n\n Hit enter if you have\n 1. Incremented the version\n 2. Updated the release notes\n\n Otherwise cancel target now."
	@read

	@echo "Check"
	@$(MAKE) check

	@$(MAKE) clean  # Required as 'publishBundle' publishes all .aab's in archive dir
	@$(MAKE) baseline-profile

	@echo "Pushing latest changes";git add .;git commit -m "${VERSION}";git push

	@$(MAKE) build-apk
	@$(MAKE) create-gh-release

	@$(MAKE) build-aab
	@$(MAKE) publish-bundle

publish-bundle:
	@echo "Publish Bundle"
	@./gradlew publishBundle --track production --console verbose

create-gh-release:
	@echo "Create GitHub Release"
	@gh release create $(VERSION) app/build/outputs/apk/release/$(VERSION).apk -F app/src/main/play/release-notes/en-US/production.txt
