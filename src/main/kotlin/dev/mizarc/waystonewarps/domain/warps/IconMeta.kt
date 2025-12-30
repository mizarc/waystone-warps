import kotlinx.serialization.Serializable

@Serializable
data class IconMeta(
    val schemaVersion: Int = 1,
    val strings: List<String> = emptyList(),
    val floats: List<Float> = emptyList(),
    val flags: List<Boolean> = emptyList(),
    val colorsArgb: List<Int> = emptyList()
)