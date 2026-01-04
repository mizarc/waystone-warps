package dev.mizarc.waystonewarps.infrastructure.localization

import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.application.services.PlayerLocaleService
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import java.io.File
import java.text.MessageFormat
import java.util.Locale
import java.util.Properties
import java.util.UUID

class PropertiesLocalizationProvider(private val config: ConfigService,
private val dataFolder: File,
private val playerLocaleService: PlayerLocaleService
): LocalizationProvider {
    private val languages: MutableMap<String, Properties> = mutableMapOf()
    private val baseDefaultLanguageCode = "en"

    init {
        loadLayeredProperties()
    }

    override fun get(playerId: UUID, key: String, vararg args: Any?): String {
        val locale = playerLocaleService.getLocale(playerId)
        return fetchMessageString(locale, key, *args)

    }

    override fun getConsole(key: String, vararg args: Any?): String {
        return fetchMessageString(config.getPluginLanguage(), key, *args)
    }

    private fun fetchMessageString(locale: String, key: String, vararg args: Any?): String {
        // Try to get the string from the exact requested language code first
        languages[locale]?.getProperty(key)?.let { pattern ->
            return formatPattern(pattern, key, *args)
        }

        // If not found, try the base language (e.g., "en" from "en_UK")
        val requestedLocale = Locale.forLanguageTag(locale.replace('_', '-'))
        val baseLanguage = requestedLocale.language
        
        if (baseLanguage != locale && baseLanguage.isNotEmpty()) {
            languages[baseLanguage]?.getProperty(key)?.let { pattern ->
                return formatPattern(pattern, key, *args)
            }
        }

        // If still not found, try the server's configured default language
        val defaultLang = config.getPluginLanguage()
        if (defaultLang != locale && defaultLang != baseLanguage) {
            languages[defaultLang]?.getProperty(key)?.let { pattern ->
                return formatPattern(pattern, key, *args)
            }
        }

        // If still not found, try the hardcoded base default language ("en")
        if (defaultLang != baseDefaultLanguageCode && baseLanguage != baseDefaultLanguageCode) {
            languages[baseDefaultLanguageCode]?.getProperty(key)?.let { pattern ->
                return formatPattern(pattern, key, *args)
            }
        }

        // If we get here, the key wasn't found in any of the fallback languages
        return key
    }

    private fun formatPattern(pattern: String, key: String, vararg args: Any?): String {
        return try {
            if (args.isNotEmpty()) {
                // If arguments are provided (the args array is not null and not empty), attempt to format the string.
                MessageFormat.format(pattern, *args)
            } else {
                // If no arguments were provided to the vararg, just return the raw pattern string.
                pattern
            }
        } catch (_: IllegalArgumentException) {
            // Handle potential formatting errors (e.g., incorrect number/type of args for placeholders).
            println("Failed to format localization key '$key' with arguments: ${args.joinToString()}")
            pattern
        } catch (e: Exception) {
            // Catch any other unexpected exceptions during formatting.
            println(
                "An unexpected error occurred while formatting localization with arguments: " +
                        "${args.joinToString()} - ${e.message}"
            )
            pattern
        }
    }

    // Private function to handle the layered loading process
    private fun loadLayeredProperties() {
        val langFolder = File(dataFolder, "lang")
        val defaultsFolder = File(langFolder, "defaults")
        val overridesFolder = File(langFolder, "overrides")

        // Find all language codes present in the defaults and overrides folders
        val availableLanguages = findAvailableLanguages(defaultsFolder, overridesFolder)

        availableLanguages.forEach { locale ->
            val properties = Properties()

            // Layer 1: If the requested language is different from base, load its default version
            val specificDefaultFile = File(defaultsFolder, "$locale.properties")
            if (specificDefaultFile.exists()) {
                try {
                    specificDefaultFile.reader(Charsets.UTF_8).use { properties.load(it) }
                    println("Loaded language: $locale")
                } catch (_: Exception) {
                    println("Failed to load default language file for $locale")
                }
            }

            // Layer 2: Load the override language file
            val overrideFile = File(overridesFolder, "$locale.properties")
            if (overrideFile.exists()) {
                try {
                    overrideFile.reader(Charsets.UTF_8).use { properties.load(it) }
                    println("Loaded override language file: $locale")
                } catch (_: Exception) {
                    println("Failed to load override language file for $locale")
                }
            }

            languages[locale] = properties
        }
    }

    private fun findAvailableLanguages(defaultsFolder: File, overridesFolder: File): Set<String> {
        val codes = mutableSetOf<String>()

        // Scan defaults folder for .properties files
        defaultsFolder.listFiles { file -> file.isFile && file.extension == "properties" }?.forEach { file ->
            codes.add(file.nameWithoutExtension)
        }

        // Scan overrides folder for .properties files
        // Overrides might introduce new language codes or just override existing ones
        overridesFolder.listFiles { file -> file.isFile && file.extension == "properties" }?.forEach { file ->
            codes.add(file.nameWithoutExtension)
        }

        return codes
    }
}
