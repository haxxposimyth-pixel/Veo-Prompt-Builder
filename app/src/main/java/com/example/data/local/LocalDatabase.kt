package com.example.data.local

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

// ==========================================
// ENTITIES
// ==========================================

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val niche: String,
    val customNiche: String?,
    val videoStyle: String,
    val language: String,
    val aspectRatio: String,
    val topic: String,
    val createdAt: Long = System.currentTimeMillis(),
    val category: String = "",
    val subNiche: String = ""
)

@Entity(
    tableName = "generated_content",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class GeneratedContent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val phaseNumber: Int, // 0 = analysis, 1..10 = phases, 99 = titles
    val text: String
)

@Entity(tableName = "custom_styles")
data class CustomStyle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val isBuiltIn: Boolean
)

@Entity(tableName = "languages")
data class Language(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isBuiltIn: Boolean
)

@Entity(
    tableName = "generated_scenes",
    primaryKeys = ["projectId", "phaseNumber", "sceneIndex"]
)
data class GeneratedScene(
    val projectId: Int,
    val phaseNumber: Int,
    val sceneIndex: Int, // 1 to 18 for each phase
    val text: String
)

@Entity(
    tableName = "qc_reports",
    primaryKeys = ["projectId", "phaseNumber"]
)
data class QcReport(
    val projectId: Int,
    val phaseNumber: Int,
    val text: String
)

@Entity(
    tableName = "generated_sets",
    primaryKeys = ["projectId", "setNumber"]
)
data class GeneratedSet(
    val projectId: Int,
    val setNumber: Int,
    val startIndex: Int,
    val endIndex: Int,
    val text: String
)

@Entity(
    tableName = "project_titles",
    primaryKeys = ["projectId"]
)
data class Titles(
    val projectId: Int,
    val text: String
)

// ==========================================
// DAOS
// ==========================================

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjectsFlow(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Int): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Int)
}

@Dao
interface GeneratedContentDao {
    @Query("SELECT * FROM generated_content WHERE projectId = :projectId AND phaseNumber = :phaseNumber LIMIT 1")
    suspend fun getContent(projectId: Int, phaseNumber: Int): GeneratedContent?

    @Query("SELECT * FROM generated_content WHERE projectId = :projectId")
    fun getAllContentForProjectFlow(projectId: Int): Flow<List<GeneratedContent>>

    @Query("SELECT phaseNumber FROM generated_content WHERE projectId = :projectId AND phaseNumber >= 1 AND phaseNumber <= 10")
    fun getCompletedPhasesFlow(projectId: Int): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: GeneratedContent)

    @Query("DELETE FROM generated_content WHERE projectId = :projectId")
    suspend fun deleteContentForProject(projectId: Int)
}

@Dao
interface CustomStyleDao {
    @Query("SELECT * FROM custom_styles ORDER BY isBuiltIn DESC, name ASC")
    fun getAllStylesFlow(): Flow<List<CustomStyle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStyle(style: CustomStyle)

    @Delete
    suspend fun deleteStyle(style: CustomStyle)

    @Query("SELECT COUNT(*) FROM custom_styles")
    suspend fun getStyleCount(): Int

    @Query("SELECT name FROM custom_styles")
    suspend fun getAllStyleNames(): List<String>
}

@Dao
interface LanguageDao {
    @Query("SELECT * FROM languages ORDER BY isBuiltIn DESC, name ASC")
    fun getAllLanguagesFlow(): Flow<List<Language>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguage(language: Language)

    @Delete
    suspend fun deleteLanguage(language: Language)

    @Query("SELECT COUNT(*) FROM languages")
    suspend fun getLanguageCount(): Int
}

@Dao
interface PipelineDao {
    @Query("SELECT * FROM generated_scenes WHERE projectId = :projectId ORDER BY phaseNumber ASC, sceneIndex ASC")
    fun getScenesForProjectFlow(projectId: Int): Flow<List<GeneratedScene>>

    @Query("SELECT * FROM generated_scenes WHERE projectId = :projectId")
    suspend fun getScenesForProject(projectId: Int): List<GeneratedScene>

    @Query("SELECT * FROM generated_scenes WHERE projectId = :projectId AND phaseNumber = :phaseNumber ORDER BY sceneIndex ASC")
    suspend fun getScenesForPhase(projectId: Int, phaseNumber: Int): List<GeneratedScene>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenes(scenes: List<GeneratedScene>)

    @Query("DELETE FROM generated_scenes WHERE projectId = :projectId AND phaseNumber = :phaseNumber")
    suspend fun deleteScenesForPhase(projectId: Int, phaseNumber: Int)

    @Query("SELECT * FROM qc_reports WHERE projectId = :projectId AND phaseNumber = :phaseNumber LIMIT 1")
    suspend fun getQcReport(projectId: Int, phaseNumber: Int): QcReport?

    @Query("SELECT * FROM qc_reports WHERE projectId = :projectId ORDER BY phaseNumber ASC")
    fun getQcReportsForProjectFlow(projectId: Int): Flow<List<QcReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQcReport(qcReport: QcReport)

    @Query("SELECT * FROM generated_sets WHERE projectId = :projectId ORDER BY setNumber ASC")
    fun getSetsForProjectFlow(projectId: Int): Flow<List<GeneratedSet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<GeneratedSet>)

    @Query("DELETE FROM generated_sets WHERE projectId = :projectId")
    suspend fun deleteSetsForProject(projectId: Int)

    @Query("SELECT * FROM project_titles WHERE projectId = :projectId LIMIT 1")
    fun getTitlesForProjectFlow(projectId: Int): Flow<Titles?>

    @Query("SELECT * FROM project_titles WHERE projectId = :projectId LIMIT 1")
    suspend fun getTitlesForProject(projectId: Int): Titles?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTitles(titles: Titles)
}

// ==========================================
// DATABASE
// ==========================================

@Database(
    entities = [
        Project::class,
        GeneratedContent::class,
        CustomStyle::class,
        Language::class,
        GeneratedScene::class,
        QcReport::class,
        GeneratedSet::class,
        Titles::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun generatedContentDao(): GeneratedContentDao
    abstract fun customStyleDao(): CustomStyleDao
    abstract fun languageDao(): LanguageDao
    abstract fun pipelineDao(): PipelineDao
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE projects ADD COLUMN category TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE projects ADD COLUMN subNiche TEXT NOT NULL DEFAULT ''")
    }
}
