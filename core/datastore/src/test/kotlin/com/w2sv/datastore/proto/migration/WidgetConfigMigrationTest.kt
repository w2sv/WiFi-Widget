package com.w2sv.datastore.proto.migration

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.w2sv.datastore.WidgetColoringProto
import com.w2sv.datastore.proto.mapping.toExternal
import com.w2sv.datastore.proto.mapping.toProto
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class WidgetConfigMigrationTest {

    private val mockPreferences = mockk<DataStore<Preferences>>(relaxed = true)
    private val mockColoringDataStore = mockk<DataStore<WidgetColoringProto>>(relaxed = true)
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val migration = WidgetConfigMigration(
        context = context,
        preferences = mockPreferences,
        coloringDataStore = mockColoringDataStore
    )

    // -----------------------------
    // shouldMigrate
    // -----------------------------

    @Test
    fun `shouldMigrate returns inverse of migration flag`() =
        runTest {
            suspend fun test(migrated: Boolean) {
                every { mockPreferences.data } returns flowOf(
                    preferencesOf(MIGRATION_DONE_PREFERENCES_KEY to migrated)
                )

                val result = migration.shouldMigrate(WidgetConfig.default.toProto())
                assertEquals(!migrated, result)
            }

            test(true)
            test(false)
        }

    // -----------------------------
    // migrate
    // -----------------------------

    @Test
    fun `migrate correctly maps preferences and coloring`() =
        runTest {
            // Preferences setup
            every { mockPreferences.data } returns flowOf(
                preferencesOf(
                    // property enablement
                    booleanPreferencesKey("SSID") to false,

                    // property order (swap first two)
                    stringPreferencesKey("wifiPropertyOrder") to "1,0",

                    // appearance
                    floatPreferencesKey("opacity") to 0.5f,

                    // refreshing
                    booleanPreferencesKey("RefreshPeriodically") to false,
                    intPreferencesKey("refreshInterval") to 30
                )
            )

            // Coloring proto setup
            val coloringProto = WidgetColoringProto.newBuilder()
                .setUseCustom(true)
                .setCustom(
                    WidgetColoringProto.Custom.newBuilder()
                        .setBackground(1)
                        .setPrimary(2)
                        .setSecondary(3)
                        .build()
                )
                .build()

            every { mockColoringDataStore.data } returns flowOf(coloringProto)

            val migrated = migration.migrate(WidgetConfig.default.toProto())
            val external = migrated.toExternal()

            // --- Assertions ---

            // property enablement
            assertFalse(external.isEnabled(WifiProperty.SSID))

            // property order
            assertEquals(
                listOf(WifiProperty.entries[1], WifiProperty.entries[0]) +
                    WifiProperty.entries.drop(2),
                external.propertyOrder
            )

            // appearance
            assertEquals(0.5f, external.appearance.backgroundOpacity)

            // coloring
            assertTrue(external.appearance.coloring.useCustom)

            // refreshing
            assertFalse(external.refreshing.refreshPeriodically)
            assertEquals(30.minutes, external.refreshing.interval)
        }

    // -----------------------------
    // empty preferences fallback
    // -----------------------------

    @Test
    fun `empty preferences results in default config with migrated coloring`() =
        runTest {
            every { mockPreferences.data } returns flowOf(preferencesOf())

            val coloringProto = WidgetConfig.default.appearance.coloring.toProto()
            every { mockColoringDataStore.data } returns flowOf(coloringProto)

            val migrated = migration.migrate(WidgetConfig.default.toProto())
            val external = migrated.toExternal()

            // should basically be default (except coloring which is also default)
            assertEquals(WidgetConfig.default, external)
        }

    @Test
    fun `cleanUp sets migration done flag`() =
        TestScope().runTest {
            val testDataStore = PreferenceDataStoreFactory.create(
                scope = this,
                produceFile = { context.preferencesDataStoreFile("test_prefs_${UUID.randomUUID()}") }
            )

            val migration = WidgetConfigMigration(
                context = context,
                preferences = testDataStore,
                coloringDataStore = mockk(relaxed = true)
            )

            migration.cleanUp()

            val prefs = testDataStore.data.first()
            assertTrue(prefs[MIGRATION_DONE_PREFERENCES_KEY] == true)
        }
}
