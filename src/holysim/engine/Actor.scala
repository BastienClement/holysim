package holysim.engine

import holysim.utils.CallbackList

object Actor {
	object Role extends Enumeration {
		val Tank, Healer, Damage, None = Value
	}

	object Reaction extends Enumeration {
		val Hostile, Friendly, Neutral = Value
	}
}

trait Actor extends Aura.Target with Modifier.Target with ActorStats {
	implicit val impl_self = this
	implicit val sim = Simulator.current.value

	// The actor's role
	val role: Actor.Role.Value

	// The actor's role
	val reaction: Actor.Reaction.Value

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

abstract class Player(val role: Actor.Role.Value) extends Actor {
	val reaction = Actor.Reaction.Friendly
}

