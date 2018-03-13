package com.github.plokhotnyuk.jsoniter_scala.core

import scala.util.Try

// FIXME: remove when perf. degradation of String.charAt when iterating through strings will be fixed in JDK 9:
// https://bugs.openjdk.java.net/browse/JDK-8013655
object UnsafeUtils {
  private[this] final val (unsafe, stringValueOffset, stringCoderOffset) = Try {
    val u = {
      val unsafeClass = classOf[sun.misc.Unsafe]
      val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
      unsafeField.setAccessible(true)
      unsafeClass.cast(unsafeField.get(null))
    }
    (u,
      u.objectFieldOffset(classOf[String].getDeclaredField("value")),
      u.objectFieldOffset(classOf[String].getDeclaredField("coder")))
  }.getOrElse((null, 0L, 0L))

  private[jsoniter_scala] final def getLatin1Array(s: String): Array[Byte] =
    if (stringCoderOffset == 0 || (s eq null) || unsafe.getByte(s, stringCoderOffset) != 0) null
    else unsafe.getObject(s, stringValueOffset).asInstanceOf[Array[Byte]]
}
