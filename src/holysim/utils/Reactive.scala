package holysim.utils

import scala.collection.mutable
import scala.language.implicitConversions
import scala.util.DynamicVariable

object Reactive {
	def apply[T](generator: => T) = new Reactive(generator)()
	def apply[T](deps: Reactive[T]*)(generator: => T) = new Reactive(generator)(deps: _*)

	implicit def extractor[T](r: Reactive[T]): T = r.value
	implicit def intractor[T](v: T): Reactive[T] = apply(v)

	val caller = new DynamicVariable[Reactive[_]](null)
}

class Reactive[T](generator: => T)(deps: Reactive[_]*) {
	// Bind this Reactive to specified dependencies
	deps.foreach(_ ~> this)

	/** The current cached value of this Reactive */
	private[this] var opt_val: Option[T] = None

	/** Enable automatic dependencies tracing */
	private[this] var trace: Boolean = deps.isEmpty

	/** List of children to notify when our value is updated */
	private[this] val children = mutable.WeakHashMap[Reactive[_], Null]()

	/**
	 * Lazily compute this reactive value
	 */
	def value: T = {
		// Fetch the Reactive calling this one internally
		val caller = Reactive.caller.value

		// If there is a caller attempting to trace dependencies
		if (caller != null) {
			children.put(caller, null)
		}

		opt_val match {
			// Cached value available
			case Some(v) => v

			// No value available yet
			case None =>
				if (trace) {
					trace = false
					Reactive.caller.value = this
				} else {
					Reactive.caller.value = null
				}

				// Safely generate the value
				val v = try generator finally Reactive.caller.value = caller

				// Cache the value
				opt_val = Some(v)
				v
		}
	}

	/** Manually define a new value */
	def :=(v: T) = {
		invalidate()
		opt_val = Some(v)
	}

	/** Update the value by applying a function to it, return the new value */
	def ~(fn: T => T): T = {
		val v = value
		val nv = fn(v)
		if (v != nv) this := nv
		nv
	}

	/** Attach this Reactive to another one, it will then be notified when our value is invalidated */
	def ~>(c: Reactive[_]) = {
		children.put(c, null)
		c.invalidate()
	}

	/** Detach this Reactive from another one previously bound by ~> */
	def ~/>(c: Reactive[_]) = {
		children.remove(c)
		c.invalidate()
	}

	/** Invalidate the current value and notify children */
	def invalidate(): Unit = if (opt_val.isDefined) {
		opt_val = None
		children foreach {
			case (k, v) => k.invalidate()
		}
	}

	/** Force the next evaluation to be traced */
	def traceNext(): Unit = {
		trace = true
	}

	override def toString = s"Reactive{$value}"
}
