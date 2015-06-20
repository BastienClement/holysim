package holysim.utils

import scala.collection.mutable.ArrayBuffer

class CallbackList(oneshot: Boolean = false) {
	type Callback = () => Unit
	private var callbacks = ArrayBuffer[Callback]()

	def +=(cb: => Unit) = callbacks += (() => cb)

	def execute() = callbacks.foreach(c => c())

	def flush() = {
		execute()
		callbacks.clear()
	}

	def apply() = {
		if (oneshot) flush()
		else execute()
	}
}

class CallbackListArg[E](oneshot: Boolean = false) {
	type Callback = (E) => Unit
	private var callbacks = ArrayBuffer[Callback]()

	def +=(cb: (E) => Unit) = callbacks += cb

	def execute(e: E) = callbacks.foreach(c => c(e))

	def flush(e: E) = {
		execute(e)
		callbacks.clear()
	}

	def apply(e: E) = {
		if (oneshot) flush(e)
		else execute(e)
	}
}
