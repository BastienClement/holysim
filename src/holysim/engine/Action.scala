package holysim.engine

import scala.language.implicitConversions
import holysim.paladin.Paladin

trait Action {
	def execute(): Unit
}

object Action {
	def apply(body: () => Unit) = new CallbackAction(body)

	class CallbackAction private[Action] (body: () => Unit) extends Action {
		def execute() = body()
	}
}

trait ActorAction extends Action {
	def available: Boolean
}

object ActorAction {
	/**
	 * Spell casting action
	 */
	case class Cast(spell: Spell)(implicit val owner: Actor) extends ActorAction {
		// Default to cast on self
		private[this] var target: QueryableActor = owner

		def on(t: QueryableActor) = {
			target = t
			this
		}

		def execute() = {

		}

		def available = false
	}
}

case class ActorPriorityList(actions: ActorAction*) {
	def select = actions.find(_.available)
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
