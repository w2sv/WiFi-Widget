SHELL=/bin/bash

VERSION := $(shell grep '^version=' gradle.properties | cut -d'=' -f2)

# ==============
# Development
# ==============

grant-location-access-permission:
	@adb shell pm grant com.w2sv.wifiwidget.debug android.permission.ACCESS_FINE_LOCATION
	@adb shell pm grant com.w2sv.wifiwidget.debug android.permission.ACCESS_COARSE_LOCATION

revoke-location-access-permission:
	@adb shell pm revoke com.w2sv.wifiwidget.debug android.permission.ACCESS_FINE_LOCATION
	@adb shell pm revoke com.w2sv.wifiwidget.debug android.permission.ACCESS_COARSE_LOCATION

optimize-drawables:
	@avocado app/src/main/res/drawable/*.xml

clean:
	@echo "Clean"
	@./gradlew clean

format:
	@./gradlew ktlintFormat

update-dependencies:
	@./gradlew versionCatalogUpdate

update-gradle:
	@./gradlew wrapper --gradle-version latest

generate-proto:
	@./gradlew :datastore-proto:generateDebugProto

generate-dependency-graph:
	@./gradlew generateModulesGraphvizText --no-configure-on-demand -Pmodules.graph.output.gv=all_modules
	@dot -Tsvg all_modules -o module-graph.svg
	@mv module-graph.svg docs/
	@rm all_modules

# ==============
# Building
# ==============

baseline-profile:
	@echo "Generate baseline profile"
	@./gradlew :app:generateReleaseBaselineProfile --console verbose

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

publish-bundle:
	@echo "Publish Bundle"
	@./gradlew publishBundle --track production --console verbose

# fdroid metadata: https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/com.w2sv.wifiwidget.yml
