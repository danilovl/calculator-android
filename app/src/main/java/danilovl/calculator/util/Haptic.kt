package danilovl.calculator.util

import android.content.Context
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class HapticType(val primitive: Int, val predefined: Int) {
    TICK(VibrationEffect.Composition.PRIMITIVE_TICK, VibrationEffect.EFFECT_TICK),
    LOW_TICK(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, VibrationEffect.EFFECT_TICK),
    CLICK(VibrationEffect.Composition.PRIMITIVE_CLICK, VibrationEffect.EFFECT_CLICK),
    HEAVY(VibrationEffect.Composition.PRIMITIVE_THUD, VibrationEffect.EFFECT_HEAVY_CLICK)
}

object Haptic {

    private const val DEFAULT_INTENSITY = 0.50f
    private val DEFAULT_TYPE = HapticType.CLICK

    fun tick(
        context: Context,
        intensity: Float = DEFAULT_INTENSITY,
        type: HapticType = DEFAULT_TYPE
    ) {
        val vibrator = vibrator(context) ?: return
        if (!vibrator.hasVibrator()) {
            return
        }

        val clamped = intensity.coerceIn(0f, 1f)
        if (clamped == 0f) {
            return
        }

        val effect = buildEffect(vibrator, clamped, type)
        val attributes = VibrationAttributes.Builder()
            .setUsage(VibrationAttributes.USAGE_TOUCH)
            .build()
        
        vibrator.vibrate(effect, attributes)
    }

    private fun buildEffect(vibrator: Vibrator, intensity: Float, type: HapticType): VibrationEffect {
        if (vibrator.areAllPrimitivesSupported(type.primitive)) {
            return VibrationEffect.startComposition()
                .addPrimitive(type.primitive, intensity)
                .compose()
        }

        return VibrationEffect.createPredefined(type.predefined)
    }

    private fun vibrator(context: Context): Vibrator? {
        val manager = context.getSystemService(VibratorManager::class.java)

        return manager?.defaultVibrator
    }
}
