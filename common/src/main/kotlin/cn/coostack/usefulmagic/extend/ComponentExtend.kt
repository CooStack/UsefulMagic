package cn.coostack.usefulmagic.extend

import net.minecraft.network.chat.Component

fun MutableList<Component?>.addMultilineTranslatable(key: String, vararg args: Any) {
    Component.translatable(key, *args).string
        .lineSequence()
        .forEach { line -> add(Component.literal(line)) }
}