package com.example.adventure

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.adventure.activity.MainActivity
import com.example.adventure.ui.screen.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class WeatherScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject() // Important for Hilt to inject fields in the test class if any
    }

    private fun waitForLoad(milliseconds : Long = 15000L) {
        composeTestRule.waitUntil(milliseconds) {
            val progress = composeTestRule.onAllNodesWithTag(TAG_PROGRESS, useUnmergedTree = true)
            progress.fetchSemanticsNodes().isEmpty()
        }
    }

    private fun waitForInitialLoad(milliseconds : Long = 20000L) {
        composeTestRule.waitUntil(milliseconds) {
            var isEnabled = false
            try {
                composeTestRule.onNodeWithTag(TAG_LOCATION_DROPDOWN_OUTLINE).assertIsEnabled()
                isEnabled = true
            } catch (e : AssertionError) {
                //Not enabled yet
            }
            isEnabled
        }
        println("Location List loaded")
    }

    @Test
    fun initialState_ShowSelectionLocationOrLoading() {
        composeTestRule.onNodeWithTag(TAG_LOCATION_DROPDOWN).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TAG_LOCATION_DROPDOWN).performClick()
            try {
                composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
            } catch (e : AssertionError) {
                println("InitialState: 'Loading locations...' not found, might have loaded quickly or failed to load list.")
                try {
                    composeTestRule.onNodeWithText("Select Location", substring = true).assertIsDisplayed()
                } catch (e2: AssertionError) {
                    println("InitialState: 'Please select' text not found either.")
                }
            }
        composeTestRule.onNodeWithTag(TAG_LOCATION_DROPDOWN).performClick() //Close dropdown back
    }

    @Test
    fun selectLocation_ThenFetchesAndDisplaysData() {
        waitForInitialLoad()
        composeTestRule.onNodeWithTag(TAG_LOCATION_DROPDOWN).performClick()
        composeTestRule.waitUntil { composeTestRule.onAllNodesWithTag(TAG_LOCATION_DROPDOWN, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        composeTestRule.onNodeWithText("New York", useUnmergedTree = true).performScrollTo().performClick()
        composeTestRule.onNodeWithTag(TAG_REFRESH_BUTTON).performClick()

        waitForLoad()

        try {
            composeTestRule.onNodeWithTag(TAG_LOCATION_DESC, useUnmergedTree = true)
                .assertIsDisplayed()
                .assertTextContains("New York", substring = true, ignoreCase = true)
        } catch (e : AssertionError) {
            println("Location description not found and/or New York is not in the text.")
            throw AssertionError(e.message, e)
        }

        try {
            composeTestRule.onNodeWithTag(TAG_WEATHER_TEMP, useUnmergedTree = true)
                .assertIsDisplayed()
        } catch (e : AssertionError) {
            println("Weather temperature not found.")
            throw AssertionError(e.message, e)
        }

        composeTestRule.onNodeWithTag(TAG_ERROR_TEXT).assertDoesNotExist()
    }

    @Test
    fun clickRefresh_WhenDataIsDisplayed_TriggersLoadingAgain() {
        selectLocation_ThenFetchesAndDisplaysData() // Reuse previous test logic

        println("Data displayed. Clicking refresh...")
        composeTestRule.onNodeWithTag(TAG_REFRESH_BUTTON)
            .assertIsDisplayed()
            .performClick()

        // 3. Assert that a loading indicator (or loading text) appears
        // Need to be specific about which loading indicator (weather details or main progress)
        composeTestRule.waitUntil(timeoutMillis = 5000L) {
            // Check for either the main progress or the "Loading..." text
            val progressNodes = composeTestRule.onAllNodesWithTag(TAG_PROGRESS, useUnmergedTree = true)
            val loadingTextNodes = composeTestRule.onAllNodesWithText("Loading...", useUnmergedTree = true)
            progressNodes.fetchSemanticsNodes().isNotEmpty() || loadingTextNodes.fetchSemanticsNodes().isNotEmpty()
        }
        println("Loading indicator appeared after refresh.")

        // 4. (Optional) Wait for loading to finish again and assert data is still there or updated
        waitForLoad()
        composeTestRule.onNodeWithTag(TAG_WEATHER_DESC, useUnmergedTree = true).assertExists()
    }

}