package holysim.engine

import scala.collection.mutable
import scala.language.implicitConversions
import scala.reflect.ClassTag
import holysim.utils.{Reactive, Memoized}

object Proc {
	trait Target {
		this: Actor =>

		/**
		 * The cache of all applied aura effects
		 */
		val procs = mutable.Set[Proc]()

		def trigger(event: Event) = procs.foreach(_.trigger(event))
	}

	trait Check extends Proc {
		def check: PartialFunction[Event, Boolean]
		abstract override def trigger(event: Event) = if (check.applyOrElse(event, (_: Event) => false)) super.trigger(event)
	}
}

abstract class Proc {
	/**
	 * Attempt to trigger a proc from an event
	 */
	def trigger(event: Event): Unit
	def trigger(): Unit = trigger(null)
}
