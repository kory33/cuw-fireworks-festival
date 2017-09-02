package com.github.kory33.cuwfireworksfestival

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.FireworkEffect.Type
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import java.lang.IllegalStateException
import java.util.*

private val colors = arrayOf(
        Color.TEAL,
        Color.AQUA,
        Color.NAVY,
        Color.BLUE,
        Color.LIME,
        Color.YELLOW,
        Color.GREEN,
        Color.OLIVE,
        Color.ORANGE,
        Color.FUCHSIA,
        Color.PURPLE,
        Color.RED,
        Color.MAROON,
        Color.WHITE,
        Color.SILVER,
        Color.GRAY,
        Color.BLACK
)

private val typeThresholds = arrayOf(
        Pair(Type.BALL, 0.7),
        Pair(Type.BALL_LARGE, 0.5),
        Pair(Type.BURST, 0.9),
        Pair(Type.STAR, 0.6),
        Pair(Type.CREEPER, 1.0)
)

class FireworksShow(private val plugin: JavaPlugin,
                    private val center: Location,
                    private val radius: Double,
                    private val lengthTick: Int,
                    private val timeBias: Double,
                    private val intensity: Double) {

    // parameters
    private val yRange = 15.0
    private val colorIndexChangeThreshold = 0.1
    private val fadeColorIndexChangeThreshold = 0.1

    private val skipBeginThreshold = 0.15
    private fun getMaxSkipNum() : Int {
        val t = pastTick * 1.0 / lengthTick
        return (15.0 * Math.exp(- 5.0 * Math.pow(t - 0.5, 2.0))).toInt()
    }

    private fun getSpawnThreshold() : Double {
        val t = 1 - (pastTick * 1.0 / lengthTick)

        return intensity * Math.pow(t, 0.2) * Math.exp(-timeBias * t * t)
    }

    private fun getRandomWeightedType() : Type {
        val resultType : Type? = typeThresholds
                .firstOrNull {(_, threshold) -> random.nextDouble() < threshold}?.first

        return resultType ?: throw IllegalStateException("Type could not be selected.")
    }

    private val random = Random()

    private var colorIndex = 0
    private var fadeColorIndex = 0
    private var pastTick = 0
    private var fireworksToSkip = 0

    private fun spawnRandomizedFirework(location: Location) {
        val firework = location.world.spawnEntity(location, EntityType.FIREWORK) as Firework
        val fireworkMeta = firework.fireworkMeta

        val effect = FireworkEffect.builder()
                .flicker(random.nextBoolean())
                .withColor(colors[colorIndex])
                .withFade(colors[fadeColorIndex])
                .with(getRandomWeightedType())
                .trail(random.nextBoolean())
                .build()

        fireworkMeta.addEffect(effect)
        fireworkMeta.power = random.nextInt(1) + 1

        firework.fireworkMeta = fireworkMeta
    }

    private fun shouldSpawn() : Boolean {
        val randVal = random.nextDouble()
        val threshold = getSpawnThreshold()

        return randVal < threshold
    }

    private fun updateColorIndex() {
        if (random.nextDouble() < colorIndexChangeThreshold) {
            colorIndex += 1
            colorIndex %= colors.size
        }

        if (random.nextDouble() < fadeColorIndexChangeThreshold) {
            fadeColorIndex += 1
            fadeColorIndex %= colors.size
        }
    }

    private fun handleSpawn() {
        if (shouldSpawn()) {
            if (fireworksToSkip != 0) {
                fireworksToSkip -= 1
                return
            }

            if (random.nextDouble() < skipBeginThreshold) {
                fireworksToSkip = (random.nextDouble() * getMaxSkipNum()).toInt()
                return
            }

            val r = radius * random.nextDouble()
            val theta = random.nextDouble() * 2.0 * Math.PI
            val vector = Vector(r * Math.cos(theta), random.nextDouble() * yRange, r * Math.sin(theta))
            val locVector = center.toVector().add(vector)

            spawnRandomizedFirework(Location(center.world, locVector.x, locVector.y, locVector.z))

            updateColorIndex()
        }
    }

    private fun play() {
        handleSpawn()

        if (pastTick < lengthTick) {
            pastTick += 1
            plugin.server.scheduler.scheduleSyncDelayedTask(plugin, this::play, 1)
        }
    }

    fun begin() {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, this::play, 1)
    }
}