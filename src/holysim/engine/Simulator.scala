package holysim.engine

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.DynamicVariable
import holysim.engine.Action.CallbackAction
import holysim.engine.Simulator.State

object Simulator {
	/**
	 * Create a new simulator instance using a given initialization function
	 */
	def apply(init: => Unit) = {
		val sim = new Simulator
		current_dyn.withValue(sim) {init}
		sim
	}

	/**
	 * The currently running simulator
	 */
	private val current_dyn = new DynamicVariable[Simulator](null)

	/**
	 * Accessor for current simulator
	 */
	def current = current_dyn.value

	/**
	 * If true, print generated events
	 */
	var debug = false

	/**
	 * Simulator states
	 */
	object State extends Enumeration {
		final val Prepare, Running, Done = Value
		final val EmptyQueueFail = Value
	}
}

class Simulator private {
	// Implicit reference to self, for implicits arguments
	implicit private val self = this

	/**
	 * Maximum time allowed for the simulation
	 */
	var time_limit = 600000

	/**
	 * The set of actors in this simulation
	 */
	val actors = mutable.Set[Actor]()

	/**
	 * The same actor set, but as a vector for performances
	 */
	lazy val actors_pool = actors.toVector

	/**
	 * Prepare the simulation roster by adding dummy tank / heal / dps players
	 */
	private def prepareRoster() = {
		for (i <- 1 to 2) new TankDummy(i)
		for (i <- 1 to 3) new HealerDummy(i)
		for (i <- 1 to 10) new DamageDummy(i)
	}

	/**
	 * The combat log of this simulation
	 */
	val log = new CombatLog(this)

	/**
	 * Format an event for display
	 */
	private def formatEvent(event: Event) = {
		val prepare = event.toString.replaceFirst("Event\\(", ",").replaceFirst("\\)$", "").replaceAll("\\(.*\\)", "")
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

	/**
	 * Trigger an event, dispatching it to Proc instances and logging it in the CombatLog
	 */
	def trigger(event: Event) = {
		// Print if debug is enabled
		if (Simulator.debug) {
			println(formatEvent(event))
		}

		// Log the event in the CombatLog
		log += event

		// Dispatch the event to each actor and let them do the actual handling
		actors.foreach(_.trigger(event))
	}

	/**
	 * The simulator action queue
	 */
	private val queue = mutable.PriorityQueue[ScheduledAction[Action]]()

	/**
	 * Add an action to the queue
	 */
	def after(time: Int)(cb: => Unit): ScheduledAction[CallbackAction] = schedule(time, Action(() => cb))
	def schedule(time: Int, cb: () => Unit): ScheduledAction[CallbackAction] = schedule(time, Action(cb))
	def schedule[T <: Action](time: Int, action: T): ScheduledAction[T] = {
		val sa = ScheduledAction(current_time + time, action)
		queue.enqueue(sa)
		sa
	}

	/**
	 * Fetch the next executable action from the queue
	 * Return null if the queue is empty
	 */
	@tailrec
	private def nextAction: ScheduledAction[Action] = {
		if (queue.isEmpty) null
		else {
			val action = queue.dequeue()
			if (action.isExecutable) action
			else nextAction
		}
	}

	/**
	 * Current simulation time
	 */
	private var current_time = 0
	def time = current_time

	/**
	 * The current simulator state
	 */
	private var current_state = State.Prepare
	def state = current_state

	def run() = Simulator.current_dyn.withValue(this) {
		// Prepare roster
		prepareRoster()

		// Prepare actors
		actors.foreach(_.prepare())

		// Enter running state
		current_state = State.Running
		this.trigger(SimulationBeginEvent())

		// Bootstrap actors
		actors.foreach(_.select())

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
					current_time += dt
					action.execute()
				}
			}
		}

		this.trigger(SimulationEndEvent(current_state))
		log
	}
}
