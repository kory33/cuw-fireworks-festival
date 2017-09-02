package com.github.kory33.cuwfireworksfestival

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class CuwFireworksFestival : JavaPlugin() {
    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>): Boolean {
        if (args.size < 4) {
            return false
        }

        if (sender == null || sender !is Player) {
            sender?.sendMessage("This command is for player only")
            return true
        }

        val (radius, timeTick, timeBias, intensity) = args.map { it.toDouble() }

        FireworksShow(this, sender.location, radius, timeTick.toInt(), timeBias, intensity).begin()

        return true
    }

    override fun onEnable() {
        super.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
    }
}