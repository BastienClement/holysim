package holysim.common

import holysim.engine.Item
import holysim.engine.Stat.Impl

object Items {
	object Paladin {
		val HelmetOfGuidingLight_M = Item(321.intellect, 481.stamina, 188.crit, 229.mastery)
		val PauldronsOfGuidingLight_MW = Item(255.intellect, 382.stamina, 170.crit, 170.haste)
		val BattleplateOfGuidingLight_M = Item(321.intellect, 481.stamina, 188.haste, 229.mastery)
		val LegplatesOfGuidingLight_M = Item(321.intellect, 481.stamina, 229.crit, 188.multistrike)
	}

	object BRF {
		val FeldsparsControlChoker_M = Item(180.intellect, 271.stamina, 124.crit, 114.spirit)
		val BarrageDodgerCloak_M = Item(180.intellect, 271.stamina, 120.crit, 120.spirit)
		val FleshmelterBracers_M = Item(180.intellect, 271.stamina, 137.mastery, 91.multistrike)
		val GauntletsOfDramaticBlows_M = Item(241.intellect, 361.stamina, 149.crit, 168.mastery)
		val UktarsBeltOfChimingRings_M = Item(241.intellect, 361.stamina, 145.haste, 170.mastery)
		val SabatonsOfFractalEarth_MW = Item(255.intellect, 382.stamina, 175.mastery, 161.multistrike)
		val FiremendersSmolderingSignet_M = Item(180.intellect, 271.stamina, 134.crit, 97.spirit)

		val FangOfTheEarth_M = Item(137.intellect, 206.stamina, 94.crit, 87.haste, 1834.spellpower)
		val HeartOfTheClefthoof_M = Item(180.intellect, 271.stamina, 120.haste, 120.mastery)

		val BlackironMicroCrucible_M = Item(444.intellect)
		val IronspikeChewToy_M = Item(264.intellect)

		val SpellboundRunicBandOfInfinitePreservation = Item(207.intellect, 311.stamina, 115.haste, 152.spirit)
	}
}
