package holysim

import holysim.common.Items.BRF._
import holysim.common.Items.Paladin._
import holysim.engine.Stat.Impl
import holysim.engine._
import holysim.paladin._

object HolySim extends App {
	val sim = Simulator {
		val Blash = new Paladin {
			// Gear
			this equip HelmetOfGuidingLight_M.gem(50.crit)
			this equip FeldsparsControlChoker_M.enchant(75.crit)
			this equip PauldronsOfGuidingLight_MW
			this equip BarrageDodgerCloak_M.gem(50.crit).enchant(100.crit)
			this equip BattleplateOfGuidingLight_M
			this equip FleshmelterBracers_M.gem(50.crit)
			this equip GauntletsOfDramaticBlows_M
			this equip UktarsBeltOfChimingRings_M.gem(50.crit)
			this equip LegplatesOfGuidingLight_M
			this equip SabatonsOfFractalEarth_MW.gem(50.crit)
			this equip FiremendersSmolderingSignet_M.enchant(50.crit)
			this equip SpellboundRunicBandOfInfinitePreservation.enchant(50.crit)
			this equip BlackironMicroCrucible_M
			this equip IronspikeChewToy_M.gem(50.crit)
			this equip FangOfTheEarth_M.gem(50.crit)
			this equip HeartOfTheClefthoof_M

			// Blood elf racial
			this gain ArcaneAcuity
		}
	}

	println(sim.run())
}
