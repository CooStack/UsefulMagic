package cn.coostack.usefulmagic.items.prop

import cn.coostack.cooparticlesapi.sound.ServerSoundManager
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.dragon.eye.MagicEyeEntity
import cn.coostack.usefulmagic.entity.custom.dragon.spawn.DragonSpawner
import cn.coostack.usefulmagic.extend.searchEntities
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.Heightmap

class MagicEyeSpawner(properties: Properties) : Item(properties) {

    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (level.dimension() != Level.END) {
            return InteractionResultHolder.pass(ItemStack.EMPTY)
        }

        player.startUsingItem(usedHand)
        return super.use(level, player, usedHand)
    }


    override fun finishUsingItem(stack: ItemStack, level: Level, livingEntity: LivingEntity): ItemStack {
        if (level.isClientSide) {
            return stack
        }
        val spawnPos = level.getHeightmapPos(
            Heightmap.Types.MOTION_BLOCKING,
            BlockPos(0, 0, 0)
        ).center.add(0.0, 2.0, 0.0)


        val canSpawned = level.searchEntities<Mob>(spawnPos, 512.0) {
            it is EnderDragon || it is MagicDragonEntity || it is MagicEyeEntity
        }.isEmpty() && !DragonSpawner.wasSpawningAt(level, spawnPos)
        if (canSpawned) {
            level.addFreshEntity(
                MagicEyeEntity(level)
                    .apply {
                        setPos(spawnPos)
                    }
            )
            // 放一个音效
            ServerSoundManager.instance(
                UsefulMagicSoundEvents.EYE_TELEPORT.get(),
                SoundSource.PLAYERS
            ).bindToEntity(livingEntity)
                .position(spawnPos)
                .uniqueKey()
                .visibleRange(256.0)
                .spawn()
            // 然后-1
            if (!livingEntity.hasInfiniteMaterials()) {
                stack.shrink(1)
            }
        } else {
            ServerSoundManager.instance(
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS
            ).volume(0.7f)
                .self(true)
                .bindToEntity(livingEntity)
                .spawn()
        }
        if (livingEntity is Player) {
            livingEntity.cooldowns.addCooldown(this, 20)
        }
        return stack
    }


    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 20
    }

}