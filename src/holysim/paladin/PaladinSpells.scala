package holysim.paladin

import holysim.engine._
import holysim.utils.Reactive

trait PaladinSpells {
	self: Paladin =>

	/**
	 * Imbues you with wrathful light, increasing healing done by 100% and haste, critical strike chance,
	 * and damage by 20% for 20 sec. (3 min cooldown)
	 */
	object AvengingWrath extends Spell('AvengingWrath) with Spell.Cooldown {
		// 3 min base (1.5 min glyphed)
		val cooldown = Reactive {
			if (Glyph.MercifulWrath) 90000 else 180000
		}

		/** The base buff */
		case class Buff() extends Aura('AvengingWrath) with Aura.Duration with Aura.Modifiers {
			// 20 sec base (30 sec talented)
			val duration = if (Talent.SanctifiedWrath) 30000 else 20000

			// Merciful Wrath reduce effects by 50%
			if (Glyph.MercifulWrath) {
				modifiers += Mod.HealingPercent(1.50)
				modifiers += Mod.CritChancePercent(10)
				modifiers += Mod.HastePercent(1.10)
			} else {
				modifiers += Mod.HealingPercent(2.00)
				modifiers += Mod.CritChancePercent(20)
				modifiers += Mod.HastePercent(1.20)
			}

			// Automatically gain SanctifiedWrath
			onGain += { ev =>
				if (Talent.SanctifiedWrath) ev.target gain SanctifiedWrath
			}

			// Automatically lose SanctifiedWrath
			onLose += { ev =>
				if (Talent.SanctifiedWrath) ev.target lose SanctifiedWrath
			}
		}

		/** Additional effects with Sanctified Wrath */
		object SanctifiedWrath extends Aura('SanctifiedWrath) with Aura.Modifiers {
			// Merciful Wrath reduce effects by 50%
			if (Glyph.MercifulWrath) {
				modifiers += HolyShock.CriticalChanceBonus(10)
				modifiers += HolyShock.CooldownMultiplier(0.75)
			} else {
				modifiers += HolyShock.CriticalChanceBonus(20)
				modifiers += HolyShock.CooldownMultiplier(0.50)
			}
		}
	}

	/**
	 * Your heals, including multistrikes, on other party or raid members will also heal the Beacon of Light target
	 * for up to 50% of the amount healed. Your Flash of Light and Holy Light on the Beacon of Light target will also
	 * refund 40% of their Mana cost.
	 *
	 * 3.125% of base mana
	 * 3 sec cooldown
	 *
	 * Empowered Beacon of Light (Level 92+)
	 * Your single-target heals heal your Beacon of Light target for 10% more. Also applies to Beacon of Faith.
	 */
	class BeaconSpell(identity: Symbol) extends Spell(identity) with Spell.Cooldown {
		val cooldown = Reactive(3000)

		object Beacon extends Aura(identity) with Aura.Modifiers with Aura.SingleTarget {
			modifiers ++= List(
				WordOfGlory, HolyLight, FlashOfLight, HolyShock
			).map(Mod.SpellHealingReceivedPercent(_, owner)(1.10))

			// Apply Beacon Transfert Aura to beacon owner
			onGain += (_.source gain BeaconTransferAura)
			onLose += (_.source lose BeaconTransferAura)
		}

		object BeaconTransferAura extends Aura(identity + "Source") {

		}

		onCast += (_.target gain Beacon)
	}

	object BeaconOfLight extends BeaconSpell('BeaconOfLight)
	object BeaconOfFaith extends BeaconSpell('BeaconOfFaith)

	/**
	 * Places a beacon of insight on a party or raid member, increasing their healing received from your next direct
	 * single-target heal, or Holy Radiance, within 1 min by 40%. When consumed, or when the target reaches at least
	 * 90% health, it moves to the most injured party or raid member within 40 yards. Limit 1.
	 *
	 * 1% of base mana
	 * 15 sec cooldown
	 */
	object BeaconOfInsight extends Spell('BeaconOfInsight)

	/**
	 * Inspire all party and raid members within 40 yards, granting them immunity to Silence and Interrupt effects
	 * and reducing all damage taken by 20%. Lasts 6 sec. (3 min cooldown)
	 */
	object DevotionAura extends Spell('DevotionAura) {
		object Devoted extends Aura('DevotionAura) with Aura.Duration {
			val duration = 6000
		}
	}

	/**
	 * Heals a friendly target for (240% of Spell power).
	 * 20% of base mana
	 * 1.5 sec cast
	 */
	object FlashOfLight extends Spell('FlashOfLight)

	/**
	 * Heals a friendly target for (300% of Spell power).
	 * 10.313% of base mana
	 * 2.5 sec cast
	 */
	object HolyLight extends Spell('HolyLight)

	/**
	 * Imbues a friendly target with radiant energy, healing that target for (151.319% of Spell power) and up
	 * to 6 allies within 10 yards for half that amount. Grants 1 Holy Power.
	 * 35.64% of base mana
	 * 2.5 sec cast
	 *
	 * Daybreak
	 * Casting Holy Radiance causes your next Holy Shock and Holy Shock multistrikes to also heal up to 6 allies
	 * within 10 yards of the target for 15% of the original healing done. Can accumulate up to 2 charges.
	 *
	 * Improved Daybreak (Level 92+)
	 * Increases the healing from Daybreak by 100%.
	 */
	object HolyRadiance extends Spell('HolyRadiance) {
		object DaybreakProc extends Proc {
			case class Daybreak() extends Aura('Daybreak) with Aura.Duration with Aura.Stackable {
				val duration = 10000
				val max_stacks = 2
			}

			def trigger(e: Event) = self gain Daybreak()
		}
	}

	/**
	 * Deals (140% of Spell power) healing to an ally, and grants 1 Holy Power.
	 * 7.35% of base mana
	 * Instant
	 * 6 sec cooldown
	 *
	 * Enhanced Holy Shock (Level 92+)
	 * Your Holy Light and Flash of Light have a 10% chance to cause your next Holy Shock to not trigger a cooldown.
	 */
	object HolyShock extends Spell('HolyShock) {
		object CriticalChanceBonus extends Modifier.Additive
		object CooldownMultiplier extends Modifier.Multiplicative

		object EnhancedHolyShock extends Aura('EnhancedHolyShock) with Aura.Duration {
			val duration = 15000
		}
	}

	/**
	 * Consumes up to 3 Holy Power to unleash a wave of healing energy, healing 6 injured allies within 30 yards
	 * for up to [(24.4992% of Spell power) * 3].
	 */
	object LightOfDawn extends Spell('LightOfDown) {

	}

	/**
	 * Heals a friendly target for an amount equal to your maximum health.
	 * Cannot be used on a target with Forbearance.
	 * Causes Forbearance for 1 min.
	 */
	object LayOnHands extends Spell('LayOnHands) {

	}

	/**
	 * Consumes up to 3 Holy Power to heal a friendly target for up to (330% of Spell power).
	 */
	object WordOfGlory extends Spell('WordOfGlory) {
		case class EternalFlame(var hp: Int) extends Aura('EternalFlame) with Aura.PeriodicAura {
			// If hp cost is 0, change it to 3 (Divine Purpose style aura)
			if (hp == 0) hp = 3
			else assert(hp > 0 && hp < 4)

			val interval = 2000
			val duration = 10000 * hp
			val healing = Reactive[Int](spellpower * 0.10)
		}

		onCast += (ev => if (Talent.EternalFlame) ev.target gain EternalFlame(ev.cost))
	}

	object HolyPrism extends Spell('HolyPrism) {

	}
}
