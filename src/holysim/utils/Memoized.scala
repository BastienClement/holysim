package holysim.utils

import scala.collection.mutable
import scala.language.higherKinds

object Memoized {
	def apply[K, T](func: (K) => T): Memoized[K, T] = new Memoized(func)
}

class Memoized[K, T](func: (K) => T) {
	private[this] val cache = mutable.Map[K, T]()
	def apply(key: K) = cache.getOrElseUpdate(key, func(key))
}

abstract class HigherKindedMemoized[T, K[_], V[_]] {
	def default[A <: T](key: K[A]): V[A]

	private[this] val cache = new TypedMap[T, K, V]
	def get[A <: T](key: K[A]): V[A] = {
		if (cache.contains(key)) cache.get(key).get
		else {
			val value = default(key)
			cache.put(key, value)
			value
		}
	}
}
