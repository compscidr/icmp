package com.jasonernst.icmp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jasonernst.icmp.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(vm: MainScreenViewModel = viewModel()) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                }
            )
        }
    ) { scaffoldPadding ->
        var hostValue by remember { mutableStateOf(vm.hostField.value) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text(text = "Host") },
                value = hostValue,
                onValueChange = {
                    hostValue = it
                    vm.onHostFieldChanged(it)
                },
                maxLines = 1
            )
            val stats = vm.stats.collectAsStateWithLifecycle(MainScreenViewModel.Stats("8.8.8.8", "8.8.8.8", 0, results = emptyList())).value
            StatsBar(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
                stats = stats)
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(stats.results.size) { index ->
                    ResultItem(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .animateItem(),
                        item = stats.results[index])
                }
            }
        }
    }
}

@Composable
private fun StatsBar(
    modifier: Modifier,
    stats: MainScreenViewModel.Stats
) {
    Column(
        modifier = modifier
    ) {
        if (stats.error == null) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "${stats.host} (${stats.ip})",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stats.pingMs?.let { "$it ms" } ?: "N/A",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            AnimatedVisibility(visible = stats.pingMs != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "min/avg/max = /${stats.minPingMS}/${stats.avgPingMS}/${stats.maxPingMS} ms",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "statistics:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${stats.packetsSent} packets transmitted, ${stats.packetsReceived} packets received, ${(stats.packetsLost * 100)} packets lost",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Text(
                text = "${stats.error}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ResultItem(
    modifier: Modifier = Modifier,
    item: MainScreenViewModel.ResultItem
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = item.num.toString(),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            modifier = Modifier.weight(1f),
            text = item.message,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = item.ms?.let { "$it ms" } ?: "",
            style = MaterialTheme.typography.bodySmall
        )
    }

}