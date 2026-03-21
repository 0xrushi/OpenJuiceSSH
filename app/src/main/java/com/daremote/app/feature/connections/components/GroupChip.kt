package com.daremote.app.feature.connections.components

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.daremote.app.core.domain.model.ServerGroup

@Composable
fun GroupChip(
    group: ServerGroup,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(group.name) },
        modifier = modifier
    )
}
