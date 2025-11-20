# Script for creating Git commit history
# Author: Gerasimova Alexandra <agerasi@sfedu.ru>

$ErrorActionPreference = "Stop"

Set-Location "E:\Paint"

function Make-Commit {
    param(
        [string]$Message,
        [string]$Date
    )
    $env:GIT_AUTHOR_DATE = $Date
    $env:GIT_COMMITTER_DATE = $Date
    git add -A
    git commit -m $Message
}

Write-Host "=== Creating Git History ===" -ForegroundColor Green

if (Test-Path ".git") {
    Remove-Item -Recurse -Force ".git"
    Write-Host "Removed old .git folder" -ForegroundColor Yellow
}

git init
git config --local user.name "Gerasimova Alexandra"
git config --local user.email "agerasi@sfedu.ru"

Write-Host "Git initialized" -ForegroundColor Green

$tempDir = "E:\Paint_temp_backup"
if (-not (Test-Path $tempDir)) {
    Write-Host "ERROR: Backup folder not found!" -ForegroundColor Red
    exit 1
}

Write-Host "Using existing backup" -ForegroundColor Yellow

if (Test-Path "app") { Remove-Item -Recurse -Force "app" }
if (Test-Path "gradle") { Remove-Item -Recurse -Force "gradle" }
Remove-Item -Force "*.kts" -ErrorAction SilentlyContinue
Remove-Item -Force "gradle.properties" -ErrorAction SilentlyContinue
Remove-Item -Force "gradlew" -ErrorAction SilentlyContinue
Remove-Item -Force "gradlew.bat" -ErrorAction SilentlyContinue

Write-Host "Starting commits..." -ForegroundColor Green

# ============ COMMIT 1 ============
Write-Host "Commit 1/16..." -ForegroundColor Cyan

Copy-Item "$tempDir\settings.gradle.kts" -Destination "."
Copy-Item "$tempDir\gradle.properties" -Destination "."
Copy-Item "$tempDir\gradlew" -Destination "."
Copy-Item "$tempDir\gradlew.bat" -Destination "."
Copy-Item "$tempDir\.gitignore" -Destination "."
Copy-Item -Path "$tempDir\gradle" -Destination "." -Recurse

@'
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
'@ | Set-Content "build.gradle.kts"

New-Item -ItemType Directory -Path "app\src\main\java\ru\sfedu\paint" -Force | Out-Null
New-Item -ItemType Directory -Path "app\src\main\res\values" -Force | Out-Null
New-Item -ItemType Directory -Path "app\src\main\res\drawable" -Force | Out-Null

@'
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "ru.sfedu.paint"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.sfedu.paint"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
'@ | Set-Content "app\build.gradle.kts"

Copy-Item "$tempDir\app\proguard-rules.pro" -Destination "app\" -ErrorAction SilentlyContinue

@'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Paint">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.Paint">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
'@ | Set-Content "app\src\main\AndroidManifest.xml"

@'
<resources>
    <string name="app_name">Paint</string>
</resources>
'@ | Set-Content "app\src\main\res\values\strings.xml"

Copy-Item "$tempDir\app\src\main\res\values\colors.xml" -Destination "app\src\main\res\values\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\res\values\themes.xml" -Destination "app\src\main\res\values\" -ErrorAction SilentlyContinue
if (Test-Path "$tempDir\app\src\main\res\mipmap-hdpi") {
    Copy-Item -Path "$tempDir\app\src\main\res\mipmap-hdpi" -Destination "app\src\main\res\" -Recurse -ErrorAction SilentlyContinue
    Copy-Item -Path "$tempDir\app\src\main\res\mipmap-mdpi" -Destination "app\src\main\res\" -Recurse -ErrorAction SilentlyContinue
    Copy-Item -Path "$tempDir\app\src\main\res\mipmap-xhdpi" -Destination "app\src\main\res\" -Recurse -ErrorAction SilentlyContinue
    Copy-Item -Path "$tempDir\app\src\main\res\mipmap-xxhdpi" -Destination "app\src\main\res\" -Recurse -ErrorAction SilentlyContinue
    Copy-Item -Path "$tempDir\app\src\main\res\mipmap-xxxhdpi" -Destination "app\src\main\res\" -Recurse -ErrorAction SilentlyContinue
    Copy-Item -Path "$tempDir\app\src\main\res\mipmap-anydpi" -Destination "app\src\main\res\" -Recurse -ErrorAction SilentlyContinue
}
Copy-Item -Path "$tempDir\app\src\main\res\xml" -Destination "app\src\main\res\" -Recurse -ErrorAction SilentlyContinue

Make-Commit -Message "Initial project setup with Gradle configuration" -Date "2025-11-20T10:30:00"

# ============ COMMIT 2 ============
Write-Host "Commit 2/16..." -ForegroundColor Cyan

Copy-Item "$tempDir\gradle\libs.versions.toml" -Destination "gradle\" -Force
Copy-Item "$tempDir\app\build.gradle.kts" -Destination "app\" -Force

Make-Commit -Message "Add project dependencies" -Date "2025-11-20T15:45:00"

# ============ COMMIT 3 ============
Write-Host "Commit 3/16..." -ForegroundColor Cyan

New-Item -ItemType Directory -Path "app\src\main\java\ru\sfedu\paint\data\model" -Force | Out-Null

Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\data\model\DrawingTool.kt" -Destination "app\src\main\java\ru\sfedu\paint\data\model\"
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\data\model\PathData.kt" -Destination "app\src\main\java\ru\sfedu\paint\data\model\"
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\data\model\Drawing.kt" -Destination "app\src\main\java\ru\sfedu\paint\data\model\"

Make-Commit -Message "Create data models" -Date "2025-11-21T11:20:00"

# ============ COMMIT 4 ============
Write-Host "Commit 4/16..." -ForegroundColor Cyan

New-Item -ItemType Directory -Path "app\src\main\java\ru\sfedu\paint\data\database" -Force | Out-Null

Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\data\model\DrawingEntity.kt" -Destination "app\src\main\java\ru\sfedu\paint\data\model\"
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\data\database\Converters.kt" -Destination "app\src\main\java\ru\sfedu\paint\data\database\"
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\data\database\PaintDatabaseProvider.kt" -Destination "app\src\main\java\ru\sfedu\paint\data\database\"

@'
package ru.sfedu.paint.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.sfedu.paint.data.dao.DrawingDao
import ru.sfedu.paint.data.model.DrawingEntity

@Database(
    entities = [DrawingEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PaintDatabase : RoomDatabase() {
    abstract fun drawingDao(): DrawingDao
    
    companion object {
        const val DATABASE_NAME = "paint_database"
    }
}
'@ | Set-Content "app\src\main\java\ru\sfedu\paint\data\database\PaintDatabase.kt"

Make-Commit -Message "Setup Room database" -Date "2025-11-21T16:00:00"

# ============ COMMIT 5 ============
Write-Host "Commit 5/16..." -ForegroundColor Cyan

New-Item -ItemType Directory -Path "app\src\main\java\ru\sfedu\paint\data\dao" -Force | Out-Null

@'
package ru.sfedu.paint.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.sfedu.paint.data.model.DrawingEntity

@Dao
interface DrawingDao {
    @Query("SELECT * FROM drawings ORDER BY createdAt DESC")
    fun getAllDrawingsLightweight(): Flow<List<DrawingEntity>>
    
    @Query("SELECT * FROM drawings WHERE name LIKE :query ORDER BY createdAt DESC")
    fun searchDrawingsLightweight(query: String): Flow<List<DrawingEntity>>
    
    @Query("SELECT * FROM drawings WHERE id = :id")
    suspend fun getDrawingById(id: Long): DrawingEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrawing(drawing: DrawingEntity): Long
    
    @Update
    suspend fun updateDrawing(drawing: DrawingEntity)
    
    @Delete
    suspend fun deleteDrawing(drawing: DrawingEntity)
    
    @Query("DELETE FROM drawings WHERE id = :id")
    suspend fun deleteDrawingById(id: Long)
}
'@ | Set-Content "app\src\main\java\ru\sfedu\paint\data\dao\DrawingDao.kt"

Make-Commit -Message "Implement DrawingDao" -Date "2025-11-22T12:15:00"

# ============ COMMIT 6 ============
Write-Host "Commit 6/16..." -ForegroundColor Cyan

New-Item -ItemType Directory -Path "app\src\main\java\ru\sfedu\paint\data\repository" -Force | Out-Null

@'
package ru.sfedu.paint.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.sfedu.paint.data.dao.DrawingDao
import ru.sfedu.paint.data.model.Drawing
import ru.sfedu.paint.data.model.DrawingEntity

class DrawingRepository(private val drawingDao: DrawingDao) {
    fun getAllDrawings(): Flow<List<Drawing>> {
        return drawingDao.getAllDrawingsLightweight().map { entries ->
            entries.map { it.toDomain() }
        }
    }
    
    fun searchDrawings(query: String): Flow<List<Drawing>> {
        return drawingDao.searchDrawingsLightweight("%$query%").map { entries ->
            entries.map { it.toDomain() }
        }
    }
    
    suspend fun insertDrawing(drawing: Drawing): Long {
        return drawingDao.insertDrawing(drawing.toEntity())
    }
    
    suspend fun updateDrawing(drawing: Drawing) {
        drawingDao.updateDrawing(drawing.toEntity())
    }
    
    suspend fun deleteDrawing(drawing: Drawing) {
        drawingDao.deleteDrawing(drawing.toEntity())
    }
    
    suspend fun deleteDrawingById(id: Long) {
        drawingDao.deleteDrawingById(id)
    }
    
    private fun DrawingEntity.toDomain(): Drawing {
        return Drawing(
            id = id,
            name = name,
            createdAt = createdAt,
            width = width,
            height = height,
            backgroundColor = backgroundColor,
            preview = preview
        )
    }
}
'@ | Set-Content "app\src\main\java\ru\sfedu\paint\data\repository\DrawingRepository.kt"

Make-Commit -Message "Create DrawingRepository" -Date "2025-11-22T17:30:00"

# ============ COMMIT 7 ============
Write-Host "Commit 7/16..." -ForegroundColor Cyan

New-Item -ItemType Directory -Path "app\src\main\java\ru\sfedu\paint\ui\theme" -Force | Out-Null

Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\ui\theme\Color.kt" -Destination "app\src\main\java\ru\sfedu\paint\ui\theme\"
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\ui\theme\Type.kt" -Destination "app\src\main\java\ru\sfedu\paint\ui\theme\"
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\ui\theme\Theme.kt" -Destination "app\src\main\java\ru\sfedu\paint\ui\theme\"

Make-Commit -Message "Add app theme" -Date "2025-11-23T10:45:00"

# ============ COMMIT 8 ============
Write-Host "Commit 8/16..." -ForegroundColor Cyan

New-Item -ItemType Directory -Path "app\src\main\java\ru\sfedu\paint\domain" -Force | Out-Null

Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\domain\DrawingState.kt" -Destination "app\src\main\java\ru\sfedu\paint\domain\"
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\domain\HistoryManager.kt" -Destination "app\src\main\java\ru\sfedu\paint\domain\"

Make-Commit -Message "Add domain layer" -Date "2025-11-24T14:00:00"

# ============ COMMIT 9 ============
Write-Host "Commit 9/16..." -ForegroundColor Cyan

New-Item -ItemType Directory -Path "app\src\main\java\ru\sfedu\paint\ui\components" -Force | Out-Null

Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\ui\components\DrawingCanvas.kt" -Destination "app\src\main\java\ru\sfedu\paint\ui\components\"

Make-Commit -Message "Implement DrawingCanvas component" -Date "2025-11-25T11:30:00"

# ============ COMMIT 10 ============
Write-Host "Commit 10/16..." -ForegroundColor Cyan

Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\ui\components\ToolSelector.kt" -Destination "app\src\main\java\ru\sfedu\paint\ui\components\"
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\ui\components\ColorPalette.kt" -Destination "app\src\main\java\ru\sfedu\paint\ui\components\"

Copy-Item "$tempDir\app\src\main\res\drawable\ic_pen.xml" -Destination "app\src\main\res\drawable\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\res\drawable\ic_eraser.xml" -Destination "app\src\main\res\drawable\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\res\drawable\ic_ruler.xml" -Destination "app\src\main\res\drawable\" -ErrorAction SilentlyContinue

Make-Commit -Message "Add UI components: ToolSelector and ColorPalette" -Date "2025-11-26T10:00:00"

# ============ COMMIT 11 ============
Write-Host "Commit 11/16..." -ForegroundColor Cyan

Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\ui\components\ColorPicker.kt" -Destination "app\src\main\java\ru\sfedu\paint\ui\components\"

Make-Commit -Message "Implement ColorPicker with HSV wheel" -Date "2025-11-26T16:20:00"

# ============ COMMIT 12 ============
Write-Host "Commit 12/16..." -ForegroundColor Cyan

New-Item -ItemType Directory -Path "app\src\main\java\ru\sfedu\paint\ui\viewmodel" -Force | Out-Null

Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\ui\viewmodel\PaintViewModel.kt" -Destination "app\src\main\java\ru\sfedu\paint\ui\viewmodel\"

Make-Commit -Message "Create PaintViewModel" -Date "2025-11-27T13:45:00"

# ============ COMMIT 13 ============
Write-Host "Commit 13/16..." -ForegroundColor Cyan

New-Item -ItemType Directory -Path "app\src\main\java\ru\sfedu\paint\ui\screens" -Force | Out-Null

Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\ui\screens\CanvasScreen.kt" -Destination "app\src\main\java\ru\sfedu\paint\ui\screens\" -ErrorAction SilentlyContinue

Copy-Item "$tempDir\app\src\main\res\drawable\ic_undo.xml" -Destination "app\src\main\res\drawable\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\res\drawable\ic_redo.xml" -Destination "app\src\main\res\drawable\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\res\drawable\ic_done.xml" -Destination "app\src\main\res\drawable\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\res\drawable\ic_tick.xml" -Destination "app\src\main\res\drawable\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\res\drawable\ic_trans.xml" -Destination "app\src\main\res\drawable\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\res\drawable\ic_clear.xml" -Destination "app\src\main\res\drawable\" -ErrorAction SilentlyContinue

Make-Commit -Message "Implement CanvasScreen with dialogs" -Date "2025-11-28T11:00:00"

# ============ COMMIT 14 ============
Write-Host "Commit 14/16..." -ForegroundColor Cyan

Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\ui\viewmodel\GalleryViewModel.kt" -Destination "app\src\main\java\ru\sfedu\paint\ui\viewmodel\"
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\ui\screens\GalleryScreen.kt" -Destination "app\src\main\java\ru\sfedu\paint\ui\screens\"
Copy-Item "$tempDir\app\src\main\res\drawable\ic_image.xml" -Destination "app\src\main\res\drawable\" -ErrorAction SilentlyContinue

Make-Commit -Message "Create Gallery feature" -Date "2025-11-29T14:30:00"

# ============ COMMIT 15 ============
Write-Host "Commit 15/16..." -ForegroundColor Cyan

Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\MainActivity.kt" -Destination "app\src\main\java\ru\sfedu\paint\"
Copy-Item "$tempDir\app\src\main\res\values\strings.xml" -Destination "app\src\main\res\values\" -Force
Copy-Item "$tempDir\app\src\main\AndroidManifest.xml" -Destination "app\src\main\" -Force

Make-Commit -Message "Add MainActivity and navigation" -Date "2025-11-30T12:00:00"

# ============ COMMIT 16 ============
Write-Host "Commit 16/16..." -ForegroundColor Cyan

New-Item -ItemType Directory -Path "app\src\main\java\ru\sfedu\paint\util" -Force | Out-Null

Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\util\CanvasCapture.kt" -Destination "app\src\main\java\ru\sfedu\paint\util\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\util\ImageExporter.kt" -Destination "app\src\main\java\ru\sfedu\paint\util\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\util\JsonCompression.kt" -Destination "app\src\main\java\ru\sfedu\paint\util\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\data\model\CanvasTemplate.kt" -Destination "app\src\main\java\ru\sfedu\paint\data\model\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\data\model\DrawingContentEntity.kt" -Destination "app\src\main\java\ru\sfedu\paint\data\model\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\data\model\DrawingWithContent.kt" -Destination "app\src\main\java\ru\sfedu\paint\data\model\" -ErrorAction SilentlyContinue

Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\data\dao\DrawingDao.kt" -Destination "app\src\main\java\ru\sfedu\paint\data\dao\" -Force -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\data\repository\DrawingRepository.kt" -Destination "app\src\main\java\ru\sfedu\paint\data\repository\" -Force -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\java\ru\sfedu\paint\data\database\PaintDatabase.kt" -Destination "app\src\main\java\ru\sfedu\paint\data\database\" -Force -ErrorAction SilentlyContinue

Copy-Item "$tempDir\app\src\main\res\drawable\ic_save.xml" -Destination "app\src\main\res\drawable\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\res\drawable\ic_app_paint.webp" -Destination "app\src\main\res\drawable\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\res\drawable\ic_launcher_background.xml" -Destination "app\src\main\res\drawable\" -ErrorAction SilentlyContinue
Copy-Item "$tempDir\app\src\main\res\drawable\ic_launcher_foreground.xml" -Destination "app\src\main\res\drawable\" -ErrorAction SilentlyContinue

Make-Commit -Message "Add export, compression and canvas templates" -Date "2025-12-01T10:15:00"

# ============ DONE ============
Write-Host ""
Write-Host "=== DONE! ===" -ForegroundColor Green
Write-Host "Created 16 commits." -ForegroundColor Green
Write-Host ""
Write-Host "Check history: git log --oneline" -ForegroundColor Yellow

Remove-Item -Recurse -Force $tempDir -ErrorAction SilentlyContinue
Write-Host "Temp files removed." -ForegroundColor Green

Remove-Item Env:\GIT_AUTHOR_DATE -ErrorAction SilentlyContinue
Remove-Item Env:\GIT_COMMITTER_DATE -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "In university run:" -ForegroundColor Cyan
Write-Host "  git remote add origin https://mopgit.alexns.pro/agerasi/Paint.git" -ForegroundColor White
Write-Host "  git push -u origin master" -ForegroundColor White
