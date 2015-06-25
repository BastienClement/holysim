package holysim.engine

import holysim.utils.{CallbackListArg, Reactive}

abstract class Spell(val identity: Symbol)(implicit impl_owner: Actor) extends WithIdentity {
	val owner = impl_owner
	val sim = owner.sim

	val onCast = new EventHook[SpellCastEvent]()
	final def cast(target: Actor) = {
		val resource = None
		val cost = 0
		onCast(SpellCastEvent(this, owner, target, resource, cost))
	}

	def available(target: Actor): Boolean = true


	protected var cast_time: Reactive[Int] = 0
	val castTime: Reactive[Int] = Reactive(cast_time / owner.haste)

	override def toString = identity.name
}

object Spell {
	trait Cooldown extends Spell {
		val cooldown: Reactive[Int]
		private var ready: Int = 0

		override def available(target: Actor) = super.available(target) && sim.time >= ready
	}
}
