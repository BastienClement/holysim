package holysim.paladin

import holysim.engine.{Actor, Aura, Spell}

trait PaladinSpells { this: Paladin =>
	object spells {
		/**
		 * Imbues you with wrathful light, increasing healing done by 100% and haste, critical strike chance,
		 * and damage by 20% for 20 sec. (3 min cooldown)
		 */
		object AvengingWrath extends Spell("Avenging Wrath")

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
		class BeaconSpell(name: String) extends Spell(name) {
			object Beaconed extends Aura(name)
			val target: Actor = null
		}

		val BeaconOfLight = new BeaconSpell("Beacon of Light")
		val BeaconOfFaith = new BeaconSpell("Beacon of Faith")

		/**
		 * Places a beacon of insight on a party or raid member, increasing their healing received from your next direct
		 * single-target heal, or Holy Radiance, within 1 min by 40%. When consumed, or when the target reaches at least
		 * 90% health, it moves to the most injured party or raid member within 40 yards. Limit 1.
		 *
		 * 1% of base mana
		 * 15 sec cooldown
		 */
		object BeaconOfInsight extends Spell("Beacon of Insight")

		/**
		 * Inspire all party and raid members within 40 yards, granting them immunity to Silence and Interrupt effects
		 * and reducing all damage taken by 20%. Lasts 6 sec. (3 min cooldown)
		 */
		object DevotionAura extends Spell("Devotion Aura") {
			object Devoted extends Aura("Devotion Aura") with Aura.Duration {
				val duration = 6000
			}
		}

		/**
		 * Heals a friendly target for (240% of Spell power).
		 * 20% of base mana
		 * 1.5 sec cast
		 */
		object FlashOfLight extends Spell("Flash of Light")

		/**
		 * Heals a friendly target for (300% of Spell power).
		 * 10.313% of base mana
		 * 2.5 sec cast
		 */
		object HolyLight extends Spell("Holy Light")

		/**
		 * Imbues a friendly target with radiant energy, healing that target for (151.319% of Spell power) and up
		 * to 6 allies within 10 yards for half that amount. Grants 1 Holy Power.
		 * 35.64% of base mana
		 * 2.5 sec cast
		 */
		object HolyRadiance extends Spell("Holy Radiance")

		/**
		 * Deals (140% of Spell power) healing to an ally, and grants 1 Holy Power.
		 * 7.35% of base mana
		 * Instant
		 * 6 sec cooldown
		 *
		 * Enhanced Holy Shock (Level 92+)
		 * Your Holy Light and Flash of Light have a 10% chance to cause your next Holy Shock to not trigger a cooldown.
		 */
		object HolyShock extends Spell("Holy Shock") {
			object EnhancedHolyShock extends Aura("Enhanced Holy Shock") with Aura.Duration {
				val duration = 15000
			}
		}

		/**
		 * Consumes up to 3 Holy Power to unleash a wave of healing energy, healing 6 injured allies within 30 yards
		 * for up to [(24.4992% of Spell power) * 3].
		 */
		object LightOfDawn extends Spell("Light of Down") {

		}
	}
}
