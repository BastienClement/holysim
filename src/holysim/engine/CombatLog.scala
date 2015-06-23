package holysim.engine

import scala.collection.mutable.ArrayBuffer

class CombatLog(sim: Simulator) {
	var events = Vector[Event]()
	def +=(event: Event) = events :+= event

	val actors = sim.actors
}
