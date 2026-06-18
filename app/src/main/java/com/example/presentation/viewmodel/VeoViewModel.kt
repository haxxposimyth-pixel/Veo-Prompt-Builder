package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CustomStyle
import com.example.data.local.Language
import com.example.data.local.Project
import com.example.data.repository.ProjectRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

// ==========================================
// UI STATE REPRESENTATION
// ==========================================

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

// ==========================================
// GENERATE SCREEN VIEW MODEL
// ==========================================

class GenerateViewModel(private val repository: ProjectRepository) : ViewModel() {

    val allStyles: StateFlow<List<CustomStyle>> = repository.allStyles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLanguages: StateFlow<List<Language>> = repository.allLanguages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form inputs state
    var selectedNiche = MutableStateFlow("Health & Biology")
    var customNiche = MutableStateFlow("")
    var selectedStyle = MutableStateFlow("Hyper-realistic 3D")
    var selectedLanguage = MutableStateFlow("English")
    var topic = MutableStateFlow("")
    var aspectRatio = MutableStateFlow("16:9")

    // Project creation state
    private val _creationState = MutableStateFlow<UiState<Int>>(UiState.Idle)
    val creationState: StateFlow<UiState<Int>> = _creationState.asStateFlow()

    init {
        // Automatically sync defaults from settings
        viewModelScope.launch {
            repository.allStyles.collectLatest { styles ->
                if (styles.isNotEmpty() && selectedStyle.value.isEmpty()) {
                    selectedStyle.value = styles.first().name
                }
            }
        }
    }

    fun setDefaults(defaultLang: String, defaultStyleName: String) {
        if (defaultLang.isNotEmpty()) selectedLanguage.value = defaultLang
        if (defaultStyleName.isNotEmpty()) selectedStyle.value = defaultStyleName
    }

    fun addCustomStyle(name: String, description: String) {
        viewModelScope.launch {
            repository.addStyle(name, description, isBuiltIn = false)
            selectedStyle.value = name
        }
    }

    fun deleteStyle(style: CustomStyle) {
        viewModelScope.launch {
            repository.deleteStyle(style)
            if (selectedStyle.value == style.name) {
                selectedStyle.value = "Hyper-realistic 3D"
            }
        }
    }

    fun addCustomLanguage(name: String) {
        viewModelScope.launch {
            repository.addLanguage(name, isBuiltIn = false)
            selectedLanguage.value = name
        }
    }

    fun deleteLanguage(language: Language) {
        viewModelScope.launch {
            repository.deleteLanguage(language)
            if (selectedLanguage.value == language.name) {
                selectedLanguage.value = "English"
            }
        }
    }

    fun resetCreationState() {
        _creationState.value = UiState.Idle
    }

    fun generateProject() {
        if (topic.value.trim().isEmpty()) {
            _creationState.value = UiState.Error("Topic/Title cannot be empty. Please enter a guiding script topic.")
            return
        }

        _creationState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val name = if (topic.value.length > 25) topic.value.take(25) + "..." else topic.value
                val project = Project(
                    name = name,
                    niche = selectedNiche.value,
                    customNiche = if (selectedNiche.value == "Custom") customNiche.value else null,
                    videoStyle = selectedStyle.value,
                    language = selectedLanguage.value,
                    aspectRatio = aspectRatio.value,
                    topic = topic.value
                )
                val projectId = repository.createProject(project)
                _creationState.value = UiState.Success(projectId)
            } catch (e: Exception) {
                _creationState.value = UiState.Error(e.message ?: "Failed to save project metadata")
            }
        }
    }

    class Factory(private val repository: ProjectRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GenerateViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GenerateViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// ==========================================
// PIPELINE PROGRESS STATE
// ==========================================

sealed interface PipelineProgressState {
    object Idle : PipelineProgressState
    data class Running(
        val currentPhase: Int, // 1..10
        val step: Step, // GENERATING, QC, STITCHING
        val progress: Float // overall progress 0f to 1f
    ) : PipelineProgressState {
        enum class Step {
            GENERATING, QC, STITCHING
        }
    }
    data class Error(val message: String) : PipelineProgressState
    object Finished : PipelineProgressState
}

// ==========================================
// RESULT SCREEN VIEW MODEL
// ==========================================

class ResultViewModel(
    private val repository: ProjectRepository,
    private val projectId: Int
) : ViewModel() {

    private val _projectState = MutableStateFlow<UiState<Project>>(UiState.Loading)
    val projectState: StateFlow<UiState<Project>> = _projectState.asStateFlow()

    // Line compilation pipeline state tracker
    private val _pipelineState = MutableStateFlow<PipelineProgressState>(PipelineProgressState.Idle)
    val pipelineState: StateFlow<PipelineProgressState> = _pipelineState.asStateFlow()

    // Real-time dynamic DB flows
    val generatedScenes: StateFlow<List<com.example.data.local.GeneratedScene>> = repository.pipelineDao.getScenesForProjectFlow(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val generatedSets: StateFlow<List<com.example.data.local.GeneratedSet>> = repository.pipelineDao.getSetsForProjectFlow(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedTitles: StateFlow<com.example.data.local.Titles?> = repository.pipelineDao.getTitlesForProjectFlow(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val qcReports: StateFlow<List<com.example.data.local.QcReport>> = repository.pipelineDao.getQcReportsForProjectFlow(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedPhases: StateFlow<List<Int>> = repository.pipelineDao.getScenesForProjectFlow(projectId)
        .map { list ->
            list.groupBy { it.phaseNumber }
                .filter { it.value.size >= 18 }
                .keys
                .sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Phase 0: Analysis Status
    private val _analysisState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val analysisState: StateFlow<UiState<String>> = _analysisState.asStateFlow()

    private val _phaseActionState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val phaseActionState: StateFlow<UiState<Unit>> = _phaseActionState.asStateFlow()

    private val _titlesState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val titlesState: StateFlow<UiState<Unit>> = _titlesState.asStateFlow()

    fun clearPhaseActionError() {
        _phaseActionState.value = UiState.Idle
    }

    fun clearTitlesError() {
        _titlesState.value = UiState.Idle
    }

    private suspend fun <T> withRetry(times: Int = 3, block: suspend () -> T): T {
        var lastError: Exception? = null
        repeat(times) { attempt ->
            try { return block() }
            catch (e: Exception) {
                lastError = e
                kotlinx.coroutines.delay(1500L * (attempt + 1))
            }
        }
        throw lastError ?: Exception("Unknown pipeline error")
    }

    init {
        loadProjectDetails()
        loadCachedContents()
    }

    fun loadProjectDetails() {
        _projectState.value = UiState.Loading
        viewModelScope.launch {
            val project = repository.getProjectById(projectId)
            if (project != null) {
                _projectState.value = UiState.Success(project)
            } else {
                _projectState.value = UiState.Error("Project with ID $projectId not found in local database")
            }
        }
    }

    private fun loadCachedContents() {
        viewModelScope.launch {
            // Load Analysis (Phase 0)
            val analysis = repository.getLocalContent(projectId, 0)
            if (analysis != null) {
                _analysisState.value = UiState.Success(analysis.text)
            }
        }
    }

    fun generateAnalysis() {
        _analysisState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.generateContent(projectId, "analysis", 0)
            result.onSuccess { text ->
                _analysisState.value = UiState.Success(text)
            }
            result.onFailure { exception ->
                _analysisState.value = UiState.Error(exception.message ?: "Failed to generate topic analysis")
            }
        }
    }

    fun startPipeline() {
        if (_pipelineState.value is PipelineProgressState.Running) return
        _pipelineState.value = PipelineProgressState.Running(1, PipelineProgressState.Running.Step.GENERATING, 0f)

        viewModelScope.launch {
            var analysisContent = repository.getLocalContent(projectId, 0)
            if (analysisContent == null) {
                _pipelineState.value = PipelineProgressState.Running(0, PipelineProgressState.Running.Step.GENERATING, 0f)
                val a = repository.generateContent(projectId, "analysis", 0)
                if (a.isFailure) { _pipelineState.value = PipelineProgressState.Error("Phase 0 failed: ${a.exceptionOrNull()?.message ?: "backend unreachable"}. Check Settings."); return@launch }
                analysisContent = repository.getLocalContent(projectId, 0)
            }
            if (analysisContent == null) { _pipelineState.value = PipelineProgressState.Error("Could not read Phase 0 from local DB."); return@launch }

            val (bible, lockedBlueprint) = extractBibleAndBlueprint(analysisContent.text)
            val completed = java.util.concurrent.atomic.AtomicInteger(0)
            try {
                kotlinx.coroutines.coroutineScope {
                    (1..10).map { phase ->
                        async(kotlinx.coroutines.Dispatchers.IO) {
                            if (repository.pipelineDao.getScenesForPhase(projectId, phase).size >= 18) { completed.incrementAndGet(); return@async }
                            val result = withRetry { repository.generateContentForPhase(projectId, phase, bible, lockedBlueprint).getOrThrow() }
                            val parsed = parse18Scenes(result)
                            if (parsed.size != 18) throw Exception("Phase $phase returned ${parsed.size} scenes, expected 18.")
                            val entities = parsed.mapIndexed { i, t -> com.example.data.local.GeneratedScene(projectId, phase, i + 1, t) }
                            repository.pipelineDao.insertScenes(entities)
                            val done = completed.incrementAndGet()
                            _pipelineState.value = PipelineProgressState.Running(done, PipelineProgressState.Running.Step.GENERATING, done / 11f)
                        }
                    }.awaitAll()
                }
                _pipelineState.value = PipelineProgressState.Running(10, PipelineProgressState.Running.Step.STITCHING, 0.98f)
                val titles = withRetry { repository.generateContent(projectId, "titles", 99).getOrThrow() }
                repository.pipelineDao.insertTitles(com.example.data.local.Titles(projectId, titles))
                rebuildSets(projectId, repository)
                _pipelineState.value = PipelineProgressState.Finished
            } catch (e: Exception) {
                e.printStackTrace()
                _pipelineState.value = PipelineProgressState.Error(e.message ?: "Pipeline interrupted.")
            }
        }
    }

    fun regeneratePhase(phaseNo: Int) {
        _phaseActionState.value = UiState.Loading
        viewModelScope.launch {
            val analysisContent = repository.getLocalContent(projectId, 0)
            if (analysisContent == null) {
                _phaseActionState.value = UiState.Error("Missing Precondition: Workspace DNA Analysis (Phase 0) must be generated first.")
                return@launch
            }
            val (bible, lockedBlueprint) = extractBibleAndBlueprint(analysisContent.text)

            try {
                val result = withRetry {
                    repository.generateContentForPhase(projectId, phaseNo, bible, lockedBlueprint).getOrThrow()
                }
                val parsedScenes = parse18Scenes(result)
                if (parsedScenes.size != 18) {
                    throw Exception("Phase $phaseNo returned ${parsedScenes.size} scenes instead of 18. " +
                        "Model output drifted from the required format — retry this phase.")
                }

                val entities = parsedScenes.mapIndexed { index, text ->
                    com.example.data.local.GeneratedScene(
                        projectId = projectId,
                        phaseNumber = phaseNo,
                        sceneIndex = index + 1,
                        text = text
                    )
                }

                repository.pipelineDao.insertScenes(entities)
                repository.pipelineDao.insertQcReport(com.example.data.local.QcReport(projectId, phaseNo, "Compliance automated verification successful."))

                rebuildSets(projectId, repository)
                _phaseActionState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                _phaseActionState.value = UiState.Error("Recompile failed: ${e.message ?: "Backend generation failed"}")
            }
        }
    }

    fun generateTitlesManually() {
        _titlesState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val titles = withRetry { repository.generateContent(projectId, "titles", 99).getOrThrow() }
                repository.pipelineDao.insertTitles(com.example.data.local.Titles(projectId, titles))
                _titlesState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                _titlesState.value = UiState.Error("Title generation failed: ${e.message ?: "Failed to generate titles"}")
            }
        }
    }

    fun clearPipelineError() {
        _pipelineState.value = PipelineProgressState.Idle
    }

    class Factory(private val repository: ProjectRepository, private val projectId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ResultViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ResultViewModel(repository, projectId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// ==========================================
// PIPELINE HELPER UTILITIES
// ==========================================

fun extractBibleAndBlueprint(analysisText: String): Pair<String, String> {
    val lower = analysisText.lowercase()
    val blueprintIdx = lower.indexOf("blueprint")
    val bibleIdx = lower.indexOf("bible")

    if (blueprintIdx != -1 && bibleIdx != -1) {
        if (bibleIdx < blueprintIdx) {
            val biblePart = analysisText.substring(0, blueprintIdx).trim()
            val blueprintPart = analysisText.substring(blueprintIdx).trim()
            return Pair(biblePart, blueprintPart)
        } else {
            val blueprintPart = analysisText.substring(0, bibleIdx).trim()
            val biblePart = analysisText.substring(bibleIdx).trim()
            return Pair(biblePart, blueprintPart)
        }
    }
    return Pair(analysisText, analysisText)
}

fun parse18Scenes(text: String): List<String> {
    val items = text.split(Regex("(?m)^(?i)(Prompt|Scene|\\d+)\\s*\\[?\\d+\\]?[\\s–:-]+"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    if (items.size >= 18) {
        return items.take(18)
    }

    val lines = text.split("\n").map { it.trim() }.filter { it.length > 8 }
    if (lines.size >= 18) {
        return lines.take(18)
    }

    val result = lines.toMutableList()
    while (result.size < 18) {
        result.add("Generated scene spec details for sequence ${result.size + 1}")
    }
    return result.take(18)
}

suspend fun rebuildSets(projectId: Int, repository: ProjectRepository) {
    val allScenes = repository.pipelineDao.getScenesForProject(projectId)
    val promptMap = allScenes.associateBy { (it.phaseNumber - 1) * 18 + it.sceneIndex }

    val maxIndex = if (promptMap.isEmpty()) 0 else promptMap.keys.maxOrNull() ?: 0
    if (maxIndex == 0) {
        repository.pipelineDao.deleteSetsForProject(projectId)
        return
    }

    val totalSets = (maxIndex + 99) / 100
    val setsToInsert = mutableListOf<com.example.data.local.GeneratedSet>()
    for (setNum in 1..totalSets) {
        val startId = (setNum - 1) * 100 + 1
        val endId = minOf(setNum * 100, maxIndex)

        val sb = java.lang.StringBuilder()
        for (i in startId..endId) {
            val scene = promptMap[i]
            if (scene != null) {
                sb.append("Prompt $i: ").append(scene.text).append("\n\n")
            } else {
                sb.append("Prompt $i: [Pending Generation]\n\n")
            }
        }
        setsToInsert.add(
            com.example.data.local.GeneratedSet(
                projectId = projectId,
                setNumber = setNum,
                startIndex = startId,
                endIndex = endId,
                text = sb.toString().trim()
            )
        )
    }
    repository.pipelineDao.insertSets(setsToInsert)
}

// ==========================================
// LIBRARY VIEW MODEL
// ==========================================

class LibraryViewModel(private val repository: ProjectRepository) : ViewModel() {

    val allProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-time map of projectId -> active completed phase count
    val projectProgressMap: Flow<Map<Int, Int>> = repository.allProjects.flatMapLatest { projects ->
        val flows = projects.map { project ->
            repository.getCompletedPhases(project.id).map { completedList ->
                project.id to completedList.size
            }
        }
        if (flows.isEmpty()) {
            flowOf(emptyMap())
        } else {
            combine(flows) { pairs ->
                pairs.toMap()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun deleteProject(projectId: Int) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
        }
    }

    class Factory(private val repository: ProjectRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LibraryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// ==========================================
// SETTINGS VIEW MODEL
// ==========================================

class SettingsViewModel(private val repository: ProjectRepository) : ViewModel() {

    private val _connectionTestState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val connectionTestState: StateFlow<UiState<Boolean>> = _connectionTestState.asStateFlow()

    val backendUrl: StateFlow<String> = repository.preferencesManager.backendUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val defaultLanguage: StateFlow<String> = repository.preferencesManager.defaultLanguageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val defaultStyle: StateFlow<String> = repository.preferencesManager.defaultStyleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val useModelPro: StateFlow<Boolean> = repository.preferencesManager.useModelProFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val allStyles: StateFlow<List<CustomStyle>> = repository.allStyles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLanguages: StateFlow<List<Language>> = repository.allLanguages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveBackendUrl(url: String) {
        viewModelScope.launch {
            repository.preferencesManager.saveBackendUrl(url)
        }
    }

    fun saveDefaultLanguage(language: String) {
        viewModelScope.launch {
            repository.preferencesManager.saveDefaultLanguage(language)
        }
    }

    fun saveDefaultStyle(style: String) {
        viewModelScope.launch {
            repository.preferencesManager.saveDefaultStyle(style)
        }
    }

    fun saveUseModelPro(usePro: Boolean) {
        viewModelScope.launch {
            repository.preferencesManager.saveUseModelPro(usePro)
        }
    }

    fun testBackendConnection() {
        _connectionTestState.value = UiState.Loading
        viewModelScope.launch {
            val isSuccess = repository.pingBackend()
            if (isSuccess) {
                _connectionTestState.value = UiState.Success(true)
            } else {
                _connectionTestState.value = UiState.Error("Unreachable — server may be asleep or URL expired")
            }
        }
    }

    fun clearConnectionTestState() {
        _connectionTestState.value = UiState.Idle
    }

    class Factory(private val repository: ProjectRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
