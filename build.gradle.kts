// // Top-level build file where you can add configuration options common to all sub-projects/modules.
// plugins {
// alias(libs.plugins.android.application) apply false
//     alias(libs.plugins.android.library) apply false
//     alias(libs.plugins.openapi.generator) apply true
// }

// // plugins {
// //     alias(libs.openapi.generator)
// // }

// openApiGenerate {
//     remoteInputSpec = "http://127.0.0.1:13745/swagger/json"
//     generatorName = 'kotlin'
//     outputDir = "generated_integration_test_openapi"
//     generateModelTests = false
//     generateApiTests = false
//     generateModelDocumentation = false
//     generateApiDocumentation = false
//     aditionalProperties.apply {
// 		put("omitGradlePluginVersions", true)
// 		put("omitGradleWrapper", true)
// 	}
// }
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.openapi.generator) apply true
}

// Equivalent of openApiGenerate in Kotlin DSL
openApiGenerate {
    remoteInputSpec.set("http://127.0.0.1:13745/swagger/json")
    generatorName.set("kotlin")
    outputDir = projectDir.path + "/generated_integration_test_openapi"
    generateModelTests.set(false)
    generateApiTests.set(false)
    generateModelDocumentation.set(false)
    generateApiDocumentation.set(false)
    additionalProperties.put("omitGradlePluginVersions", true)
    additionalProperties.put("omitGradleWrapper", true)
}
