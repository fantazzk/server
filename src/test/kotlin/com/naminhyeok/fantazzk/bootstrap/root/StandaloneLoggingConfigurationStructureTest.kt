package com.naminhyeok.fantazzk.bootstrap.root

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.w3c.dom.Element
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

class StandaloneLoggingConfigurationStructureTest {
    @Test
    fun `루트 애플리케이션은 default와 non-default 프로필 모두에서 콘솔 로깅을 정의한다`() {
        standaloneLoggingConfigurations().forEach { path ->
            val profilesByName = parseProfiles(path)

            assertThat(profilesByName.keys)
                .describedAs("%s should define both default and non-default logging profiles", path)
                .contains("default", "!default")

            assertDefaultProfileUsesConsoleAppender(path, profilesByName.getValue("default"))
            assertNonDefaultProfileUsesStructuredConsoleAppender(path, profilesByName.getValue("!default"))
        }
    }

    private fun assertDefaultProfileUsesConsoleAppender(
        path: Path,
        profile: Element,
    ) {
        val appenderNames =
            profile.childElements("appender")
                .mapNotNull { it.getAttribute("name").takeIf(String::isNotBlank) }

        assertThat(appenderNames)
            .describedAs("%s default profile should register a CONSOLE appender", path)
            .contains("CONSOLE")

        assertRootReferencesConsole(path, profile)
    }

    private fun assertNonDefaultProfileUsesStructuredConsoleAppender(
        path: Path,
        profile: Element,
    ) {
        val includes =
            profile.childElements("include")
                .mapNotNull { it.getAttribute("resource").takeIf(String::isNotBlank) }

        assertThat(includes)
            .describedAs("%s non-default profile should import Boot's structured console appender", path)
            .contains("org/springframework/boot/logging/logback/structured-console-appender.xml")

        assertRootReferencesConsole(path, profile)
    }

    private fun assertRootReferencesConsole(
        path: Path,
        profile: Element,
    ) {
        val root = profile.childElements("root").singleOrNull()

        assertThat(root)
            .describedAs("%s profile %s should declare a root logger", path, profile.getAttribute("name"))
            .isNotNull

        val appenderRefs =
            root!!.childElements("appender-ref")
                .mapNotNull { it.getAttribute("ref").takeIf(String::isNotBlank) }

        assertThat(appenderRefs)
            .describedAs("%s profile %s root logger should reference CONSOLE", path, profile.getAttribute("name"))
            .contains("CONSOLE")
    }

    private fun parseProfiles(path: Path): Map<String, Element> {
        val document =
            DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(path.toFile())

        return document.documentElement
            .childElements("springProfile")
            .associateBy { it.getAttribute("name") }
    }

    private fun standaloneLoggingConfigurations(): List<Path> =
        listOf(
            Path.of("src/main/resources/logback-spring.xml"),
        )

    private fun Element.childElements(tagName: String): List<Element> =
        childNodes
            .toElementList()
            .filter { it.tagName == tagName }

    private fun org.w3c.dom.NodeList.toElementList(): List<Element> =
        (0 until length)
            .map(::item)
            .filterIsInstance<Element>()
}
