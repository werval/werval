/**
 * Copyright (c) 2013 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.werval.gradle

import io.werval.commands.SecretCommand
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * Output a newly generated Application secret.
 */
class SecretTask extends DefaultTask
{
    @TaskAction
    void generateNewSecret()
    {
        project.logger.lifecycle ">> Generate new Werval Application Secret"
        // Reflective call to prevent JDK8/ASM headache with Gradle Groovy Compiler
        // Should be rewritten once Gradle use ASM 5
        // new SecretCommand().run()
        Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass( "io.werval.commands.SecretCommand" )
        Object secretCommand = clazz.newInstance()
        clazz.getDeclaredMethod( "run" ).invoke( secretCommand )
    }
}
