package com.example.presentation.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.viewmodel.GenerateViewModel
import com.example.presentation.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateScreen(
    viewModel: GenerateViewModel,
    onNavigateToResult: (Int) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToManageData: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val styles by viewModel.allStyles.collectAsState()
    val languages by viewModel.allLanguages.collectAsState()
    val creationState by viewModel.creationState.collectAsState()

    // Form Flows
    val selectedNiche by viewModel.selectedNiche.collectAsState()
    val customNiche by viewModel.customNiche.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val topic by viewModel.topic.collectAsState()
    val aspectRatio by viewModel.aspectRatio.collectAsState()

    // Local Dropdown Expanders
    var nicheExpanded by remember { mutableStateOf(false) }
    var styleExpanded by remember { mutableStateOf(false) }
    var langExpanded by remember { mutableStateOf(false) }

    // Dialog Expanders
    var showAddStyleDialog by remember { mutableStateOf(false) }
    var showAddLanguageDialog by remember { mutableStateOf(false) }

    val nicheList = listOf(
        "Health & Biology", "Money & Finance", "Mind & Psychology", 
        "Nature & Earth", "Space & Cosmos", "Animals & Wildlife", 
        "Civilization & History", "Business & Startups", "Custom"
    )

    // Trigger redirection on success
    LaunchedEffect(creationState) {
        if (creationState is UiState.Success) {
            val projectId = (creationState as UiState.Success<Int>).data
            viewModel.resetCreationState()
            onNavigateToResult(projectId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "VEO",
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Divider(
                            modifier = Modifier
                                .height(16.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Doc Builder",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToLibrary, modifier = Modifier.testTag("nav_library_button")) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "Script Library")
                    }
                    IconButton(onClick = onNavigateToManageData, modifier = Modifier.testTag("nav_manage_button")) {
                        Icon(imageVector = Icons.Default.Category, contentDescription = "Standards Manager")
                    }
                    IconButton(onClick = onNavigateToSettings, modifier = Modifier.testTag("nav_settings_button")) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Preferences")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Hero Pitch
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Documentary Generation Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Formulate scene sequences and product DNA frameworks powered by Gemini Vertex AI.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // NICHE DROPDOWN CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Category Niche", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box {
                            OutlinedTextField(
                                value = selectedNiche,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { nicheExpanded = true },
                                trailingIcon = {
                                    IconButton(onClick = { nicheExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, "Open niche dropdown")
                                    }
                                },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            DropdownMenu(
                                expanded = nicheExpanded,
                                onDismissRequest = { nicheExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                nicheList.forEach { nicheName ->
                                    DropdownMenuItem(
                                        text = { Text(nicheName) },
                                        onClick = {
                                            viewModel.selectedNiche.value = nicheName
                                            nicheExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = selectedNiche == "Custom",
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Text("Custom Niche Description", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = customNiche,
                                    onValueChange = { viewModel.customNiche.value = it },
                                    label = { Text("Describe the custom conversion DNA packs") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .testTag("custom_niche_input"),
                                    maxLines = 4,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }
                    }
                }

                // STYLE DROPDOWN CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Visual style", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            TextButton(onClick = { showAddStyleDialog = true }, modifier = Modifier.testTag("add_style_dialog_trigger")) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = "Add custom style", modifier = Modifier.size(14.dp))
                                    Text("Add style", fontSize = 12.sp)
                                }
                            }
                        }
                        Box {
                            OutlinedTextField(
                                value = selectedStyle,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { styleExpanded = true },
                                trailingIcon = {
                                    IconButton(onClick = { styleExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, "Open style dropdown")
                                    }
                                },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            DropdownMenu(
                                expanded = styleExpanded,
                                onDismissRequest = { styleExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                styles.forEach { style ->
                                    DropdownMenuItem(
                                        text = { Text(style.name) },
                                        onClick = {
                                            viewModel.selectedStyle.value = style.name
                                            styleExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // LANGUAGE DROPDOWN CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Narration Script Language", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            TextButton(onClick = { showAddLanguageDialog = true }, modifier = Modifier.testTag("add_language_dialog_trigger")) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = "Add language", modifier = Modifier.size(14.dp))
                                    Text("Add language", fontSize = 12.sp)
                                }
                            }
                        }
                        Box {
                            OutlinedTextField(
                                value = selectedLanguage,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { langExpanded = true },
                                trailingIcon = {
                                    IconButton(onClick = { langExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, "Open language dropdown")
                                    }
                                },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            DropdownMenu(
                                expanded = langExpanded,
                                onDismissRequest = { langExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                languages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang.name) },
                                        onClick = {
                                            viewModel.selectedLanguage.value = lang.name
                                            langExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // TOPIC / TITLE TEXT FIELD CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Doc Script Topic / Title", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = topic,
                            onValueChange = { viewModel.topic.value = it },
                            label = { Text("What is this cinematic sequence about?") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .testTag("topic_input_field"),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                // ASPECT RATIO CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Render Framing Aspect Ratio", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.aspectRatio.value = "16:9" },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("aspect_16_9"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (aspectRatio == "16:9") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (aspectRatio == "16:9") MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = if (aspectRatio == "16:9") null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.AspectRatio, contentDescription = "landscape")
                                    Text("16:9 (YouTube Video)")
                                }
                            }

                            Button(
                                onClick = { viewModel.aspectRatio.value = "9:16" },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("aspect_9_16"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (aspectRatio == "9:16") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (aspectRatio == "9:16") MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = if (aspectRatio == "9:16") null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Smartphone, contentDescription = "vertical")
                                    Text("9:16 (Shorts/Reels)")
                                }
                            }
                        }
                    }
                }

                // MAIN GENERATE CTA
                Button(
                    onClick = { viewModel.generateProject() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("primary_generate_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = creationState !is UiState.Loading
                ) {
                    if (creationState is UiState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating Workspace...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Sparkle")
                            Text("Initialize VEO Workspace", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }

                // Inline Errors
                if (creationState is UiState.Error) {
                    val errMsg = (creationState as UiState.Error).message
                    Text(
                        text = errMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // ==========================================
    // INLINE COMPACT DIALOGS
    // ==========================================

    if (showAddStyleDialog) {
        var styleName by remember { mutableStateOf("") }
        var styleDesc by remember { mutableStateOf("") }
        var styleErr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddStyleDialog = false },
            title = { Text("New Custom Visual Style") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Introduce standard instructions for camera rendering styles.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    OutlinedTextField(
                        value = styleName,
                        onValueChange = { styleName = it },
                        label = { Text("Style Name") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_style_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = styleDesc,
                        onValueChange = { styleDesc = it },
                        label = { Text("Seed Prompt Action") },
                        modifier = Modifier.fillMaxWidth().height(80.dp).testTag("dialog_style_desc_input"),
                        maxLines = 3
                    )
                    if (styleErr.isNotEmpty()) {
                        Text(styleErr, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (styleName.trim().isEmpty() || styleDesc.trim().isEmpty()) {
                            styleErr = "All values are required."
                            return@Button
                        }
                        viewModel.addCustomStyle(styleName.trim(), styleDesc.trim())
                        showAddStyleDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStyleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddLanguageDialog) {
        var langName by remember { mutableStateOf("") }
        var langErr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddLanguageDialog = false },
            title = { Text("New Custom Language") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Introduce representing languages for the narration engine.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    OutlinedTextField(
                        value = langName,
                        onValueChange = { langName = it },
                        label = { Text("Language Name") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_lang_name_input"),
                        singleLine = true
                    )
                    if (langErr.isNotEmpty()) {
                        Text(langErr, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (langName.trim().isEmpty()) {
                            langErr = "Language name is required."
                            return@Button
                        }
                        viewModel.addCustomLanguage(langName.trim())
                        showAddLanguageDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLanguageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
