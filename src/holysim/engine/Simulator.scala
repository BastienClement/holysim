package holysim.engine

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.DynamicVariable
import holysim.engine.Simulator.State

object Simulator {
	/** Create a new simulator instance using a given initialization function */
	def apply(init: => Unit) = {
		val sim = new Simulator
		current.withValue(sim) {
			init
			sim
		}
	}

	/** The currently running simulator */
	val current = new DynamicVariable[Simulator](null)

	/** Simulator states */
	object State extends Enumeration {
		final val Prepare, Running, Done, Fail = Value
	}
}

class Simulator private {
	/** Time to skip if no event are scheduled */
	var skip_time = 50

	/** Maximum time allowed for the simulation */
	var time_limit = 600000

	/** The set of actors in this simulation */
	val actors = mutable.Set[Actor]()

	/** Trigger an event, dispatching it to Proc instances */
	def trigger(event: Event) = actors.foreach(_.trigger(event))

	/** The simulator action queue */
	private val queue = mutable.PriorityQueue[ScheduledAction]()

	/** Fetch the next action from the queue */
	@tailrec
	private def nextAction: ScheduledAction = {
		if (queue.isEmpty) ScheduledAction(current_time + skip_time, DummyAction)
		else {
			val action = queue.dequeue()
			if (action.isExecutable) action
			else nextAction
		}
	}

	/** Current simulation time */
	private var current_time: Int = 0
	def time = current_time

	/** The current simulator state */
	private var current_state = State.Prepare
	def state = current_state

	def run() = {
		// Enter running state
		current_state = State.Running

		// Main loop
		while (current_state == State.Running) {
			// Fetch the next action
			val action = nextAction

			// Compute delta-time
			val dt = action.time - current_time
			assert(dt >= 0)
			current_time += dt

			// Check time limit
			if (current_time > time_limit) {
				current_state = State.Done
			} else {
				if (dt > 0) tick(dt)
				action.execute()
			}
		}
	}

	def tick(dt: Int) = {
		println("tick", dt, current_time)
	}
}
