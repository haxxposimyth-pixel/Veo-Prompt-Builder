package com.example.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CustomStyle
import com.example.data.local.Language
import com.example.presentation.viewmodel.GenerateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageDataScreen(
    viewModel: GenerateViewModel,
    onBack: () -> Unit
) {
    val styles by viewModel.allStyles.collectAsState()
    val languages by viewModel.allLanguages.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showAddStyleDialog by remember { mutableStateOf(false) }
    var showAddLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library Standards", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("manage_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Video Styles", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Narration Languages", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedTab == 0) {
                    StyleListSection(
                        styles = styles,
                        onDelete = viewModel::deleteStyle,
                        onAddClick = { showAddStyleDialog = true }
                    )
                } else {
                    LanguageListSection(
                        languages = languages,
                        onDelete = viewModel::deleteLanguage,
                        onAddClick = { showAddLanguageDialog = true }
                    )
                }
            }
        }
    }

    // ==========================================
    // DIALOGS
    // ==========================================

    if (showAddStyleDialog) {
        var styleName by remember { mutableStateOf("") }
        var styleDesc by remember { mutableStateOf("") }
        var styleErr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddStyleDialog = false },
            title = { Text("Add Custom Video Style") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Introduce a standard prompt style.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = styleName,
                        onValueChange = { styleName = it },
                        label = { Text("Style Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = styleDesc,
                        onValueChange = { styleDesc = it },
                        label = { Text("Prompt Instruction (Seed)") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        maxLines = 4
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
                            styleErr = "All fields are required."
                            return@Button
                        }
                        viewModel.addCustomStyle(styleName.trim(), styleDesc.trim())
                        showAddStyleDialog = false
                    }
                ) {
                    Text("Save Style")
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
            title = { Text("Add Custom Language") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Introduce representing narration scripts.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = langName,
                        onValueChange = { langName = it },
                        label = { Text("Language Name") },
                        modifier = Modifier.fillMaxWidth(),
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
                    Text("Save Language")
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

@Composable
fun StyleListSection(
    styles: List<CustomStyle>,
    onDelete: (CustomStyle) -> Unit,
    onAddClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (styles.isEmpty()) {
            Text("No styles configured.", modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(styles) { style ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(style.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    if (style.isBuiltIn) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("Built-In", fontSize = 10.sp) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    style.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            if (!style.isBuiltIn) {
                                IconButton(onClick = { onDelete(style) }, modifier = Modifier.testTag("delete_style_${style.name}")) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete custom style", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddClick,
            icon = { Icon(Icons.Default.Add, contentDescription = "Add Icon") },
            text = { Text("Standard Style") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).testTag("add_style_fab")
        )
    }
}

@Composable
fun LanguageListSection(
    languages: List<Language>,
    onDelete: (Language) -> Unit,
    onAddClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (languages.isEmpty()) {
            Text("No languages configured.", modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(languages) { lang ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp, 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(lang.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                if (lang.isBuiltIn) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Built-In", fontSize = 10.sp) }
                                    )
                                }
                            }
                            if (!lang.isBuiltIn) {
                                IconButton(onClick = { onDelete(lang) }, modifier = Modifier.testTag("delete_lang_${lang.name}")) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete custom language", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddClick,
            icon = { Icon(Icons.Default.Add, contentDescription = "Add Icon") },
            text = { Text("Standard Language") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).testTag("add_lang_fab")
        )
    }
}
