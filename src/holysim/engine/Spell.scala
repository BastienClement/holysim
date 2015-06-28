package holysim.engine

import holysim.utils.Reactive

abstract class Spell(val identity: Symbol)(implicit impl_owner: Actor) extends WithIdentity {
	/**
	 * The spell owner
	 */
	val owner = impl_owner

	/**
	 * Reference to the current simulator
	 */
	val sim = owner.sim

	/**
	 * OnCast event hook
	 */
	protected val onCast = new EventHook[SpellCastSuccessEvent]()

	/**
	 * Cast the spell on the target
	 */
	final def cast(target: Actor) = {
		// The spell must be available on the target
		assert(available(target))

		// Determine resources cost
		val resource = None
		val cost = 0

		// Trigger GCD
		owner.cooldowns(gcd_category).begin(effective_gcd)

		// Spell cast event
		onCast(SpellCastSuccessEvent(this, owner, target, resource, cost))
	}

	/**
	 * Base spell cast time
	 */
	protected val cast_time = Reactive(0)

	/**
	 * Effective cast time after modifiers and haste
	 */
	val effective_cast_time = Reactive[Int] {
		cast_time / owner.haste + owner.modifier(Mod.SpellCastTime(this.identity))
	}

	/**
	 * Spell's GCD category
	 */
	val gcd_category = 'GCD

	/**
	 * GCD delay caused by this spell
	 */
	protected val gcd_time = Reactive(1500)

	/**
	 * Effective GCD after effects and haste
	 */
	val effective_gcd = Reactive[Int] {
		math.max(1000, gcd_time / owner.haste)
	}

	/**
	 * Check GCD availability
	 */
	def available(target: Actor) = owner.cooldowns(gcd_category).ready

	override def toString = identity.name
}

object Spell {
	/**
	 * A spell with a cooldown
	 */
	trait Cooldown extends Spell {
		val cooldown: Reactive[Int]
		val cooldown_category: Symbol = identity

		private lazy val cd = owner.cooldowns(cooldown_category)

		def cooldown_left = cd.left

		onCast ~= (_ => cd.begin(cooldown))

		override def available(target: Actor) = super.available(target) && cd.ready
	}

	trait Healing extends Spell {
		val base: Int
		val scaling: Double

		def healing(target: Actor) = Reactive[Int] {
			val base_healing = base + scaling * owner.spellpower
			val own_multiplier = owner.modifier(Mod.SpellHealingPercent(this.identity)) * owner.modifier(Mod.HealingPercent)
			val target_multiplier = target.modifier(Mod.SpellHealingReceivedPercent(this.identity, owner))
			base_healing * own_multiplier * target_multiplier
		}
	}

	trait OffGCD extends Spell {
		override val gcd_category = 'None
	}
}
