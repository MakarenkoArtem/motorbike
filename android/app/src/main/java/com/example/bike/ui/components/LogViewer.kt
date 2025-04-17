package com.example.bike.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun LogViewer(logs: List<String>, modifier: Modifier = Modifier
    .fillMaxSize()
    .padding(16.dp)) {
    Column(modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(logs) {log ->
                Text(log, modifier = Modifier.padding(4.dp))
            }
        }
    }
}