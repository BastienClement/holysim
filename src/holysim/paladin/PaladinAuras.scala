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
		modifiers += BaseIntellect(1045)
		modifiers += BaseStamina(890)
		modifiers += BaseSpirit(780)
	}

	/**
	 * Increases your Intellect by 5% while wearing only Plate armor.
	 */
	object PlateSpecialization extends Aura("Plate Specialization") with Aura.Modifiers {
		modifiers += IntellectPercent(1.05)
	}

	/**
	 * You gain 5% more of the Critical Strike stat from all sources.
	 */
	object SanctifiedLight extends Aura("Sanctified Light") with Aura.Modifiers {
		modifiers += CriticalStrikeAttunement(1.05)
	}

	/**
	 * Increases the effectiveness of all your healing by 25%, your Word of Glory by 20%, and Light of Dawn by 50%.
	 * Increases your mana pool by 400%.
	 * Allows 50% of your mana regeneration from Spirit to continue while in combat.
	 */
	object HolyInsight extends Aura("Holy Insight") with Aura.Modifiers {
		// (#1) Apply Aura: Mod Mana Pool %
		modifiers += ManaPoolPercent(5.0)

		// (#3) Apply Aura: Allow % of Mana Regeneration to Continue in Combat
		modifiers += InCombatRegen(0.5)

		// (#6) Apply Aura: Modifies Damage/Healing Done
		modifiers ++= List(
			LayOnHands, HolyLight, HolyRadiance, FlashOfLight, HolyShock
		).map(SpellHealingPercent(_)(1.25))

		// (#7) Apply Aura: Modifies Periodic Damage/Healing Done (22)
		//modifiers += SpellHealingPercent(StayOfExecutionTick)(1.5)

		// (#9) Apply Aura: Modifies Damage/Healing Done
		modifiers += SpellHealingPercent(LightOfDawn)(1.5)

		// (#10) Apply Aura: Modifies Periodic Damage/Healing Done (22)
		//modifiers += SpellHealingPercent(EternalFlameTick)(1.5)

		// (#11) Apply Aura: Modifies Damage/Healing Done
		modifiers += SpellHealingPercent(FlashOfLight)(1.25)

		// (#12) Apply Aura: Modifies Damage/Healing Done
		modifiers += SpellHealingPercent(WordOfGlory)(1.25)
	}

	/**
	 * Your Holy Shock criticals reduce the cast time of your next Holy Light or Holy Radiance by 1.50 sec or
	 * increase the healing of your next Flash of Light by 50%.
	 *
	 * Also increases your haste by 10%.
	 */
	object InfusionOfLight extends Aura("Infusion of Light") with Aura.Modifiers {
		modifiers += HastePercent(1.1)
	}

	/**
	 * Your healing spells and their multistrikes also place an absorb shield on your target for 10% of the amount
	 * healed lasting 15 sec. Does not trigger from healing caused by Beacon of Light.
	 */
	object IlluminatedHealing extends Aura("Mastery: Illuminated Healing") with Aura.Modifiers {
		modifiers += MasteryFactor(1.25)
		object Shield extends Aura("Illuminated Healing") with Aura.Duration {
			val duration = 15000
		}
	}

	/**
	 * Fills you with Holy Light, increasing healing done by 5% and giving melee attacks a chance to
	 * heal you for (16% of Spell power).
	 */
	object SealOfInsight extends Aura("Seal of Insight") with Aura.Modifiers {
		modifiers += HealingPercent(1.05)
	}
}
