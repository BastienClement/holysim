package holysim.utils

import scala.collection.mutable.ArrayBuffer

class CallbackList(oneshot: Boolean = false) {
	type Callback = () => Unit
	private var callbacks = Vector[Callback]()

	def +=(cb: => Unit) = callbacks = callbacks :+ (() => cb)
	def ~=(cb: => Unit) = callbacks = (() => cb) +: callbacks

	def execute() = callbacks.foreach(c => c())

	def flush() = {
		execute()
		callbacks = Vector()
	}

	def apply() = {
		if (oneshot) flush()
		else execute()
	}
}

class CallbackListArg[E](oneshot: Boolean = false) {
	type Callback = (E) => Unit
	private var callbacks = Vector[Callback]()

	def +=(cb: (E) => Unit) = callbacks = callbacks :+ cb
	def ~=(cb: (E) => Unit) = callbacks = cb +: callbacks

	def execute(e: E) = callbacks.foreach(c => c(e))

	def flush(e: E) = {
		execute(e)
		callbacks = Vector()
	}

	def apply(e: E) = {
		if (oneshot) flush(e)
		else execute(e)
	}
}
