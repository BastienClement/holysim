package holysim.engine

import scala.language.implicitConversions

/**
 * An action is something that can be executed by the simulator
 */
trait Action {
	def execute(): Unit
}

/**
 * Basic action classes
 */
object Action {
	/**
	 * Callback action constructor
	 */
	def apply(body: () => Unit) = new CallbackAction(body)

	/**
	 * A callback action receive its execution function as a constructor parameter
	 */
	class CallbackAction private[Action](body: () => Unit) extends Action {
		def execute() = body()
	}
}

/**
 * Once scheduled to occur at a specific simulation time, the action is wrapped into a ScheduledAction
 * This wrapper provides the ability to cancel the action execution
 */
case class ScheduledAction[+T <: Action](time: Int, action: T)(implicit val sim: Simulator) extends Ordered[ScheduledAction[Action]] {
	/**
	 * Define the ScheduledAction ordering, based on execution time
	 */
	override def compare(that: ScheduledAction[Action]): Int = that.time - time

	/**
	 * The wrapped action will never be executed if this field is false
	 */
	private[this] var executable = true
	def isExecutable = executable

	/**
	 * Execute the inner action, provided that this ScheduledAction is still executable
	 */
	def execute() = if (executable) {
		executable = false
		action.execute()
	}

	/**
	 * Prevent future execution of this action
	 */
	def cancel() = {
		executable = false
	}

	/**
	 * Reschedule the same action at a later time
	 * The current action is cancelled
	 */
	def reschedule(dt: Int) = {
		executable = false
		sim.schedule(dt, action)
	}
}
