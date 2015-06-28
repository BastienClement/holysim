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
	protected val cast_time: Int

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
	protected val gcd_time = 1500

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
		val cooldown: Int
		val cooldown_category: Symbol = identity

		private lazy val cd = owner.cooldowns(cooldown_category)

		def cooldown_left = cd.left

		val effective_cooldown = Reactive[Int] {
			(cooldown + owner.modifier(Mod.SpellCooldown(this))) * owner.modifier(Mod.SpellCooldownPercent(this))
		}

		onCast ~= (_ => cd.begin(effective_cooldown))

		override def available(target: Actor) = super.available(target) && cd.ready
	}

	/**
	 * Instant spell
	 */
	trait Instant extends Spell {
		val cast_time = 0
	}

	/**
	 * Spell crit & multistrike implementation
	 */
	trait CombatMechanisms extends Spell {
		/**
		 * Spell crit chance multiplier
		 */
		val crit_chance_multiplier = 1

		/**
		 * Effective critical strike chances on the target
		 */
		def effective_critical_chance(target: Actor) = {
			val base_chance: Double = owner.criticalstrike
			val bonus_chance: Double = owner.modifier(Mod.SpellCriticalChance(this)) / 100.0
			val chance_multiplier: Double = owner.modifier(Mod.SpellCriticalChancePercent(this))
			(base_chance + bonus_chance) * chance_multiplier * crit_chance_multiplier
		}

		/**
		 * Spell multistrike chance multiplier
		 */
		val ms_chance_multiplier = 1

		/**
		 * Effective multistrike chances on the target
		 */
		def effective_multistrike_chance(target: Actor) = {
			val base_chance: Double = owner.multistrike
			val bonus_chance: Double = owner.modifier(Mod.SpellMultistrikeChance(this)) / 100.0
			val chance_multiplier: Double = owner.modifier(Mod.SpellMultistrikeChancePercent(this))
			(base_chance + bonus_chance) * chance_multiplier * ms_chance_multiplier
		}

		/**
		 * Crit and multistrike for this spell cast
		 */
		var crit = false
		var ms = Seq[(Boolean, Boolean)]()

		/**
		 * Refresh combat mechanism flags on spell cast
		 */
		onCast ~= { ev =>
			val target = ev.target

			// Crit proc
			val crit_chance = effective_critical_chance(target)
			crit = sim.rng.roll(crit_chance)

			// Multistrike proc
			val ms_chance = effective_multistrike_chance(target)
			ms = for (i <- 1 to 2) yield (sim.rng.roll(ms_chance), sim.rng.roll(crit_chance))
		}
	}

	/**
	 * Healing spell
	 */
	trait Healing extends Spell with CombatMechanisms {
		val base: Int
		val scaling: Double

		def effective_healing(target: Actor): Int = {
			val base_healing = base + scaling * owner.spellpower
			val own_multiplier = owner.modifier(Mod.SpellHealingPercent(this.identity)) * owner.modifier(Mod.HealingPercent)
			val target_multiplier = target.modifier(Mod.SpellHealingReceivedPercent(this.identity, owner))
			base_healing * own_multiplier * target_multiplier
		}

		def crit_healing(target: Actor, base: Int) = base * 2
		def ms_healing(target: Actor, base: Int) = base / 3

		onCast += { ev =>
			// Spell target
			val target = ev.target

			// Base healing + crit
			val base_healing = effective_healing(target)
			val amount = if (crit) crit_healing(target, base_healing) else base_healing

			// Healing the actor
			target.heal(this, amount, crit, false)

			// Multristrike procs
			val ms_base = ms_healing(target, base_healing)
			for ((trigger, ms_crit) <- ms if trigger) {
				val ms_amount = if (ms_crit) crit_healing(target, ms_base) else ms_base
				target.heal(this, ms_amount, ms_crit, true)
			}
		}
	}

	trait OffGCD extends Spell {
		override val gcd_category = 'None
	}
}
