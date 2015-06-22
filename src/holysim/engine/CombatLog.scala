package holysim.engine

import scala.collection.mutable.ArrayBuffer

class CombatLog(sim: Simulator) extends ArrayBuffer[Event] {
	val actors = sim.actors
}
