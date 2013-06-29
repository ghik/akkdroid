package com.akkdroid.client

import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}

object JavaSerializer {
  def serialize(obj: Any): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val oos = new ObjectOutputStream(baos)
    oos.writeObject(obj)
    baos.toByteArray
  }

  def deserialize(bytes: Array[Byte]): Any = {
    val bais = new ByteArrayInputStream(bytes)
    val ois = new ObjectInputStream(bais)
    ois.readObject()
  }
}
