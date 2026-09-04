package cn.coostack.usefulmagic.command

import cn.coostack.cooparticlesapi.test.TestManager
import cn.coostack.usefulmagic.test.UsefulMagicGamingTestBuilder
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object UsefulMagicCommands {

    fun initCommand(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("usefulmagic")
                .requires { source -> source.player?.isCreative == true }
                .then(
                    Commands.literal("test")
                        .executes { context ->
                            val user = context.source.player ?: return@executes 1
                            TestManager.startTest(UsefulMagicGamingTestBuilder.ID, user)
                            1
                        }
                )
        )

    }
}
