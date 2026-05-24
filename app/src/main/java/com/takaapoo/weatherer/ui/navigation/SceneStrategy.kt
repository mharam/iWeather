package com.takaapoo.weatherer.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.takaapoo.weatherer.ui.theme.customColorScheme

// --- ListDetailScene ---
/**
 * A [Scene] that displays a list and a detail [NavEntry] side-by-side in a 50/50 split.
 *
 */
data class ListDetailScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val listEntry: NavEntry<T>,
    val detailEntry: NavEntry<T>?,
) : Scene<T> {
    override val entries: List<NavEntry<T>> = detailEntry?.let { listOf(listEntry, it) } ?: listOf(listEntry)
    override val content: @Composable (() -> Unit) = {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxHeight().weight(1f)) {
                listEntry.Content()
            }
            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(color = MaterialTheme.customColorScheme.paneSpacerColor)
            )
            Column(
                modifier = Modifier.fillMaxHeight().weight(1f)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                detailEntry?.Content()
            }
        }
    }
}

@Composable
fun <T : Any> rememberListDetailSceneStrategy(singlePane: Boolean): ListDetailSceneStrategy<T> {
//    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    return remember(singlePane) {
        ListDetailSceneStrategy(singlePane)
    }
}

// --- ListDetailSceneStrategy ---
/**
 * A [SceneStrategy] that returns a [ListDetailScene] if the window is wide enough, the last item
 * in the backstack is a detail, and before it, at any point in the backstack is a list.
 */
class ListDetailSceneStrategy<T : Any>(val singlePane: Boolean) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {

        if (singlePane ||
            entries.lastOrNull()?.takeIf {
                it.metadata.containsKey(DETAIL_KEY) || it.metadata.containsKey(LIST_KEY)
            } == null) return null

        val detailEntry = entries.lastOrNull()?.takeIf { it.metadata.containsKey(DETAIL_KEY) }
        val listEntry = entries.findLast { it.metadata.containsKey(LIST_KEY) } ?: return null

        // We use the list's contentKey to uniquely identify the scene.
        // This allows the detail panes to be displayed instantly through recomposition, rather than
        // having NavDisplay animate the whole scene out when the selected detail item changes.
        val sceneKey = listEntry.contentKey

        return ListDetailScene(
            key = sceneKey,
            previousEntries = entries.dropLast(1),
            listEntry = listEntry,
            detailEntry = detailEntry
        )
    }

    companion object {
        internal const val LIST_KEY = "ListDetailScene-List"
        internal const val DETAIL_KEY = "ListDetailScene-Detail"

        /**
         * Helper function to add metadata to a [NavEntry] indicating it can be displayed
         * as a list in the [ListDetailScene].
         */
        fun listPane() = mapOf(LIST_KEY to true)

        /**
         * Helper function to add metadata to a [NavEntry] indicating it can be displayed
         * as a detail in the [ListDetailScene].
         */
        fun detailPane() = mapOf(DETAIL_KEY to true)
    }
}