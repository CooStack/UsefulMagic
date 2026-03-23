package cn.coostack.usefulmagic.extend

import net.minecraft.nbt.ByteArrayTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.IntArrayTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.LongArrayTag
import net.minecraft.nbt.NumericTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag

val Tag?.asString: String?
    get() = (this as? StringTag)?.asString

val Tag?.asDouble: Double?
    get() = (this as? NumericTag)?.asDouble

val Tag?.asFloat: Float?
    get() = (this as? NumericTag)?.asFloat

val Tag?.asInt: Int?
    get() = (this as? NumericTag)?.asInt

val Tag?.asByte: Byte?
    get() = (this as? NumericTag)?.asByte

val Tag?.asShort: Short?
    get() = (this as? NumericTag)?.asShort

val Tag?.asLong: Long?
    get() = (this as? NumericTag)?.asLong

val Tag?.asBoolean: Boolean?
    get() = (this as? NumericTag)?.asByte?.let { it.toInt() != 0 }

val Tag?.asByteArray: ByteArray?
    get() = (this as? ByteArrayTag)?.asByteArray

val Tag?.asIntArray: IntArray?
    get() = (this as? IntArrayTag)?.asIntArray

val Tag?.asLongArray: LongArray?
    get() = (this as? LongArrayTag)?.asLongArray

val Tag?.asCompound: CompoundTag?
    get() = this as? CompoundTag

val Tag?.asList: ListTag?
    get() = this as? ListTag
