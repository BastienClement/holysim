package holysim.common

import holysim.engine.{Mod, Aura, Actor}

trait Auras {
	this: Actor =>

	object ArcaneAcuity extends Aura('ArcaneAcuity) with Aura.Modifiers {
		modifiers += Mod.CritChancePercent(1)
	}

	object PercentStatsBuff extends Aura('PercentStatsBuff) with Aura.Modifiers {
		modifiers += Mod.IntellectPercent(1.05)
		modifiers += Mod.AgilityPercent(1.05)
		modifiers += Mod.StrengthPercent(1.05)
	}

	object StaminaBuff extends Aura('StaminaBuff) with Aura.Modifiers {
		modifiers += Mod.StaminaPercent(1.10)
	}

	object CritBuff extends Aura('CritBuff) with Aura.Modifiers {
		modifiers += Mod.CritChancePercent(5)
	}

	object HasteBuff extends Aura('HasteBuff) with Aura.Modifiers {
		modifiers += Mod.HastePercent(1.05)
	}

	object MasteryBuff extends Aura('MasteryBuff) with Aura.Modifiers {
		modifiers += Mod.MasteryScore(550)
	}

	object MultistrikeBuff extends Aura('MultistrikeBuff) with Aura.Modifiers {
		modifiers += Mod.MultistrikePercent(5)
	}

	object VersatilityBuff extends Aura('VersatilityBuff) with Aura.Modifiers {
		modifiers += Mod.VersatilityPercent(3)
	}

	object SpellPowerBuff extends Aura('SpellPowerBuff) with Aura.Modifiers {
		modifiers += Mod.SpellPowerPercent(1.10)
	}
}
