package io.github.xfreskix.luxdtypewriter.style

import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Negative
import com.typewritermc.engine.paper.utils.Color

/** @param image blank means the stock image, or hidden for optional layers. */
data class LuxImageLayer(
    @Help(IMAGE_HELP)
    val image: String = "",
    @Help("Tint applied to the image. White keeps the original colours.")
    val color: Color = Color.WHITE,
    @Negative
    @Help("Horizontal offset in pixels. Negative values shift the image to the left.")
    val offset: Int = 0,
)

private const val IMAGE_HELP = "Image id from LuxDialogues' images.yml. Empty uses the stock image."
