package holysim.utils

import scala.collection.mutable
import scala.language.higherKinds

class TypedMap[T, K[_], V[_]] {
	private val data = mutable.Map[K[_], V[_]]()
	def get[A <: T](key: K[A]): Option[V[A]] = data.get(key).asInstanceOf[Option[V[A]]]
	def put[A <: T](key: K[A], value: V[A]): Unit = data.put(key, value)
	def contains[A <: T](key: K[A]): Boolean = data.contains(key)
}

class DirectTypedMap[K, V[_]] {
	private val data = mutable.Map[K, V[_]]()
	def get[A <: K](key: A): Option[V[A]] = data.get(key).asInstanceOf[Option[V[A]]]
	def put[A <: K](key: A, value: V[A]): Unit = data.put(key, value)
	def contains[A <: K](key: A): Boolean = data.contains(key)
}
