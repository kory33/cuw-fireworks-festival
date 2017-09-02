package com.github.kory33.cuwfireworksfestival

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.FireworkEffect.Type
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
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

class FireworksShow(private val plugin: JavaPlugin,
                    private val center: Location,
                    private val radius: Double,
                    private val lengthTick: Int) {

    // parameters
    private val timeBias = 8.0
    private val yRange = 15.0
    private val intensity = 0.5
    private val colorIndexChangeThreshold = 0.1
    private val fadeColorIndexChangeThreshold = 0.2

    private val random = Random()

    private var colorIndex = 0
    private var fadeColorIndex = 0
    private var pastTick = 0

    private fun spawnRandomizedFirework(location: Location) {
        val firework = location.world.spawnEntity(location, EntityType.FIREWORK) as Firework
        val fireworkMeta = firework.fireworkMeta

        val type = Type.values()[random.nextInt(Type.values().size)]

        val effect = FireworkEffect.builder()
                .flicker(random.nextBoolean())
                .withColor(colors[colorIndex])
                .withFade(colors[fadeColorIndex])
                .with(type)
                .trail(random.nextBoolean())
                .build()

        fireworkMeta.addEffect(effect)
        fireworkMeta.power = random.nextInt(1) + 1

        firework.fireworkMeta = fireworkMeta
    }

    private fun shouldSpawn() : Boolean {
        val randVal = random.nextDouble()
        val t = 1 - (pastTick * 1.0 / lengthTick)

        val threshold = intensity * Math.pow(t, 0.2) * Math.exp(-timeBias * t * t)

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

    private fun play() {
        if (shouldSpawn()) {
            val r = radius * random.nextDouble()
            val theta = random.nextDouble() * 2.0 * Math.PI
            val vector = Vector(r * Math.cos(theta), random.nextDouble() * yRange, r * Math.sin(theta))
            val locVector = center.toVector().add(vector)

            spawnRandomizedFirework(Location(center.world, locVector.x, locVector.y, locVector.z))

            updateColorIndex()
        }

        if (pastTick < lengthTick) {
            pastTick += 1
            plugin.server.scheduler.scheduleSyncDelayedTask(plugin, this::play, 1)
        }
    }

    fun begin() {
        plugin.server.scheduler.scheduleSyncDelayedTask(plugin, this::play, 1)
    }
}