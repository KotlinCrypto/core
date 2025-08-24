/*
 * Copyright (c) 2023 KotlinCrypto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
plugins {
    id("configuration")
}

repositories { google() }

kmpConfiguration {
    configure {
        androidLibrary {
            android {
                buildToolsVersion = "35.0.1"
                compileSdk = 35
                namespace = "org.kotlincrypto.core"

                defaultConfig {
                    minSdk = 14

                    testInstrumentationRunnerArguments["disableAnalytics"] = true.toString()
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
            }

            sourceSetTest {
                kotlin.srcDir("src/androidUnitTest/digest")
                kotlin.srcDir("src/androidUnitTest/mac")
            }

            sourceSetTestInstrumented {
                kotlin.srcDir("src/androidInstrumentedTest/digest")
                kotlin.srcDir("src/androidInstrumentedTest/mac")

                dependencies {
                    implementation(libs.androidx.test.runner)
                }
            }
        }

        common {
            sourceSetTest {
                dependencies {
                    implementation(kotlin("test"))
                    implementation(project(":library:digest"))
                    implementation(project(":library:mac"))
                }
            }
        }
    }
}
