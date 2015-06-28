package holysim.paladin

import holysim.common
import holysim.engine.Mod
import holysim.engine.{Aura, Modifier}
import holysim.engine.SymbolUtils

trait PaladinAuras extends common.Auras {
	this: Paladin =>
	/**
	 * Character base stats
	 */
	object BaseStats extends Aura('BaseStats) with Aura.Modifiers {
		modifiers += Mod.BaseIntellect(1045)
		modifiers += Mod.BaseStamina(890)
		modifiers += Mod.BaseSpirit(780)
	}

	/**
	 * Increases your Intellect by 5% while wearing only Plate armor.
	 */
	object PlateSpecialization extends Aura('PlateSpecialization) with Aura.Modifiers {
		modifiers += Mod.IntellectPercent(1.05)
	}

	/**
	 * You gain 5% more of the Critical Strike stat from all sources.
	 */
	object SanctifiedLight extends Aura('SanctifiedLight) with Aura.Modifiers {
		modifiers += Mod.CriticalStrikeAttunement(1.05)
	}

	/**
	 * Increases the effectiveness of all your healing by 25%, your Word of Glory by 20%, and Light of Dawn by 50%.
	 * Increases your mana pool by 400%.
	 * Allows 50% of your mana regeneration from Spirit to continue while in combat.
	 */
	object HolyInsight extends Aura('HolyInsight) with Aura.Modifiers {
		// (#1) Apply Aura: Mod Mana Pool %
		modifiers += Mod.ManaPoolPercent(5.0)

		// (#3) Apply Aura: Allow % of Mana Regeneration to Continue in Combat
		modifiers += Mod.InCombatRegenPercent(0.5)

		// (#6) Apply Aura: Modifies Damage/Healing Done
		modifiers ++= List(
			LayOnHands, HolyLight, HolyRadiance, FlashOfLight, HolyShock
		).map(Mod.SpellHealingPercent(_)(1.25))

		// (#7) Apply Aura: Modifies Periodic Damage/Healing Done (22)
		//modifiers += SpellHealingPercent(StayOfExecutionTick)(1.5)

		// (#9) Apply Aura: Modifies Damage/Healing Done
		modifiers += Mod.SpellHealingPercent(LightOfDawn)(1.5)

		// (#10) Apply Aura: Modifies Periodic Damage/Healing Done (22)
		modifiers += Mod.SpellHealingPercent('EternalFlame).apply(1.5)

		// (#11) Apply Aura: Modifies Damage/Healing Done
		modifiers += Mod.SpellHealingPercent(FlashOfLight)(1.25)

		// (#12) Apply Aura: Modifies Damage/Healing Done
		modifiers += Mod.SpellHealingPercent(WordOfGlory)(1.20)
	}

	/**
	 * Your Holy Shock criticals reduce the cast time of your next Holy Light or Holy Radiance by 1.50 sec or
	 * increase the healing of your next Flash of Light by 50%.
	 *
	 * Also increases your haste by 10%.
	 */
	object InfusionOfLight extends Aura('InfusionOfLightPassive) with Aura.Modifiers {
		modifiers += Mod.HastePercent(1.1)
		object Buff extends Aura('InfusionOfLight) with Aura.Modifiers with Aura.Duration {
			val duration = 15000
			modifiers += Mod.SpellHealingPercent(FlashOfLight)(1.50)
			modifiers += Mod.SpellCastTime(HolyLight)(-1500)
			modifiers += Mod.SpellCastTime(HolyRadiance)(-1500)
		}
	}

	/**
	 * Your healing spells and their multistrikes also place an absorb shield on your target for 10% of the amount
	 * healed lasting 15 sec. Does not trigger from healing caused by Beacon of Light.
	 */
	object IlluminatedHealing extends Aura('MasteryIlluminatedHealing) with Aura.Modifiers {
		modifiers += Mod.MasteryFactor(1.25)
		object Shield extends Aura('IlluminatedHealing) with Aura.Duration {
			val duration = 15000
		}
	}

	/**
	 * Fills you with Holy Light, increasing healing done by 5% and giving melee attacks a chance to
	 * heal you for (16% of Spell power).
	 */
	object SealOfInsight extends Aura('SealOfInsight) with Aura.Modifiers {
		modifiers += Mod.HealingPercent(1.05)
	}
}
