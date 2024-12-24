package cn.li.router.plugins

import cn.li.router.plugins.tasks.RouterTransformTask
import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * @author suweikai 2024/12/17
 */
class LiRouterPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)

        project.extensions.getByType(AndroidComponentsExtension::class.java).let { extension ->

            extension.onVariants { variant ->
                val taskName = "LiRouter${variant.name}TransformTask"
                val taskProvider = project.tasks.register(taskName, RouterTransformTask::class.java) {

                }
                variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                    .use(taskProvider)
                    .toTransform(
                        ScopedArtifact.CLASSES,
                        RouterTransformTask::allJars,
                        RouterTransformTask::allDirs,
                        RouterTransformTask::outputJar
                    )
            }

        }

    }
}