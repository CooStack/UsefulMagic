package cn.coostack.usefulmagic.extend

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag

operator fun CompoundTag.set(key: String, value: Tag) {
    put(key, value)
}

operator fun CompoundTag.set(key: String, value: String) {
    putString(key, value)
}

operator fun CompoundTag.set(key: String, value: Double) {
    putDouble(key, value)
}

operator fun CompoundTag.set(key: String, value: Float) {
    putFloat(key, value)
}

operator fun CompoundTag.set(key: String, value: Int) {
    putInt(key, value)
}

operator fun CompoundTag.set(key: String, value: Byte) {
    putByte(key, value)
}

operator fun CompoundTag.set(key: String, value: Short) {
    putShort(key, value)
}

operator fun CompoundTag.set(key: String, value: Long) {
    putLong(key, value)
}

operator fun CompoundTag.set(key: String, value: Boolean) {
    putBoolean(key, value)
}

operator fun CompoundTag.set(key: String, value: ByteArray) {
    putByteArray(key, value)
}

operator fun CompoundTag.set(key: String, value: IntArray) {
    putIntArray(key, value)
}

operator fun CompoundTag.set(key: String, value: LongArray) {
    putLongArray(key, value)
}
