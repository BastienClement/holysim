package holysim

import holysim.common.Items.BRF._
import holysim.common.Items.Paladin._
import holysim.engine.Stat.Impl
import holysim.engine._
import holysim.paladin._

object HolySim extends App {
	Simulator.debug = true

	implicit class BlizzRound(val n: Double) extends AnyVal {
		def blizzr: Int = (if (n - n.floor == 0.5) n.floor + (n.floor % 2) else n.round).toInt
	}

	val sim = Simulator {
		val Blash = new Paladin("Blash") {
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

			Talent.EternalFlame = true
			Talent.DivinePurpose = true

			Glyph.BeaconOfLight = true
			Glyph.MercifulWrath = true
			Glyph.ProtectorOfTheInnocent = true

			// Blood elf racial
			onPrepare += this gain ArcaneAcuity
			//onPrepare += this gain PercentStatsBuff
		}

		val Jouzladin = new Paladin("Jouzladin") {}
	}

	sim.run()
}
