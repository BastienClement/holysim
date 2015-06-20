package holysim.common

import holysim.engine.{Mod, Aura, Actor}

trait Auras {
	this: Actor =>

	object ArcaneAcuity extends Aura("Arcane Acuity") with Aura.Modifiers {
		modifiers += Mod.CritChancePercent(1)
	}
}
