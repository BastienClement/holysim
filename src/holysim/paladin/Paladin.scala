package holysim.paladin

import holysim.engine.{Actor, ActorStats}
import holysim.utils.Reactive

class Paladin(val name: String) extends Actor with PaladinAuras with PaladinSpells {
	val role = Actor.Role.Healer

	// Glyphs
	object Glyph {
		var BeaconOfLight = false
		var Divinity = false
		var FlashOfLight = false
		var MercifulWrath = false
		var ProtectorOfTheInnocent = false
	}

	// Talents
	object Talent {
		var DivinePurpose = false
		var EternalFlame = false
		var SanctifiedWrath = false
		var SavedByTheLight = false
	}

	// Perks
	object Perk {
		var ImprovedForbearance = true
		var ImprovedDaybreak = true
		var EnhancedHolyShock = true
		var EmpoweredBeaconOfLight = true
	}

	// Apply passive auras
	onPrepare += {
		this gain BaseStats
		this gain PlateSpecialization
		this gain SanctifiedLight
		this gain HolyInsight
		this gain InfusionOfLight
		this gain IlluminatedHealing
		this gain SealOfInsight
	}
}
