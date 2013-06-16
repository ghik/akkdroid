package com.akkdroid.util

import java.{lang => jl, util => ju}

class EnumerationIterator[T](enumeration: ju.Enumeration[T]) extends ju.Iterator[T] {
  def hasNext: Boolean = enumeration.hasMoreElements

  def next(): T = enumeration.nextElement()

  def remove() {
    throw new UnsupportedOperationException
  }
}
