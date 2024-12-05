package dev.jay.ecoclassify

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.jay.ecoclassify.data.ClassifierType
import dev.jay.ecoclassify.data.TfLiteClassifier
import dev.jay.ecoclassify.domain.Classification
import dev.jay.ecoclassify.presentation.CameraPreview
import dev.jay.ecoclassify.presentation.ImageAnalyzer
import dev.jay.ecoclassify.ui.theme.EcoClassifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, CAMERAX_PERMISSIONS, 0)
        }
        enableEdgeToEdge()
        setContent {
            EcoClassifyTheme {
                var selectedCategory by remember { mutableStateOf(ClassifierType.PLANT) }
                var classifications by remember { mutableStateOf(emptyList<Classification>()) }

                // Initialize the ImageAnalyzer and CameraController
                val analyzer = remember {
                    ImageAnalyzer(classifier = TfLiteClassifier(
                        context = applicationContext, classifierType = selectedCategory
                    ), onResults = {
                        classifications = it
                    })
                }

                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                        setImageAnalysisAnalyzer(
                            ContextCompat.getMainExecutor(applicationContext), analyzer
                        )
                    }
                }

                Scaffold(topBar = {
                    // Category Selection Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(top = 28.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RadioButtonGroup(options = listOf(
                            ClassifierType.BIRD,
                            ClassifierType.INSECT,
                            ClassifierType.PLANT,
                            ClassifierType.FOOD
                        ),
                            selectedOption = selectedCategory,
                            onOptionSelected = { newCategory ->
                                selectedCategory = newCategory
                            })
                    }
                }, contentWindowInsets = WindowInsets.systemBars) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Camera Preview
                        CameraPreview(controller, Modifier.fillMaxSize())

                        // Classification Results
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            classifications.forEach {
                                Text(
                                    text = it.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(8.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}

@Composable
fun RadioButtonGroup(
    options: List<ClassifierType>,
    selectedOption: ClassifierType,
    onOptionSelected: (ClassifierType) -> Unit
) {
    Row {
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                RadioButton(selected = option == selectedOption,
                    onClick = { onOptionSelected(option) })
                Text(
                    text = option.classifierType,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}