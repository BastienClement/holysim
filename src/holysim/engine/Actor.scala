package holysim.engine

import holysim.utils.CallbackList

object Actor {
	object Role extends Enumeration {
		val Tank, Healer, Damage = Value
	}
}

trait Actor extends Aura.Target with Modifier.Target with ActorStats {
	implicit val impl_self = this
	implicit val sim = Simulator.current.value

	// The actor's role
	val role: Actor.Role.Value

	// Add self to simulator list of actors
	sim.actors.add(this)

	val onPrepare = new CallbackList()

	// Actor gear
	val gear = new Gear
	def equip(item: Item) = gear.equip(item)

	// Attempt to trigger procs
	def trigger(e: Event): Unit = {

	}

	val name: String
	override def toString = name
}
