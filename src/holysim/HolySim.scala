package holysim

import holysim.common.Items.BRF._
import holysim.common.Items.Paladin._
import holysim.engine.ActorAction.Cast
import holysim.engine.ActorQuery._
import holysim.engine.Stat.Impl
import holysim.engine._
import holysim.paladin._

object HolySim extends App {
	Simulator.debug = true
	//Simulator.deterministic = true

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

			// Talents
			Talent.EternalFlame = true
			Talent.DivinePurpose = true
			Talent.SanctifiedWrath = false

			// Gylphs
			Glyph.BeaconOfLight = true
			Glyph.MercifulWrath = true
			Glyph.ProtectorOfTheInnocent = true

			// Blood elf racial
			onPrepare += this gain ArcaneAcuity

			// Cast beacons
			onPrepare += BeaconOfLight cast (query first Tank)
			onPrepare += BeaconOfFaith cast (query second Tank)

			// Filter for non-beaconned target
			val nonBeaconTargets = (a: Actor) => !a.has(BeaconOfLight.Beacon) && !a.has(BeaconOfFaith.Beacon)

			// Default actor prioritization
			// - Most injured non-beacon target
			// - Most injured beacon target
			// - Random tank
			val Auto = query mostInjured Friendly prefer nonBeaconTargets or (query random Tank)

			// Priority list
			actions = ActorPriorityList(
				Cast (AvengingWrath),
				Cast (LayOnHands) on Auto when false,
				Cast (HolyShock) on Auto when (this.name == "Blash"),
				Cast (HolyPrism),
				Cast (LightOfDawn) when (query mostInjured Friendly).get.exists(_.name == "Blash"),
				Cast (WordOfGlory) when (this has 'DivinePurpose),
				Cast (HolyLight) on Auto
			)
		}
	}

	sim.run()
}
