package holysim.common

import holysim.engine.{Mod, Aura, Actor}

trait Auras {
	this: Actor =>

	object ArcaneAcuity extends Aura("Arcane Acuity") with Aura.Modifiers {
		val modifiers = Mod.CritChancePercent(1) :: Nil
	}
}
