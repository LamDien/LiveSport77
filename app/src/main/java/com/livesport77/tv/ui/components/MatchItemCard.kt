package com.livesport77.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.livesport77.tv.model.Match
import com.livesport77.tv.model.MatchStatus
import com.livesport77.tv.ui.theme.*

/**
 * Match Item Card redesigned strictly based on LiveScore.com mobile/TV UI.
 * Layout: [ Status | Home Name + Crest | Score | Crest + Away Name | Star ]
 */
@Composable
fun MatchItemCard(
    match: Match,
    isSelected: Boolean,
    isFavorite: Boolean = false,
    onFocused: () -> Unit,
    onSelect: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TvFocusCard(
        onClick = onSelect,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
        modifier = modifier.fillMaxWidth()
    ) { isFocused ->
        LaunchedEffect(isFocused) {
            if (isFocused) {
                onFocused()
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. LEFT: STATUS / TIME
            Box(
                modifier = Modifier.width(44.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val statusColor = when (match.status) {
                    MatchStatus.LIVE -> TvLiveRed
                    MatchStatus.FINISHED -> if (isFocused) TvFocusGold else Color(0xFF94A3B8)
                    MatchStatus.SCHEDULED -> TvTextMuted
                    MatchStatus.POSTPONED -> Color(0xFFEF4444)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    if (match.status == MatchStatus.LIVE) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(TvLiveRed)
                        )
                    }
                    Text(
                        text = match.statusDisplay,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            // VERTICAL SEPARATOR LINE (LEFT)
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(Color(0xFF283144))
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 2. CENTER: TEAMS & SCORES
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Home Team (Name + Logo)
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = match.homeTeam.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (isFocused) TvFocusGold else TvTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.End
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp)
                    )

                    TeamLogo(
                        logoUrl = match.homeTeam.logoUrl,
                        emoji = match.homeTeam.iconEmoji,
                        name = match.homeTeam.name
                    )
                }

                // Middle Score
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .widthIn(min = 34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (match.status == MatchStatus.SCHEDULED) {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TvTextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    } else {
                        Text(
                            text = "${match.score.home} - ${match.score.away}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = if (match.status == MatchStatus.LIVE) TvLiveRed else Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                // Away Team (Logo + Name)
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    TeamLogo(
                        logoUrl = match.awayTeam.logoUrl,
                        emoji = match.awayTeam.iconEmoji,
                        name = match.awayTeam.name
                    )

                    Text(
                        text = match.awayTeam.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (isFocused) TvFocusGold else TvTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Start
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // VERTICAL SEPARATOR LINE (RIGHT)
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(Color(0xFF283144))
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 3. RIGHT: STAR FAVORITE ICON
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFavorite) "★" else "☆",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (isFavorite) TvFocusGold else Color(0xFF64748B),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun TeamLogo(
    logoUrl: String?,
    emoji: String,
    name: String
) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(Color(0xFF232A3B)),
        contentAlignment = Alignment.Center
    ) {
        if (!logoUrl.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(logoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(19.dp)
                    .clip(CircleShape)
            )
        } else {
            Text(text = emoji, fontSize = 12.sp)
        }
    }
}
