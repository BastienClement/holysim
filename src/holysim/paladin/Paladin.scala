package holysim.paladin

import holysim.engine.{Actor, PlayerStats}
import holysim.utils.Reactive

class Paladin extends Actor with PlayerStats with PaladinAuras with PaladinSpells {
	private def Flag = Reactive { false }

	// Glyphs
	object Glyph {
		var BeaconOfLight = Flag
		var Divinity = Flag
		var FlashOfLight = Flag
		var MercifulWrath = Flag
		var ProtectorOfTheInnocent = Flag
	}

	// Talents
	object Talent {
		var DivinePurpose = Flag
		var EternalFlame = Flag
		var SanctifiedWrath = Flag
		var SavedByTheLight = Flag
	}

	// Apply passive auras
	this gain BaseStats
	this gain PlateSpecialization
	this gain SanctifiedLight
	this gain HolyInsight
	this gain InfusionOfLight
	this gain IlluminatedHealing
}
