package com.example.xoiandroidcodechallenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.xoiandroidcodechallenge.loader.ConcurrentResourceLoader
import com.example.xoiandroidcodechallenge.loader.FakeNetwork
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val uiScope = rememberCoroutineScope()
    var k by remember { mutableStateOf(2) }
    var url by remember { mutableStateOf("https://example.com/imageA") }
    var logs by remember { mutableStateOf(listOf<String>()) }
    val runningJobs = remember { mutableStateListOf<Job>() }
    val listState = rememberLazyListState()
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }
    var loader by remember(k) {
        mutableStateOf(ConcurrentResourceLoader(maxConcurrentRequests = k, fetcher = FakeNetwork::fetch))
    }

    val callerOptions = listOf(1, 3, 5, 10)
    var callerCount by remember { mutableStateOf(3) }
    var callerMenuExpanded by remember { mutableStateOf(false) }

    val distinctOptions = listOf(1, 2, 3)
    var distinctCount by remember { mutableStateOf(2) }
    var distinctMenuExpanded by remember { mutableStateOf(false) }

    fun log(msg: String) {
        val timestamp = timeFormat.format(Date())
        logs = (logs + "[$timestamp] $msg").takeLast(200)
    }

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Concurrent Resource Loader Harness", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = k.toString(), onValueChange = { it.toIntOrNull()?.let { v -> if (v >= 1) k = v } }, label = { Text("k (concurrency)") }, modifier = Modifier.width(140.dp), singleLine = true)
        }

        // NEW: row with two ExposedDropdownMenuBox controls
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Callers dropdown
            ExposedDropdownMenuBox(
                expanded = callerMenuExpanded,
                onExpandedChange = { callerMenuExpanded = !callerMenuExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = "$callerCount callers",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("# of callers") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = callerMenuExpanded,
                    onDismissRequest = { callerMenuExpanded = false }
                ) {
                    callerOptions.forEach { c ->
                        DropdownMenuItem(
                            text = { Text("$c callers") },
                            onClick = {
                                callerCount = c
                                callerMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Distinct URLs dropdown
            ExposedDropdownMenuBox(
                expanded = distinctMenuExpanded,
                onExpandedChange = { distinctMenuExpanded = !distinctMenuExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = "$distinctCount distinct",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Distinct URLs") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = distinctMenuExpanded,
                    onDismissRequest = { distinctMenuExpanded = false }
                ) {
                    distinctOptions.forEach { d ->
                        DropdownMenuItem(
                            text = { Text("$d distinct") },
                            onClick = {
                                distinctCount = d
                                distinctMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                // Use callerCount for SAME-URL dedup test
                repeat(callerCount) { i ->
                    val requestId = i + 1
                    val job = uiScope.launch {
                        log("Request: #$requestId → load($url)")
                        try {
                            val bytes = loader.load(url)
                            log("Response: #$requestId → success: ${bytes.size} bytes")
                        } catch (e: CancellationException) {
                            log("Response: #$requestId → cancelled")
                        } catch (e: Throwable) {
                            log("Response: #$requestId → error: ${e.message}")
                        }
                    }
                    runningJobs += job
                }
            }) { Text("Fetch $callerCount same URL") }

            Button(onClick = {
                // Generate `distinctCount` URLs, launch `callerCount` calls round-robin
                val urls = List(distinctCount) { i -> if (i == 0) url else "$url?d=$i" }
                repeat(callerCount) { i ->
                    val u = urls[i % urls.size]
                    val requestId = i + 1
                    val job = uiScope.launch {
                        log("Request: #$requestId → load($u)")
                        try {
                            val bytes = loader.load(u)
                            log("Response: #$requestId → success: ${bytes.size} bytes")
                        } catch (e: CancellationException) {
                            log("Response: #$requestId → cancelled")
                        } catch (e: Throwable) {
                            log("Response: #$requestId → error: ${e.message}")
                        }
                    }
                    runningJobs += job
                }
            }) { Text("Fetch $distinctCount distinct URL") }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                runningJobs.forEach { it.cancel() }
                runningJobs.clear()
                log("Cancelled all caller jobs")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Cancel all")
        }

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("Logs (latest 50):", fontWeight = FontWeight.SemiBold)
            TextButton(onClick = { logs = emptyList() }) {
                Text("Clear logs")
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            itemsIndexed(logs.takeLast(50)) { index, line ->
                val backgroundColor = if (index % 2 == 1) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.surface
                }

                val parts = line.split("] ", limit = 2)
                val timestamp = if (parts.size == 2) parts[0] + "]" else ""
                val message = if (parts.size == 2) parts[1] else line

                val annotatedMessage = buildAnnotatedString {
                    val requestMatch = Regex("""(Request: #\d+)""").find(message)
                    val responseMatch = Regex("""(Response: #\d+)""").find(message)

                    when {
                        requestMatch != null -> {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(requestMatch.value)
                            }
                            append(message.substring(requestMatch.range.last + 1))
                        }
                        responseMatch != null -> {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(responseMatch.value)
                            }
                            append(message.substring(responseMatch.range.last + 1))
                        }
                        else -> append(message)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    if (timestamp.isNotEmpty()) {
                        Text(
                            text = timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = annotatedMessage,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
