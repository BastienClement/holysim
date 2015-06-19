package holysim.paladin

import holysim.common
import holysim.engine.Mod._
import holysim.engine.{Aura, Modifier}

trait PaladinAuras extends common.Auras {
	this: Paladin =>
	/**
	 * Character base stats
	 */
	object BaseStats extends Aura("Base Stats") with Aura.Modifiers {
		val modifiers = BaseIntellect(1045) :: BaseStamina(890) :: BaseSpirit(780) :: Nil
	}

	/**
	 * Increases your Intellect by 5% while wearing only Plate armor.
	 */
	object PlateSpecialization extends Aura("Plate Specialization") with Aura.Modifiers {
		val modifiers = IntellectPercent(1.05) :: Nil
	}

	/**
	 * You gain 5% more of the Critical Strike stat from all sources.
	 */
	object SanctifiedLight extends Aura("Sanctified Light") with Aura.Modifiers {
		val modifiers = CriticalStrikeAttunement(1.05) :: Nil
	}

	/**
	 * Increases the effectiveness of all your healing by 25%, your Word of Glory by 20%, and Light of Dawn by 50%.
	 * Increases your mana pool by 400%.
	 * Allows 50% of your mana regeneration from Spirit to continue while in combat.
	 */
	object HolyInsight extends Aura("Holy Insight") with Aura.Modifiers {
		case object HealingBonus extends Modifier.Multiplicative
		case object WoGBonus extends Modifier.Multiplicative
		case object LoDBonus extends Modifier.Multiplicative

		val modifiers = ManaPoolPercent(5.0) :: InCombatRegen(0.5) ::
				HealingBonus(1.25) :: WoGBonus(1.2) :: LoDBonus(1.5) :: Nil
	}

	/**
	 * Your Holy Shock criticals reduce the cast time of your next Holy Light or Holy Radiance by 1.50 sec or
	 * increase the healing of your next Flash of Light by 50%.
	 *
	 * Also increases your haste by 10%.
	 */
	object InfusionOfLight extends Aura("Infusion of Light") with Aura.Modifiers {
		val modifiers = HastePercent(1.1) :: Nil
	}

	/**
	 * Your healing spells and their multistrikes also place an absorb shield on your target for 10% of the amount
	 * healed lasting 15 sec. Does not trigger from healing caused by Beacon of Light.
	 */
	object IlluminatedHealing extends Aura("Mastery: Illuminated Healing") with Aura.Modifiers {
		val modifiers = MasteryFactor(1.25) :: Nil
		object Shield extends Aura("Illuminated Healing") with Aura.Duration {
			val duration = 15000
		}
	}
}
