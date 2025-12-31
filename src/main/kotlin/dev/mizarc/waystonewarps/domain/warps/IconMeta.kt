import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class IconMeta(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val schemaVersion: Int = 1,
    val strings: List<String> = emptyList(),
    val floats: List<Float> = emptyList(),
    val flags: List<Boolean> = emptyList(),
    val colorsArgb: List<Int> = emptyList(),
    val potionTypeKey: String? = null,
    val leatherColorRgb: Int? = null,
    val trimPatternKey: String? = null,
    val trimMaterialKey: String? = null,
    val bannerBaseColor: String? = null,
    val bannerPatterns: List<String> = emptyList(),
    val skullTextureValue: String? = null,
    val skullTextureSignature: String? = null,
    val fireworkStarColorRgb: Int? = null
)