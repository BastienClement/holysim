package holysim.engine

import holysim.utils.{CallbackListArg, Reactive}

class Spell(val identity: Symbol)(implicit impl_owner: Actor) extends WithIdentity {
	val owner = impl_owner
	val sim = owner.sim

	def cast(target: Actor) = {
		val resource = None
		val cost = 0

		val event = SpellCastEvent(this, owner, target, resource, cost)
		onCast(event)
		sim.trigger(event)
	}

	val onCast = new CallbackListArg[SpellCastEvent]()

	override def toString = identity.name
}

object Spell {
	trait Cooldown extends Spell {
		val cooldown: Reactive[Int]
	}
}
