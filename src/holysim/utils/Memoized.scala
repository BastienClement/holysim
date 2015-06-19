package holysim.utils

import scala.collection.mutable

object Memoized {
	def apply[K, T](func: (K) => T): Memoized[K, T] = new Memoized(func)
}

class Memoized[K, T](func: (K) => T) {
	val cache = mutable.Map[K, T]()
	def apply(key: K) = cache.getOrElseUpdate(key, func(key))
}
