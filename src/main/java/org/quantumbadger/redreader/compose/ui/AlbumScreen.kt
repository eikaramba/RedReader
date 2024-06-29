package org.quantumbadger.redreader.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.quantumbadger.redreader.R
import org.quantumbadger.redreader.compose.prefs.LocalComposePrefs
import org.quantumbadger.redreader.compose.theme.LocalComposeTheme
import org.quantumbadger.redreader.image.AlbumInfo
import org.quantumbadger.redreader.settings.types.AlbumViewMode
import kotlin.math.min
import kotlin.math.roundToInt

// TODO check for unstable composables
// TODO dark themes
// TODO theme progress spinner
// TODO make cards clickable
// TODO compact view/"list view"
// TODO grid view?
// TODO handle long-click
// TODO settings menu
// TODO pref to hide card buttons
// TODO find best reddit preview
// TODO resize image on another thread
// TODO actually hook up the buttons on the card
// TODO go through all todos on this branch
// TODO strings
// TODO screen reader testing
// TODO move the initial loading screen inside Compose
// TODO tidy up AlbumListingActivity2
// TODO gradient at top for status bar/nav bar?
// TODO error view
// TODO set RedditAccountId when fetching
// TODO handle videos in all three views
// TODO action bar on scroll, move head() out of the list
@Composable
fun AlbumScreen(
	album: AlbumInfo
) {
	val prefs = LocalComposePrefs.current
	val theme = LocalComposeTheme.current

	val topBarHeight = 48.dp

	val contentPadding: PaddingValues = WindowInsets(top = 8.dp, bottom = 24.dp)
		.add(WindowInsets.systemBars)
		.asPaddingValues()

	val head = @Composable {
		Column(
            Modifier
                .padding(horizontal = 14.dp)
                .fillMaxWidth()
		) {

			// Space for the top bar
			Spacer(Modifier.height(topBarHeight))

			Text(
				modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = album.title?.let { title ->
                            "Image gallery: $title"
                        } ?: "Image gallery" // TODO strings
                    },
				text = album.title ?: "Gallery",  // TODO string
				style = theme.album.title,
				overflow = TextOverflow.Ellipsis,
				maxLines = 2
			)

			Spacer(Modifier.height(6.dp))

			val s = "s".takeIf { album.images.size != 1 } ?: ""

			Text(
				modifier = Modifier.fillMaxWidth(),
				text = album.description ?: "${album.images.size} image$s", // TODO string
				style = theme.album.subtitle,
				overflow = TextOverflow.Ellipsis,
				maxLines = 3
			)

			Spacer(Modifier.height(12.dp))
		}
	}

	Box {

		// Content

		val scrollState = when (prefs.albumViewMode.value) {
			AlbumViewMode.Cards -> {

				val state = rememberLazyListState()

				LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .background(theme.postCard.listBackgroundColor),
					contentPadding = contentPadding,
					state = state
				) {
					item {
						head()
					}

					items(count = album.images.size) {
						AlbumCard(image = album.images[it])
					}
				}

				object : TopBarScrollObservable {
					override val firstVisibleItemIndex: Int
						get() = state.firstVisibleItemIndex
					override val firstVisibleItemOffset: Int
						get() = state.firstVisibleItemScrollOffset
				}
			}

			AlbumViewMode.List -> {
				val state = rememberLazyListState()

				LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .background(theme.postCard.listBackgroundColor),
					contentPadding = contentPadding,
					state = state
				) {
					item {
						head()
					}

					items(count = album.images.size) {
						AlbumCard(image = album.images[it])
					}
				}

				object : TopBarScrollObservable {
					override val firstVisibleItemIndex: Int
						get() = state.firstVisibleItemIndex
					override val firstVisibleItemOffset: Int
						get() = state.firstVisibleItemScrollOffset
				}
			}

			AlbumViewMode.Grid -> {
				val state = rememberLazyStaggeredGridState()

				LazyVerticalStaggeredGrid(
					state = state,
					columns = StaggeredGridCells.Fixed(prefs.albumGridColumns.value.roundToInt()),
					modifier = Modifier
                        .fillMaxSize()
                        .background(theme.postCard.listBackgroundColor),
					contentPadding = contentPadding,
					verticalItemSpacing = 8.dp,
					horizontalArrangement = Arrangement.spacedBy(8.dp),
				) {
					item(span = StaggeredGridItemSpan.FullLine) {
						head()
					}

					items(count = album.images.size, key = { it }) {
						Box {
							NetImage(
								image = album.images[it].run {
									bigSquare ?: preview ?: original
								},
								cropToAspect = 1f.takeIf { prefs.albumGridCropToSquare.value }
							)
						}
					}
				}

				object : TopBarScrollObservable {
					override val firstVisibleItemIndex: Int
						get() = state.firstVisibleItemIndex
					override val firstVisibleItemOffset: Int
						get() = state.firstVisibleItemScrollOffset
				}
			}
		}

		val insets = WindowInsets.systemBars.asPaddingValues().override(bottom = 0.dp)

		val topBarShadow by with(LocalDensity.current) {
			remember(scrollState) {
				derivedStateOf {
					if (scrollState.firstVisibleItemIndex > 0) {
						10.dp
					} else {
						min(10f, scrollState.firstVisibleItemOffset.toDp().value / 10f).dp
					}
				}
			}
		}

		// Top bar
		Row(
			modifier = Modifier
                .shadow(topBarShadow)
                .background(theme.postCard.listBackgroundColor)
                .padding(insets)
                .height(topBarHeight)
                .fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.End
		) {
			AlbumSettingsButton()
		}
	}
}

@Composable
fun AlbumSettingsButton(
	modifier: Modifier = Modifier
) {
	val prefs = LocalComposePrefs.current
	val theme = LocalComposeTheme.current

	Box(modifier = modifier) {

		val settingsMenuState = rememberRRDropdownMenuState()

		RRIconButton(
			onClick = { settingsMenuState.expanded = true },
			icon = R.drawable.ic_settings_dark,
			contentDescription = R.string.options_settings,
		)

		RRDropdownMenu(state = settingsMenuState) {

			ItemPrefRadio(pref = prefs.albumViewMode) {
				Option(
					value = AlbumViewMode.Cards,
					text = "Card view",
					icon = R.drawable.cards_variant,
				)
				Option(
					value = AlbumViewMode.List,
					text = "List view",
					icon = R.drawable.view_list,
				)
				Option(
					value = AlbumViewMode.Grid,
					text = "Grid view",
					icon = R.drawable.view_grid,
				)
			}

			ItemDivider()

			when (prefs.albumViewMode.value) {
				AlbumViewMode.Cards -> {
					ItemPrefBool(
						text = "Buttons on cards",
						pref = prefs.albumCardShowButtons
					)
				}

				AlbumViewMode.List -> {
					ItemPrefBool(
						text = "Show thumbnails",
						pref = prefs.albumListShowThumbnails
					)
				}

				AlbumViewMode.Grid -> {
					ItemPrefBool(
						text = "Crop to square",
						pref = prefs.albumGridCropToSquare
					)

					ItemDivider()

					DropdownMenuItem(
						onClick = {},
						enabled = false,
						text = {
							Column(
                                Modifier
                                    .fillMaxWidth()
									.padding(6.dp)
                                    .sizeIn(
                                        minWidth = 112.dp,
                                        maxWidth = 280.dp,
                                        minHeight = 48.dp
                                    )
							) {
								Spacer(Modifier.height(6.dp))

								Text(
									text = "Columns",
									style = theme.dropdownMenu.text
								)

								Slider(
									modifier = Modifier
                                        .width(200.dp)
                                        .height(40.dp),
									value = prefs.albumGridColumns.value,
									onValueChange = { prefs.albumGridColumns.value = it },
									valueRange = 2f..5f,
									steps = (5 - 2) - 1,
								)
							}
						}
					)
				}
			}


			ItemDivider()

			Item(
				text = "All settings...",
				icon = R.drawable.ic_settings_dark,
				onClick = { /*TODO*/ },
			)
		}
	}
}

class OverridePaddingValues(
	private val parent: PaddingValues,
	private val top: Dp? = null,
	private val bottom: Dp? = null,
	private val left: Dp? = null,
	private val right: Dp? = null,
) : PaddingValues {
	override fun calculateBottomPadding() = bottom ?: parent.calculateBottomPadding()

	override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
		left ?: parent.calculateLeftPadding(layoutDirection)

	override fun calculateRightPadding(layoutDirection: LayoutDirection) =
		right ?: parent.calculateRightPadding(layoutDirection)

	override fun calculateTopPadding() = top ?: parent.calculateTopPadding()
}

fun PaddingValues.override(
	top: Dp? = null,
	bottom: Dp? = null,
	left: Dp? = null,
	right: Dp? = null,
) = OverridePaddingValues(
	parent = this,
	top = top,
	bottom = bottom,
	left = left,
	right = right
)

@Stable
interface TopBarScrollObservable {
	val firstVisibleItemIndex: Int
	val firstVisibleItemOffset: Int
}
