package com.example.bteu_schedule.ui.screens.onboarding

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.example.bteu_schedule.data.local.AppStateManager
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.domain.models.OnboardingStep

@Composable
fun OnboardingNavigation(
    currentStep: OnboardingStep,
    onStepChanged: (OnboardingStep) -> Unit,
    onOnboardingComplete: (GroupUi) -> Unit
) {
    val context = LocalContext.current
    val stateManager = remember { AppStateManager(context) }

    var selectedFacultyId by rememberSaveable { mutableStateOf(stateManager.loadFaculty().first) }
    var selectedFacultyCode by rememberSaveable { mutableStateOf(stateManager.loadFaculty().second) }
    var selectedEducationForm by rememberSaveable { mutableStateOf(stateManager.loadEducationForm()) }
    var selectedCourse by rememberSaveable { mutableStateOf(stateManager.loadCourse()) }

    when (currentStep) {
        OnboardingStep.WELCOME -> WelcomeScreen(onStartClicked = { onStepChanged(OnboardingStep.FACULTY) })
        OnboardingStep.FACULTY -> FacultySelectionScreen(
            onFacultySelected = { faculty ->
                selectedFacultyId = faculty.id
                selectedFacultyCode = faculty.code
                stateManager.saveFaculty(faculty.id, faculty.code)
                onStepChanged(OnboardingStep.EDUCATION_FORM)
            }
        )
        OnboardingStep.EDUCATION_FORM -> EducationFormSelectionScreen(
            onBack = { onStepChanged(OnboardingStep.FACULTY) },
            onFormSelected = { form ->
                selectedEducationForm = form.code
                stateManager.saveEducationForm(form.code)
                onStepChanged(OnboardingStep.COURSE)
            }
        )
        OnboardingStep.COURSE -> CourseSelectionScreen(
            onBack = { onStepChanged(OnboardingStep.EDUCATION_FORM) },
            onCourseSelected = { course ->
                selectedCourse = course
                stateManager.saveCourse(course)
                onStepChanged(OnboardingStep.GROUP)
            }
        )
        OnboardingStep.GROUP -> {
            // Проверяем, что все параметры выбраны перед переходом к выбору группы
            if (selectedFacultyCode != null && selectedEducationForm != null && selectedCourse != null && selectedCourse!! > 0) {
                GroupSelectionScreen(
                    facultyCode = selectedFacultyCode,
                    educationFormCode = selectedEducationForm,
                    course = selectedCourse,
                    onBack = { onStepChanged(OnboardingStep.COURSE) },
                    onGroupSelected = { group ->
                        onOnboardingComplete(group)
                    }
                )
            } else {
                // Если параметры не выбраны, возвращаемся к выбору курса
                CourseSelectionScreen(
                    onBack = { onStepChanged(OnboardingStep.EDUCATION_FORM) },
                    onCourseSelected = { course ->
                        selectedCourse = course
                        stateManager.saveCourse(course)
                        onStepChanged(OnboardingStep.GROUP)
                    }
                )
            }
        }
        OnboardingStep.MAIN -> { /* MAIN step is handled outside the onboarding flow */ }
    }
}