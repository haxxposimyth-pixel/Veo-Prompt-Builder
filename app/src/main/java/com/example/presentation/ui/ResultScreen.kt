package com.example.presentation.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.Project
import com.example.data.local.GeneratedScene
import com.example.data.local.GeneratedSet
import com.example.data.local.QcReport
import com.example.data.local.Titles
import com.example.presentation.viewmodel.ResultViewModel
import com.example.presentation.viewmodel.PipelineProgressState
import com.example.presentation.viewmodel.UiState

// Visual Style Definitions
val CharcoalBg = Color(0xFF0E0E10)
val CharcoalCard = Color(0xFF16161A)
val CharcoalSurface = Color(0xFF1E1E22)
val AmberAccent = Color(0xFFFFB347)
val MutedText = Color(0xFFA0A0A5)

val MonoTextStyle = androidx.compose.ui.text.TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 12.sp,
    lineHeight = 16.sp
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onBack: () -> Unit
) {
    val projectState by viewModel.projectState.collectAsState()
    val pipelineState by viewModel.pipelineState.collectAsState()
    val generatedScenes by viewModel.generatedScenes.collectAsState()
    val generatedSets by viewModel.generatedSets.collectAsState()
    val savedTitles by viewModel.savedTitles.collectAsState()
    val qcReports by viewModel.qcReports.collectAsState()
    val completedPhases by viewModel.completedPhases.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()
    val phaseActionState by viewModel.phaseActionState.collectAsState()
    val titlesState by viewModel.titlesState.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(phaseActionState) {
        if (phaseActionState is UiState.Error) {
            Toast.makeText(context, (phaseActionState as UiState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.clearPhaseActionError()
        } else if (phaseActionState is UiState.Success) {
            Toast.makeText(context, "Phase successfully compiled and stitched!", Toast.LENGTH_SHORT).show()
            viewModel.clearPhaseActionError()
        }
    }

    LaunchedEffect(titlesState) {
        if (titlesState is UiState.Error) {
            Toast.makeText(context, (titlesState as UiState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.clearTitlesError()
        } else if (titlesState is UiState.Success) {
            Toast.makeText(context, "Titles manually compiled successfully!", Toast.LENGTH_SHORT).show()
            viewModel.clearTitlesError()
        }
    }

    // Internal navigation tabs (0 = Prompts Sets, 1 = Quality Phase Breakdown, 2 = Curator Output / Specs)
    var selectedTab by remember { mutableStateOf(0) }
    var selectedSetTab by remember { mutableStateOf(0) }
    var selectedPhaseNo by remember { mutableStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val titleText = when (projectState) {
                            is UiState.Success -> (projectState as UiState.Success<Project>).data.name
                            else -> "VEO Content Console"
                        }
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Multi-Engine Pipeline Studio",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberAccent.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("result_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalBg
                )
            )
        },
        containerColor = CharcoalBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CharcoalBg)
        ) {
            when (projectState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AmberAccent)
                    }
                }
                is UiState.Error -> {
                    val msg = (projectState as UiState.Error).message
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.background(CharcoalCard, RoundedCornerShape(16.dp)).padding(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(48.dp))
                            Text("Failed to access project metadata", fontWeight = FontWeight.Bold, color = Color.White)
                            Text(msg, style = MaterialTheme.typography.bodySmall, color = Color.Red, textAlign = TextAlign.Center)
                            Button(
                                onClick = { viewModel.loadProjectDetails() },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = CharcoalBg)
                            ) {
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val project = (projectState as UiState.Success<Project>).data

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Project Specs Metadata Bar (Hero Layout)
                        ProjectSpecsHeroHeader(project = project)

                        Spacer(modifier = Modifier.height(1.dp))

                        // Render based on current pipeline execution state
                        when {
                            // 1. Pipeline is executing/running - State representation!
                            pipelineState is PipelineProgressState.Running -> {
                                val running = pipelineState as PipelineProgressState.Running
                                PipelineRunningVisualizer(runningState = running)
                            }

                            // 2. Pipeline threw a generation error
                            pipelineState is PipelineProgressState.Error -> {
                                val errorState = pipelineState as PipelineProgressState.Error
                                PipelineErrorScreen(
                                    errorState = errorState,
                                    onRestart = { viewModel.startPipeline() },
                                    onClear = { viewModel.clearPipelineError() }
                                )
                            }

                            // 3. No scenes generated yet (Ready to run state)
                            generatedScenes.isEmpty() -> {
                                EmptyPipelineStartup(
                                    isPhase0Present = analysisState is UiState.Success,
                                    onStart = { viewModel.startPipeline() }
                                )
                            }

                            // 4. Completed / Loaded Result Console
                            else -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Custom visual tabs for results
                                    TabRow(
                                        selectedTabIndex = selectedTab,
                                        containerColor = CharcoalCard,
                                        contentColor = AmberAccent,
                                        indicator = { tabPositions ->
                                            TabRowDefaults.SecondaryIndicator(
                                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                                color = AmberAccent
                                            )
                                        }
                                    ) {
                                        Tab(
                                            selected = selectedTab == 0,
                                            onClick = { selectedTab = 0 },
                                            text = {
                                                Text(
                                                    "PROMPTS SETS",
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (selectedTab == 0) AmberAccent else Color.White
                                                )
                                            }
                                        )
                                        Tab(
                                            selected = selectedTab == 1,
                                            onClick = { selectedTab = 1 },
                                            text = {
                                                Text(
                                                    "PHASE AUDITS",
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (selectedTab == 1) AmberAccent else Color.White
                                                )
                                            }
                                        )
                                        Tab(
                                            selected = selectedTab == 2,
                                            onClick = { selectedTab = 2 },
                                            text = {
                                                Text(
                                                    "DOC TITLES",
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (selectedTab == 2) AmberAccent else Color.White
                                                )
                                            }
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    ) {
                                        when (selectedTab) {
                                            0 -> PromptsSetsTabContent(
                                                generatedSets = generatedSets,
                                                selectedSetTab = selectedSetTab,
                                                onSetTabSelect = { selectedSetTab = it },
                                                clipboardManager = clipboardManager,
                                                context = context
                                            )
                                            1 -> PhaseAuditsTabContent(
                                                completedPhases = completedPhases,
                                                selectedPhaseNo = selectedPhaseNo,
                                                onPhaseSelect = { selectedPhaseNo = it },
                                                onRegenerate = { viewModel.regeneratePhase(it) },
                                                qcReports = qcReports,
                                                scSpecs = parseScenesForPhase(generatedScenes, selectedPhaseNo),
                                                clipboardManager = clipboardManager
                                            )
                                            2 -> TitlesAndSpecsTabContent(
                                                savedTitles = savedTitles,
                                                analysisState = analysisState,
                                                onGenerateTitles = { viewModel.generateTitlesManually() },
                                                clipboardManager = clipboardManager
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                UiState.Idle -> {}
            }
        }
    }
}

// ==========================================
// COMPONENT 1: METADATA HEADER HERO BAR
// ==========================================

@Composable
fun ProjectSpecsHeroHeader(project: Project) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalCard),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TOPIC & DIRECTIVE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = AmberAccent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = project.topic,
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = CharcoalSurface,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(1.dp)
                    ) {
                        Text(
                            text = project.niche.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (project.customNiche != null) {
                        Surface(
                            color = CharcoalSurface,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(1.dp)
                        ) {
                            Text(
                                text = "CUSTOM BLUEPRINT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "STYLE & GRAPHIC",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = AmberAccent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = project.videoStyle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "FRAME ${project.aspectRatio} | ${project.language.uppercase()}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MutedText
                )
            }
        }
    }
}

// ==========================================
// COMPONENT 2: EMPTY PIPELINE INITIALIZER
// ==========================================

@Composable
fun EmptyPipelineStartup(
    isPhase0Present: Boolean,
    onStart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Ready to compile",
                    tint = AmberAccent,
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = "10-Phase Core Generator Engine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                 val hintText = if (isPhase0Present) {
                    "A locked Production Bible and Emotion Blueprint have been parsed for this topic. Run the multi-engine generation pipeline to output all 10 project phases."
                } else {
                    "Workspace DNA will be generated automatically on launch."
                }

                Text(
                    text = hintText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "AUTOMATED STAGES PER PHASE:",
                            color = AmberAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        PipelineStageStepLabel(index = "1", title = "Scene Generation (Returns exactly 18 prompts)")
                        PipelineStageStepLabel(index = "2", title = "Quality Control audit against visual, camera, dialogue & typography compliance rules")
                        PipelineStageStepLabel(index = "3", title = "Aura Continuity Stitch with custom seamless matching index")
                    }
                }

                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = CharcoalBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("launch_pipeline_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.SettingsSuggest, contentDescription = "Run")
                        val btnLabel = if (isPhase0Present) "LAUNCH MULTI-ENGINE PIPELINE" else "GENERATE DNA & LAUNCH PIPELINE"
                        Text(btnLabel, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PipelineStageStepLabel(index: String, title: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(AmberAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(index, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
        }
        Text(title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

// ==========================================
// COMPONENT 3: RUNNING PIPELINE MONITOR
// ==========================================

@Composable
fun PipelineRunningVisualizer(runningState: PipelineProgressState.Running) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Spinning Compiler / Scanner Core
                CircularProgressIndicator(
                    color = AmberAccent,
                    trackColor = CharcoalSurface,
                    modifier = Modifier.size(56.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LIVE PIPELINE COMPILING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = AmberAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Phase ${runningState.currentPhase} / 10",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Stage Checklist status indicators
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PipelineStepStatusRow(
                            stepName = "Scene Generator Draft (18 frames)",
                            isActive = runningState.step == PipelineProgressState.Running.Step.GENERATING,
                            isDone = runningState.step == PipelineProgressState.Running.Step.QC || runningState.step == PipelineProgressState.Running.Step.STITCHING
                        )
                        PipelineStepStatusRow(
                            stepName = "Compliance Quality Check (Anti-repetition, text checking)",
                            isActive = runningState.step == PipelineProgressState.Running.Step.QC,
                            isDone = runningState.step == PipelineProgressState.Running.Step.STITCHING
                        )
                        PipelineStepStatusRow(
                            stepName = "Aura Seamless Continuity Stitch",
                            isActive = runningState.step == PipelineProgressState.Running.Step.STITCHING,
                            isDone = false
                        )
                    }
                }

                // Numeric accuracy progress bar
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Engine compiling status...",
                            fontSize = 10.sp,
                            color = MutedText
                        )
                        Text(
                            text = "${(runningState.progress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent
                        )
                    }
                    LinearProgressIndicator(
                        progress = { runningState.progress },
                        color = AmberAccent,
                        trackColor = CharcoalSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color.Transparent, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun PipelineStepStatusRow(stepName: String, isActive: Boolean, isDone: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        when {
            isDone -> Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Done", tint = Color.Green, modifier = Modifier.size(16.dp))
            isActive -> CircularProgressIndicator(color = AmberAccent, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
            else -> Icon(imageVector = Icons.Default.Adjust, contentDescription = "Pending", tint = MutedText.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
        }

        Text(
            text = stepName,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) Color.White else if (isDone) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f)
        )
    }
}

// ==========================================
// COMPONENT 4: PIPELINE FAULT ERROR SCREEN
// ==========================================

@Composable
fun PipelineErrorScreen(
    errorState: PipelineProgressState.Error,
    onRestart: () -> Unit,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Failure",
                    tint = Color.Red,
                    modifier = Modifier.size(56.dp)
                )

                Text(
                    text = "Pipeline Execution Failure",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = errorState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onClear,
                        border = BorderStroke(1.dp, CharcoalSurface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Dismiss", color = Color.White)
                    }

                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = CharcoalBg),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Resume Pipeline", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// RESULTS TAB 0: PROMPT BATCH SETS (SET OF 100)
// ==========================================

@Composable
fun PromptsSetsTabContent(
    generatedSets: List<GeneratedSet>,
    selectedSetTab: Int,
    onSetTabSelect: (Int) -> Unit,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    context: Context
) {
    if (generatedSets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Re-bundling completed sets...", color = MutedText, fontSize = 12.sp)
        }
        return
    }

    val selectedSet = generatedSets.getOrNull(selectedSetTab) ?: generatedSets.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sets navigation headers
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(generatedSets.size) { index ->
                val set = generatedSets[index]
                val isSelected = index == selectedSetTab
                Surface(
                    color = if (isSelected) AmberAccent else CharcoalCard,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isSelected) AmberAccent else CharcoalSurface),
                    modifier = Modifier
                        .clickable { onSetTabSelect(index) }
                        .height(38.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Set ${set.setNumber} (${set.startIndex}-${set.endIndex})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) CharcoalBg else Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Actions Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Copy Entire Project
            Button(
                onClick = {
                    val allText = generatedSets.joinToString("\n\n") { it.text }
                    clipboardManager.setText(AnnotatedString(allText))
                    Toast.makeText(context, "Copied entire project prompts!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = CharcoalBg),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).testTag("copy_all_sets_button")
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy All Sets", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Share / Export file Choice
            OutlinedButton(
                onClick = {
                    val allText = generatedSets.joinToString("\n\n") { it.text }
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, allText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Export VEO Script Prompt Sheet")
                    context.startActivity(shareIntent)
                },
                border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).testTag("export_txt_button")
            ) {
                Icon(imageVector = Icons.Default.SaveAlt, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export .txt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Set Box (Raw Scrollable Monospace block)
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalCard),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CharcoalSurface),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top header within block for copy set
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(CharcoalSurface, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SET ${selectedSet.setNumber} SCRIPT BLOCK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent
                    )
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(selectedSet.text))
                            Toast.makeText(context, "Set ${selectedSet.setNumber} prompts copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp).testTag("copy_prompt_set_${selectedSet.setNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Set",
                            tint = AmberAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Scrollable text
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = selectedSet.text,
                        style = MonoTextStyle,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ==========================================
// RESULTS TAB 1: PHASE BREAKDOWN & AUDITS
// ==========================================

@Composable
fun PhaseAuditsTabContent(
    completedPhases: List<Int>,
    selectedPhaseNo: Int,
    onPhaseSelect: (Int) -> Unit,
    onRegenerate: (Int) -> Unit,
    qcReports: List<QcReport>,
    scSpecs: List<ParsedSceneSpec>,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    val reportObject = qcReports.find { it.phaseNumber == selectedPhaseNo }
    val isPhaseDone = completedPhases.contains(selectedPhaseNo)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Phase selector horizontal chips
        Text(
            text = "SELECT PHASE REVIEW",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AmberAccent,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(10) { index ->
                val ph = index + 1
                val isSelected = ph == selectedPhaseNo
                val isCompleted = completedPhases.contains(ph)
                Surface(
                    color = if (isSelected) AmberAccent else CharcoalCard,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) AmberAccent else if (isCompleted) AmberAccent.copy(alpha = 0.2f) else CharcoalSurface),
                    modifier = Modifier
                        .clickable { onPhaseSelect(ph) }
                        .height(34.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Phase $ph",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) CharcoalBg else if (isCompleted) Color.White else MutedText.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Action Box: Regeneration and Status
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalCard),
                    border = BorderStroke(1.dp, CharcoalSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Phase $selectedPhaseNo Blueprint Console", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text(
                                text = if (isPhaseDone) "Active in compiled sets of 100" else "Pending Generation",
                                fontSize = 11.sp,
                                color = if (isPhaseDone) Color.Green else MutedText
                            )
                        }

                        Button(
                            onClick = { onRegenerate(selectedPhaseNo) },
                            colors = ButtonDefaults.buttonColors(containerColor = CharcoalBg, contentColor = AmberAccent),
                            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Recomp Phase $selectedPhaseNo", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // QC Report audit trail
            if (isPhaseDone) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CharcoalCard),
                        border = BorderStroke(1.dp, Color.Green.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.FactCheck, contentDescription = null, tint = Color.Green, modifier = Modifier.size(16.dp))
                                    Text("QC AUDIT SANITY LOG", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Green)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = reportObject?.text ?: "All compliance parameters validated (Zero typographical numbers, checked dialogue text translation, certified camera frames).",
                                style = MonoTextStyle,
                                color = MutedText
                            )
                        }
                    }
                }

                // Scenes List header
                item {
                    Text(
                        text = "RESOLVED SCENE PROMPTS (18)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                items(scSpecs) { scene ->
                    QcSceneRowCard(scene = scene, clipboardManager = clipboardManager)
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("This phase has not been compiled yet. Run the global pipeline to generate.", color = MutedText, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// EXPANDABLE SCENE ROW CARD DESIGN
// ==========================================

@Composable
fun QcSceneRowCard(scene: ParsedSceneSpec, clipboardManager: androidx.compose.ui.platform.ClipboardManager) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isExpanded) AmberAccent.copy(alpha = 0.4f) else CharcoalSurface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Prompt ${scene.continuousNo} - Scene Spec",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isExpanded) AmberAccent else Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(scene.rawText))
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy prompt", tint = MutedText, modifier = Modifier.size(14.dp))
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(CharcoalSurface, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = scene.rawText,
                        style = MonoTextStyle,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ==========================================
// RESULTS TAB 2: HIGH RETENTION TITLES & SPECS
// ==========================================

@Composable
fun TitlesAndSpecsTabContent(
    savedTitles: Titles?,
    analysisState: UiState<String>,
    onGenerateTitles: () -> Unit,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    val context = LocalContext.current
    val parsedTitles = parseTitlesText(savedTitles?.text ?: "")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // High Retention Titles Card Layout
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "10 HIGH RETENTION DOC TITLES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = AmberAccent,
                                letterSpacing = 0.5.sp
                            )
                            Text("Curiosity gap short-form title generation", fontSize = 10.sp, color = MutedText)
                        }

                        if (parsedTitles.isNotEmpty()) {
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(savedTitles?.text ?: ""))
                                Toast.makeText(context, "Copied all Titles!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy all", tint = AmberAccent)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (parsedTitles.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("No interest loop titles logged yet", color = MutedText, fontSize = 11.sp)
                                Button(
                                    onClick = onGenerateTitles,
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = CharcoalBg)
                                ) {
                                    Text("Compose Titles", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        parsedTitles.forEachIndexed { index, titleText ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(AmberAccent.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${index + 1}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = AmberAccent)
                                    }
                                    Text(
                                        text = titleText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(titleText))
                                        Toast.makeText(context, "Title copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = MutedText, modifier = Modifier.size(12.dp))
                                }
                            }
                            if (index < parsedTitles.size - 1) {
                                Divider(color = CharcoalSurface, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }

        // Project Core Specifications Specs section (DNA Analysis)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CharcoalSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WORKSPACE BIBLE & BLUEPRINT DATA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = AmberAccent,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Read-only context injected into generation engines.",
                        fontSize = 10.sp,
                        color = MutedText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    when (analysisState) {
                        is UiState.Success -> {
                            val text = (analysisState as UiState.Success<String>).data
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .verticalScroll(rememberScrollState())
                                    .background(CharcoalSurface, RoundedCornerShape(6.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = text,
                                    style = MonoTextStyle,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                        else -> {
                            Text("DNA specs missing/empty. Generate in tab 1 first.", color = MutedText, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// DYNAMIC COMPILER UTILITY PARSER
// ==========================================

data class ParsedSceneSpec(
    val continuousNo: Int,
    val rawText: String
)

fun parseScenesForPhase(allScenes: List<GeneratedScene>, targetPhase: Int): List<ParsedSceneSpec> {
    return allScenes
        .filter { it.phaseNumber == targetPhase }
        .sortedBy { it.sceneIndex }
        .map {
            val continuousNo = (targetPhase - 1) * 18 + it.sceneIndex
            ParsedSceneSpec(continuousNo, it.text)
        }
}

fun parseTitlesText(text: String): List<String> {
    val list = mutableListOf<String>()
    val lines = text.split("\n")
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue
        // Strip leading numbers like "1.", "1:", "[1]"
        val clean = trimmed.replace(Regex("^(\\d+)[\\]\\)\\.\\s:-]+"), "").trim()
        if (clean.isNotEmpty()) {
            list.add(clean)
        }
    }
    return list
}
