package holysim.engine

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.DynamicVariable
import holysim.engine.Action.CallbackAction
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

	/** If true, print generated events */
	var debug = false

	/** Simulator states */
	object State extends Enumeration {
		final val Prepare, Running, Done = Value
		final val EmptyQueueFail = Value
	}
}

class Simulator private {
	/** Maximum time allowed for the simulation */
	var time_limit = 600000

	/** The set of actors in this simulation */
	val actors = mutable.Set[Actor]()

	/** The combat log for this simulation */
	val log = new CombatLog(this)

	/** Format an event for display */
	private[this] def formatEvent(event: Event) = {
		val prepare = event.toString.replace("Event(", ",").replaceFirst("\\)$", "")
		val parts = prepare.split(",").zipWithIndex
		val formatted = parts.map { case (part, index) =>
			val width = index match {
				case 0 => 20
				case 1 => 20
				case default => 10
			}
			s"%-${width}s".format(part).slice(0, width.abs)
		}
		"[%7.3f]  %s".format(current_time / 1000.0, formatted.mkString("  "))
	}

	/** Trigger an event, dispatching it to Proc instances */
	def trigger(event: Event) = {
		if (Simulator.debug) println(formatEvent(event))
		log += event
		actors.foreach(_.trigger(event))
	}

	/** The simulator action queue */
	private val queue = mutable.PriorityQueue[ScheduledAction]()

	/** Add an action to the queue */
	def after(time: Int)(cb: => Unit): ScheduledAction = schedule(time, () => cb)
	def schedule(time: Int, cb: () => Unit): ScheduledAction = {
		val action = ScheduledAction(current_time + time, Action(cb))
		queue.enqueue(action)
		action
	}

	/** Fetch the next action from the queue */
	@tailrec
	private def nextAction: ScheduledAction = {
		if (queue.isEmpty) null
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

	def run() = Simulator.current.withValue(this) {
		// Prepare actors
		actors.foreach(_.onPrepare())

		// Enter running state
		current_state = State.Running
		this.trigger(SimulationBeginEvent())

		// Initial tick to bootstrap actors
		tick(0)

		// Main loop
		while (current_state == State.Running) {
			// Fetch the next action
			val action = nextAction

			if (action == null) {
				// No action to execute, failure
				current_state = State.EmptyQueueFail
			} else {
				// Compute delta-time
				val dt = action.time - current_time
				assert(dt >= 0)

				// Check time limit
				if (current_time + dt > time_limit) {
					current_state = State.Done
				} else {
					if (dt > 0) {
						current_time += dt
						tick(dt)
					}
					action.execute()
				}
			}
		}

		this.trigger(SimulationEndEvent(current_state))
		log
	}

	def tick(dt: Int) = {
		//println("tick", dt, current_time)
	}
}
