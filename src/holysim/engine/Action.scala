package holysim.engine

import scala.language.implicitConversions

trait Action {
	def execute(): Unit
}

object Action {
	def apply(body: () => Unit) = new CallbackAction(body)

	class CallbackAction private[Action](body: () => Unit) extends Action {
		def execute() = body()
	}
}

trait ActorAction extends Action {
	val owner: Actor

	def available: Boolean
	def begin() : ScheduledAction

	def execute_action(): Unit
	final def execute() = {
		execute_action()
		if (owner.current_action.action == this) {
			owner.current_action = null
			owner.select()
		}
	}
}

object ActorAction {
	/**
	 * Spell casting action
	 */
	case class Cast(spell: Spell)(implicit val owner: Actor) extends ActorAction {
		// Default to cast on self
		private[this] var target_query: QueryableActor = owner

		// Requirement for this action
		private[this] var requirement: (Actor) => Boolean = (a) => true

		def on(t: QueryableActor) = {
			target_query = t
			this
		}

		def when(req: => Boolean) = {
			requirement = (_) => req
			this
		}

		def when(req: (Actor) => Boolean) = {
			requirement = (a) => req(a)
			this
		}

		def available = target_query.get match {
			case Some(target) => requirement(target) && spell.available(target)
			case None => false
		}

		// Must call TargettedCast.execute instead
		def execute_action() = ???

		def begin() = owner.sim.schedule(spell.castTime, TargettedCast(spell, target_query.get.get))
	}

	case class TargettedCast(spell: Spell, target: Actor)(implicit val owner: Actor) extends ActorAction {
		val available = true
		def begin() = ???
		def execute_action() = spell.cast(target)
	}

	case class Wait(time: Int)(implicit val owner: Actor) extends ActorAction {
		def available = true
		def duration = time
		def begin() = owner.sim.schedule(time, this)
		def execute_action() = {}
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
