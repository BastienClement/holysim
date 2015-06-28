package holysim.engine

import scala.annotation.tailrec
import scala.collection.mutable
import holysim.engine.Actor.{DummyCooldown, Cooldown}
import holysim.engine.ActorAction.Wait
import holysim.utils.{Memoized, CallbackList}

object Actor {
	object Role extends Enumeration {
		val Tank, Healer, Damage, None = Value
	}

	object Reaction extends Enumeration {
		val Hostile, Friendly, Neutral = Value
	}

	trait CooldownLike {
		def begin(time: Int): Unit
		def update(dt: Int): Unit
		def reset(): Unit
		def ready: Boolean
		def left: Int
	}

	class Cooldown(implicit val owner: Actor) extends CooldownLike {
		private var cooldown_time = 0
		private def now = owner.sim.time

		def begin(time: Int) = cooldown_time = now + time
		def update(dt: Int) = cooldown_time += dt
		def reset() = cooldown_time = now

		def ready = cooldown_time <= now
		def left = math.max(cooldown_time - now, 0)
	}

	object DummyCooldown extends CooldownLike {
		def ready = true
		def begin(time: Int) = {}
		def update(dt: Int) = {}
		def reset() = {}
		def left = 0
	}
}

trait Actor extends Aura.Target with Modifier.Target with ActorStats {
	implicit val impl_self = this
	implicit val sim = Simulator.current

	// The actor's role
	val role: Actor.Role.Value

	// The actor's role
	val reaction: Actor.Reaction.Value

	// Add self to simulator list of actors
	sim.actors.add(this)

	// Actor gear
	val gear = new Gear
	def equip(item: Item) = gear.equip(item)

	// Actions
	var actions: ActorPriorityList = null
	var current_action: ScheduledAction[ActorAction] = null

	@tailrec
	final def select(): Unit = if (current_action == null && actions != null) {
		val action = actions.select getOrElse Wait(50)
		current_action = action.begin()
	} else if (current_action != null && current_action.action.interruptible) {
		current_action.cancel()
		current_action = null
		select()
	}

	// Actor's cooldowns
	val cooldowns = Memoized { (sym: Symbol) => if (sym == 'None) DummyCooldown else new Cooldown() }

	// Prepare
	val onPrepare = new CallbackList()
	def prepare() = onPrepare()

	// Attempt to trigger procs
	def trigger(e: Event): Unit = {

	}

	val name: String
	override def toString = name
}

abstract class Player(val role: Actor.Role.Value) extends Actor {
	val reaction = Actor.Reaction.Friendly
}

