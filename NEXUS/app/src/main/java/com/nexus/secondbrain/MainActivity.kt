package com.nexus.secondbrain

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.secondbrain.data.MemoryEntity
import com.nexus.secondbrain.data.NexusDatabase
import kotlinx.coroutines.launch

private val Background = Color(0xFF070B16)
private val Panel = Color(0xFF10182A)
private val PanelLight = Color(0xFF17233A)
private val Cyan = Color(0xFF5CEBFF)
private val Violet = Color(0xFF9D8CFF)
private val MainText = Color(0xFFF2F6FF)
private val Muted = Color(0xFF9AA9C2)
private val Border = Color(0xFF263A55)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { NexusApp(applicationContext) } }
    }
}

@Composable
private fun NexusApp(context: Context) {
    val database = remember { NexusDatabase.getInstance(context) }
    val notes by database.memoryDao().observeAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf("Home") }
    var query by remember { mutableStateOf("") }
    var showCapture by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteBody by remember { mutableStateOf("") }
    var chatInput by remember { mutableStateOf("") }
    var chatReply by remember { mutableStateOf("Your local memory is ready. Gemini is not connected yet.") }

    Surface(color = Background, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().navigationBarsPadding()) {
            TopBar()
            when (selectedTab) {
                "Home" -> HomeScreen(
                    notes = notes,
                    onCapture = { showCapture = true },
                    onOpenMemory = { selectedTab = "Memory" },
                    onOpenChat = { selectedTab = "Chat" }
                )
                "Memory" -> MemoryScreen(
                    notes = notes,
                    query = query,
                    onQuery = { query = it },
                    onCapture = { showCapture = true },
                    onDelete = { note -> scope.launch { database.memoryDao().delete(note) } }
                )
                "Chat" -> ChatScreen(
                    input = chatInput,
                    reply = chatReply,
                    onInput = { chatInput = it },
                    onSend = {
                        if (chatInput.isNotBlank()) {
                            chatReply = "This prototype has not sent your message to an AI service. Gemini integration is the next milestone."
                            chatInput = ""
                        }
                    }
                )
                else -> ActionsScreen()
            }
            BottomBar(selected = selectedTab, onSelect = { selectedTab = it })
        }
    }

    if (showCapture) {
        AlertDialog(
            onDismissRequest = { showCapture = false },
            containerColor = Panel,
            title = { Text("Capture a memory", color = MainText, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DarkField(noteTitle, { noteTitle = it }, "Title")
                    DarkField(noteBody, { noteBody = it }, "Write a note, idea or decision…", singleLine = false)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank() || noteBody.isNotBlank()) {
                            scope.launch {
                                database.memoryDao().insert(
                                    MemoryEntity(
                                        title = noteTitle.trim().ifBlank { "Untitled memory" },
                                        body = noteBody.trim()
                                    )
                                )
                            }
                            noteTitle = ""
                            noteBody = ""
                            showCapture = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Background)
                ) { Text("Save locally", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showCapture = false }) { Text("Cancel", color = Muted) } }
        )
    }
}

@Composable
private fun TopBar() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(42.dp).background(Brush.linearGradient(listOf(Cyan, Violet)), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Background, modifier = Modifier.size(23.dp))
            }
            Column {
                Text("N E X U S", color = MainText, fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Text("PERSONAL INTELLIGENCE", color = Muted, fontSize = 9.sp, letterSpacing = 1.5.sp)
            }
        }
        Icon(Icons.Default.Tune, contentDescription = "Preferences", tint = Cyan, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ColumnScope.HomeScreen(
    notes: List<MemoryEntity>,
    onCapture: () -> Unit,
    onOpenMemory: () -> Unit,
    onOpenChat: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("YOUR SECOND BRAIN", color = Cyan, fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                Text("Think clearly.\nRemember everything.", color = MainText, fontSize = 29.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold)
                Text("A private workspace for your ideas, plans and knowledge.", color = Muted, fontSize = 13.sp)
            }
        }
        item {
            Column(
                Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF142B43), Color(0xFF211A40), Panel)), RoundedCornerShape(22.dp))
                    .border(1.dp, Border, RoundedCornerShape(22.dp)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Cyan)
                    Spacer(Modifier.width(8.dp))
                    Text("NEXUS CORE", color = MainText, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.weight(1f))
                    Text("MVP", color = Cyan, fontSize = 10.sp)
                }
                Text("Local memory online", color = MainText, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text("Your notes are saved in a local Room database. AI reasoning will be connected in a later stage.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onOpenChat, colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Background)) {
                        Icon(Icons.Default.ChatBubbleOutline, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(7.dp)); Text("Open chat")
                    }
                    TextButton(onClick = onCapture) { Icon(Icons.Default.Add, null, tint = Cyan); Text("Capture", color = Cyan) }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("MEMORY VAULT", color = MainText, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onOpenMemory) { Text("View all", color = Cyan) }
            }
        }
        if (notes.isEmpty()) item { EmptyMemoryCard(onCapture) }
        else items(notes.take(3), key = { it.id }) { note -> MemoryCard(note) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("MEMORIES", notes.size.toString(), Icons.Default.Memory, Modifier.weight(1f))
                StatCard("AI LINK", "Pending", Icons.Default.Bolt, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ColumnScope.MemoryScreen(
    notes: List<MemoryEntity>,
    query: String,
    onQuery: (String) -> Unit,
    onCapture: () -> Unit,
    onDelete: (MemoryEntity) -> Unit
) {
    Column(Modifier.weight(1f).padding(horizontal = 18.dp, vertical = 8.dp)) {
        Text("Memory vault", color = MainText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Saved thoughts, stored locally.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))
        Row(
            Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).border(1.dp, Border, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = Cyan)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f)) { DarkField(query, onQuery, "Search your memories") }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onCapture, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Background)) {
            Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Capture a memory")
        }
        Spacer(Modifier.height(12.dp))
        val filtered = notes.filter { "${it.title} ${it.body}".contains(query, ignoreCase = true) }
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(if (query.isBlank()) "Your memory vault is empty.\nCapture your first idea." else "No matching memories.", color = Muted, lineHeight = 22.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(filtered, key = { it.id }) { note ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) { MemoryCard(note) }
                        IconButton(onClick = { onDelete(note) }) { Icon(Icons.Default.DeleteOutline, "Delete memory", tint = Muted) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.ChatScreen(input: String, reply: String, onInput: (String) -> Unit, onSend: () -> Unit) {
    Column(Modifier.weight(1f).padding(horizontal = 18.dp, vertical = 8.dp)) {
        Text("NEXUS chat", color = MainText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Ask, plan and connect your ideas.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(20.dp))
        Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).border(1.dp, Border, RoundedCornerShape(18.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("NEXUS CORE", color = Cyan, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            Text(reply, color = MainText, fontSize = 14.sp, lineHeight = 21.sp)
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).border(1.dp, Border, RoundedCornerShape(18.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).padding(horizontal = 6.dp)) { DarkField(input, onInput, "Message NEXUS…") }
            IconButton(onClick = onSend) { Icon(Icons.Default.Send, "Send", tint = Cyan) }
        }
        Text("Gemini is not connected yet. Messages are not sent to an AI service.", color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp, start = 4.dp, bottom = 8.dp))
    }
}

@Composable
private fun ColumnScope.ActionsScreen() {
    Column(Modifier.weight(1f).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Action engine", color = MainText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Planned phone actions will require clear permissions and confirmation.", color = Muted, fontSize = 13.sp, lineHeight = 20.sp)
        ActionCard("Reminders", "Create and schedule personal reminders", "NEXT STAGE")
        ActionCard("Voice interface", "Speech input and spoken responses", "PLANNED")
        ActionCard("Privacy controls", "Manage memory, export and deletion", "PLANNED")
        Text("No device actions run from this prototype.", color = Cyan, fontSize = 11.sp)
    }
}

@Composable
private fun ActionCard(title: String, body: String, status: String) {
    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).border(1.dp, Border, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = MainText, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text(status, color = Cyan, fontSize = 9.sp, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(body, color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun MemoryCard(note: MemoryEntity) {
    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).border(1.dp, Border, RoundedCornerShape(16.dp)).padding(14.dp)) {
        Text(note.category, color = Cyan, fontSize = 9.sp, letterSpacing = 1.3.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(note.title, color = MainText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (note.body.isNotBlank()) {
            Spacer(Modifier.height(5.dp))
            Text(note.body, color = Muted, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun EmptyMemoryCard(onCapture: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).border(1.dp, Border, RoundedCornerShape(18.dp)).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Memory, null, tint = Violet, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(8.dp))
        Text("Nothing stored yet", color = MainText, fontWeight = FontWeight.SemiBold)
        Text("Capture a thought to begin building your second brain.", color = Muted, fontSize = 12.sp)
        TextButton(onClick = onCapture) { Text("Create first memory", color = Cyan) }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Column(modifier.background(Panel, RoundedCornerShape(16.dp)).border(1.dp, Border, RoundedCornerShape(16.dp)).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Cyan, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(value, color = MainText, fontSize = 19.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BottomBar(selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color(0xFF0A1020)).border(1.dp, Border).padding(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        NavItem("Home", Icons.Default.AutoAwesome, selected, onSelect)
        NavItem("Memory", Icons.Default.Memory, selected, onSelect)
        NavItem("Chat", Icons.Default.ChatBubbleOutline, selected, onSelect)
        NavItem("Actions", Icons.Default.Bolt, selected, onSelect)
    }
}

@Composable
private fun NavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: String, onSelect: (String) -> Unit) {
    val active = label == selected
    Column(Modifier.clickable { onSelect(label) }.background(if (active) PanelLight else Color.Transparent, RoundedCornerShape(12.dp)).padding(horizontal = 13.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = label, tint = if (active) Cyan else Muted, modifier = Modifier.size(20.dp))
        Text(label, color = if (active) Cyan else Muted, fontSize = 10.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun DarkField(value: String, onValueChange: (String) -> Unit, hint: String, singleLine: Boolean = true) {
    Box(Modifier.fillMaxWidth()) {
        if (value.isEmpty()) Text(hint, color = Muted, fontSize = 13.sp)
        BasicTextField(value = value, onValueChange = onValueChange, singleLine = singleLine, textStyle = androidx.compose.ui.text.TextStyle(color = MainText, fontSize = 13.sp), modifier = Modifier.fillMaxWidth())
    }
}
