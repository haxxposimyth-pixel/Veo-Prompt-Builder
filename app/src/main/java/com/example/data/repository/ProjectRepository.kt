package com.example.data.repository

import com.example.data.local.*
import com.example.data.remote.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val generatedContentDao: GeneratedContentDao,
    private val customStyleDao: CustomStyleDao,
    private val languageDao: LanguageDao,
    private val apiService: ApiService,
    val preferencesManager: PreferencesManager,
    private val tokenProvider: TokenProvider,
    val pipelineDao: PipelineDao
) {

    suspend fun pingBackend(): Boolean = try {
        val base = preferencesManager.backendUrlFlow.first()
        val url = if (base.endsWith("/")) "${base}health" else "$base/health"
        apiService.health(url).isSuccessful
    } catch (e: Exception) { false }

    // ==========================================
    // PROJECTS & CONTENT
    // ==========================================

    val allProjects: Flow<List<Project>> = projectDao.getAllProjectsFlow()

    suspend fun getProjectById(projectId: Int): Project? {
        return projectDao.getProjectById(projectId)
    }

    suspend fun createProject(project: Project): Int {
        return projectDao.insertProject(project).toInt()
    }

    suspend fun deleteProject(projectId: Int) {
        projectDao.deleteProjectById(projectId)
    }

    fun getCompletedPhases(projectId: Int): Flow<List<Int>> {
        return generatedContentDao.getCompletedPhasesFlow(projectId)
    }

    suspend fun getLocalContent(projectId: Int, phaseNumber: Int): GeneratedContent? {
        return generatedContentDao.getContent(projectId, phaseNumber)
    }

    // ==========================================
    // SEEDING
    // ==========================================

    suspend fun seedDatabaseIfEmpty() {
        if (customStyleDao.getStyleCount() == 0) {
            val defaultStyles = listOf(
                CustomStyle(name = "Hyper-realistic 3D", description = "Ultra-detailed three-dimensional graphics with ray-traced particles and realistic dynamic fluid simulation", isBuiltIn = true),
                CustomStyle(name = "Cinematic Documentary", description = "Slow cinematic pans, high contrast atmospheric lighting, and epic slow-motion biological actions", isBuiltIn = true),
                CustomStyle(name = "Macro Micro-world", description = "Ultra close-up macro imaging showing molecular level details, high-depth of field, and crisp micro textures", isBuiltIn = true),
                CustomStyle(name = "Dark Cinematic", description = "Deep charcoal tones, heavy ambient contrast, soft edge shadows, and moody, mysterious dramatic lighting", isBuiltIn = true),
                CustomStyle(name = "Clean Educational", description = "Crisp focus, bright clinical lighting, high-contrast flat diagrams, and clean geometric camera passes", isBuiltIn = true)
            )
            for (style in defaultStyles) {
                customStyleDao.insertStyle(style)
            }
        }

        if (languageDao.getLanguageCount() == 0) {
            val defaultLanguages = listOf(
                "Hindi", "English", "Spanish", "French", "Arabic", "Portuguese",
                "Indonesian", "German", "Japanese", "Russian", "Mandarin Chinese", "Bengali"
            )
            for (langName in defaultLanguages) {
                languageDao.insertLanguage(Language(name = langName, isBuiltIn = true))
            }
        }
    }

    // ==========================================
    // STYLES
    // ==========================================

    val allStyles: Flow<List<CustomStyle>> = customStyleDao.getAllStylesFlow()

    suspend fun addStyle(name: String, description: String, isBuiltIn: Boolean = false) {
        customStyleDao.insertStyle(CustomStyle(name = name, description = description, isBuiltIn = isBuiltIn))
    }

    suspend fun deleteStyle(style: CustomStyle) {
        if (!style.isBuiltIn) {
            customStyleDao.deleteStyle(style)
        }
    }

    // ==========================================
    // LANGUAGES
    // ==========================================

    val allLanguages: Flow<List<Language>> = languageDao.getAllLanguagesFlow()

    suspend fun addLanguage(name: String, isBuiltIn: Boolean = false) {
        languageDao.insertLanguage(Language(name = name, isBuiltIn = isBuiltIn))
    }

    suspend fun deleteLanguage(language: Language) {
        if (!language.isBuiltIn) {
            languageDao.deleteLanguage(language)
        }
    }

    // ==========================================
    // NETWORK GENERATION
    // ==========================================

    suspend fun generateContent(
        projectId: Int,
        mode: String,              // "analysis", "phase", "titles"
        phaseNumber: Int           // 0 for analysis, 1..10 for phase, 99 for titles
    ): Result<String> {
        return try {
            val project = projectDao.getProjectById(projectId) 
                ?: return Result.failure(Exception("Project not found in local database (ID: $projectId)"))

            // Look up custom style description if needed
            var customStyleDesc: String? = null
            if (project.videoStyle != "Custom") {
                val styles = customStyleDao.getAllStylesFlow().first()
                val matchedStyle = styles.find { it.name.equals(project.videoStyle, ignoreCase = true) }
                if (matchedStyle != null && !matchedStyle.isBuiltIn) {
                    customStyleDesc = matchedStyle.description
                }
            }

            val curBackendUrl = preferencesManager.backendUrlFlow.first()
            val usePro = preferencesManager.useModelProFlow.first()

            val request = GenerateRequest(
                mode = mode,
                niche = project.niche,
                customNiche = project.customNiche,
                videoStyle = project.videoStyle,
                customStyleDescription = customStyleDesc,
                topic = project.topic,
                aspectRatio = project.aspectRatio,
                language = project.language,
                phase = if (mode == "phase") phaseNumber else null,
                model = if (usePro) "pro" else "flash"
            )

            val fullUrl = if (curBackendUrl.endsWith("/")) "${curBackendUrl}generate" else "$curBackendUrl/generate"

            val response = apiService.generate(
                url = fullUrl,
                request = request,
                appCheckToken = tokenProvider.getAppCheckToken(),
                bearerToken = tokenProvider.getBearerToken()
            )

            // Cache generated content in SQLite database
            val content = GeneratedContent(
                projectId = projectId,
                phaseNumber = phaseNumber,
                text = response.text
            )
            generatedContentDao.insertContent(content)

            Result.success(response.text)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun generateContentForPhase(
        projectId: Int,
        phase: Int,
        bible: String?,
        blueprint: String?
    ): Result<String> {
        return try {
            val project = projectDao.getProjectById(projectId)
                ?: return Result.failure(Exception("Project not found in local database (ID: $projectId)"))

            var customStyleDesc: String? = null
            if (project.videoStyle != "Custom") {
                val styles = customStyleDao.getAllStylesFlow().first()
                val matchedStyle = styles.find { it.name.equals(project.videoStyle, ignoreCase = true) }
                if (matchedStyle != null && !matchedStyle.isBuiltIn) {
                    customStyleDesc = matchedStyle.description
                }
            }

            val curBackendUrl = preferencesManager.backendUrlFlow.first()
            val usePro = preferencesManager.useModelProFlow.first()
            val fullUrl = if (curBackendUrl.endsWith("/")) "${curBackendUrl}generate" else "$curBackendUrl/generate"

            val request = GenerateRequest(
                mode = "phase",
                niche = project.niche,
                customNiche = project.customNiche,
                videoStyle = project.videoStyle,
                customStyleDescription = customStyleDesc,
                topic = project.topic,
                aspectRatio = project.aspectRatio,
                language = project.language,
                phase = phase,
                model = if (usePro) "pro" else "flash",
                bible = bible,
                blueprint = blueprint
            )

            val response = apiService.generate(
                url = fullUrl,
                request = request,
                appCheckToken = tokenProvider.getAppCheckToken(),
                bearerToken = tokenProvider.getBearerToken()
            )

            // Cache generated content in SQLite database
            val content = GeneratedContent(
                projectId = projectId,
                phaseNumber = phase,
                text = response.text
            )
            generatedContentDao.insertContent(content)

            Result.success(response.text)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
