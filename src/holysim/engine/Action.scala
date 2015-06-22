package holysim.engine

object Action {
	def apply(body: () => Unit) = new CallbackAction(body)

	class CallbackAction private[Action] (body: () => Unit) extends Action {
		def execute() = body()
	}
}

trait Action {
	def execute(): Unit
}

object DummyAction extends Action {
	def execute() = {}
}

case class ScheduledAction(time: Int, action: Action) extends Ordered[ScheduledAction] {
	override def compare(that: ScheduledAction): Int = that.time - time

	private var executable = true
	def isExecutable = executable

	def execute() = if (executable) {
		executable = false
		action.execute()
	}

	def cancel() = {
		executable = false
	}
}
