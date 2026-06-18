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
        val existingStyleNames = customStyleDao.getAllStyleNames().toSet()
        val defaultStyles = listOf(
            CustomStyle(name = "Hyper-realistic 3D", description = "Ultra-detailed three-dimensional graphics with ray-traced particles, realistic dynamic fluid simulation, volumetric god rays, and 8K resolution rendering of ancient environments.", isBuiltIn = true),
            CustomStyle(name = "Cinematic Epic", description = "Hollywood blockbuster style, anamorphic lens distortion, sweeping drone shots over ancient stone cities, golden hour lighting, high dynamic range, and atmospheric dust motes.", isBuiltIn = true),
            CustomStyle(name = "Renaissance Oil Painting", description = "Living classical masterpiece, intense chiaroscuro lighting, deep rich pigments, dynamic brush strokes slowly coming to life, ethereal divine glow, and classical composition.", isBuiltIn = true),
            CustomStyle(name = "Stained Glass Animation", description = "Luminous fractured glass art, vibrant jewel tones illuminated by a moving backlight, slow rotating camera, sunbeams piercing through colorful transparent panes, holy atmosphere.", isBuiltIn = true),
            CustomStyle(name = "Ancient Parchment Ink", description = "Sepia-toned ancient scroll aesthetic, flowing black ink organically revealing shapes, rough textured vellum background, subtle catching of light on gold leaf accents, slow pan and reveal.", isBuiltIn = true),
            CustomStyle(name = "Ethereal Watercolor", description = "Soft pastel color bleeding, liquid pigment blooming on cold-pressed paper textures, dreamlike scene transitions, glowing light leaks, gentle fluid motion suited for divine visions.", isBuiltIn = true),
            CustomStyle(name = "Stop-Motion Claymation", description = "Tactile sculpted clay figures and sets, visible fingerprints, dramatic miniature studio lighting, cinematic 12-fps stutter, rich earthy terracotta and bronze tones.", isBuiltIn = true),
            CustomStyle(name = "Byzantine Mosaic", description = "Shimmering gold and lapis lazuli tesserae, thousands of tiny reflective tiles shifting dynamically in the light, 2D iconographic style brought into deep 3D space, warm flickering candlelight illumination.", isBuiltIn = true),
            CustomStyle(name = "Monochromatic High-Contrast", description = "Deep black and white cinematic lighting, stark harsh shadows, profound atmospheric tension, swirling desert winds, extreme focus on emotional facial micro-expressions.", isBuiltIn = true),
            CustomStyle(name = "Sand Art Animation", description = "Golden grains of sand shifting on a backlit glass surface, continuous morphing shapes and silhouettes, warm amber undertones, organic and tactile transitions, ancient desert storytelling.", isBuiltIn = true),
            CustomStyle(name = "Macro Depth of Field", description = "Extreme close-up macro cinematography, incredibly shallow depth of field, microscopic details of ancient textures like olive wood, woven linen, and rusted iron, smooth blurred bokeh background.", isBuiltIn = true),
            CustomStyle(name = "Surrealist Prophetic Dreamscape", description = "Floating landscapes, defiance of gravity, massive scale contrast between human figures and celestial bodies, shifting ethereal environments, mysterious and awe-inspiring aura.", isBuiltIn = true),
            CustomStyle(name = "Classical Cel-Shaded Animation", description = "High-budget 90s anime style, hand-painted lush Middle Eastern backgrounds, dramatic wind effects moving through garments, exaggerated lighting, deeply expressive and fluid motion.", isBuiltIn = true),
            CustomStyle(name = "Medieval Woodcut Print", description = "Bold black etched lines on rough parchment, historical engraving style, stark contrast, slow dramatic zooming, shifting ink weights that mimic ancient biblical manuscripts.", isBuiltIn = true),
            CustomStyle(name = "Bioluminescent Ethereal", description = "Deep mysterious environments, glowing spiritual auras, cool blue and purple ambient lighting contrasting with warm firelight, floating ethereal spores, slow tracking camera shot.", isBuiltIn = true),
            CustomStyle(name = "Golden Hour Silhouette", description = "Stark dark figures against a massive blazing sunset, deep orange and magenta skies, cinematic lens flares, visible heat distortion waves, epic and solemn visual weight.", isBuiltIn = true),
            CustomStyle(name = "Kinetic Charcoal Sketch", description = "Rough, sweeping dark charcoal strokes appearing on textured canvas, smudged dynamic shadows, kinetic drawing process evolving in real-time, emotional and raw visual style.", isBuiltIn = true),
            CustomStyle(name = "Majestic Aerial Photorealism", description = "8K resolution, sweeping helicopter shots of rugged Middle Eastern landscapes, dramatic cloud shadows moving rapidly over deep canyons, epic scale, hyper-detailed rocky terrain.", isBuiltIn = true),
            CustomStyle(name = "Tilt-Shift Miniature", description = "Photorealistic tilt-shift photography effect, ancient biblical battles and crowds looking like tiny hyper-detailed dioramas, vibrant colors, fast-paced timelapse cloud movement.", isBuiltIn = true),
            CustomStyle(name = "Looming Atmospheric Fog", description = "Moody and mysterious lighting, thick rolling volumetric fog covering ancient valleys, cool moonlight piercing through the mist, slow and deliberate camera push-in, highly cinematic tension.", isBuiltIn = true)
        )
        for (style in defaultStyles) {
            if (style.name !in existingStyleNames) {
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
                if (matchedStyle != null) {
                    customStyleDesc = matchedStyle.description
                }
            }

            val curBackendUrl = preferencesManager.backendUrlFlow.first()
            val usePro = preferencesManager.useModelProFlow.first()

            val activeCustomNiche = if (project.niche == "Custom") {
                project.customNiche
            } else {
                com.example.data.nicheDna[project.niche]
            }

            val request = GenerateRequest(
                mode = mode,
                niche = project.niche,
                customNiche = activeCustomNiche,
                videoStyle = project.videoStyle,
                customStyleDescription = customStyleDesc,
                topic = project.topic,
                aspectRatio = project.aspectRatio,
                language = project.language,
                phase = if (mode == "phase") phaseNumber else null,
                model = if (usePro) "pro" else "flash",
                category = project.category,
                subNiche = project.subNiche
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
                if (matchedStyle != null) {
                    customStyleDesc = matchedStyle.description
                }
            }

            val curBackendUrl = preferencesManager.backendUrlFlow.first()
            val usePro = preferencesManager.useModelProFlow.first()
            val fullUrl = if (curBackendUrl.endsWith("/")) "${curBackendUrl}generate" else "$curBackendUrl/generate"

            val activeCustomNiche = if (project.niche == "Custom") {
                project.customNiche
            } else {
                com.example.data.nicheDna[project.niche]
            }

            val request = GenerateRequest(
                mode = "phase",
                niche = project.niche,
                customNiche = activeCustomNiche,
                videoStyle = project.videoStyle,
                customStyleDescription = customStyleDesc,
                topic = project.topic,
                aspectRatio = project.aspectRatio,
                language = project.language,
                phase = phase,
                model = if (usePro) "pro" else "flash",
                bible = bible,
                blueprint = blueprint,
                category = project.category,
                subNiche = project.subNiche
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
